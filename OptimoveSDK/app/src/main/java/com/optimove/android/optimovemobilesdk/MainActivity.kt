package com.optimove.android.optimovemobilesdk

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.setContent
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import android.location.Location
import android.content.Context
import android.content.SharedPreferences
import android.os.Build
import android.provider.Settings
import com.optimove.android.Optimove
import com.optimove.android.main.events.OptimoveEvent
import com.optimove.android.optimobile.InAppDeepLinkHandlerInterface
import com.optimove.android.optimobile.InAppMessageInterceptor
import com.optimove.android.optimobile.InAppMessageInterceptorCallback
import com.optimove.android.optimobile.OptimoveInApp
import com.optimove.android.preferencecenter.Channel
import com.optimove.android.preferencecenter.OptimovePreferenceCenter
import com.optimove.android.preferencecenter.PreferenceUpdate
import com.optimove.android.preferencecenter.Topic
import com.optimove.android.optimovemobilesdk.ui.MainScreen
import com.optimove.android.optimovemobilesdk.ui.theme.AppTheme
import org.json.JSONObject

@SuppressLint("SetTextI18n")
class MainActivity : AppCompatActivity() {

    private var outputText by mutableStateOf("Welcome to the Optimove Android SDK test")
    private var credentialsSubmitted by mutableStateOf(false)
    private var isInterceptingInApp by mutableStateOf(false)
    private var inAppDecisionDialog: AlertDialog? = null
    private var persistedUserId by mutableStateOf("")
    private var persistedUserEmail by mutableStateOf("")
    private lateinit var identityPrefs: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        OptimoveInApp.getInstance().setDeepLinkHandler(object : InAppDeepLinkHandlerInterface {
            override fun handle(context: android.content.Context, buttonPress: InAppDeepLinkHandlerInterface.InAppButtonPress) {
                Log.d(TAG, "DeepLink handler invoked")
                startActivity(Intent(this@MainActivity, MainActivity::class.java))
            }
        })

