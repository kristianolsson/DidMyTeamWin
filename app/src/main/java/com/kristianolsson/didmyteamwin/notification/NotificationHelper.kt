package com.kristianolsson.didmyteamwin.notification

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.kristianolsson.didmyteamwin.MainActivity

object NotificationHelper {

    private const val CHANNEL_ID = "game_results"
    private const val CHANNEL_NAME = "Game Results"

    fun createChannel(context: Context) {
        val channel = NotificationChannel(
            CHANNEL_ID,
            CHANNEL_NAME,
            NotificationManager.IMPORTANCE_DEFAULT,
        ).apply {
            description = "Notifications when your tracked team finishes a game"
        }
        val manager = context.getSystemService(NotificationManager::class.java)
        manager.createNotificationChannel(channel)
    }

    fun showResultNotification(context: Context, teamId: String, teamName: String) {
        if (!hasPermission(context)) return

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra("navigate_to_result", teamId)
        }
        val pending = PendingIntent.getActivity(
            context, teamId.hashCode(), intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle("$teamName played!")
            .setContentText("Tap to see the result")
            .setContentIntent(pending)
            .setAutoCancel(true)
            .build()

        NotificationManagerCompat.from(context).notify(teamId.hashCode(), notification)
    }

    fun showErrorNotification(context: Context, teamId: String, teamName: String, message: String) {
        if (!hasPermission(context)) return

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_alert)
            .setContentTitle(teamName)
            .setContentText(message)
            .setAutoCancel(true)
            .build()

        // Use a different notification ID so it doesn't replace result notifications
        NotificationManagerCompat.from(context).notify(teamId.hashCode() + 1, notification)
    }

    fun showCancelledNotification(context: Context, teamId: String, teamName: String) {
        showErrorNotification(context, teamId, teamName, "Game was postponed or cancelled")
    }

    private fun hasPermission(context: Context): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.checkSelfPermission(
                context, Manifest.permission.POST_NOTIFICATIONS,
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            true
        }
    }
}
