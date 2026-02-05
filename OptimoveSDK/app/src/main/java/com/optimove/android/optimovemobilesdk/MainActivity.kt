package com.optimove.android.optimovemobilesdk

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
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
import com.optimove.android.Optimove
import com.optimove.android.main.events.OptimoveEvent
import com.optimove.android.optimobile.InAppDeepLinkHandlerInterface
import com.optimove.android.optimobile.Optimobile
import com.optimove.android.optimobile.InAppMessageInterceptor
import com.optimove.android.optimobile.InAppMessageInterceptorCallback
import com.optimove.android.optimobile.OptimoveInApp
import com.optimove.android.preferencecenter.Channel
import com.optimove.android.preferencecenter.OptimovePreferenceCenter
import com.optimove.android.preferencecenter.PreferenceUpdate
import com.optimove.android.preferencecenter.Topic
import com.optimove.android.optimovemobilesdk.ui.MainScreen
import com.optimove.android.optimovemobilesdk.ui.theme.AppTheme
import android.text.InputFilter
import android.widget.EditText
import org.json.JSONObject

@SuppressLint("SetTextI18n")
class MainActivity : AppCompatActivity() {

    private var outputText by mutableStateOf("Welcome to the Optimove Android SDK test")
    private var credentialsSubmitted by mutableStateOf(false)
    private var isInterceptingInApp by mutableStateOf(false)
    private var lastSetLocation by mutableStateOf<String?>(null)
    private var inAppDecisionDialog: AlertDialog? = null

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

        setContent {
            AppTheme {
                MainScreen(
                    outputText = outputText,
                    showPreferenceCenter = showPreferenceCenter,
                    showEmbeddedMessaging = showEmbeddedMessaging,
                    showDelayedConfig = showDelayedConfig,
                    credentialsSubmitted = credentialsSubmitted,
                    isInterceptingInApp = isInterceptingInApp,
                    onReportEvent = ::reportEvent,
                    onKillActivity = ::killActivity,
                    onUpdateUserId = ::updateUserId,
                    onReadInbox = ::readInbox,
                    onMarkInboxAsRead = ::markInboxAsRead,
                    onDeleteInbox = ::deleteInbox,
                    onGetPreferences = ::getPreferences,
                    onSetPreferences = ::setPreferences,
                    onViewEmbeddedMessaging = ::viewEmbeddedMessaging,
                    onSetCredentials = ::setCredentials,
                    onEnableInAppInterceptionClicked = ::enableInAppInterceptionClicked,
                    onResetToken = {},
                    onOpenDeeplinkTest = ::openDeeplinkTest,
                    lastSetLocation = lastSetLocation,
                    onSetLocationClicked = ::showSetLocationDialog,
                    onSetAttributesClicked = ::showSetAttributesDialog
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

    private fun showSetLocationDialog() {
        val dialogView = layoutInflater.inflate(R.layout.dialog_set_location, null)
        val latInput = dialogView.findViewById<EditText>(R.id.lat_input)
        val lngInput = dialogView.findViewById<EditText>(R.id.lng_input)

        lastSetLocation?.let { location ->
            val parts = location.split(",").map { it.trim() }
            if (parts.size == 2) {
                latInput.setText(parts[0])
                lngInput.setText(parts[1])
            }
        }

        val commaFilter = InputFilter { source, _, _, _, _, _ ->
            if (source.toString().contains(",")) "" else null
        }
        latInput.filters = arrayOf(commaFilter)
        lngInput.filters = arrayOf(commaFilter)

        val dialog = AlertDialog.Builder(this)
            .setTitle("Set Location")
            .setView(dialogView)
            .setPositiveButton("Set Location", null)
            .setNegativeButton("Cancel", null)
            .create()

        dialog.setOnShowListener {
            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener {
                val latText = latInput.text.toString().replace(",", ".").trim()
                val lngText = lngInput.text.toString().replace(",", ".").trim()
                val lat = latText.toDoubleOrNull()
                val lng = lngText.toDoubleOrNull()

                when {
                    lat == null || lng == null -> {
                        Toast.makeText(this, "Invalid coordinates. Enter numbers only.", Toast.LENGTH_LONG).show()
                    }
                    lat !in -90.0..90.0 -> {
                        Toast.makeText(this, "Latitude must be between -90 and 90.", Toast.LENGTH_LONG).show()
                    }
                    lng !in -180.0..180.0 -> {
                        Toast.makeText(this, "Longitude must be between -180 and 180.", Toast.LENGTH_LONG).show()
                    }
                    else -> {
                        sendTestLocation(lat, lng)
                        dialog.dismiss()
                    }
                }
            }
        }
        dialog.show()
    }

    private fun sendTestLocation(latitude: Double, longitude: Double) {
        val testLocation = Location("test").apply {
            this.latitude = latitude
            this.longitude = longitude
            time = System.currentTimeMillis()
        }
        Optimove.getInstance().sendLocationUpdate(testLocation)
        lastSetLocation = "$latitude, $longitude"
        Toast.makeText(this, "Location sent", Toast.LENGTH_SHORT).show()
    }

    private fun showSetAttributesDialog() {
        val dialogView = layoutInflater.inflate(R.layout.dialog_set_attributes, null)
        val keyInput = dialogView.findViewById<EditText>(R.id.attr_key_input)
        val valueInput = dialogView.findViewById<EditText>(R.id.attr_value_input)

        val dialog = AlertDialog.Builder(this)
            .setTitle("Set Attributes")
            .setView(dialogView)
            .setPositiveButton("Send", null)
            .setNegativeButton("Cancel", null)
            .create()

        dialog.setOnShowListener {
            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener {
                val key = keyInput.text.toString().trim()
                val value = valueInput.text.toString().trim()

                when {
                    key.isEmpty() -> {
                        Toast.makeText(this, "Attribute key is required.", Toast.LENGTH_LONG).show()
                    }
                    value.isEmpty() -> {
                        Toast.makeText(this, "Attribute value is required.", Toast.LENGTH_LONG).show()
                    }
                    else -> {
                        sendAttributes(key, value)
                        dialog.dismiss()
                    }
                }
            }
        }
        dialog.show()
    }

    private fun sendAttributes(key: String, value: String) {
        val userId = Optimobile.getCurrentUserIdentifier(this)
        val attributes = JSONObject().apply { put(key, value) }
        Optimobile.associateUserWithInstall(this, userId, attributes)
        outputText = "Sent attribute: $key = $value"
        Toast.makeText(this, "Attributes sent", Toast.LENGTH_SHORT).show()
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
    }
}
