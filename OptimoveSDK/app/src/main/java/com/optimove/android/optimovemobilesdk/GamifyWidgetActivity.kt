package com.optimove.android.optimovemobilesdk

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.optimove.android.gamifywidgetsdk.GamifyWidgetSDK
import com.optimove.android.optimovemobilesdk.ui.GamifyWidgetScreen
import com.optimove.android.optimovemobilesdk.ui.theme.AppTheme

enum class GamifyEnv(val label: String, val baseUrl: String) {
    DEV("Dev", "https://opti-ls-widget-dev.optimove.net"),
    PROD_US("Prod US", "https://opti-ls-widget-us.optimove.net"),
    PROD_EU("Prod EU", "https://opti-ls-widget-eu.optimove.net")
}

class GamifyWidgetActivity : AppCompatActivity() {

    private lateinit var prefs: SharedPreferences

    private var tenant by mutableStateOf("")
    private var userId by mutableStateOf("")
    private var env by mutableStateOf(GamifyEnv.DEV)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        tenant = prefs.getString(KEY_TENANT, "") ?: ""
        userId = prefs.getString(KEY_USER_ID, "") ?: ""
        env = enumValues<GamifyEnv>().find { it.name == prefs.getString(KEY_ENV, null) } ?: GamifyEnv.DEV

        setContent {
            AppTheme {
                GamifyWidgetScreen(
                    tenant = tenant,
                    userId = userId,
                    env = env,
                    onTenantChange = { tenant = it; save() },
                    onWidgetIdChange = { userId = it; save() },
                    onEnvChange = { env = it; save() },
                    onOpenWidget = ::openWidget
                )
            }
        }
    }

    private fun save() {
        prefs.edit()
            .putString(KEY_TENANT, tenant)
            .putString(KEY_USER_ID, userId)
            .putString(KEY_ENV, env.name)
            .apply()
    }

    private fun openWidget() {
        val widgetUrl = "${env.baseUrl}/$tenant/$userId"
        GamifyWidgetSDK.init(widgetUrl = widgetUrl)
        GamifyWidgetSDK.open(supportFragmentManager)
    }

    companion object {
        private const val PREFS_NAME = "gamify_widget_config"
        private const val KEY_TENANT = "tenant"
        private const val KEY_USER_ID = "user_id"
        private const val KEY_ENV = "env"
    }
}
