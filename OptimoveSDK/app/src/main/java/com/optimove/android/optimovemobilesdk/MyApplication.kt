package com.optimove.android.optimovemobilesdk

import android.app.Application
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
        Optimove.initialize(
            this,
            OptimoveConfig.Builder(
                "optimove_creds",
                "optimobile_creds"
            )
                .enableInAppMessaging(OptimoveConfig.InAppConsentStrategy.AUTO_ENROLL)
                .enableEmbeddedMessaging("embedded_config_string")
                .setPushSmallIconId(R.drawable.small_icon)
                .setPushAccentColor(Color.parseColor("#FF0000"))
                .build()
        )
        Optimove.enableStagingRemoteLogs()
    }

    companion object {
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
