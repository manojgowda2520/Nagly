package com.manojbuilds.nagly.notifications

import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.coroutines.suspendCancellableCoroutine
import platform.UserNotifications.UNAuthorizationOptionAlert
import platform.UserNotifications.UNAuthorizationOptionBadge
import platform.UserNotifications.UNAuthorizationOptionSound
import platform.UserNotifications.UNMutableNotificationContent
import platform.UserNotifications.UNNotificationAction
import platform.UserNotifications.UNNotificationActionOptionNone
import platform.UserNotifications.UNNotificationCategory
import platform.UserNotifications.UNNotificationCategoryOptionNone
import platform.UserNotifications.UNNotificationRequest
import platform.UserNotifications.UNNotificationSound
import platform.UserNotifications.UNTimeIntervalNotificationTrigger
import platform.UserNotifications.UNUserNotificationCenter
import kotlin.coroutines.resume
import kotlin.time.Clock

@OptIn(ExperimentalForeignApi::class)
actual class Notifier {

    init {
        registerCategory()
    }

    actual suspend fun requestPermission(): Boolean = suspendCancellableCoroutine { cont ->
        val center = UNUserNotificationCenter.currentNotificationCenter()
        center.requestAuthorizationWithOptions(
            UNAuthorizationOptionAlert or UNAuthorizationOptionSound or UNAuthorizationOptionBadge,
        ) { granted, _ ->
            cont.resume(granted)
        }
    }

    actual fun schedule(id: Int, atEpochMs: Long, title: String, body: String) {
        val content = UNMutableNotificationContent().apply {
            setTitle(title)
            setBody(body)
            setSound(UNNotificationSound.defaultSound)
            setCategoryIdentifier(CATEGORY_ID)
        }
        val nowMs = Clock.System.now().toEpochMilliseconds()
        val intervalSec = ((atEpochMs - nowMs).coerceAtLeast(1_000L)) / 1000.0
        val trigger = UNTimeIntervalNotificationTrigger.triggerWithTimeInterval(
            intervalSec,
            repeats = false,
        )
        val request = UNNotificationRequest.requestWithIdentifier(
            identifier = "nagly-$id",
            content = content,
            trigger = trigger,
        )
        UNUserNotificationCenter.currentNotificationCenter()
            .addNotificationRequest(request, withCompletionHandler = null)
    }

    actual fun cancelAll() {
        val center = UNUserNotificationCenter.currentNotificationCenter()
        center.removeAllPendingNotificationRequests()
        center.removeAllDeliveredNotifications()
    }

    private fun registerCategory() {
        val action = UNNotificationAction.actionWithIdentifier(
            identifier = ACTION_LOGGED_IT,
            title = "Logged it",
            options = UNNotificationActionOptionNone,
        )
        val category = UNNotificationCategory.categoryWithIdentifier(
            identifier = CATEGORY_ID,
            actions = listOf(action),
            intentIdentifiers = emptyList<String>(),
            options = UNNotificationCategoryOptionNone,
        )
        UNUserNotificationCenter.currentNotificationCenter()
            .setNotificationCategories(setOf(category))
    }

    companion object {
        const val CATEGORY_ID = "WATER_NUDGE"
        const val ACTION_LOGGED_IT = "LOGGED_IT"
    }
}
