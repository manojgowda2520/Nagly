package com.manojbuilds.nagly.notifications

import android.content.Context

actual class Notifier(private val context: Context) {
    actual suspend fun requestPermission(): Boolean {
        // Real POST_NOTIFICATIONS handling arrives in step 7.
        return true
    }

    actual fun schedule(id: Int, atEpochMs: Long, title: String, body: String) {
        // no-op until step 7
    }

    actual fun cancelAll() {
        // no-op until step 7
    }
}
