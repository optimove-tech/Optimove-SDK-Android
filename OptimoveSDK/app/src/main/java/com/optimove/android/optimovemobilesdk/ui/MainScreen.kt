package com.optimove.android.optimovemobilesdk.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

private val CardShape = RoundedCornerShape(12.dp)
private val SectionPadding = 16.dp

@Composable
fun MainScreen(
    outputText: String,
    showPreferenceCenter: Boolean,
    showEmbeddedMessaging: Boolean,
    showDelayedConfig: Boolean,
    credentialsSubmitted: Boolean,
    isInterceptingInApp: Boolean,
    onReportEvent: () -> Unit,
    onKillActivity: () -> Unit,
    onUpdateUserId: (userId: String, userEmail: String) -> Unit,
    onReadInbox: () -> Unit,
    onMarkInboxAsRead: () -> Unit,
    onDeleteInbox: () -> Unit,
    onGetPreferences: () -> Unit,
    onSetPreferences: () -> Unit,
    onViewEmbeddedMessaging: () -> Unit,
    onSetCredentials: (optimove: String?, optimobile: String?, prefCenter: String?) -> Unit,
    onEnableInAppInterceptionClicked: () -> Unit,
    onResetToken: () -> Unit,
    onOpenDeeplinkTest: () -> Unit,
    lastSetLocation: String?,
    onSetLocationClicked: () -> Unit,
    onSetAttributesClicked: () -> Unit
) {
    var userId by remember { mutableStateOf("") }
    var userEmail by remember { mutableStateOf("") }
    var optimoveCred by remember { mutableStateOf("") }
    var optimobileCred by remember { mutableStateOf("") }
    var prefCenterCred by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surface)
            .verticalScroll(rememberScrollState())
            .padding(SectionPadding)
    ) {
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = CardShape,
            color = MaterialTheme.colorScheme.surfaceVariant
        ) {
            Text(
                outputText,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(SectionPadding),
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Spacer(modifier = Modifier.height(20.dp))

        Surface(modifier = Modifier.fillMaxWidth(), shape = CardShape, color = MaterialTheme.colorScheme.surfaceVariant) {
            Column(modifier = Modifier.padding(SectionPadding)) {
                OutlinedTextField(
                    value = userId,
                    onValueChange = { userId = it },
                    label = { Text("User ID") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        focusedLabelColor = MaterialTheme.colorScheme.primary,
                        cursorColor = MaterialTheme.colorScheme.primary
                    )
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = userEmail,
                    onValueChange = { userEmail = it },
                    label = { Text("Email") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        focusedLabelColor = MaterialTheme.colorScheme.primary,
                        cursorColor = MaterialTheme.colorScheme.primary
                    )
                )
                Spacer(modifier = Modifier.height(12.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = { onUpdateUserId(userId, "") },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text("Update userId")
                    }
                    Button(
                        onClick = { onUpdateUserId("", userEmail) },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text("Update email")
                    }
                }
            }
        }
        Spacer(modifier = Modifier.height(4.dp))

        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(
                onClick = onResetToken,
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
            ) {
                Text("Reset Token", style = MaterialTheme.typography.labelLarge)
            }
            Button(
                onClick = onReportEvent,
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(10.dp)
            ) {
                Text("Report Event", style = MaterialTheme.typography.labelLarge)
            }
        }
        Spacer(modifier = Modifier.height(4.dp))

        Button(
            onClick = onEnableInAppInterceptionClicked,
            enabled = !isInterceptingInApp,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(10.dp),
            colors = if (isInterceptingInApp) ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.outline) else ButtonDefaults.buttonColors()
        ) {
            Text(if (isInterceptingInApp) "Intercepting In-App (restart to disable)" else "Enable In-App Interception")
        }
        Spacer(modifier = Modifier.height(4.dp))

        Button(
            onClick = onSetLocationClicked,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(10.dp)
        ) {
            Text("Set Location")
        }
        Spacer(modifier = Modifier.height(4.dp))

        Button(
            onClick = onSetAttributesClicked,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(10.dp)
        ) {
            Text("Set Attributes")
        }
        Spacer(modifier = Modifier.height(4.dp))

        if (lastSetLocation != null) {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = CardShape,
                color = MaterialTheme.colorScheme.surfaceVariant
            ) {
                Text(
                    text = "Test location: $lastSetLocation",
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(SectionPadding),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
        }

        Spacer(modifier = Modifier.height(4.dp))

        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            val inboxButtonModifier = Modifier.weight(1f).height(56.dp)
            Button(
                onClick = onReadInbox,
                modifier = inboxButtonModifier,
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.tertiary),
                contentPadding = ButtonDefaults.ContentPadding
            ) {
                Text(
                    "Read Inbox",
                    style = MaterialTheme.typography.labelLarge,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            }
            Button(
                onClick = onMarkInboxAsRead,
                modifier = inboxButtonModifier,
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.tertiary),
                contentPadding = ButtonDefaults.ContentPadding
            ) {
                Text("Mark Read", style = MaterialTheme.typography.labelLarge)
            }
            Button(
                onClick = onDeleteInbox,
                modifier = inboxButtonModifier,
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                contentPadding = ButtonDefaults.ContentPadding
            ) {
                Text("Delete", style = MaterialTheme.typography.labelLarge)
            }
        }

        if (showPreferenceCenter) {
            Spacer(modifier = Modifier.height(4.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(
                    onClick = onGetPreferences,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.tertiary)
                ) {
                    Text("Get Prefs", style = MaterialTheme.typography.labelLarge)
                }
                Button(
                    onClick = onSetPreferences,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.tertiary)
                ) {
                    Text("Set Prefs", style = MaterialTheme.typography.labelLarge)
                }
            }
        }

        if (showEmbeddedMessaging) {
            Spacer(modifier = Modifier.height(4.dp))
            Button(
                onClick = onViewEmbeddedMessaging,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(10.dp)
            ) {
                Text("View Embedded Messaging")
            }
        }

        if (showDelayedConfig) {
            Spacer(modifier = Modifier.height(16.dp))
            Surface(modifier = Modifier.fillMaxWidth(), shape = CardShape, color = MaterialTheme.colorScheme.surfaceVariant) {
                Column(modifier = Modifier.padding(SectionPadding)) {
                    OutlinedTextField(
                        value = optimoveCred,
                        onValueChange = { optimoveCred = it },
                        label = { Text("Optimove credentials") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        enabled = !credentialsSubmitted,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            focusedLabelColor = MaterialTheme.colorScheme.primary
                        )
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = optimobileCred,
                        onValueChange = { optimobileCred = it },
                        label = { Text("Optimobile credentials") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        enabled = !credentialsSubmitted,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            focusedLabelColor = MaterialTheme.colorScheme.primary
                        )
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = prefCenterCred,
                        onValueChange = { prefCenterCred = it },
                        label = { Text("Preference center (optional)") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        enabled = !credentialsSubmitted,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            focusedLabelColor = MaterialTheme.colorScheme.primary
                        )
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Button(
                        onClick = {
                            val o = optimoveCred.ifBlank { null }
                            val m = optimobileCred.ifBlank { null }
                            val p = prefCenterCred.ifBlank { null }
                            onSetCredentials(o, m, p)
                        },
                        enabled = !credentialsSubmitted,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text("Submit credentials")
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(4.dp))
        Button(
            onClick = onOpenDeeplinkTest,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(10.dp),
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.tertiary)
        ) {
            Text("Open deeplink test page")
        }
        Spacer(modifier = Modifier.height(4.dp))
        Button(
            onClick = onKillActivity,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(10.dp),
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
        ) {
            Text("Kill Activity")
        }
        Spacer(modifier = Modifier.height(24.dp))
    }
}
