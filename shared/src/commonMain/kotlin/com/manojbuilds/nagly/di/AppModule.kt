package com.manojbuilds.nagly.di

import com.manojbuilds.nagly.data.DatabaseDriverFactory
import com.manojbuilds.nagly.data.DrinkLogRepository
import com.manojbuilds.nagly.data.GoalRepository
import com.manojbuilds.nagly.data.UnlockRepository
import com.manojbuilds.nagly.db.NaglyDatabase
import com.manojbuilds.nagly.ui.history.HistoryStateHolder
import com.manojbuilds.nagly.ui.onboarding.OnboardingStateHolder
import com.manojbuilds.nagly.ui.today.TodayStateHolder
import org.koin.core.context.startKoin
import org.koin.core.module.Module
import org.koin.dsl.KoinAppDeclaration
import org.koin.dsl.module

fun commonModule(): Module = module {
    single { NaglyDatabase(get<DatabaseDriverFactory>().createDriver()) }
    single { DrinkLogRepository(get()) }
    single { GoalRepository(get()) }
    single { UnlockRepository(get()) }
    single { TodayStateHolder(get(), get()) }
    single { OnboardingStateHolder(get(), get(), get()) }
    single { HistoryStateHolder(get(), get()) }
}

fun initKoin(
    platformModules: List<Module> = emptyList(),
    appDeclaration: KoinAppDeclaration = {},
) {
    startKoin {
        appDeclaration()
        modules(listOf(commonModule()) + platformModules)
    }
}