        val config = Optimove.getConfig()
        val showPreferenceCenter = config.isPreferenceCenterConfigured
        val showEmbeddedMessaging = config.isEmbeddedMessagingConfigured
        val showDelayedConfig = config.usesDelayedConfiguration()

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE), WRITE_EXTERNAL_PERMISSION_REQUEST_CODE)
        }

        Optimove.getInstance().seeIntent(intent, savedInstanceState)
        Optimove.getInstance().pushRequestDeviceToken()

        identityPrefs = getSharedPreferences(IDENTITY_PREF_NAME, Context.MODE_PRIVATE)
        persistedUserEmail = identityPrefs.getString(KEY_USER_EMAIL, "") ?: ""
        persistedUserId = identityPrefs.getString(KEY_USER_ID, "") ?: ""

        setContent {
            AppTheme {
                MainScreen(
                    outputText = outputText,
                    showPreferenceCenter = showPreferenceCenter,
                    showEmbeddedMessaging = showEmbeddedMessaging,
                    showDelayedConfig = showDelayedConfig,
                    credentialsSubmitted = credentialsSubmitted,
                    isInterceptingInApp = isInterceptingInApp,
                    userId = persistedUserId,
                    userEmail = persistedUserEmail,
                    onUserIdChange = { persistedUserId = it },
                    onUserEmailChange = { persistedUserEmail = it },
                    onReportEvent = ::reportEvent,
                    onKillActivity = ::killActivity,
                    onUpdateUserId = ::updateUserId,
                    onClearIdentity = ::clearIdentity,
                    onReadInbox = ::readInbox,
                    onMarkInboxAsRead = ::markInboxAsRead,
                    onDeleteInbox = ::deleteInbox,
                    onGetPreferences = ::getPreferences,
                    onSetPreferences = ::setPreferences,
                    onViewEmbeddedMessaging = ::viewEmbeddedMessaging,
                    onSetCredentials = ::setCredentials,
                    onEnableInAppInterceptionClicked = ::enableInAppInterceptionClicked,
                    onSendLocation = { lat, lng ->
                        val location = Location("test").apply {
                            latitude = lat
                            longitude = lng
                            time = System.currentTimeMillis()
                        }
                        Optimove.getInstance().sendLocationUpdate(location)
                        outputText = "Location sent: ($lat, $lng)"
                    },
                    onResetToken = {},
                    onOpenDeeplinkTest = ::openDeeplinkTest,
                    onRegisterPush = {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
                            ContextCompat.checkSelfPermission(this@MainActivity, Manifest.permission.POST_NOTIFICATIONS)
                                == PackageManager.PERMISSION_DENIED &&
                            !shouldShowRequestPermissionRationale(Manifest.permission.POST_NOTIFICATIONS)
                        ) {
                            outputText = "Notification permission permanently denied. Opening settings..."
                            startActivity(Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS).apply {
                                putExtra(Settings.EXTRA_APP_PACKAGE, this@MainActivity.packageName)
                            })
                        } else {
                            outputText = "Requesting push registration..."
                            Optimove.getInstance().pushRequestDeviceToken()
                            outputText = "Push registration requested"
                        }
                    },
                    onUnregisterPush = {
                        outputText = "Unregistering push..."
                        Optimove.getInstance().pushUnregister()
                        outputText = "Push unregistration requested"
                    }
                )
            }
        }
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        Optimove.getInstance().seeInputFocus(hasFocus)
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        Optimove.getInstance().seeIntent(intent)
    }

    override fun onDestroy() {
        inAppDecisionDialog?.takeIf { it.isShowing }?.dismiss()
        super.onDestroy()
    }

    private fun reportEvent() {
        outputText = "Reporting Custom Event for Visitor without optional value"
        runFromWorker { Optimove.getInstance().reportEvent(SimpleCustomEvent()) }
        runFromWorker { Optimove.getInstance().reportEvent("Event_No ParaMs     ") }
    }

    private fun killActivity() {
        Log.d("TAG", "killActivity called")
        finish()
    }

    private fun updateUserId(userId: String, userEmail: String) {
        when {
            userEmail.isEmpty() -> {
                outputText = "Calling setUserId"
                Optimove.getInstance().setUserId(userId)
            }
            userId.isEmpty() -> {
                outputText = "Calling setUserEmail"
                Optimove.getInstance().setUserEmail(userEmail)
            }
            else -> {
                outputText = "Calling registerUser"
                Optimove.getInstance().registerUser(userId, userEmail)
            }
        }
        identityPrefs.edit()
            .putString(KEY_USER_ID, userId)
            .putString(KEY_USER_EMAIL, userEmail)
            .apply()
    }

    private fun clearIdentity() {
        identityPrefs.edit()
            .remove(KEY_USER_ID)
            .remove(KEY_USER_EMAIL)
            .apply()
        persistedUserId = ""
        persistedUserEmail = ""
        outputText = "Identity cleared (saved values removed)"
    } 

    private fun readInbox() {
        val items = OptimoveInApp.getInstance().inboxItems
        if (items.isEmpty()) {
            Log.d(TAG, "no inbox items!")
            return
        }
        items.forEach { item ->
            Log.d(TAG, "title: ${item.title}, isRead: ${item.isRead}")
        }
    }

    private fun markInboxAsRead() {
        Log.d(TAG, "mark  all inbox read")
        OptimoveInApp.getInstance().markAllInboxItemsAsRead()
    }

    private fun deleteInbox() {
        val items = OptimoveInApp.getInstance().inboxItems
        if (items.isEmpty()) {
            Log.d(TAG, "no inbox items!")
            return
        }
        items.forEach { OptimoveInApp.getInstance().deleteMessageFromInbox(it) }
    }

    private fun getPreferences() {
        OptimovePreferenceCenter.getInstance().getPreferencesAsync { result, preferences ->
            when (result) {
                OptimovePreferenceCenter.ResultType.ERROR_USER_NOT_SET,
                OptimovePreferenceCenter.ResultType.ERROR,
                OptimovePreferenceCenter.ResultType.ERROR_CREDENTIALS_NOT_SET -> Log.d(PC_TAG, result.toString())
                OptimovePreferenceCenter.ResultType.SUCCESS -> preferences?.let { prefs ->
                    Log.d(PC_TAG, "configured: ${prefs.configuredChannels}")
                    prefs.customerPreferences.forEach { topic ->
                        Log.d(PC_TAG, "${topic.id} ${topic.name} ${topic.subscribedChannels}")
                    }
                }
                else -> Log.d(PC_TAG, "unknown res type")
            }
        }
    }

    private fun setPreferences() {
        OptimovePreferenceCenter.getInstance().getPreferencesAsync { result, preferences ->
            when (result) {
                OptimovePreferenceCenter.ResultType.ERROR_USER_NOT_SET,
                OptimovePreferenceCenter.ResultType.ERROR,
                OptimovePreferenceCenter.ResultType.ERROR_CREDENTIALS_NOT_SET -> Log.d(PC_TAG, result.toString())
                OptimovePreferenceCenter.ResultType.SUCCESS -> preferences?.let { prefs ->
                    Log.d(PC_TAG, "loaded prefs for set: good")
                    val configuredChannels: List<Channel> = prefs.configuredChannels
                    val topics = prefs.customerPreferences
                    val updates = topics.map { topic ->
                        PreferenceUpdate(topic.id, configuredChannels.subList(0, 1))
                    }
                    OptimovePreferenceCenter.getInstance().setCustomerPreferencesAsync(
                        { setResult -> Log.d(PC_TAG, setResult.toString()) },
                        updates
                    )
                }
                else -> Log.d(PC_TAG, "unknown res type")
            }
        }
    }

    private fun viewEmbeddedMessaging() {
        startActivity(Intent(this, EmbeddedMessagingActivity::class.java))
    }

    private fun openDeeplinkTest() {
        startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(DeeplinkTargetActivity.DEEPLINK_TEST_URI)))
    }

    private fun setCredentials(optimove: String?, optimobile: String?, prefCenter: String?) {
        if (optimove.isNullOrEmpty() && optimobile.isNullOrEmpty()) return
        try {
            Optimove.setCredentials(optimove, optimobile, prefCenter)
        } catch (e: Exception) {
            outputText = e.message ?: "Error"
            return
        }
        outputText = "Credentials submitted"
        credentialsSubmitted = true
    }

    private fun runFromWorker(runnable: () -> Unit) {
        Thread(runnable).start()
    }

    private fun enableInAppInterceptionClicked() {
        val options = arrayOf("Default (5000 ms)", "12,000 ms")
        var selected = 0

        AlertDialog.Builder(this)
            .setTitle("Enable In-App Interception")
            .setSingleChoiceItems(options, 0) { _, which -> selected = which }
            .setPositiveButton("Enable") { d, _ ->
                val timeoutMs = if (selected == 0) 5000L else 12000L
                enableInAppInterception(timeoutMs)
                isInterceptingInApp = true
                Toast.makeText(this, "In-App interception enabled ($timeoutMs ms)", Toast.LENGTH_SHORT).show()
                d.dismiss()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun enableInAppInterception(timeoutMs: Long) {
        OptimoveInApp.getInstance().setInAppMessageInterceptor(object : InAppMessageInterceptor {
            override fun processMessage(messageData: JSONObject?, decision: InAppMessageInterceptorCallback) {
                runOnUiThread {
                    inAppDecisionDialog?.takeIf { it.isShowing }?.dismiss()
                    val dataText = messageData?.toString() ?: "No data provided"
                    inAppDecisionDialog = AlertDialog.Builder(this@MainActivity)
                        .setTitle("QA: In-App Message")
                        .setMessage("$dataText\nShow this message?")
                        .setPositiveButton("Show") { dialog, _ ->
                            decision.show()
                            dialog.dismiss()
                        }
                        .setNegativeButton("Suppress") { dialog, _ ->
                            decision.suppress()
                            dialog.dismiss()
                        }
                        .setOnCancelListener { decision.suppress() }
                        .create()
                    inAppDecisionDialog?.show()
                }
            }

            override fun getTimeoutMs(): Long = timeoutMs
        })
    }

    private class SimpleCustomEvent : OptimoveEvent() {
        override fun getName(): String = "Simple cUSTOM_Event     "
        override fun getParameters(): Map<String, Any> = mapOf(
            "strinG_param" to "  some_string  ",
            "number_param" to 42
        )
    }

    companion object {
        private const val TAG = "TestAppMainActvity"
        private const val PC_TAG = "OptimovePC"
        private const val WRITE_EXTERNAL_PERMISSION_REQUEST_CODE = 169
        private const val IDENTITY_PREF_NAME = "optimove_identity"
        private const val KEY_USER_EMAIL = "user_email"
        private const val KEY_USER_ID = "user_id"
    }
}
