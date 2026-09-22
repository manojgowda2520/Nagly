import 'dart:io' show Platform;

import 'package:flutter/foundation.dart' show kReleaseMode;

/// How the app makes money. Set remotely from the RevenueCat dashboard: current
/// offering → Metadata → `{"monetization_mode": "payments" | "ads" | "both"}`.
/// iOS is always [payments].
enum MonetizationMode { payments, ads, both }

/// Single place to flip sandbox fakes → real SDK clients.
///
/// Each integration goes live independently as soon as its key is filled in
/// and [sandboxMode] is false. Keep keys out of git history if the repo is public:
/// these are *public* client keys (RevenueCat public SDK key, OneSignal app id,
/// AdMob ids) — never put secret/server keys here.
abstract final class Integrations {
  static const bool sandboxMode = false;

  /// payments: paywall only, no ads anywhere (default).
  /// ads: no purchases — every Pro feature is unlocked for 24h by a rewarded ad.
  /// both: paywall, plus "watch an ad for 24h" on locked voices.
  static MonetizationMode _mode = MonetizationMode.payments;

  static MonetizationMode get monetizationMode =>
      Platform.isIOS ? MonetizationMode.payments : _mode;
  static set monetizationMode(MonetizationMode m) => _mode = m;

  /// Applies the remote `monetization_mode` value; unknown values are ignored.
  static void applyRemoteMode(Object? value) {
    for (final m in MonetizationMode.values) {
      if (m.name == value) _mode = m;
    }
  }

  static bool get purchasesEnabled => monetizationMode != MonetizationMode.ads;
  static set purchasesEnabled(bool on) =>
      _mode = on ? MonetizationMode.payments : MonetizationMode.ads;
  static bool get adsEnabled => monetizationMode != MonetizationMode.payments;

  // RevenueCat public SDK keys (Project settings → API keys).
  static const String revenueCatAndroidKey = 'goog_fhjwCDIAKDpUQTIWJKNmIsozRWP';
  static const String revenueCatIosKey = '';

  /// Entitlement that unlocks everything.
  static const String proEntitlement = 'pro';

  // OneSignal App ID (Settings → Keys & IDs).
  static const String oneSignalAppId = '2858113c-c323-453d-bbc9-976b8d4c999e';

  // AdMob. The app id also lives in android/app/src/main/AndroidManifest.xml.
  // Real unit in release builds; Google's official test unit in debug so we
  // never click our own live ads while developing.
  static const String adMobRewardedUnitAndroid = kReleaseMode
      ? 'ca-app-pub-7379182928133388/4618814501'
      : 'ca-app-pub-3940256099942544/5224354917';
  static const String adMobRewardedUnitIos =
      'ca-app-pub-3940256099942544/1712485313';

  static bool get useRevenueCat =>
      !sandboxMode &&
      (Platform.isIOS ? revenueCatIosKey : revenueCatAndroidKey).isNotEmpty;
  static bool get useOneSignal => !sandboxMode && oneSignalAppId.isNotEmpty;
  // Never on iOS: the iPhone app has no ads at all (App Store policy choice).
  static bool get useAdMob => !sandboxMode && !Platform.isIOS;

  static const String privacyUrl =
      'https://manojgowda2520.github.io/Nagly/privacy.html';
  static const String termsUrl =
      'https://manojgowda2520.github.io/Nagly/terms.html';
  static const String supportEmail = 'mgmanoj1481@gmail.com';
  static const String playStoreUrl =
      'https://play.google.com/store/apps/details?id=com.manojbuilds.nagly';
  static const String shareMessage =
      "My family nags me to drink water now. It's weirdly effective. Try Nagly: $playStoreUrl";
}
