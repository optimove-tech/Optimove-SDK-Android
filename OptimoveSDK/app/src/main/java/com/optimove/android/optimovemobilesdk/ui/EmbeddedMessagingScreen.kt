package com.optimove.android.optimovemobilesdk.ui

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.optimove.android.embeddedmessaging.EmbeddedMessage

private val CardShape = RoundedCornerShape(12.dp)
private val SectionPadding = 16.dp

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun EmbeddedMessagingScreen(
    statusText: String,
    messages: List<EmbeddedMessage>,
    onGetMessages: (containerIds: String, limits: String) -> Unit,
    selectedMessage: EmbeddedMessage?,
    onMessageLongPress: (EmbeddedMessage) -> Unit,
    onDismissMessageMenu: () -> Unit,
    onSetAsRead: () -> Unit,
    onClickMetric: () -> Unit,
    onDeleteMessage: () -> Unit
) {
    var containerIds by remember { mutableStateOf("") }
    var limits by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surface)
            .padding(SectionPadding)
    ) {
        Text(
            "Embedded Messaging",
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp)
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedTextField(
                value = containerIds,
                onValueChange = { containerIds = it },
                label = { Text("Container Id(s)") },
                modifier = Modifier.weight(1f),
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    focusedLabelColor = MaterialTheme.colorScheme.primary
                )
            )
            OutlinedTextField(
                value = limits,
                onValueChange = { limits = it },
                label = { Text("Limit(s)") },
                modifier = Modifier.weight(1f),
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    focusedLabelColor = MaterialTheme.colorScheme.primary
                )
            )
        }
        Spacer(modifier = Modifier.height(12.dp))
        Button(
            onClick = { onGetMessages(containerIds, limits) },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(10.dp)
        ) {
            Text("Get Messages")
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            statusText,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface
        )
        Spacer(modifier = Modifier.height(8.dp))
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            shape = CardShape,
            color = MaterialTheme.colorScheme.surfaceVariant
        ) {
            LazyColumn(
                modifier = Modifier.padding(SectionPadding),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                itemsIndexed(messages) { _, message ->
                    val title = message.title ?: ""
                    val content = message.content ?: ""
                    val unread = message.readAt == null
                    val line = "$title: $content ${if (unread) "•" else ""}"
                    Text(
                        line,
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier
                            .fillMaxWidth()
                            .combinedClickable(
                                onClick = { },
                                onLongClick = { onMessageLongPress(message) }
                            )
                            .padding(vertical = 8.dp, horizontal = 4.dp)
                    )
                }
            }
        }
    }

    if (selectedMessage != null) {
        AlertDialog(
            onDismissRequest = onDismissMessageMenu,
            title = { Text("Choose an Action", style = MaterialTheme.typography.titleLarge) },
            text = {
                Column(modifier = Modifier.padding(vertical = 8.dp)) {
                    TextButton(onClick = { onSetAsRead(); onDismissMessageMenu() }) {
                        Text("Toggle Set as Read")
                    }
                    TextButton(onClick = { onClickMetric(); onDismissMessageMenu() }) {
                        Text("Click Metric")
                    }
                    TextButton(onClick = { onDeleteMessage(); onDismissMessageMenu() }) {
                        Text("Delete", color = MaterialTheme.colorScheme.error)
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = onDismissMessageMenu) {
                    Text("Cancel")
                }
            }
        )
    }
}
