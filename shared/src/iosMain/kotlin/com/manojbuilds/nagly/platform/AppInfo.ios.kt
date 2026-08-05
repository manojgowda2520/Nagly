package com.manojbuilds.nagly.platform

import platform.Foundation.NSBundle

actual object AppInfo {
    actual val versionName: String =
        NSBundle.mainBundle.objectForInfoDictionaryKey("CFBundleShortVersionString") as? String
            ?: "1.0"
}
