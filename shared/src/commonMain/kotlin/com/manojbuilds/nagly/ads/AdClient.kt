package com.manojbuilds.nagly.ads

import kotlinx.coroutines.flow.StateFlow

interface AdClient {
    val isRewardedReady: StateFlow<Boolean>
    suspend fun loadRewarded()
    suspend fun showRewarded(): Result<Unit> // success == reward earned
}

/**
 * TODO: AdMobAdClient + RevenueCat Ads tracking
 * - Load/show rewarded unit Integrations.ADMOB_REWARDED_UNIT_ID
 * - Never show interstitials/banners
 * - Only invoke from explicit "watch an ad to unlock" taps
 */
