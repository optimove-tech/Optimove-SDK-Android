package com.optimove.android.optimovemobilesdk

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.optimove.android.Optimove
import com.optimove.android.gamifywidgetsdk.GamifyWidgetSDK
import com.optimove.android.gamifywidgetsdk.OpenAdactParams
import com.optimove.android.gamifywidgetsdk.OpenLoyaltyWidgetParams
import com.optimove.android.optimovemobilesdk.ui.GamifyWidgetScreen
import com.optimove.android.optimovemobilesdk.ui.theme.AppTheme

/**
 * Both gamify URLs come from the `gamify` block of the tenant config, so this screen only shows them — set Optimove
 * credentials on the main screen for them to load.
 */
class GamifyWidgetActivity : AppCompatActivity() {

    private lateinit var prefs: SharedPreferences

    private var userId by mutableStateOf("")
    private var brand by mutableStateOf("")
    private var campaignId by mutableStateOf("")
    private var cid by mutableStateOf("")
    private var adactToken by mutableStateOf("")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        userId = prefs.getString(KEY_USER_ID, "") ?: ""
        brand = prefs.getString(KEY_BRAND, "") ?: ""
        campaignId = prefs.getString(KEY_CAMPAIGN_ID, "") ?: ""
        cid = prefs.getString(KEY_CID, "") ?: ""
        adactToken = prefs.getString(KEY_ADACT_TOKEN, "") ?: ""

        setContent {
            AppTheme {
                GamifyWidgetScreen(
                    userId = userId,
                    brand = brand,
                    loyaltyWidgetSrc = loyaltyWidgetSrc(),
                    adactUrl = GamifyWidgetSDK.getInstance().adactUrl,
                    campaignId = campaignId,
                    cid = cid,
                    adactToken = adactToken,
                    onUserIdChange = { userId = it; save() },
                    onBrandChange = { brand = it; save() },
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
            .putString(KEY_USER_ID, userId)
            .putString(KEY_BRAND, brand)
            .putString(KEY_CAMPAIGN_ID, campaignId)
            .putString(KEY_CID, cid)
            .putString(KEY_ADACT_TOKEN, adactToken)
            .apply()
    }

    private fun widgetParams() = OpenLoyaltyWidgetParams(userId.ifBlank { null }, brand.ifBlank { null })

    /** What openWidget will load; the SDK user stands in when User ID is blank. */
    private fun loyaltyWidgetSrc(): String {
        val params = widgetParams()
        val resolvedUserId = params.userId
            ?: runCatching { Optimove.getInstance().userInfo.userId }.getOrNull()
        return GamifyWidgetSDK.getInstance().buildLoyaltyWidgetUrl(resolvedUserId, params.brand)
    }

    /** When auth is enabled the SDK mints the JWT for the user itself. */
    private fun openWidget() {
        GamifyWidgetSDK.getInstance().openWidget(this, widgetParams())
    }

    private fun openAdactCampaign() {
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
        GamifyWidgetSDK.getInstance().closeAdactCampaign()
    }

    companion object {
        private const val PREFS_NAME = "gamify_widget_config"
        private const val KEY_USER_ID = "user_id"
        private const val KEY_BRAND = "brand"
        private const val KEY_CAMPAIGN_ID = "campaign_id"
        private const val KEY_CID = "cid"
        private const val KEY_ADACT_TOKEN = "adact_token"
    }
}
