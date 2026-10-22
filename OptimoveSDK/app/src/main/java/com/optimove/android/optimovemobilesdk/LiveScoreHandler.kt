package com.optimove.android.optimovemobilesdk

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import android.widget.RemoteViews
import androidx.core.app.NotificationCompat
import com.optimove.android.optimobile.LiveUpdate
import com.optimove.android.optimobile.LiveUpdateEvent
import com.optimove.android.optimobile.LiveUpdateHandler
import com.optimove.android.optimobile.LiveUpdateNotifications

/**
 * Reference Live Update integration: renders content_state like
 * {"homeTeam":"Spain","awayTeam":"Argentina","homeScore":1,"awayScore":0,"status":"15'"}
 * as an ongoing scoreboard notification and lets the SDK helper post, update, time out and dismiss it.
 */
class LiveScoreHandler : LiveUpdateHandler {

    override fun onUpdate(context: Context, event: LiveUpdateEvent, liveUpdate: LiveUpdate) {
        LiveUpdateDebugLog.add("handler: ${LiveUpdateDebugLog.describe(event, liveUpdate)}")
        ensureChannel(context)

        val content = liveUpdate.contentState
        val homeTeam = content.optString("homeTeam", "Home")
        val awayTeam = content.optString("awayTeam", "Away")
        val score = "${content.optInt("homeScore")} - ${content.optInt("awayScore")}"
        val status = content.optString("status")

        val views = RemoteViews(context.packageName, R.layout.live_update_scoreboard).apply {
            setTextViewText(R.id.live_update_home_team, homeTeam)
            setTextViewText(R.id.live_update_away_team, awayTeam)
            setTextViewText(R.id.live_update_score, score)
            setTextViewText(R.id.live_update_status, status)
        }

        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.small_icon)
            .setContentTitle(liveUpdate.alertTitle ?: "$homeTeam $score $awayTeam")
            .setContentText(liveUpdate.alertBody ?: status)
            .setStyle(NotificationCompat.DecoratedCustomViewStyle())
            .setCustomContentView(views)
            .setCustomBigContentView(views)

        LiveUpdateNotifications.post(context, event, liveUpdate, builder)
    }

    private fun ensureChannel(context: Context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
        val manager = context.getSystemService(NotificationManager::class.java)
        if (manager.getNotificationChannel(CHANNEL_ID) != null) return
        manager.createNotificationChannel(
            NotificationChannel(CHANNEL_ID, "Live scores", NotificationManager.IMPORTANCE_DEFAULT)
        )
    }

    companion object {
        const val CHANNEL_ID = "live_scores"
    }
}
