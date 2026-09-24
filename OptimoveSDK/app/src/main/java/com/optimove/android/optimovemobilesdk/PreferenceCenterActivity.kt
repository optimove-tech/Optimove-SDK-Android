package com.optimove.android.optimovemobilesdk

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.optimove.android.optimovemobilesdk.ui.PreferenceCenterScreen
import com.optimove.android.optimovemobilesdk.ui.theme.AppTheme
import com.optimove.android.preferencecenter.Channel
import com.optimove.android.preferencecenter.OptimovePreferenceCenter
import com.optimove.android.preferencecenter.PreferenceUpdate
import com.optimove.android.preferencecenter.Preferences

class PreferenceCenterActivity : AppCompatActivity() {

    private var statusText by mutableStateOf("No preferences loaded yet")
    private var preferences by mutableStateOf<Preferences?>(null)
    private val selections = mutableStateMapOf<String, Set<Channel>>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            AppTheme {
                PreferenceCenterScreen(
                    statusText = statusText,
                    preferences = preferences,
                    selections = selections,
                    onGetPreferences = ::loadPreferences,
                    onToggleChannel = ::toggleChannel,
                    onSaveTopic = ::saveTopic
                )
            }
        }
        loadPreferences()
    }

    private fun loadPreferences() {
        statusText = "Loading preferences..."
        OptimovePreferenceCenter.getInstance().getPreferencesAsync { result, prefs ->
            runOnUiThread {
                when (result) {
                    OptimovePreferenceCenter.ResultType.SUCCESS -> {
                        preferences = prefs
                        selections.clear()
                        prefs?.customerPreferences?.forEach { topic ->
                            selections[topic.id] = topic.subscribedChannels.toSet()
                        }
                        statusText = "Loaded ${prefs?.customerPreferences?.size ?: 0} topic(s)"
                    }
                    OptimovePreferenceCenter.ResultType.ERROR_USER_NOT_SET ->
                        statusText = "User not set."
                    OptimovePreferenceCenter.ResultType.ERROR_CREDENTIALS_NOT_SET ->
                        statusText = "Preference center not configured."
                    OptimovePreferenceCenter.ResultType.ERROR ->
                        statusText = "Failed to fetch preferences."
                    else -> statusText = "Unknown result type."
                }
            }
        }
    }

    private fun toggleChannel(topicId: String, channel: Channel, checked: Boolean) {
        val current = selections[topicId] ?: emptySet()
        selections[topicId] = if (checked) current + channel else current - channel
    }

    private fun saveTopic(topicId: String) {
        val channels = selections[topicId]?.toList() ?: emptyList()
        statusText = "Saving preferences..."
        val update = PreferenceUpdate(topicId, channels)
        OptimovePreferenceCenter.getInstance().setCustomerPreferencesAsync({ result ->
            runOnUiThread {
                when (result) {
                    OptimovePreferenceCenter.ResultType.SUCCESS -> {
                        statusText = "Preferences updated"
                        loadPreferences()
                    }
                    OptimovePreferenceCenter.ResultType.ERROR_USER_NOT_SET ->
                        statusText = "User not set."
                    OptimovePreferenceCenter.ResultType.ERROR_CREDENTIALS_NOT_SET ->
                        statusText = "Preference center not configured."
                    OptimovePreferenceCenter.ResultType.ERROR ->
                        statusText = "Failed to update preferences."
                    else -> statusText = "Unknown result type."
                }
            }
        }, listOf(update))
    }
}
