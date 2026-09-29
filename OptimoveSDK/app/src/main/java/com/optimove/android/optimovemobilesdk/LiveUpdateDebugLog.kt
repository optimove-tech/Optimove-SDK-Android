package com.optimove.android.optimovemobilesdk

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.compose.runtime.mutableStateListOf
import com.optimove.android.optimobile.LiveUpdate
import com.optimove.android.optimobile.LiveUpdateCustomHandler
import com.optimove.android.optimobile.LiveUpdateEvent
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * In-memory log of Live Update handler calls and simulated pushes, shown on the Live Updates screen.
 * Handlers run on the push thread, so writes are posted to the main thread for Compose.
 */
object LiveUpdateDebugLog {

    private const val TAG = "LiveUpdatesQA"
    private const val MAX_ENTRIES = 100

    val entries = mutableStateListOf<String>()

    private val mainHandler = Handler(Looper.getMainLooper())
    private val timeFormat = SimpleDateFormat("HH:mm:ss.SSS", Locale.US)

    fun add(message: String) {
        Log.d(TAG, message)
        val line = "${timeFormat.format(Date())}  $message"
        mainHandler.post {
            entries.add(0, line)
            if (entries.size > MAX_ENTRIES) entries.removeAt(entries.lastIndex)
        }
    }

    fun clear() {
        mainHandler.post { entries.clear() }
    }

    fun describe(event: LiveUpdateEvent, liveUpdate: LiveUpdate): String =
        "$event ${liveUpdate.activityId.take(8)} type=${liveUpdate.type} seq=${liveUpdate.sequence} " +
            "content=${liveUpdate.content}"
}

/**
 * Stands in for a host that renders Live Updates itself (widget / in-app UI). Only logs.
 */
class LoggingLiveUpdateCustomHandler : LiveUpdateCustomHandler {

    override fun onLiveUpdateEvent(context: Context, event: LiveUpdateEvent, liveUpdate: LiveUpdate) {
        LiveUpdateDebugLog.add("custom handler: ${LiveUpdateDebugLog.describe(event, liveUpdate)}")
    }

    companion object {
        const val TYPE = "widget"
    }
}
