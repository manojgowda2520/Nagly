package com.manojbuilds.nagly.platform

import android.content.Intent
import android.net.Uri
import androidx.core.net.toUri

actual object PlatformActions {
    private var contextProvider: (() -> android.content.Context)? = null

    fun init(contextProvider: () -> android.content.Context) {
        this.contextProvider = contextProvider
    }

    actual fun openUrl(url: String) {
        val context = contextProvider?.invoke() ?: return
        val intent = Intent(Intent.ACTION_VIEW, url.toUri()).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(intent)
    }

    actual fun shareApp(message: String) {
        val context = contextProvider?.invoke() ?: return
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, message)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(Intent.createChooser(intent, "Share Nagly").apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        })
    }

    actual fun rateApp() {
        val context = contextProvider?.invoke() ?: return
        val packageName = context.packageName
        val marketUri = "market://details?id=$packageName".toUri()
        val webUri = "https://play.google.com/store/apps/details?id=$packageName".toUri()
        val intent = Intent(Intent.ACTION_VIEW, marketUri).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        try {
            context.startActivity(intent)
        } catch (_: Exception) {
            context.startActivity(Intent(Intent.ACTION_VIEW, webUri).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            })
        }
    }

    actual fun openEmail(to: String, subject: String) {
        val context = contextProvider?.invoke() ?: return
        val intent = Intent(Intent.ACTION_SENDTO).apply {
            data = Uri.parse("mailto:")
            putExtra(Intent.EXTRA_EMAIL, arrayOf(to))
            putExtra(Intent.EXTRA_SUBJECT, subject)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(intent)
    }
}
