package com.optimove.android.optimovemobilesdk

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.optimove.android.optimobile.OptimoveOverlayMessaging
import com.optimove.android.optimovemobilesdk.ui.theme.AppTheme

class OverlayMessagingActivity : AppCompatActivity() {

    private var isInterceptorSet by mutableStateOf(false)
    private var pendingCallback by mutableStateOf<OptimoveOverlayMessaging.OverlayMessagingInterceptorCallback?>(null)
    private var timeoutSeconds by mutableStateOf("30")
    private var lastOutcome by mutableStateOf<String?>(null)
    private val handler = Handler(Looper.getMainLooper())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            AppTheme {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.Top
                ) {
                    Button(
                        onClick = { OptimoveOverlayMessaging.getInstance().resetSession() },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text("Reset Session")
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Button(
                            onClick = { setInterceptor() },
                            enabled = !isInterceptorSet,
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text("Set Interceptor")
                        }
                        Button(
                            onClick = { unsetInterceptor() },
                            enabled = isInterceptorSet,
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.secondary
                            )
                        ) {
                            Text("Unset Interceptor")
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = "Interceptor timeout",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = timeoutSeconds,
                            onValueChange = { timeoutSeconds = it.filter { c -> c.isDigit() } },
                            suffix = { Text("s") },
                            singleLine = true,
                            enabled = !isInterceptorSet,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.width(88.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = MaterialTheme.colorScheme.primary,
                                focusedLabelColor = MaterialTheme.colorScheme.primary
                            )
                        )
                    }

                    if (isInterceptorSet) {
                        Spacer(modifier = Modifier.height(16.dp))
                        val hasMessage = pendingCallback != null
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Button(
                                onClick = { pendingCallback?.show(); pendingCallback = null; lastOutcome = "show" },
                                enabled = hasMessage,
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Text("Show")
                            }
                            Button(
                                onClick = { pendingCallback?.discard(); pendingCallback = null; lastOutcome = "discard" },
                                enabled = hasMessage,
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(10.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.error
                                )
                            ) {
                                Text("Discard")
                            }
                            Button(
                                onClick = { pendingCallback?.defer(); pendingCallback = null; lastOutcome = "defer" },
                                enabled = hasMessage,
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(10.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.secondary
                                )
                            ) {
                                Text("Defer")
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Outcome: ${lastOutcome ?: "—"}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }

    private fun setInterceptor() {
        val timeoutMs = (timeoutSeconds.toLongOrNull() ?: 30L) * 1000L
        OptimoveOverlayMessaging.getInstance().setInterceptor(object : OptimoveOverlayMessaging.OverlayMessagingInterceptor {
            override fun onMessageLoaded(
                message: com.optimove.android.optimobile.OverlayMessagingMessage,
                callback: OptimoveOverlayMessaging.OverlayMessagingInterceptorCallback
            ) {
                runOnUiThread {
                    pendingCallback = callback
                    handler.postDelayed({ pendingCallback = null; lastOutcome = "timeout" }, timeoutMs)
                }
            }

            override fun getTimeoutMs(): Long = timeoutMs
        })
        isInterceptorSet = true
    }

    private fun unsetInterceptor() {
        OptimoveOverlayMessaging.getInstance().setInterceptor(null)
        isInterceptorSet = false
        pendingCallback = null
    }
}
