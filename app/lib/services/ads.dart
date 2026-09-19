// RevenueCat's ad tracking API is marked experimental.
// ignore_for_file: experimental_member_use
import 'dart:async';
import 'dart:io' show Platform;

import 'package:flutter/foundation.dart';
import 'package:google_mobile_ads/google_mobile_ads.dart';
import 'package:purchases_flutter/purchases_flutter.dart';

import '../config/integrations.dart';

enum AdResult { rewarded, cancelled, unavailable }

/// Rewarded ads only — never banners or interstitials, and only from an explicit
/// "watch an ad to unlock" tap. The reward is a 24h taste of a Pro voice, which
/// doubles as the most honest upsell we have.
abstract class AdService {
  Future<void> init();

  /// [placement] names where the ad was offered, e.g. `unlock_dad`.
  Future<AdResult> showRewarded({required String placement});
}

/// Sandbox ad: the UI plays a fake ad and calls [complete] or [cancel].
class FakeAdService implements AdService {
  Completer<AdResult>? _pending;

  @override
  Future<void> init() async {}

  @override
  Future<AdResult> showRewarded({required String placement}) {
    _pending = Completer<AdResult>();
    return _pending!.future;
  }

  void complete() => _finish(AdResult.rewarded);

  void cancel() => _finish(AdResult.cancelled);

  void _finish(AdResult r) {
    final p = _pending;
    _pending = null;
    if (p != null && !p.isCompleted) p.complete(r);
  }
}

/// AdMob rewarded ads, with every lifecycle event reported to RevenueCat Ads so ad
/// revenue sits next to subscription revenue in RevenueCat charts. When RevenueCat is
/// live the reward is verified server-side by RevenueCat before it's granted.
class AdMobAdService implements AdService {
  AdMobAdService({required this.trackWithRevenueCat});

  final bool trackWithRevenueCat;

  String get _unitId =>
      Platform.isIOS ? Integrations.adMobRewardedUnitIos : Integrations.adMobRewardedUnitAndroid;

  @override
  Future<void> init() => MobileAds.instance.initialize();

  Future<void> _track(Future<void> Function() call) async {
    if (!trackWithRevenueCat) return;
    try {
      await call();
    } catch (e) {
      debugPrint('RevenueCat ad tracking failed: $e');
    }
  }

  @override
  Future<AdResult> showRewarded({required String placement}) async {
    final loaded = Completer<RewardedAd?>();
    RewardedAd.load(
      adUnitId: _unitId,
      request: const AdRequest(),
      rewardedAdLoadCallback: RewardedAdLoadCallback(
        onAdLoaded: loaded.complete,
        onAdFailedToLoad: (e) {
          debugPrint('Rewarded ad failed to load: $e');
          _track(() => Purchases.adTracker.trackAdFailedToLoad(AdFailedToLoadData(
                mediatorName: AdMediatorName.adMob,
                adFormat: AdFormat.rewarded,
                placement: placement,
                adUnitId: _unitId,
                mediatorErrorCode: e.code,
              )));
          loaded.complete(null);
        },
      ),
    );
    final ad = await loaded.future;
    if (ad == null) return AdResult.unavailable;

    final impressionId = ad.responseInfo?.responseId ?? '${DateTime.now().millisecondsSinceEpoch}';
    final network = ad.responseInfo?.mediationAdapterClassName;
    await _track(() => Purchases.adTracker.trackAdLoaded(AdLoadedData(
          networkName: network,
          mediatorName: AdMediatorName.adMob,
          adFormat: AdFormat.rewarded,
          placement: placement,
          adUnitId: _unitId,
          impressionId: impressionId,
        )));

    // Server-side reward verification through RevenueCat, when available.
    RewardVerificationToken? token;
    if (trackWithRevenueCat) {
      try {
        token = await Purchases.generateRewardVerificationToken(impressionId);
        await ad.setServerSideOptions(
            ServerSideVerificationOptions(userId: token.appUserID, customData: token.customData));
      } catch (e) {
        debugPrint('Reward verification unavailable, using client reward: $e');
        token = null;
      }
    }

    ad.onPaidEvent = (ad, valueMicros, precision, currencyCode) {
      _track(() => Purchases.adTracker.trackAdRevenue(AdRevenueData(
            networkName: network,
            mediatorName: AdMediatorName.adMob,
            adFormat: AdFormat.rewarded,
            placement: placement,
            adUnitId: _unitId,
            impressionId: impressionId,
            revenueMicros: valueMicros.toInt(),
            currency: currencyCode,
            precision: switch (precision) {
              PrecisionType.precise => AdRevenuePrecision.exact,
              PrecisionType.estimated => AdRevenuePrecision.estimated,
              PrecisionType.publisherProvided => AdRevenuePrecision.publisherDefined,
              _ => AdRevenuePrecision.unknown,
            },
          )));
    };

    final done = Completer<AdResult>();
    var earned = false;
    ad.fullScreenContentCallback = FullScreenContentCallback(
      onAdShowedFullScreenContent: (_) => _track(() => Purchases.adTracker.trackAdDisplayed(AdDisplayedData(
            networkName: network,
            mediatorName: AdMediatorName.adMob,
            adFormat: AdFormat.rewarded,
            placement: placement,
            adUnitId: _unitId,
            impressionId: impressionId,
          ))),
      onAdClicked: (_) => _track(() => Purchases.adTracker.trackAdOpened(AdOpenedData(
            networkName: network,
            mediatorName: AdMediatorName.adMob,
            adFormat: AdFormat.rewarded,
            placement: placement,
            adUnitId: _unitId,
            impressionId: impressionId,
          ))),
      onAdDismissedFullScreenContent: (ad) {
        ad.dispose();
        if (!done.isCompleted) done.complete(earned ? AdResult.rewarded : AdResult.cancelled);
      },
      onAdFailedToShowFullScreenContent: (ad, _) {
        ad.dispose();
        if (!done.isCompleted) done.complete(AdResult.unavailable);
      },
    );
    await ad.show(onUserEarnedReward: (_, _) => earned = true);
    final result = await done.future;

    if (result == AdResult.rewarded && token != null) {
      try {
        final verification = await Purchases.pollRewardVerification(
          token.clientTransactionId,
          trackingMetadata: RewardedAdTrackingMetadata(
            networkName: network,
            mediatorName: AdMediatorName.adMob,
            adFormat: AdFormat.rewarded,
            placement: placement,
            adUnitId: _unitId,
            impressionId: impressionId,
          ),
        );
        if (verification.failed) return AdResult.unavailable;
      } catch (e) {
        debugPrint('Reward verification poll failed, trusting client reward: $e');
      }
    }
    return result;
  }
}
