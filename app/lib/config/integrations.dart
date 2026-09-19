/// Single place to flip sandbox fakes → real SDK clients.
///
/// Each integration goes live independently as soon as its key is filled in
/// and [sandboxMode] is false. Keep keys out of git history if the repo is public:
/// these are *public* client keys (RevenueCat public SDK key, OneSignal app id,
/// AdMob ids) — never put secret/server keys here.
abstract final class Integrations {
  static const bool sandboxMode = true;

  /// Plan A (true): paywall with Lifetime / Annual / Monthly via RevenueCat.
  /// Plan B (false): no purchases anywhere — every Pro feature is unlocked for 24h by a
  /// rewarded ad (still reported to RevenueCat Ads). Flip if Play payments aren't approved.
  static bool purchasesEnabled = true;

  // RevenueCat public SDK keys (Project settings → API keys).
  static const String revenueCatAndroidKey = 'goog_fhjwCDIAKDpUQTIWJKNmIsozRWP';
  static const String revenueCatIosKey = '';

  /// Entitlement that unlocks everything.
  static const String proEntitlement = 'pro';

  // OneSignal App ID (Settings → Keys & IDs).
  static const String oneSignalAppId = '';

  // AdMob. The app id also lives in android/app/src/main/AndroidManifest.xml.
  // Defaults are Google's official *test* ids — safe to ship in sandbox builds.
  static const String adMobRewardedUnitAndroid =
      'ca-app-pub-3940256099942544/5224354917';
  static const String adMobRewardedUnitIos =
      'ca-app-pub-3940256099942544/1712485313';

  static bool get useRevenueCat =>
      !sandboxMode && revenueCatAndroidKey.isNotEmpty;
  static bool get useOneSignal => !sandboxMode && oneSignalAppId.isNotEmpty;
  static bool get useAdMob => !sandboxMode;

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
