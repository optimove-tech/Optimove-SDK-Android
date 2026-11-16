package com.optimove.android.optimovemobilesdk

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.google.firebase.messaging.RemoteMessage
import com.optimove.android.optimobile.FirebaseMessageHandler
import com.optimove.android.optimobile.LiveUpdate
import com.optimove.android.optimobile.OptimoveLiveUpdates
import com.optimove.android.optimovemobilesdk.ui.theme.AppTheme
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

/**
 * QA screen for Live Updates. "Simulated push" builds a real FCM RemoteMessage in the 292915 envelope and
 * feeds it to FirebaseMessageHandler on a background thread, the same path as a delivered FCM message.
 */
class LiveUpdatesActivity : AppCompatActivity() {

    private var name by mutableStateOf(newName())
    private var type by mutableStateOf("SportsActivityAttributes")
    private var timestamp by mutableStateOf(nowSeconds().toString())
    private var homeScore by mutableStateOf("0")
    private var awayScore by mutableStateOf("0")
    private var status by mutableStateOf("Kick-off")
    private var dismissalMinutes by mutableStateOf("5")
    private var includeAlert by mutableStateOf(false)
    private var active by mutableStateOf<List<LiveUpdate>>(emptyList())

    private val timeFormat = SimpleDateFormat("HH:mm:ss", Locale.US)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val prefs = getSharedPreferences(PREFS, MODE_PRIVATE)
        name = prefs.getString(KEY_NAME, null) ?: name
        timestamp = prefs.getString(KEY_TIMESTAMP, null) ?: timestamp
        setContent {
            AppTheme {
                LaunchedEffect(LiveUpdateDebugLog.entries.size) { refreshActive() }
                Screen()
            }
        }
    }

    override fun onResume() {
        super.onResume()
        refreshActive()
    }

    override fun onPause() {
        super.onPause()
        getSharedPreferences(PREFS, MODE_PRIVATE).edit()
            .putString(KEY_NAME, name)
            .putString(KEY_TIMESTAMP, timestamp)
            .apply()
    }

    @Composable
    private fun Screen() {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            SectionTitle("Activity")
            OutlinedTextField(
                value = name,
                onValueChange = { name = it.trim() },
                label = { Text("name") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = type,
                onValueChange = { type = it.trim() },
                label = { Text("type (attributes_type)") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                NumberField("timestamp (s)", timestamp, Modifier.weight(2f)) { timestamp = it }
                ActionButton("Now") { timestamp = nowSeconds().toString() }
                ActionButton("New name") { name = newName(); timestamp = nowSeconds().toString() }
            }

            SectionTitle("Content")
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                NumberField("Spain", homeScore, Modifier.weight(1f)) { homeScore = it }
                NumberField("Argentina", awayScore, Modifier.weight(1f)) { awayScore = it }
                NumberField("dismiss after (min)", dismissalMinutes, Modifier.weight(1f)) { dismissalMinutes = it }
            }
            OutlinedTextField(
                value = status,
                onValueChange = { status = it },
                label = { Text("status") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
            Row(verticalAlignment = Alignment.CenterVertically) {
                Checkbox(checked = includeAlert, onCheckedChange = { includeAlert = it })
                Text("Include alert (heads-up / sound)", style = MaterialTheme.typography.bodyMedium)
            }

            SectionTitle("Simulated push (FCM path)")
            Text(
                "Timestamp advances by 1s after each push. \"Update (older)\" sends timestamp - 60 to test staleness.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                ActionButton("Start") { simulatePush("start") }
                ActionButton("Update") { simulatePush("update") }
                ActionButton("End") { simulatePush("end") }
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                ActionButton("Update (older)") { simulatePush("update", timestampOverride = currentTimestamp() - 60) }
                ActionButton("Malformed", containerColor = MaterialTheme.colorScheme.error) {
                    simulateRaw("malformed", "{\"event\":\"start\"}")
                }
                ActionButton("Normal push") { simulateNormalPush() }
            }

            SectionTitle("Local API")
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                ActionButton("end(name)") {
                    OptimoveLiveUpdates.getInstance().end(name)
                    LiveUpdateDebugLog.add("local end($name)")
                }
                ActionButton("clearAll()", containerColor = MaterialTheme.colorScheme.error) {
                    OptimoveLiveUpdates.getInstance().clearAll()
                    LiveUpdateDebugLog.add("local clearAll()")
                }
                ActionButton("Clear log") { LiveUpdateDebugLog.clear() }
            }

            SectionTitle("Active (${active.size})")
            if (!OptimoveLiveUpdates.getInstance().isEnabled) {
                MonoText("Live Updates not enabled in config")
            } else if (active.isEmpty()) {
                MonoText("none")
            }
            active.forEach { liveUpdate ->
                MonoText(
                    "${liveUpdate.name}\n  type=${liveUpdate.type} ts=${liveUpdate.timestamp}" +
                        " started=${timeFormat.format(Date(liveUpdate.startedAtMillis))}" +
                        " updated=${timeFormat.format(Date(liveUpdate.lastUpdatedAtMillis))}" +
                        "\n  ${liveUpdate.contentState}",
                    modifier = Modifier.padding(bottom = 4.dp)
                )
            }

            SectionTitle("Log")
            LiveUpdateDebugLog.entries.forEach { MonoText(it) }
        }
    }

    // ---------------------------------------------------------------------------------------------

    private fun contentState(): JSONObject = JSONObject()
        .put("homeTeam", "Spain")
        .put("awayTeam", "Argentina")
        .put("homeScore", homeScore.toIntOrNull() ?: 0)
        .put("awayScore", awayScore.toIntOrNull() ?: 0)
        .put("status", status)

    private fun currentTimestamp(): Long = timestamp.toLongOrNull() ?: nowSeconds()

    private fun simulatePush(event: String, timestampOverride: Long? = null) {
        val ts = timestampOverride ?: currentTimestamp()
        val envelope = JSONObject()
            .put("event", event)
            .put("name", name)
            .put("type", type)
            .put("timestamp", ts)
            .put("content_state", contentState())
        if (event == "start") {
            envelope.put("topic", "lu_qa_$name")
        }
        if (event == "end") {
            val minutes = dismissalMinutes.toLongOrNull() ?: 0L
            if (minutes > 0) envelope.put("dismissal_date", nowSeconds() + minutes * 60)
        }
        if (includeAlert) {
            envelope.put("alert", JSONObject().put("title", "Goal!").put("body", "$status $homeScore-$awayScore"))
        }

        simulateRaw("$event ts=$ts", envelope)
        if (timestampOverride == null) {
            timestamp = (ts + 1).toString()
        }
    }

    private fun simulateRaw(label: String, liveUpdate: Any) {
        val custom = JSONObject().put("a", JSONObject().put("k.liveUpdate", liveUpdate))
        deliver("push $label", custom)
    }

    private fun simulateNormalPush() {
        val custom = JSONObject().put(
            "a",
            JSONObject().put("k.message", JSONObject().put("type", 1).put("data", JSONObject().put("id", 1)))
        )
        deliver("normal push (no k.liveUpdate)", custom, title = "QA normal push", alert = "Should show as a standard push")
    }

    private fun deliver(label: String, custom: JSONObject, title: String? = null, alert: String? = null) {
        val builder = RemoteMessage.Builder("qa@fcm.googleapis.com")
            .setMessageId(UUID.randomUUID().toString())
            .addData("custom", custom.toString())
        title?.let { builder.addData("title", it) }
        alert?.let { builder.addData("alert", it) }
        val message = builder.build()

        Thread {
            val handled = FirebaseMessageHandler.onMessageReceived(applicationContext, message)
            LiveUpdateDebugLog.add("$label -> handled=$handled")
        }.start()
    }

    private fun refreshActive() {
        active = OptimoveLiveUpdates.getInstance().activeLiveUpdates.sortedByDescending { it.lastUpdatedAtMillis }
    }

    private fun nowSeconds(): Long = System.currentTimeMillis() / 1000

    private fun newName(): String = "spain-vs-argentina-" + UUID.randomUUID().toString().take(8)

    private companion object {
        const val PREFS = "live_updates_qa"
        const val KEY_NAME = "name"
        const val KEY_TIMESTAMP = "timestamp"
    }

    // ---------------------------------------------------------------------------------------------

    @Composable
    private fun SectionTitle(text: String) {
        Spacer(modifier = Modifier.height(8.dp))
        Text(text, style = MaterialTheme.typography.titleMedium)
    }

    @Composable
    private fun MonoText(text: String, modifier: Modifier = Modifier) {
        Text(
            text,
            style = MaterialTheme.typography.bodySmall,
            fontFamily = FontFamily.Monospace,
            modifier = modifier
        )
    }

    @Composable
    private fun NumberField(label: String, value: String, modifier: Modifier, onChange: (String) -> Unit) {
        OutlinedTextField(
            value = value,
            onValueChange = { onChange(it.filter { c -> c.isDigit() }) },
            label = { Text(label) },
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = modifier
        )
    }

    @Composable
    private fun RowScope.ActionButton(
        text: String,
        containerColor: Color? = null,
        onClick: () -> Unit
    ) {
        Button(
            onClick = onClick,
            modifier = Modifier.weight(1f),
            shape = RoundedCornerShape(10.dp),
            colors = containerColor?.let { ButtonDefaults.buttonColors(containerColor = it) }
                ?: ButtonDefaults.buttonColors()
        ) {
            Text(text, style = MaterialTheme.typography.labelSmall)
        }
    }
}
