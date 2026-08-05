package com.manojbuilds.nagly.notifications

import android.Manifest
import android.app.AlarmManager
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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

actual class Notifier(private val context: Context) {

    init {
        ensureChannel()
    }

    actual suspend fun requestPermission(): Boolean = withContext(Dispatchers.Main) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) return@withContext true
        ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.POST_NOTIFICATIONS,
        ) == PackageManager.PERMISSION_GRANTED
    }

    actual fun schedule(
        id: Int,
        atEpochMs: Long,
        title: String,
        body: String,
        actions: NudgeActions,
    ) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, NudgeAlarmReceiver::class.java).apply {
            action = ACTION_SHOW_NUDGE
            putExtra(EXTRA_ID, id)
            putExtra(EXTRA_TITLE, title)
            putExtra(EXTRA_BODY, body)
            putExtra(EXTRA_SKIP_LABEL, actions.skipLabel)
            putExtra(EXTRA_MOOD, actions.mood.name)
        }
        val pending = PendingIntent.getBroadcast(
            context,
            id,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
        val canExact = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            alarmManager.canScheduleExactAlarms()
        } else {
            true
        }
        try {
            when {
                canExact && Build.VERSION.SDK_INT >= Build.VERSION_CODES.M -> {
                    alarmManager.setExactAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP,
                        atEpochMs,
                        pending,
                    )
                }
                Build.VERSION.SDK_INT >= Build.VERSION_CODES.M -> {
                    alarmManager.setAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP,
                        atEpochMs,
                        pending,
                    )
                }
                else -> {
                    alarmManager.set(AlarmManager.RTC_WAKEUP, atEpochMs, pending)
                }
            }
        } catch (_: SecurityException) {
            alarmManager.set(AlarmManager.RTC_WAKEUP, atEpochMs, pending)
        }
    }

    actual fun cancelAll() {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        repeat(8) { index ->
            val id = index + 1
            val intent = Intent(context, NudgeAlarmReceiver::class.java).apply {
                action = ACTION_SHOW_NUDGE
            }
            val pending = PendingIntent.getBroadcast(
                context,
                id,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
            )
            alarmManager.cancel(pending)
        }
        NotificationManagerCompat.from(context).cancelAll()
    }

    private fun ensureChannel() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
        val manager = context.getSystemService(NotificationManager::class.java)
        val channel = NotificationChannel(
            CHANNEL_ID,
            "Hydration nudges",
            NotificationManager.IMPORTANCE_DEFAULT,
        )
        manager.createNotificationChannel(channel)
    }

    companion object {
        const val CHANNEL_ID = "nagly_nudges"
        const val ACTION_SHOW_NUDGE = "com.manojbuilds.nagly.SHOW_NUDGE"
        const val ACTION_NUDGE = "com.manojbuilds.nagly.NUDGE_ACTION"
        const val EXTRA_ID = "id"
        const val EXTRA_TITLE = "title"
        const val EXTRA_BODY = "body"
        const val EXTRA_SKIP_LABEL = "skip_label"
        const val EXTRA_MOOD = "mood"
        const val EXTRA_ACTION_ID = "action_id"

        fun showNudge(
            context: Context,
            id: Int,
            title: String,
            body: String,
            skipLabel: String,
        ) {
            fun actionPending(actionId: String, requestCode: Int): PendingIntent {
                val intent = Intent(context, LogDrinkReceiver::class.java).apply {
                    action = ACTION_NUDGE
                    putExtra(EXTRA_ACTION_ID, actionId)
                    putExtra(EXTRA_ID, id)
                }
                return PendingIntent.getBroadcast(
                    context,
                    requestCode,
                    intent,
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
                )
            }
            val notification = NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(android.R.drawable.ic_dialog_info)
                .setContentTitle(title)
                .setContentText(body)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setAutoCancel(true)
                .addAction(0, "+250 ml", actionPending(NudgeActionIds.ADD_250, 2000 + id))
                .addAction(0, "+500 ml", actionPending(NudgeActionIds.ADD_500, 3000 + id))
                .addAction(0, skipLabel, actionPending(NudgeActionIds.SKIP, 4000 + id))
                .build()
            NotificationManagerCompat.from(context).notify(id, notification)
        }
    }
}
