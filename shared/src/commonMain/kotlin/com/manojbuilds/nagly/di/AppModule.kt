package com.manojbuilds.nagly.di

import com.manojbuilds.nagly.ads.AdClient
import com.manojbuilds.nagly.ads.FakeAdClient
import com.manojbuilds.nagly.billing.BillingRepository
import com.manojbuilds.nagly.billing.FakeBillingRepository
import com.manojbuilds.nagly.config.Integrations
import com.manojbuilds.nagly.data.DatabaseDriverFactory
import com.manojbuilds.nagly.data.DrinkLogRepository
import com.manojbuilds.nagly.data.GoalRepository
import com.manojbuilds.nagly.data.UnlockRepository
import com.manojbuilds.nagly.db.NaglyDatabase
import com.manojbuilds.nagly.domain.UnlockExpiryWatcher
import com.manojbuilds.nagly.notifications.IgnoredNudgeStore
import com.manojbuilds.nagly.notifications.NotificationCoordinator
import com.manojbuilds.nagly.notifications.NudgeScheduler
import com.manojbuilds.nagly.push.FakePushClient
import com.manojbuilds.nagly.push.PushClient
import com.manojbuilds.nagly.push.PushTagSync
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
    single { IgnoredNudgeStore() }
    single { NudgeScheduler(get(), get()) }
    single { NotificationCoordinator(get(), get(), get(), get()) }
    single {
        val ignored = get<IgnoredNudgeStore>()
        TodayStateHolder(get(), get(), ignoredNudgeCountProvider = { ignored.count })
    }
    single { OnboardingStateHolder(get(), get(), get(), get()) }
    single { HistoryStateHolder(get(), get()) }
    single { PushTagSync(get(), get(), get(), get()) }
    single { UnlockExpiryWatcher(get(), get(), get()) }

    // Sandbox ↔ production swap lives here only.
    single<BillingRepository> {
        if (Integrations.SANDBOX_MODE) {
            FakeBillingRepository(get())
        } else {
            // TODO: bind RevenueCatBillingRepository when keys exist
            FakeBillingRepository(get())
        }
    }
    single<PushClient> {
        if (Integrations.SANDBOX_MODE) {
            FakePushClient()
        } else {
            // TODO: bind OneSignalPushClient when ONESIGNAL_APP_ID exists
            FakePushClient()
        }
    }
    single<AdClient> {
        if (Integrations.SANDBOX_MODE) {
            FakeAdClient()
        } else {
            // TODO: bind AdMobAdClient when ADMOB_* ids exist
            FakeAdClient()
        }
    }
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
