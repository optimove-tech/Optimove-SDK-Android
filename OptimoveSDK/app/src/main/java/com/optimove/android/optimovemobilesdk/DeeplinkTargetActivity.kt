package com.optimove.android.optimovemobilesdk

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import com.optimove.android.optimovemobilesdk.ui.DeeplinkTargetScreen
import com.optimove.android.optimovemobilesdk.ui.theme.AppTheme

class DeeplinkTargetActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val uri: String? = intent?.data?.toString()
        setContent {
            AppTheme {
                DeeplinkTargetScreen(
                    openedViaUri = uri,
                    testUriHint = DEEPLINK_TEST_URI
                )
            }
        }
    }

    companion object {
        const val DEEPLINK_TEST_URI = "optimoveapp://testdeeplink"
    }
}
