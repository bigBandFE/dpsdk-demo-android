package com.dragonpass.en.dpsdk.demo.sso

import android.util.Base64
import java.io.ByteArrayInputStream
import java.security.KeyFactory
import java.security.SecureRandom
import java.security.Signature
import java.security.spec.PKCS8EncodedKeySpec
import java.security.spec.X509EncodedKeySpec
import javax.crypto.Cipher
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec

/**
 * Crypto utilities — mirrors iOS DemoDragonPassSSOClient.Crypto.
 * AES-128-CBC, RSA-PKCS1, RSA-SHA256.
 */
object SSOCrypto {

    fun randomBytes(count: Int): ByteArray {
        val bytes = ByteArray(count)
        SecureRandom().nextBytes(bytes)
        return bytes
    }

    // ═══ AES ═══

    fun encryptAES(plainText: String, keyBase64: String, ivBase64: String): String {
        val key = Base64.decode(keyBase64, Base64.DEFAULT)
        val iv = Base64.decode(ivBase64, Base64.DEFAULT)
        val cipher = Cipher.getInstance("AES/CBC/PKCS7Padding")
        cipher.init(Cipher.ENCRYPT_MODE, SecretKeySpec(key, "AES"), IvParameterSpec(iv))
        return Base64.encodeToString(cipher.doFinal(plainText.toByteArray(Charsets.UTF_8)), Base64.NO_WRAP)
    }

    fun decryptAES(cipherTextBase64: String, keyBase64: String, ivBase64: String): String {
        val cipherText = Base64.decode(cipherTextBase64, Base64.DEFAULT)
        val key = Base64.decode(keyBase64, Base64.DEFAULT)
        val iv = Base64.decode(ivBase64, Base64.DEFAULT)
        val cipher = Cipher.getInstance("AES/CBC/PKCS7Padding")
        cipher.init(Cipher.DECRYPT_MODE, SecretKeySpec(key, "AES"), IvParameterSpec(iv))
        return String(cipher.doFinal(cipherText), Charsets.UTF_8)
    }

    // ═══ RSA ═══

    fun encryptRSA(plainText: String, publicKeyPEM: String): String {
        val key = parsePublicKey(publicKeyPEM)
        val cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding")
        cipher.init(Cipher.ENCRYPT_MODE, key)
        return Base64.encodeToString(cipher.doFinal(plainText.toByteArray(Charsets.UTF_8)), Base64.NO_WRAP)
    }

    fun decryptRSA(cipherTextBase64: String, privateKeyPEM: String): String {
        val cipherText = Base64.decode(cipherTextBase64, Base64.DEFAULT)
        val key = parsePrivateKey(privateKeyPEM)
        val cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding")
        cipher.init(Cipher.DECRYPT_MODE, key)
        val decrypted = cipher.doFinal(cipherText)
        return normalizeAESKeyMaterial(decrypted)
    }

    fun signSHA256RSA(content: String, privateKeyPEM: String): String {
        val key = parsePrivateKey(privateKeyPEM)
        val sig = Signature.getInstance("SHA256withRSA")
        sig.initSign(key)
        sig.update(content.toByteArray(Charsets.UTF_8))
        return Base64.encodeToString(sig.sign(), Base64.NO_WRAP)
    }

    fun verifySHA256RSA(content: String, signatureBase64: String, publicKeyPEM: String): Boolean {
        val sigBytes = Base64.decode(signatureBase64, Base64.DEFAULT)
        val key = parsePublicKey(publicKeyPEM)
        val sig = Signature.getInstance("SHA256withRSA")
        sig.initVerify(key)
        sig.update(content.toByteArray(Charsets.UTF_8))
        return sig.verify(sigBytes)
    }

    // ═══ Key parsing ═══

    private fun parsePublicKey(pem: String): java.security.PublicKey {
        val der = pkcs1ToX509PublicKey(pemToDer(pem))
        val spec = X509EncodedKeySpec(der)
        return KeyFactory.getInstance("RSA").generatePublic(spec)
    }

