package com.manojbuilds.nagly.di

import com.manojbuilds.nagly.data.DatabaseDriverFactory
import com.manojbuilds.nagly.notifications.Notifier
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

fun androidPlatformModule() = module {
    single { DatabaseDriverFactory(androidContext()) }
    single { Notifier(androidContext()) }
}
