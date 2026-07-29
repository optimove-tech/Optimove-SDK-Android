package com.optimove.android.optimovemobilesdk

import android.content.Context
import android.util.Base64
import org.json.JSONObject
import java.security.KeyFactory
import java.security.PrivateKey
import java.security.Signature
import java.security.spec.PKCS8EncodedKeySpec

/**
 * SAFE FOR TESTING ONLY — signs JWTs locally to simulate a tenant backend minting
 * tokens for the SDK's [com.optimove.android.AuthTokenProvider] hook.
 *
 * The private key is intentionally NOT checked into source control. It is loaded at
 * runtime from `assets/$KEY_ASSET_NAME`, which is listed in `app/.gitignore`. Place a
 * PKCS#8 PEM private key there (matching the `kid` registered for the QA tenant in the
 * Auth Config Service) before enabling Auth in the QA app.
 */
object QAJWTSigner {
    const val KEY_ASSET_NAME = "qa_jwt_private_key.pem"

    // Must match the `kid` of the key stored for this tenant in the Auth Config Service.
    const val KEY_ID = "3013-qa-2"

    private var privateKey: PrivateKey? = null

    var loadError: String? = null
        private set

    fun initialize(context: Context) {
        if (privateKey != null) return
        try {
            val pem = context.assets.open(KEY_ASSET_NAME).bufferedReader().use { it.readText() }
            privateKey = parsePrivateKey(pem)
            loadError = null
        } catch (e: Exception) {
            loadError = "Could not load assets/$KEY_ASSET_NAME (git-ignored, provision it locally): ${e.message}"
        }
    }

    fun isReady(): Boolean = privateKey != null

    @Throws(Exception::class)
    fun sign(userId: String, subjectOverride: String? = null, expiresInSeconds: Long = 600): String {
        val key = privateKey ?: throw IllegalStateException(
            loadError ?: "QAJWTSigner not initialized. Call QAJWTSigner.initialize(context) first."
        )

        val header = JSONObject()
            .put("alg", "RS256")
            .put("typ", "JWT")
            .put("kid", KEY_ID)

        val nowSeconds = System.currentTimeMillis() / 1000
        val payload = JSONObject()
            .put("sub", subjectOverride ?: userId)
            .put("iss", "3013")
            .put("aud", "optimove")
            .put("iat", nowSeconds)
            .put("exp", nowSeconds + expiresInSeconds)

        val headerB64 = base64Url(header.toString().toByteArray(Charsets.UTF_8))
        val payloadB64 = base64Url(payload.toString().toByteArray(Charsets.UTF_8))
        val signingInput = "$headerB64.$payloadB64"

        val signatureBytes = Signature.getInstance("SHA256withRSA").run {
            initSign(key)
            update(signingInput.toByteArray(Charsets.UTF_8))
            sign()
        }

        return "$signingInput.${base64Url(signatureBytes)}"
    }

    private fun parsePrivateKey(pem: String): PrivateKey {
        val base64 = pem
            .replace("-----BEGIN PRIVATE KEY-----", "")
            .replace("-----END PRIVATE KEY-----", "")
            .replace("\\s".toRegex(), "")
        val der = Base64.decode(base64, Base64.DEFAULT)
        return KeyFactory.getInstance("RSA").generatePrivate(PKCS8EncodedKeySpec(der))
    }

    private fun base64Url(data: ByteArray): String =
        Base64.encodeToString(data, Base64.NO_WRAP or Base64.NO_PADDING or Base64.URL_SAFE)
}
