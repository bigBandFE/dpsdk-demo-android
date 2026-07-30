package com.dragonpass.en.dpsdk.demo.sso

/**
 * SSO Configuration — mirrors iOS DemoDragonPassSSOConfig.
 * Replace the keys and endpoints for your environment.
 */
object SSOConfig {
    // ── Environment endpoints ──
    const val OPENAPI_BASE_URL =
        "https://global-h5-uat.dragonpass.com/api/business/open/interface/request"
    const val VISITOR_LOGIN_BASE_URL = "https://global-h5-uat.dragonpass.com"
    const val VISITOR_LOGIN_PATH = "/api/business/auth/visitor/login"

    // ── Tenant / member ──
    const val TENANT_CODE = "0494"
    const val MEMBER_SHIP_CODE = "8576376517994777"
    const val LOGIN_PRODUCT_CODE = "IL0494000001"
    const val VERIFY_PURCHASE_PRO_CODE = ""

    // ── Locale ──
    const val LANGUAGE = "en-US"
    const val CHANNEL = "eBridge"
    const val MODULE = "1"
    const val REDIRECT_URL =
        "https://g-front-uat.dragonpass.com/standard-lfd/#/lounge/landing"

    // ── RSA keys (copy your actual keys here) ──
    val SAAS_PUBLIC_KEY: String
        get() = "-----BEGIN PUBLIC KEY-----\n" +
                "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAuzMo5tfeIgEpe2XX9KMEZQADgVPIhelVhcbH9rgMQDInFleJvIW4WLJ5zH0RfMezLDN6rEslfAn/AkL/gg6PIPn3NKBF8JvDrr5p3Xai+KVypU42MdlIKa2QxVeI9rVDUb4CpI0OwZEes8w78HL1yQ1/lSwpFUolerw+wsrq3A6/AVpzidNzI92xl7cWF4pO1tSG4g3cnT0Pg6ORJfMgrufCvp3YpZoxvHAwlIEsO1DffMmELQJOzhzbWpHWbXvQ58rDhU5a/vFxAHYW1BxxzBTL+ESV6GO9gLNCN1fXetQusygytf8ozDr4Ec899V90s6hiteDASGF/1YDbBDfPPwIDAQAB\n" +
                "-----END PUBLIC KEY-----"

    val CUSTOMER_PRIVATE_KEY: String
        get() = "[REDACTED PRIVATE KEY]"

    // ── Flow control ──
    const val NEED_H5_TOKEN = true
    const val REQUEST_TIMEOUT_SEC = 60L
}

// ── Data Transfer Objects ──

/** Plain body before encryption — serialized to JSON then AES-encrypted. */
data class OpenAPIPlainBody(
    val tenantCode: String,
    val memberShipCode: String,
    val redirectURL: String,
    val generateShortUrl: Boolean = true,
    val privateKey: String = "",
    val language: String,
    val channel: String,
    val productCode: String,
    val maxPassengerPerOrder: Int = 6,
    val pageType: String,
    val module: Int,
    val actionType: String,
    val thirdInfo: String = "111111",
    val timeZone: String = "Asia/Seoul",
    val needH5Token: Boolean? = null,
    val memberCode: String? = null,
    val verifyPurchaseProCode: String? = null,
    val timeout: String? = null,
)

/** Encrypted request body sent to OpenAPI. */
data class OpenAPIEncryptedBody(
    val operationCode: String,
    val requestId: String,
    val version: String,
    val encryptKey: String,
    val iv: String,
    val body: String,
)

/** Encrypted response body received from OpenAPI. */
data class OpenAPIEncryptedResponse(
    val timestamp: Long,
    val nonce: String,
    val tenantCode: String,
    val encryptKey: String,
    val iv: String,
    val body: String,
    val sign: String,
)

/** Decrypted OpenAPI data payload. */
data class OpenAPIDataBody(
    val shortUrl: String? = null,
    val h5Token: String? = null,
)

/** Decrypted OpenAPI response. */
data class OpenAPIDecryptedBody(
    val code: String?,
    val msg: String?,
    val message: String?,
    val data: OpenAPIDataBody?,
)

/** Visitor / normal login request body. */
data class VisitorLoginRequestBody(
    val params: String,
    val tenantCode: String,
    val productCode: String,
    val verifyPurchaseProCode: String? = null,
    val mode: String = "visitor",
)

/** Visitor login response. */
data class VisitorLoginResponse(
    val code: String?,
    val msg: String?,
    val data: VisitorLoginData?,
)

data class VisitorLoginData(
    val token: String?,
)
