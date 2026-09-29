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
import java.util.UUID

/**
 * QA screen for Live Updates. "Simulated push" builds a real FCM RemoteMessage in the 292915 envelope and
 * feeds it to FirebaseMessageHandler on a background thread, i.e. the same path as a delivered FCM message.
 */
class LiveUpdatesActivity : AppCompatActivity() {

    private var activityId by mutableStateOf(UUID.randomUUID().toString())
    private var type by mutableStateOf(LiveScoreNotificationHandler.TYPE)
    private var sequence by mutableStateOf("0")
    private var homeScore by mutableStateOf("0")
    private var awayScore by mutableStateOf("0")
    private var status by mutableStateOf("Kick-off")
    private var includeAlert by mutableStateOf(false)
    private var active by mutableStateOf<List<LiveUpdate>>(emptyList())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
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
                value = activityId,
                onValueChange = { activityId = it.trim() },
                label = { Text("activity_id") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                ActionButton("New ID") {
                    activityId = UUID.randomUUID().toString()
                    sequence = "0"
                }
                TypeButton(LiveScoreNotificationHandler.TYPE)
                TypeButton(LoggingLiveUpdateCustomHandler.TYPE)
            }

            SectionTitle("Content")
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                NumberField("Spain", homeScore, Modifier.weight(1f)) { homeScore = it }
                NumberField("Argentina", awayScore, Modifier.weight(1f)) { awayScore = it }
                NumberField("sequence", sequence, Modifier.weight(1f)) { sequence = it }
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
                "Sequence auto-increments after each push. Lower it to test stale updates.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                ActionButton("Start") { simulatePush("start") }
                ActionButton("Update") { simulatePush("update") }
                ActionButton("End") { simulatePush("end") }
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                ActionButton("Update, no type") { simulatePush("update", includeType = false) }
                ActionButton("Malformed", containerColor = MaterialTheme.colorScheme.error) { simulateRaw("malformed", "{\"event\":\"start\"}") }
                ActionButton("Normal push") { simulateNormalPush() }
            }

            SectionTitle("Local API")
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                ActionButton("start()") {
                    OptimoveLiveUpdates.getInstance().start(activityId, type, content())
                    LiveUpdateDebugLog.add("local start() $activityId")
                }
                ActionButton("update()") {
                    OptimoveLiveUpdates.getInstance().update(activityId, content())
                    LiveUpdateDebugLog.add("local update() $activityId")
                }
                ActionButton("end()") {
                    OptimoveLiveUpdates.getInstance().end(activityId)
                    LiveUpdateDebugLog.add("local end() $activityId")
                }
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                ActionButton("clearAll()", containerColor = MaterialTheme.colorScheme.error) {
                    OptimoveLiveUpdates.getInstance().clearAll()
                    LiveUpdateDebugLog.add("local clearAll()")
                }
                ActionButton("Clear log") { LiveUpdateDebugLog.clear() }
            }

            SectionTitle("Active (${active.size})")
            if (active.isEmpty()) {
                MonoText("none")
            }
            active.forEach { liveUpdate ->
                MonoText(
                    "${liveUpdate.activityId}\n  type=${liveUpdate.type} seq=${liveUpdate.sequence}" +
                        (liveUpdate.name?.let { " name=$it" } ?: "") +
                        "\n  ${liveUpdate.content}",
                    modifier = Modifier.padding(bottom = 4.dp)
                )
            }

            SectionTitle("Log")
            LiveUpdateDebugLog.entries.forEach { MonoText(it) }
        }
    }

    // ---------------------------------------------------------------------------------------------

    private fun content(): JSONObject = JSONObject()
        .put("home_team", "Spain")
        .put("away_team", "Argentina")
        .put("home_score", homeScore.toIntOrNull() ?: 0)
        .put("away_score", awayScore.toIntOrNull() ?: 0)
        .put("status", status)

    private fun simulatePush(event: String, includeType: Boolean = true) {
        val seq = sequence.toLongOrNull() ?: 0L
        val envelope = JSONObject()
            .put("event", event)
            .put("activity_id", activityId)
            .put("sequence", seq)
            .put("content", content())
            .put("name", "spain-vs-argentina")
        if (includeType) envelope.put("type", type)
        if (includeAlert) {
            envelope.put("alert", JSONObject().put("title", "Goal!").put("body", "$status $homeScore-$awayScore"))
        }

        simulateRaw("$event seq=$seq", envelope)
        sequence = (seq + 1).toString()
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
        active = OptimoveLiveUpdates.getInstance().activeLiveUpdates.sortedByDescending { it.updatedAt }
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

    @Composable
    private fun RowScope.TypeButton(value: String) {
        val colors = MaterialTheme.colorScheme
        ActionButton(
            "Type: $value",
            containerColor = if (type == value) colors.primary else colors.secondary
        ) { type = value }
    }
}
