package com.optimove.android.optimovemobilesdk.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.unit.dp
import com.optimove.android.optimovemobilesdk.AuthDebugMode

private val CardShape = RoundedCornerShape(12.dp)
private val SectionPadding = 16.dp

@Composable
fun AuthDebugScreen(
    isAuthEnabled: Boolean,
    mode: AuthDebugMode,
    onModeChange: (AuthDebugMode) -> Unit,
    lastUserId: String,
    lastIssuedAt: String,
    issuedTokenCount: Int,
    lastError: String,
    lastPayload: String,
    lastToken: String,
    onClear: () -> Unit
) {
    val clipboardManager = LocalClipboardManager.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surface)
            .verticalScroll(rememberScrollState())
            .padding(SectionPadding)
    ) {
        Text(
            "JWT Provider Debug",
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp)
        )

        Surface(modifier = Modifier.fillMaxWidth(), shape = CardShape, color = MaterialTheme.colorScheme.surfaceVariant) {
            Column(modifier = Modifier.padding(SectionPadding)) {
                Text("Status", style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    if (isAuthEnabled) {
                        "Auth: Enabled — the SDK token provider is configured on app startup."
                    } else {
                        "Auth: Disabled — enable Auth on the main screen and restart the app to configure the SDK token provider."
                    },
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
        Spacer(modifier = Modifier.height(12.dp))

        Surface(modifier = Modifier.fillMaxWidth(), shape = CardShape, color = MaterialTheme.colorScheme.surfaceVariant) {
            Column(modifier = Modifier.padding(SectionPadding)) {
                Text("Provider Response", style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    "Choose what the QA app returns the next time the SDK asks the token provider for a JWT.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.outline
                )
                Spacer(modifier = Modifier.height(8.dp))
                AuthDebugMode.values().forEach { m ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .selectable(
                                selected = mode == m,
                                enabled = isAuthEnabled,
                                onClick = { onModeChange(m) }
                            ),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(selected = mode == m, onClick = { onModeChange(m) }, enabled = isAuthEnabled)
                        Text(m.label, style = MaterialTheme.typography.bodyMedium)
                    }
                }
            }
        }
        Spacer(modifier = Modifier.height(12.dp))

        Surface(modifier = Modifier.fillMaxWidth(), shape = CardShape, color = MaterialTheme.colorScheme.surfaceVariant) {
            Column(modifier = Modifier.padding(SectionPadding)) {
                Text("Last Provider Call", style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(modifier = Modifier.height(4.dp))
                LabeledValue("Last user id", lastUserId)
                LabeledValue("Last issued at", lastIssuedAt)
                LabeledValue("Issued count", issuedTokenCount.toString())
                if (lastError.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        "Last error: $lastError",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }
        }

        if (lastPayload.isNotEmpty()) {
            Spacer(modifier = Modifier.height(12.dp))
            Surface(modifier = Modifier.fillMaxWidth(), shape = CardShape, color = MaterialTheme.colorScheme.surfaceVariant) {
                Column(modifier = Modifier.padding(SectionPadding)) {
                    Text("JWT Payload", style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(lastPayload, style = MaterialTheme.typography.bodySmall)
                    TextButton(onClick = { clipboardManager.setText(AnnotatedString(lastPayload)) }) {
                        Text("Copy JWT Payload")
                    }
                }
            }
        }

        if (lastToken.isNotEmpty()) {
            Spacer(modifier = Modifier.height(12.dp))
            Surface(modifier = Modifier.fillMaxWidth(), shape = CardShape, color = MaterialTheme.colorScheme.surfaceVariant) {
                Column(modifier = Modifier.padding(SectionPadding)) {
                    Text("JWT", style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(lastToken, style = MaterialTheme.typography.bodySmall)
                    TextButton(onClick = { clipboardManager.setText(AnnotatedString(lastToken)) }) {
                        Text("Copy JWT")
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))
        Button(
            onClick = onClear,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(10.dp),
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
        ) {
            Text("Clear Auth Debug State")
        }
    }
}

@Composable
private fun LabeledValue(label: String, value: String) {
    Row(modifier = Modifier.fillMaxWidth()) {
        Text(label, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.outline)
        Spacer(modifier = Modifier.height(0.dp))
        Text(": $value", style = MaterialTheme.typography.bodyMedium)
    }
}
