/// Ad-free stand-in for google_mobile_ads, swapped in for iOS builds only so the
/// iPhone app ships with no ad SDK at all. Mirrors just the API lib/services/ads.dart
/// uses; nothing ever loads, so AdMobAdService reports "unavailable".
library;

import 'dart:async';

class MobileAds {
  MobileAds._();
  static final MobileAds instance = MobileAds._();
  Future<void> initialize() async {}
}

class AdRequest {
  const AdRequest();
}

class LoadAdError {
  const LoadAdError(this.code, this.message);
  final int code;
  final String message;
  @override
  String toString() => 'LoadAdError($code, $message)';
}

class ResponseInfo {
  String? get responseId => null;
  String? get mediationAdapterClassName => null;
}

class ServerSideVerificationOptions {
  const ServerSideVerificationOptions({this.userId, this.customData});
  final String? userId;
  final String? customData;
}

enum PrecisionType { unknown, estimated, publisherProvided, precise }

class RewardItem {
  const RewardItem(this.amount, this.type);
  final num amount;
  final String type;
}

abstract class Ad {
  Future<void> dispose() async {}
}

class RewardedAdLoadCallback {
  const RewardedAdLoadCallback({
    required this.onAdLoaded,
    required this.onAdFailedToLoad,
  });
  final void Function(RewardedAd ad) onAdLoaded;
  final void Function(LoadAdError error) onAdFailedToLoad;
}

class FullScreenContentCallback<T> {
  const FullScreenContentCallback({
    this.onAdShowedFullScreenContent,
    this.onAdClicked,
    this.onAdDismissedFullScreenContent,
    this.onAdFailedToShowFullScreenContent,
  });
  final void Function(T ad)? onAdShowedFullScreenContent;
  final void Function(T ad)? onAdClicked;
  final void Function(T ad)? onAdDismissedFullScreenContent;
  final void Function(T ad, Object error)? onAdFailedToShowFullScreenContent;
}

class RewardedAd extends Ad {
  RewardedAd._();

  static Future<void> load({
    required String adUnitId,
    required AdRequest request,
    required RewardedAdLoadCallback rewardedAdLoadCallback,
  }) async {
    rewardedAdLoadCallback.onAdFailedToLoad(
      const LoadAdError(-1, 'Ads are not part of the iOS build'),
    );
  }

  ResponseInfo? get responseInfo => null;
  FullScreenContentCallback<RewardedAd>? fullScreenContentCallback;
  void Function(Ad ad, double valueMicros, PrecisionType precision,
      String currencyCode)? onPaidEvent;

  Future<void> setServerSideOptions(ServerSideVerificationOptions o) async {}

  Future<void> show({
    required void Function(Ad ad, RewardItem reward) onUserEarnedReward,
  }) async {}
}
