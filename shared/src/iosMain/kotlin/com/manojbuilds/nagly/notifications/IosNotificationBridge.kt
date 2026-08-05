package com.manojbuilds.nagly.notifications

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

object IosNotificationBridge : KoinComponent {
    private val coordinator: NotificationCoordinator by inject()

    fun handleAction(actionId: String) {
        if (actionId != Notifier.ACTION_LOGGED_IT) return
        CoroutineScope(Dispatchers.Default).launch {
            coordinator.logFromNotification(250)
        }
    }

    fun onNudgeDelivered() {
        coordinator.onNudgeDelivered()
    }
}
