package com.manojbuilds.nagly.notifications

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class NudgeAlarmReceiver : BroadcastReceiver(), KoinComponent {
    private val coordinator: NotificationCoordinator by inject()

    override fun onReceive(context: Context, intent: Intent?) {
        if (intent?.action != Notifier.ACTION_SHOW_NUDGE) return
        val id = intent.getIntExtra(Notifier.EXTRA_ID, 1)
        val title = intent.getStringExtra(Notifier.EXTRA_TITLE).orEmpty()
        val body = intent.getStringExtra(Notifier.EXTRA_BODY).orEmpty()
        val skipLabel = intent.getStringExtra(Notifier.EXTRA_SKIP_LABEL) ?: "Skip"
        coordinator.onNudgeDelivered()
        Notifier.showNudge(context, id, title, body, skipLabel)
    }
}
