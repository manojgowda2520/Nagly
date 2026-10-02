import 'dart:io' show Platform;

/// Single place to flip sandbox fakes → real SDK clients.
///
/// Each integration goes live independently as soon as its key is filled in
/// and [sandboxMode] is false. Keep keys out of git history if the repo is public:
/// these are *public* client keys (RevenueCat public SDK key, OneSignal app id)
/// — never put secret/server keys here.
///
/// Nagly has no ads on any platform: it makes money only through RevenueCat
/// purchases (Lifetime, Annual, Monthly), the same on iPhone and Android.
abstract final class Integrations {
  static const bool sandboxMode = false;

  // RevenueCat public SDK keys (Project settings → API keys).
  static const String revenueCatAndroidKey = 'goog_fhjwCDIAKDpUQTIWJKNmIsozRWP';
  static const String revenueCatIosKey = 'appl_dZYKwxdvPdctNChgasheHkdxRPs';

  /// Entitlement that unlocks everything.
  static const String proEntitlement = 'pro';

  // OneSignal App ID (Settings → Keys & IDs).
  static const String oneSignalAppId = '2858113c-c323-453d-bbc9-976b8d4c999e';

  static bool get useRevenueCat =>
      !sandboxMode &&
      (Platform.isIOS ? revenueCatIosKey : revenueCatAndroidKey).isNotEmpty;
  static bool get useOneSignal => !sandboxMode && oneSignalAppId.isNotEmpty;

  static const String privacyUrl =
      'https://manojgowda2520.github.io/Nagly/privacy.html';
  static const String termsUrl =
      'https://manojgowda2520.github.io/Nagly/terms.html';
  static const String supportEmail = 'mgmanoj1481@gmail.com';
  static const String playStoreUrl =
      'https://play.google.com/store/apps/details?id=com.manojbuilds.nagly';
  static const String appStoreId = '6814609746';
  static const String appStoreUrl =
      'https://apps.apple.com/app/nagly-moms-water-pill-nags/id$appStoreId';

  /// Share text with the link for the store the sharer is on.
  static String shareMessage({required bool ios}) =>
      "My family nags me to drink water now. It's weirdly effective. Try Nagly: ${ios ? appStoreUrl : playStoreUrl}";
}
