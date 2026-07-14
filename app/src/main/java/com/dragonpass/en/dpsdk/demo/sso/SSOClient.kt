package com.dragonpass.en.dpsdk.demo.sso

import android.util.Base64
import android.util.Log
import com.google.gson.Gson
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.util.UUID
import java.util.concurrent.TimeUnit

/**
 * SSO Client — mirrors iOS DemoDragonPassSSOClient.
 *
 * Flow: build plainBody → AES+RSA encrypt → POST OpenAPI → verify+decrypt → extract h5Token → setAuthCode.
 */
object SSOClient {
    private const val TAG = "SSOClient"
    private const val OPERATION_CODE = "get_login_free_redirect_url"
    private const val VERSION = "1.0"

    private val gson = Gson()
    private val client = OkHttpClient.Builder()
        .connectTimeout(SSOConfig.REQUEST_TIMEOUT_SEC, TimeUnit.SECONDS)
        .readTimeout(SSOConfig.REQUEST_TIMEOUT_SEC, TimeUnit.SECONDS)
        .writeTimeout(SSOConfig.REQUEST_TIMEOUT_SEC, TimeUnit.SECONDS)
        .build()

    data class SSOResult(val token: String, val requestId: String)

    // ── Public API ──

    /**
     * Get auth code via the full SSO flow (quick mode: needH5Token=true).
     * Returns the token that should be passed to DPSDK.setAuthCode().
     */
    fun getAuthCode(): Result<SSOResult> = runCatching {
        val requestId = "req-${System.currentTimeMillis()}"
        logSSOStart(requestId)

        // ① Build plain body
        val plainBody = buildPlainBody()

        // ② Encrypt: AES key + IV → encrypt body, RSA encrypt AES key, RSA sign
        val encrypted = buildEncryptedRequest(plainBody, requestId)

        // ③ POST to OpenAPI
        val response = sendOpenAPIRequest(encrypted)

        // ④ Decrypt + verify
        val decrypted = decryptOpenAPIResponse(response)

        // ⑤ Extract h5Token
        val token = extractH5Token(decrypted)

        logAuthCodeSuccess(token)
        SSOResult(token, requestId)
    }

    // ── Step ①: Build plain body ──

    private fun buildPlainBody(): OpenAPIPlainBody {
        return OpenAPIPlainBody(
            tenantCode = SSOConfig.TENANT_CODE,
            memberShipCode = SSOConfig.MEMBER_SHIP_CODE,
            redirectURL = SSOConfig.REDIRECT_URL,
            language = SSOConfig.LANGUAGE,
            channel = SSOConfig.CHANNEL,
            productCode = SSOConfig.LOGIN_PRODUCT_CODE,
            pageType = "lounge-landing",
            module = SSOConfig.MODULE.toInt(),
            actionType = "operate",
            needH5Token = SSOConfig.NEED_H5_TOKEN,
        )
    }

    // ── Step ②: Encrypt ──

    private fun buildEncryptedRequest(
        plainBody: OpenAPIPlainBody,
        requestId: String,
    ): EncryptedRequestBundle {
        val timestamp = System.currentTimeMillis()
        val nonce = "nonce-${UUID.randomUUID().toString().replace("-", "").lowercase()}"
        val aesKey = Base64.encodeToString(
            SSOCrypto.randomBytes(16), Base64.NO_WRAP
        )
        val iv = Base64.encodeToString(
            SSOCrypto.randomBytes(16), Base64.NO_WRAP
        )

        val plainJson = gson.toJson(plainBody)
        val encryptedBody = SSOCrypto.encryptAES(plainJson, aesKey, iv)
        val encryptKey = SSOCrypto.encryptRSA(aesKey, SSOConfig.SAAS_PUBLIC_KEY)

        val signContent = "$timestamp$nonce${SSOConfig.TENANT_CODE}$encryptKey$iv$encryptedBody"
        val sign = SSOCrypto.signSHA256RSA(signContent, SSOConfig.CUSTOMER_PRIVATE_KEY)

        val headers = mapOf(
            "Content-Type" to "application/json",
            "X-Tenant-Code" to SSOConfig.TENANT_CODE,
            "X-Timestamp" to timestamp.toString(),
            "X-Nonce" to nonce,
            "X-Sign" to sign,
        )

        val body = OpenAPIEncryptedBody(
            operationCode = OPERATION_CODE,
            requestId = requestId,
            version = VERSION,
            encryptKey = encryptKey,
            iv = iv,
            body = encryptedBody,
        )

        return EncryptedRequestBundle(headers, body)
    }

    // ── Step ③: POST OpenAPI ──

    private fun sendOpenAPIRequest(bundle: EncryptedRequestBundle): OpenAPIEncryptedResponse {
        val json = gson.toJson(bundle.body)
        val requestBody = json.toRequestBody("application/json".toMediaType())

        val builder = Request.Builder().url(SSOConfig.OPENAPI_BASE_URL).post(requestBody)
        bundle.headers.forEach { (key, value) -> builder.addHeader(key, value) }

        val response = client.newCall(builder.build()).execute()
        val responseBody = response.body?.string()
            ?: throw IllegalStateException("OpenAPI response body is null")

        if (!response.isSuccessful) {
            throw IllegalStateException("OpenAPI HTTP ${response.code}: $responseBody")
        }

        return gson.fromJson(responseBody, OpenAPIEncryptedResponse::class.java)
    }

    // ── Step ④: Decrypt + verify ──

    private fun decryptOpenAPIResponse(response: OpenAPIEncryptedResponse): OpenAPIDecryptedBody {
        // Verify signature
        val signContent = "${response.timestamp}${response.nonce}${response.tenantCode}" +
                "${response.encryptKey}${response.iv}${response.body}"
        val signatureOK = SSOCrypto.verifySHA256RSA(
            signContent, response.sign, SSOConfig.SAAS_PUBLIC_KEY
        )
        if (!signatureOK) {
            throw IllegalStateException("OpenAPI signature verification failed")
        }

        // Decrypt AES key
        val aesKey = SSOCrypto.decryptRSA(
            response.encryptKey, SSOConfig.CUSTOMER_PRIVATE_KEY
        )

        // Decrypt body
        val plainJson = SSOCrypto.decryptAES(response.body, aesKey, response.iv)

        return gson.fromJson(plainJson, OpenAPIDecryptedBody::class.java)
    }

    // ── Step ⑤: Extract token ──

    private fun extractH5Token(decrypted: OpenAPIDecryptedBody): String {
        val token = decrypted.data?.h5Token
        if (token.isNullOrEmpty()) {
            throw IllegalStateException(
                "DragonPass OpenAPI response did not include h5Token (code=${decrypted.code}, msg=${decrypted.msg})"
            )
        }
        return token
    }

    // ── Logging ──

    private fun logSSOStart(requestId: String) {
        Log.d(TAG, "[step=1 SSO_START] requestId=$requestId, tenantCode=${SSOConfig.TENANT_CODE}")
    }

    private fun logAuthCodeSuccess(token: String) {
        val preview = if (token.length > 8) "${token.take(4)}...${token.takeLast(4)}" else "***"
        Log.d(TAG, "[step=7 DONE] authCodeLength=${token.length}, authCodePreview=$preview")
    }

    // ── Data class ──

    private data class EncryptedRequestBundle(
        val headers: Map<String, String>,
        val body: OpenAPIEncryptedBody,
    )
}
