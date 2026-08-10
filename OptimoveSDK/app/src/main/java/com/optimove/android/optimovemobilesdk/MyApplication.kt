package com.optimove.android.optimovemobilesdk

import android.app.Application
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.os.Build
import android.provider.Settings
import com.optimove.android.Optimove
import com.optimove.android.OptimoveConfig

class MyApplication : Application() {

    override fun onCreate() {
        super.onCreate()

        // Default true so first launch doesn't crash on placeholder credentials.
        // Toggle "Delayed Initialization" in the demo UI, then restart to switch modes.
        val useDelayed = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .getBoolean(KEY_DELAYED_INIT, true)

        val config = if (useDelayed) {
            // No placeholder embedded-messaging / Optimove creds — those must be real base64
            // configs or the app crashes on startup. Gamify/Adact work without them.
            OptimoveConfig.Builder(
                OptimoveConfig.FeatureSet().withOptimove().withOptimobile()
            )
                .enableInAppMessaging(OptimoveConfig.InAppConsentStrategy.AUTO_ENROLL)
                .setPushSmallIconId(R.drawable.small_icon)
                .setPushAccentColor(Color.parseColor("#FF0000"))
                .enableOverlayMessaging(1)
                .build()
        } else {
            OptimoveConfig.Builder(
                DEFAULT_OPTIMOVE_CRED,
                DEFAULT_OPTIMOBILE_CRED
            )
                .enableInAppMessaging(OptimoveConfig.InAppConsentStrategy.AUTO_ENROLL)
                .setPushSmallIconId(R.drawable.small_icon)
                .setPushAccentColor(Color.parseColor("#FF0000"))
                .enableOverlayMessaging(1)
                .build()
        }

        Optimove.initialize(this, config)
        Optimove.enableStagingRemoteLogs()
    }

    companion object {
        const val PREFS_NAME = "optimove_qa_settings"
        const val KEY_DELAYED_INIT = "delayed_init"
        const val DEFAULT_OPTIMOVE_CRED = "optimove_creds"
        const val DEFAULT_OPTIMOBILE_CRED = "optimobile_creds"

        fun askForOverlayPermissions() {
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) return
            val context = Optimove.getInstance().applicationContext
            Intent().apply {
                action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
                addCategory(Intent.CATEGORY_DEFAULT)
                data = Uri.parse("package:${context.packageName}")
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY)
                addFlags(Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS)
            }.let { context.startActivity(it) }
        }
    }
}
