package com.manojbuilds.nagly.notifications

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationManagerCompat
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class LogDrinkReceiver : BroadcastReceiver(), KoinComponent {
    private val coordinator: NotificationCoordinator by inject()

    override fun onReceive(context: Context, intent: Intent?) {
        if (intent == null) return
        val actionId = when (intent.action) {
            Notifier.ACTION_NUDGE -> intent.getStringExtra(Notifier.EXTRA_ACTION_ID)
            // legacy single-button action
            "com.manojbuilds.nagly.LOGGED_IT" -> NudgeActionIds.ADD_250
            else -> return
        } ?: return

        val notificationId = intent.getIntExtra(Notifier.EXTRA_ID, 0)
        if (notificationId > 0) {
            NotificationManagerCompat.from(context).cancel(notificationId)
        }

        val pending = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                coordinator.handleAction(actionId)
            } finally {
                pending.finish()
            }
        }
    }
}
