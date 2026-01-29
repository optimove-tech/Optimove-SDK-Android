package com.optimove.android.optimovemobilesdk

import android.os.Bundle
import android.util.Log
import androidx.activity.compose.setContent
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.appcompat.app.AppCompatActivity
import com.optimove.android.embeddedmessaging.ContainerRequestOptions
import com.optimove.android.embeddedmessaging.EmbeddedMessage
import com.optimove.android.embeddedmessaging.OptimoveEmbeddedMessaging
import com.optimove.android.optimovemobilesdk.ui.EmbeddedMessagingScreen
import com.optimove.android.optimovemobilesdk.ui.theme.AppTheme

class EmbeddedMessagingActivity : AppCompatActivity() {

    private var statusText by mutableStateOf("Containers: ")
    private var messages by mutableStateOf<List<EmbeddedMessage>>(emptyList())
    private var selectedMessage by mutableStateOf<EmbeddedMessage?>(null)
    private var lastContainerIds = ""
    private var lastLimits = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            AppTheme {
                EmbeddedMessagingScreen(
                    statusText = statusText,
                    messages = messages,
                    onGetMessages = ::loadMessages,
                    selectedMessage = selectedMessage,
                    onMessageLongPress = { msg ->
                        Log.d("DEBUG", "Message long-pressed: ${msg.id}")
                        selectedMessage = msg
                    },
                    onDismissMessageMenu = { selectedMessage = null },
                    onSetAsRead = ::setAsRead,
                    onClickMetric = ::sendClickMetrics,
                    onDeleteMessage = ::deleteMessage
                )
            }
        }
    }

    private fun loadMessages(containerIdsStr: String, limitsStr: String) {
        lastContainerIds = containerIdsStr
        lastLimits = limitsStr
        val request = buildRequest(containerIdsStr, limitsStr)
        if (request.isEmpty()) return
        OptimoveEmbeddedMessaging.getInstance().getMessagesAsync(request) { result, response ->
            when (result) {
                OptimoveEmbeddedMessaging.ResultType.SUCCESS -> response?.containersMap?.let { map ->
                    statusText = "${map.size} containers retrieved"
                    messages = map.values.flatMap { c -> c.messages?.toList() ?: emptyList() }
                }
                else -> statusText = errorText(result)
            }
        }
    }

    private fun errorText(result: OptimoveEmbeddedMessaging.ResultType): String = when (result) {
        OptimoveEmbeddedMessaging.ResultType.ERROR -> "Generic error"
        OptimoveEmbeddedMessaging.ResultType.ERROR_USER_NOT_SET -> "User not set error"
        OptimoveEmbeddedMessaging.ResultType.ERROR_CONFIG_NOT_SET -> "Config not set error"
        else -> "Unknown error"
    }

    private fun buildRequest(containerIdsStr: String, limitsStr: String): Array<ContainerRequestOptions> {
        if (containerIdsStr.isBlank()) return emptyArray()
        val ids = containerIdsStr.split(";")
        val limits = limitsStr.split(";")
        return ids.mapIndexed { i, id ->
            val limit = limits.getOrNull(i)?.toIntOrNull() ?: 50
            ContainerRequestOptions(id, limit)
        }.toTypedArray()
    }

    private fun sendClickMetrics() {
        val msg = selectedMessage ?: return
        OptimoveEmbeddedMessaging.getInstance().reportClickMetricAsync(msg) { result ->
            when (result) {
                OptimoveEmbeddedMessaging.ResultType.SUCCESS -> statusText = "Click Metrics Sent"
                else -> statusText = errorText(result)
            }
        }
    }

    private fun setAsRead() {
        val msg = selectedMessage ?: return
        val markAsRead = msg.readAt == null
        OptimoveEmbeddedMessaging.getInstance().setAsReadASync(msg, markAsRead) { result ->
            when (result) {
                OptimoveEmbeddedMessaging.ResultType.SUCCESS -> {
                    statusText = "Message marked as ${if (markAsRead) "read" else "unread"}"
                    loadMessages(lastContainerIds, lastLimits)
                }
                else -> statusText = errorText(result)
            }
        }
    }

    private fun deleteMessage() {
        val msg = selectedMessage ?: return
        OptimoveEmbeddedMessaging.getInstance().deleteMessageAsync(msg) { result ->
            when (result) {
                OptimoveEmbeddedMessaging.ResultType.SUCCESS -> {
                    statusText = "Message Deleted"
                    loadMessages(lastContainerIds, lastLimits)
                }
                else -> statusText = errorText(result)
            }
        }
    }

}
