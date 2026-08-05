package com.manojbuilds.nagly.platform

import platform.Foundation.NSURL
import platform.UIKit.UIActivityViewController
import platform.UIKit.UIApplication
import platform.UIKit.UIViewController

actual object PlatformActions {
    actual fun openUrl(url: String) {
        val nsUrl = NSURL.URLWithString(url) ?: return
        UIApplication.sharedApplication.openURL(nsUrl)
    }

    actual fun shareApp(message: String) {
        val root = UIApplication.sharedApplication.keyWindow?.rootViewController ?: return
        val activity = UIActivityViewController(listOf(message), null)
        root.presentViewController(activity, animated = true, completion = null)
    }

    actual fun rateApp() {
        openUrl("https://apps.apple.com/app/id0000000000")
    }

    actual fun openEmail(to: String, subject: String) {
        val encoded = "mailto:$to?subject=${subject.replace(" ", "%20")}"
        openUrl(encoded)
    }
}
