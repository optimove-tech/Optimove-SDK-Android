package com.optimove.android.optimovemobilesdk

import android.content.Context
import android.widget.RemoteViews
import androidx.core.app.NotificationCompat
import com.optimove.android.optimobile.LiveUpdate
import com.optimove.android.optimobile.LiveUpdateEvent
import com.optimove.android.optimobile.LiveUpdateNotificationHandler
import com.optimove.android.optimobile.OptimoveLiveUpdates

/**
 * Renders Live Updates of type "notification" as a scoreboard. Expects content like
 * {"home_team":"Spain","away_team":"Argentina","home_score":1,"away_score":0,"status":"15'"}
 */
class LiveScoreNotificationHandler : LiveUpdateNotificationHandler {

    override fun onCreateNotification(
        context: Context,
        event: LiveUpdateEvent,
        liveUpdate: LiveUpdate
    ): NotificationCompat.Builder {
        LiveUpdateDebugLog.add("notification handler: ${LiveUpdateDebugLog.describe(event, liveUpdate)}")

        val content = liveUpdate.content
        val homeTeam = content.optString("home_team")
        val awayTeam = content.optString("away_team")
        val score = "${content.optInt("home_score")} - ${content.optInt("away_score")}"
        val status = content.optString("status")

        val views = RemoteViews(context.packageName, R.layout.live_update_scoreboard).apply {
            setTextViewText(R.id.live_update_home_team, homeTeam)
            setTextViewText(R.id.live_update_away_team, awayTeam)
            setTextViewText(R.id.live_update_score, score)
            setTextViewText(R.id.live_update_status, status)
        }

        return NotificationCompat.Builder(context, OptimoveLiveUpdates.NOTIFICATION_CHANNEL_ID)
            .setSmallIcon(R.drawable.small_icon)
            .setContentTitle(liveUpdate.alertTitle ?: "$homeTeam $score $awayTeam")
            .setContentText(liveUpdate.alertBody ?: status)
            .setStyle(NotificationCompat.DecoratedCustomViewStyle())
            .setCustomContentView(views)
            .setCustomBigContentView(views)
    }

    companion object {
        const val TYPE = "notification"
    }
}
