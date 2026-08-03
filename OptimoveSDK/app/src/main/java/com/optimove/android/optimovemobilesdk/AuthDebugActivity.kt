package com.optimove.android.optimovemobilesdk

import android.content.Context
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.activity.compose.setContent
import com.optimove.android.optimovemobilesdk.ui.AuthDebugScreen
import com.optimove.android.optimovemobilesdk.ui.theme.AppTheme

class AuthDebugActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val isAuthEnabled = getSharedPreferences(MyApplication.PREFS_NAME, Context.MODE_PRIVATE)
            .getBoolean(MyApplication.KEY_AUTH_ENABLED, false)

        setContent {
            AppTheme {
                AuthDebugScreen(
                    isAuthEnabled = isAuthEnabled,
                    mode = AuthDebugState.mode,
                    onModeChange = { AuthDebugState.mode = it },
                    lastUserId = AuthDebugState.lastUserId,
                    lastIssuedAt = AuthDebugState.lastIssuedAt,
                    issuedTokenCount = AuthDebugState.issuedTokenCount,
                    lastError = AuthDebugState.lastError,
                    lastPayload = AuthDebugState.lastPayload,
                    lastToken = AuthDebugState.lastToken,
                    onClear = { AuthDebugState.clear() }
                )
            }
        }
    }
}
