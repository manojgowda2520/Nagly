package com.manojbuilds.nagly.platform

actual object AppInfo {
    private var contextProvider: (() -> android.content.Context)? = null

    fun init(contextProvider: () -> android.content.Context) {
        this.contextProvider = contextProvider
    }

    actual val versionName: String
        get() {
            val context = contextProvider?.invoke() ?: return "1.0"
            return context.packageManager
                .getPackageInfo(context.packageName, 0)
                .versionName ?: "1.0"
        }
}
