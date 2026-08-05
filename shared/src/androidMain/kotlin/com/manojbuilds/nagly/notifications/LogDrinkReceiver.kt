package com.manojbuilds.nagly.notifications

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class LogDrinkReceiver : BroadcastReceiver(), KoinComponent {
    private val coordinator: NotificationCoordinator by inject()

    override fun onReceive(context: Context, intent: Intent?) {
        if (intent?.action != Notifier.ACTION_LOGGED_IT) return
        val pending = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                coordinator.logFromNotification(250)
            } finally {
                pending.finish()
            }
        }
    }
}
