package com.optimove.android.optimovemobilesdk

import android.util.Base64
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.optimove.android.AuthTokenProvider
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

enum class AuthDebugMode(val label: String) {
    VALID("Return valid JWT"),
    EXPIRED("Return expired JWT"),
    SUBJECT_MISMATCH("Return JWT with wrong subject"),
    INVALID_SIGNATURE("Return JWT with invalid signature"),
    PROVIDER_ERROR("Fail to return JWT")
}

/**
 * QA-only [AuthTokenProvider] that lets a developer pick which scenario the SDK's auth
 * hook should simulate on its next call, mirroring the iOS QA app's AuthDebugState.
 */
object AuthDebugState : AuthTokenProvider {

    var mode: AuthDebugMode by mutableStateOf(AuthDebugMode.VALID)

    var lastUserId: String by mutableStateOf("")
        private set
    var lastToken: String by mutableStateOf("")
        private set
    var lastPayload: String by mutableStateOf("")
        private set
    var lastIssuedAt: String by mutableStateOf("Never")
        private set
    var lastError: String by mutableStateOf("")
        private set
    var issuedTokenCount: Int by mutableStateOf(0)
        private set

    override fun getToken(userId: String, callback: AuthTokenProvider.Callback) {
        try {
            val token = tokenForCurrentMode(userId)
            recordSuccess(userId, token)
            callback.onComplete(token, null)
        } catch (e: Exception) {
            recordFailure(userId, e)
            callback.onComplete(null, e)
        }
    }

    fun clear() {
        lastUserId = ""
        lastToken = ""
        lastPayload = ""
        lastIssuedAt = "Never"
        lastError = ""
        issuedTokenCount = 0
    }

    private fun tokenForCurrentMode(userId: String): String = when (mode) {
        AuthDebugMode.VALID -> QAJWTSigner.sign(userId = userId)
        AuthDebugMode.EXPIRED -> QAJWTSigner.sign(userId = userId, expiresInSeconds = -60)
        AuthDebugMode.SUBJECT_MISMATCH -> QAJWTSigner.sign(userId = userId, subjectOverride = "${userId}_mismatch")
        AuthDebugMode.INVALID_SIGNATURE -> corruptSignature(QAJWTSigner.sign(userId = userId))
        AuthDebugMode.PROVIDER_ERROR -> throw Exception("QA auth debug provider error")
    }

    private fun recordSuccess(userId: String, token: String) {
        lastUserId = userId
        lastToken = token
        lastPayload = prettyPayload(token)
        lastIssuedAt = timestamp()
        lastError = ""
        issuedTokenCount += 1
    }

    private fun recordFailure(userId: String, error: Exception) {
        lastUserId = userId
        lastToken = ""
        lastPayload = ""
        lastIssuedAt = timestamp()
        lastError = error.message ?: "Unknown error"
    }

    // The last base64url char of an RS256 signature only carries a couple of meaningful
    // bits, so mutate a char in the middle of the signature segment instead — guaranteed
    // to change the decoded signature bytes.
    private fun corruptSignature(token: String): String {
        val parts = token.split(".")
        if (parts.size != 3 || parts[2].isEmpty()) return token
        val sigChars = parts[2].toCharArray()
        val idx = sigChars.size / 2
        sigChars[idx] = if (sigChars[idx] == 'A') 'B' else 'A'
        return "${parts[0]}.${parts[1]}.${String(sigChars)}"
    }

    private fun prettyPayload(token: String): String {
        val parts = token.split(".")
        if (parts.size < 2) return "Unable to decode JWT payload"
        return try {
            val decoded = Base64.decode(parts[1], Base64.URL_SAFE or Base64.NO_WRAP)
            JSONObject(String(decoded, Charsets.UTF_8)).toString(2)
        } catch (e: Exception) {
            "Unable to decode JWT payload"
        }
    }

    private fun timestamp(): String = SimpleDateFormat("HH:mm:ss", Locale.US).format(Date())
}
