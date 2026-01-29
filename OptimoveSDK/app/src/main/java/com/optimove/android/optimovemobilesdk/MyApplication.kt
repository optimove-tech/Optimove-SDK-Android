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
                "WyIxIiwgIjgwYTRhMjI0ZGRkMTRhNDQ4MTNlYzIwZmNkMjAxNjE2IiwgIm1vYmlsZS1jb25maWd1cmF0aW9uLjEuMC4wLXN0ZyJd",
                "WzEsInVrLTEiLCI2YjE5OThhYS1lZmM1LTRjODUtYjg4ZC1mMjQzMTE4ODA1NTAiLCJKcTMxVEJ6dmxmVTQxb2xzMXltQVZTSVdjNXlnY3VmbHpjbysiXQ=="
            )
                .enableInAppMessaging(OptimoveConfig.InAppConsentStrategy.AUTO_ENROLL)
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
