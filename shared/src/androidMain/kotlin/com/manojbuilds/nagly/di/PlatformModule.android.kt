package com.manojbuilds.nagly.di

import com.manojbuilds.nagly.data.DatabaseDriverFactory
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

fun androidPlatformModule() = module {
    single { DatabaseDriverFactory(androidContext()) }
}
