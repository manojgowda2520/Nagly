package com.manojbuilds.nagly.notifications

import com.manojbuilds.nagly.domain.model.Mood
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
        registerCategories()
    }

    actual suspend fun requestPermission(): Boolean = suspendCancellableCoroutine { cont ->
        val center = UNUserNotificationCenter.currentNotificationCenter()
        center.requestAuthorizationWithOptions(
            UNAuthorizationOptionAlert or UNAuthorizationOptionSound or UNAuthorizationOptionBadge,
        ) { granted, _ ->
            cont.resume(granted)
        }
    }

    actual fun schedule(
        id: Int,
        atEpochMs: Long,
        title: String,
        body: String,
        actions: NudgeActions,
    ) {
        val content = UNMutableNotificationContent().apply {
            setTitle(title)
            setBody(body)
            setSound(UNNotificationSound.defaultSound)
            setCategoryIdentifier(actions.iosCategoryId)
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

    private fun registerCategories() {
        val categories = Mood.entries.map { mood ->
            val add250 = UNNotificationAction.actionWithIdentifier(
                identifier = NudgeActionIds.ADD_250,
                title = "+250 ml",
                options = UNNotificationActionOptionNone,
            )
            val add500 = UNNotificationAction.actionWithIdentifier(
                identifier = NudgeActionIds.ADD_500,
                title = "+500 ml",
                options = UNNotificationActionOptionNone,
            )
            val skip = UNNotificationAction.actionWithIdentifier(
                identifier = NudgeActionIds.SKIP,
                title = iosSkipTitle(mood),
                options = UNNotificationActionOptionNone,
            )
            UNNotificationCategory.categoryWithIdentifier(
                identifier = iosCategoryIdFor(mood),
                actions = listOf(add250, add500, skip),
                intentIdentifiers = emptyList<String>(),
                options = UNNotificationCategoryOptionNone,
            )
        }
        UNUserNotificationCenter.currentNotificationCenter()
            .setNotificationCategories(categories.toSet())
    }
}
