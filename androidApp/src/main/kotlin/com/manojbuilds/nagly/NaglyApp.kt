package com.manojbuilds.nagly

import android.app.Application
import com.manojbuilds.nagly.di.initKoinAndroid

class NaglyApp : Application() {
    override fun onCreate() {
        super.onCreate()
        initKoinAndroid(this)
    }
}
