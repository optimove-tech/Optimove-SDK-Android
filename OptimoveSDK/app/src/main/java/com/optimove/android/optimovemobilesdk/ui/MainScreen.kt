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
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.text.KeyboardOptions
import com.optimove.android.optimovemobilesdk.MyApplication

private val CardShape = RoundedCornerShape(12.dp)
private val SectionPadding = 16.dp

@Composable
fun MainScreen(
    outputText: String,
    showPreferenceCenter: Boolean,
    showEmbeddedMessaging: Boolean,
    showOverlayMessaging: Boolean,
    showDelayedConfig: Boolean,
    credentialsSubmitted: Boolean,
    isInterceptingInApp: Boolean,
    onReportEvent: () -> Unit,
    userId: String,
    userEmail: String,
    onUserIdChange: (String) -> Unit,
    onUserEmailChange: (String) -> Unit,
    onClearAppData: () -> Unit,
    onKillActivity: () -> Unit,
    onUpdateUserId: (userId: String, userEmail: String) -> Unit,
    onClearIdentity: () -> Unit,
    onReadInbox: () -> Unit,
    onMarkInboxAsRead: () -> Unit,
    onDeleteInbox: () -> Unit,
    onGetPreferences: () -> Unit,
    onSetPreferences: () -> Unit,
    onViewEmbeddedMessaging: () -> Unit,
    onViewOverlayMessaging: () -> Unit,
    onSetCredentials: (optimove: String?, optimobile: String?, prefCenter: String?) -> Unit,
    onInAppInterceptionClicked: () -> Unit,
    onResetToken: () -> Unit,
    onSendLocation: (latitude: Double, longitude: Double) -> Unit,
    onOpenDeeplinkTest: () -> Unit,
    onRegisterPush: () -> Unit,
    onUnregisterPush: () -> Unit,
    isDelayedInit: Boolean,
    onDelayedInitToggle: (Boolean) -> Unit,
    onOpenGamifyWidget: () -> Unit
) {
    var optimoveCred by remember {
        mutableStateOf(if (showDelayedConfig) MyApplication.DEFAULT_OPTIMOVE_CRED else "")
    }
    var optimobileCred by remember {
        mutableStateOf(if (showDelayedConfig) MyApplication.DEFAULT_OPTIMOBILE_CRED else "")
    }
    var prefCenterCred by remember { mutableStateOf("") }
    var latitude by remember { mutableStateOf("") }
    var longitude by remember { mutableStateOf("") }
    var showGeolocationDialog by remember { mutableStateOf(false) }

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
                    onValueChange = onUserIdChange,
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
                    onValueChange = onUserEmailChange,
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
                        onClick = { onUpdateUserId(userId, userEmail) },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text("Update userId")
                    }
                    Button(
                        onClick = { onUpdateUserId(userId, userEmail) },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text("Update email")
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
                Button(
                    onClick = onClearIdentity,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
                ) {
                    Text("Clear saved login")
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
            onClick = onInAppInterceptionClicked,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(10.dp),
            colors = if (isInterceptingInApp) {
                ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
            } else {
                ButtonDefaults.buttonColors()
            }
        ) {
            Text(
                if (isInterceptingInApp) "Disable In-App Interception" else "Enable In-App Interception"
            )
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

        if (showOverlayMessaging) {
            Spacer(modifier = Modifier.height(4.dp))
            Button(
                onClick = onViewOverlayMessaging,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(10.dp)
            ) {
                Text("Overlay Messaging")
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
            onClick = onOpenGamifyWidget,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(10.dp)
        ) {
            Text("Open Gamify Widget")
        }
        Spacer(modifier = Modifier.height(12.dp))
        Button(
            onClick = { showGeolocationDialog = true },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(10.dp)
        ) {
            Text("Test Geolocation")
        }
        if (showGeolocationDialog) {
            val latError = latitudeValidationMessage(latitude)
            val lngError = longitudeValidationMessage(longitude)
            val latParsed = parseLatitude(latitude)
            val lngParsed = parseLongitude(longitude)
            val canSendLocation = latParsed != null && lngParsed != null
            AlertDialog(
                onDismissRequest = { showGeolocationDialog = false },
                title = { Text("Test Geolocation") },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = latitude,
                            onValueChange = { latitude = it },
                            label = { Text("Latitude") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            isError = latError != null,
                            supportingText = {
                                if (latError != null) {
                                    Text(latError, color = MaterialTheme.colorScheme.error)
                                }
                            },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = MaterialTheme.colorScheme.primary,
                                focusedLabelColor = MaterialTheme.colorScheme.primary,
                                cursorColor = MaterialTheme.colorScheme.primary
                            )
                        )
                        OutlinedTextField(
                            value = longitude,
                            onValueChange = { longitude = it },
                            label = { Text("Longitude") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            isError = lngError != null,
                            supportingText = {
                                if (lngError != null) {
                                    Text(lngError, color = MaterialTheme.colorScheme.error)
                                }
                            },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = MaterialTheme.colorScheme.primary,
                                focusedLabelColor = MaterialTheme.colorScheme.primary,
                                cursorColor = MaterialTheme.colorScheme.primary
                            )
                        )
                    }
                },
                confirmButton = {
                    TextButton(
                        onClick = {
                            val lat = latParsed
                            val lng = lngParsed
                            if (lat != null && lng != null) {
                                onSendLocation(lat, lng)
                                showGeolocationDialog = false
                            }
                        },
                        enabled = canSendLocation
                    ) {
                        Text("Send Location Update")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showGeolocationDialog = false }) {
                        Text("Cancel")
                    }
                }
            )
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
            onClick = onClearAppData,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(10.dp),
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
        ) {
            Text("Clear App Data")
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
        Spacer(modifier = Modifier.height(12.dp))
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = CardShape,
            color = MaterialTheme.colorScheme.surfaceVariant
        ) {
            Column(modifier = Modifier.padding(SectionPadding)) {
                Text(
                    "Push Registration",
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(12.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = onRegisterPush,
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary
                        )
                    ) {
                        Text("Register Push")
                    }
                    Button(
                        onClick = onUnregisterPush,
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.error
                        )
                    ) {
                        Text("Unregister Push")
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = CardShape,
            color = MaterialTheme.colorScheme.surfaceVariant
        ) {
            Row(
                modifier = Modifier.padding(SectionPadding).fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        "Delayed Initialization",
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        "Restart app to apply",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.outline
                    )
                }
                Switch(
                    checked = isDelayedInit,
                    onCheckedChange = onDelayedInitToggle
                )
            }
        }
    }
}

private fun parseLatitude(value: String): Double? {
    if (value.isBlank()) return null
    val v = value.toDoubleOrNull() ?: return null
    if (v.isNaN() || v.isInfinite()) return null
    return if (v in -90.0..90.0) v else null
}

private fun parseLongitude(value: String): Double? {
    if (value.isBlank()) return null
    val v = value.toDoubleOrNull() ?: return null
    if (v.isNaN() || v.isInfinite()) return null
    return if (v in -180.0..180.0) v else null
}

private fun latitudeValidationMessage(value: String): String? {
    if (value.isBlank()) return null
    val v = value.toDoubleOrNull() ?: return "Enter a valid number"
    if (v.isNaN() || v.isInfinite()) return "Enter a valid number"
    return if (v in -90.0..90.0) null else "Latitude must be between -90 and 90"
}

private fun longitudeValidationMessage(value: String): String? {
    if (value.isBlank()) return null
    val v = value.toDoubleOrNull() ?: return "Enter a valid number"
    if (v.isNaN() || v.isInfinite()) return "Enter a valid number"
    return if (v in -180.0..180.0) null else "Longitude must be between -180 and 180"
}
