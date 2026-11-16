package com.optimove.android.optimovemobilesdk

import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.compose.runtime.mutableStateListOf
import com.optimove.android.optimobile.LiveUpdate
import com.optimove.android.optimobile.LiveUpdateEvent
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

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
        "$event ${liveUpdate.name} type=${liveUpdate.type} ts=${liveUpdate.timestamp}" +
            (liveUpdate.endReason?.let { " reason=$it" } ?: "") +
            (liveUpdate.dismissalDateMillis?.let { " dismissAt=${timeFormat.format(Date(it))}" } ?: "") +
            (if (liveUpdate.hasAlert()) " alert=${liveUpdate.alertTitle}" else "") +
            " content=${liveUpdate.contentState}"
}
