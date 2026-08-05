package com.manojbuilds.nagly.di

import android.content.Context
import org.koin.android.ext.koin.androidContext

fun initKoinAndroid(context: Context) {
    initKoin(platformModules = listOf(androidPlatformModule())) {
        androidContext(context)
    }
}
