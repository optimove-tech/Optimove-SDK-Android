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
import com.optimove.android.gamifywidgetsdk.OpenAdactParams
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
    private var adactUrl by mutableStateOf(DEFAULT_ADACT_URL)
    private var campaignId by mutableStateOf("")
    private var cid by mutableStateOf("")
    private var adactToken by mutableStateOf("")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        tenant = prefs.getString(KEY_TENANT, "") ?: ""
        userId = prefs.getString(KEY_USER_ID, "") ?: ""
        env = enumValues<GamifyEnv>().find { it.name == prefs.getString(KEY_ENV, null) } ?: GamifyEnv.DEV
        adactUrl = prefs.getString(KEY_ADACT_URL, DEFAULT_ADACT_URL) ?: DEFAULT_ADACT_URL
        campaignId = prefs.getString(KEY_CAMPAIGN_ID, "") ?: ""
        cid = prefs.getString(KEY_CID, "") ?: ""
        adactToken = prefs.getString(KEY_ADACT_TOKEN, "") ?: ""

        setContent {
            AppTheme {
                GamifyWidgetScreen(
                    tenant = tenant,
                    userId = userId,
                    env = env,
                    adactUrl = adactUrl,
                    campaignId = campaignId,
                    cid = cid,
                    adactToken = adactToken,
                    onTenantChange = { tenant = it; save() },
                    onWidgetIdChange = { userId = it; save() },
                    onEnvChange = { env = it; save() },
                    onAdactUrlChange = { adactUrl = it; save() },
                    onCampaignIdChange = { campaignId = it; save() },
                    onCidChange = { cid = it; save() },
                    onAdactTokenChange = { adactToken = it; save() },
                    onOpenWidget = ::openWidget,
                    onOpenAdactCampaign = ::openAdactCampaign,
                    onCloseAdactCampaign = ::closeAdactCampaign
                )
            }
        }
    }

    private fun save() {
        prefs.edit()
            .putString(KEY_TENANT, tenant)
            .putString(KEY_USER_ID, userId)
            .putString(KEY_ENV, env.name)
            .putString(KEY_ADACT_URL, adactUrl)
            .putString(KEY_CAMPAIGN_ID, campaignId)
            .putString(KEY_CID, cid)
            .putString(KEY_ADACT_TOKEN, adactToken)
            .apply()
    }

    private fun ensureInitialized() {
        val widgetUrl = if (tenant.isNotBlank() && userId.isNotBlank()) {
            "${env.baseUrl}/$tenant/$userId"
        } else {
            null
        }
        GamifyWidgetSDK.initialize(widgetUrl, adactUrl.ifBlank { null })
    }

    private fun openWidget() {
        ensureInitialized()
        GamifyWidgetSDK.getInstance().open(this, userId, null)
    }

    private fun openAdactCampaign() {
        ensureInitialized()
        val id = campaignId.trim().toIntOrNull() ?: return
        GamifyWidgetSDK.getInstance().openAdactCampaign(
            this,
            OpenAdactParams(
                id,
                cid.ifBlank { null },
                adactToken.ifBlank { null }
            )
        )
    }

    private fun closeAdactCampaign() {
        try {
            GamifyWidgetSDK.getInstance().closeAdactCampaign()
        } catch (_: IllegalStateException) {
            // SDK not initialized yet
        }
    }

    companion object {
        private const val PREFS_NAME = "gamify_widget_config"
        private const val KEY_TENANT = "tenant"
        private const val KEY_USER_ID = "user_id"
        private const val KEY_ENV = "env"
        private const val KEY_ADACT_URL = "adact_url"
        private const val KEY_CAMPAIGN_ID = "campaign_id"
        private const val KEY_CID = "cid"
        private const val KEY_ADACT_TOKEN = "adact_token"
        private const val DEFAULT_ADACT_URL = "https://adact-campaign-dev.optimove.net/"
    }
}