    private fun parsePrivateKey(pem: String): java.security.PrivateKey {
        val der = pemToDer(pem)
        // Try PKCS#8 first, then PKCS#1
        return try {
            val spec = PKCS8EncodedKeySpec(der)
            KeyFactory.getInstance("RSA").generatePrivate(spec)
        } catch (e: Exception) {
            val spec = pkcs1ToPkcs8PrivateKey(der)
            KeyFactory.getInstance("RSA").generatePrivate(PKCS8EncodedKeySpec(spec))
        }
    }

    /** Strip PEM header/footer and decode base64. */
    private fun pemToDer(pem: String): ByteArray {
        val base64 = pem
            .lines()
            .filter { !it.trimStart().startsWith("-----") }
            .joinToString("")
            .trim()
        return Base64.decode(base64, Base64.DEFAULT)
    }

    /** PKCS#1 public key → X.509 SubjectPublicKeyInfo. */
    private fun pkcs1ToX509PublicKey(pkcs1: ByteArray): ByteArray {
        // X.509 SPKI = SEQUENCE { AlgorithmIdentifier, BIT STRING (PKCS#1) }
        val algoId = byteArrayOf(
            0x30, 0x0d, 0x06, 0x09, 0x2a, 0x86, 0x48, 0x86, 0xf7, 0x0d, 0x01, 0x01, 0x01, 0x05, 0x00
        )
        val bitString = ByteArray(1 + pkcs1.size) { 0x00 }.also { System.arraycopy(pkcs1, 0, it, 1, pkcs1.size) }
        return wrapSequence(algoId + wrapSequence(0x03, bitString))
    }

    /** PKCS#1 private key → PKCS#8. */
    private fun pkcs1ToPkcs8PrivateKey(pkcs1: ByteArray): ByteArray {
        val version = byteArrayOf(0x02, 0x01, 0x00)
        val algoId = byteArrayOf(
            0x30, 0x0d, 0x06, 0x09, 0x2a, 0x86, 0x48, 0x86, 0xf7, 0x0d, 0x01, 0x01, 0x01, 0x05, 0x00
        )
        val pkcs1Wrapped = wrapSequence(0x04, pkcs1)
        return wrapSequence(version + algoId + pkcs1Wrapped)
    }

    private fun wrapSequence(content: ByteArray): ByteArray {
        val len = content.size
        val lenBytes = when {
            len < 0x80 -> byteArrayOf(len.toByte())
            len < 0x100 -> byteArrayOf(0x81.toByte(), len.toByte())
            else -> byteArrayOf(0x82.toByte(), (len shr 8).toByte(), len.toByte())
        }
        return byteArrayOf(0x30.toByte()) + lenBytes + content
    }

    private fun wrapSequence(tag: Int, content: ByteArray): ByteArray {
        val len = content.size
        val lenBytes = when {
            len < 0x80 -> byteArrayOf(len.toByte())
            len < 0x100 -> byteArrayOf(0x81.toByte(), len.toByte())
            else -> byteArrayOf(0x82.toByte(), (len shr 8).toByte(), len.toByte())
        }
        return byteArrayOf(tag.toByte()) + lenBytes + content
    }

    /** Normalize RSA-decrypted AES key material (try base64 wrapping if needed). */
    private fun normalizeAESKeyMaterial(data: ByteArray): String {
        val text = String(data, Charsets.UTF_8).trim()
        // Try: if it's already base64 and decodes to 16/24/32 bytes, use it directly
        try {
            val decoded = Base64.decode(text, Base64.DEFAULT)
            if (decoded.size in listOf(16, 24, 32)) return text
        } catch (_: Exception) {}
        // Raw bytes of correct size
        if (data.size in listOf(16, 24, 32)) return Base64.encodeToString(data, Base64.NO_WRAP)
        // Try text as bytes
        val textData = text.toByteArray(Charsets.UTF_8)
        if (textData.size in listOf(16, 24, 32)) return Base64.encodeToString(textData, Base64.NO_WRAP)
        throw IllegalArgumentException("Unsupported AES key material length: ${data.size}")
    }
}
