import 'dart:async';
import 'dart:io' show Platform;

import 'package:flutter/foundation.dart';
import 'package:flutter/services.dart';
import 'package:purchases_flutter/purchases_flutter.dart';

import '../config/integrations.dart';
import '../data/database.dart';

enum PlanKind { lifetime, annual, monthly }

class Plan {
  const Plan({
    required this.kind,
    required this.title,
    required this.price,
    required this.detail,
    this.badge,
    this.rcPackage,
  });

  final PlanKind kind;
  final String title;
  final String price;
  final String detail;
  final String? badge;

  /// Real store package when RevenueCat is live.
  final Package? rcPackage;
}

enum PurchaseOutcome { success, cancelled, failed }

/// Puts the plan named by an offering's `highlight` metadata first, so a
/// placement's offering decides which plan the paywall opens on. Unknown or
/// missing values keep the default order (Lifetime first).
List<Plan> orderPlans(List<Plan> plans, Object? highlight) {
  final first = plans.where((p) => p.kind.name == highlight).firstOrNull;
  if (first == null) return plans;
  return [first, ...plans.where((p) => p != first)];
}

/// Store billing. Apple/Google take the payment and email the receipt;
/// RevenueCat tells us whether the `pro` entitlement is active. No gateway, no accounts.
abstract class BillingService {
  ValueListenable<bool> get isPro;
  Future<void> init();

  /// Plans for a paywall placement (`persona_locked`, `medication_limit`, `trial_end`,
  /// `streak_upsell`, `settings`). With RevenueCat, each placement can serve its own
  /// offering, so pricing and packaging can be targeted and A/B tested from the dashboard.
  Future<List<Plan>> plans({String placement = 'settings'});

  /// Customer attributes for segmenting RevenueCat charts (persona, care mode, streak…).
  Future<void> setAttributes(Map<String, String> attributes);
  Future<PurchaseOutcome> purchase(Plan plan);

  /// Returns true if Pro is active after restoring.
  Future<bool> restore();
}

const _fallbackPlans = [
  Plan(
    kind: PlanKind.lifetime,
    title: 'Lifetime',
    price: r'$29.99',
    detail: 'Pay once, yours forever',
    badge: 'BEST VALUE',
  ),
  Plan(
    kind: PlanKind.annual,
    title: 'Annual',
    price: r'$19.99/yr',
    detail: 'Try free for 7 days',
    badge: 'FREE TRIAL',
  ),
  Plan(
    kind: PlanKind.monthly,
    title: 'Monthly',
    price: r'$1.99/mo',
    detail: 'Cancel any time',
  ),
];

/// Sandbox billing: persists a local Pro flag so the whole funnel can be demoed.
class FakeBillingService implements BillingService {
  FakeBillingService(this._db);

  final NaglyDatabase _db;
  final _isPro = ValueNotifier(false);

  @override
  ValueListenable<bool> get isPro => _isPro;

  @override
  Future<void> init() async => _isPro.value = await _db.getBool(Keys.isPro);

  @override
  Future<List<Plan>> plans({String placement = 'settings'}) async =>
      _fallbackPlans;

  @override
  Future<void> setAttributes(Map<String, String> attributes) async {}

  @override
  Future<PurchaseOutcome> purchase(Plan plan) async {
    await Future<void>.delayed(const Duration(milliseconds: 900));
    await _db.setBool(Keys.isPro, true);
    _isPro.value = true;
    return PurchaseOutcome.success;
  }

  @override
  Future<bool> restore() async {
    await Future<void>.delayed(const Duration(milliseconds: 500));
    _isPro.value = await _db.getBool(Keys.isPro);
    return _isPro.value;
  }

  /// Sandbox-only helper for demos and QA.
  Future<void> setPro(bool value) async {
    await _db.setBool(Keys.isPro, value);
    _isPro.value = value;
  }
}

class RevenueCatBillingService implements BillingService {
  final _isPro = ValueNotifier(false);

  @override
  ValueListenable<bool> get isPro => _isPro;

  void _apply(CustomerInfo info) => _isPro.value = info.entitlements.active
      .containsKey(Integrations.proEntitlement);

  @override
  Future<void> init() async {
    final key = Platform.isIOS
        ? Integrations.revenueCatIosKey
        : Integrations.revenueCatAndroidKey;
    await Purchases.configure(PurchasesConfiguration(key));
    Purchases.addCustomerInfoUpdateListener(_apply);
    try {
      _apply(await Purchases.getCustomerInfo());
    } on PlatformException catch (e) {
      debugPrint('RevenueCat getCustomerInfo failed: $e');
    }
    // Remote payments/ads switch (offering metadata). The SDK caches offerings, so
    // this is fast after first launch; on failure we keep the last/default mode.
    try {
      final offerings = await Purchases.getOfferings().timeout(
        const Duration(seconds: 4),
      );
      Integrations.applyRemoteMode(
        offerings.current?.metadata['monetization_mode'],
      );
    } catch (e) {
      debugPrint('RevenueCat monetization_mode fetch failed: $e');
    }
  }

  @override
  Future<void> setAttributes(Map<String, String> attributes) async {
    try {
      await Purchases.setAttributes(attributes);
    } on PlatformException catch (e) {
      debugPrint('RevenueCat setAttributes failed: $e');
    }
  }

  @override
  Future<List<Plan>> plans({String placement = 'settings'}) async {
    try {
      final current =
          await Purchases.getCurrentOfferingForPlacement(placement) ??
          (await Purchases.getOfferings()).current;
      if (current == null) return _fallbackPlans;
      return orderPlans([
        if (current.lifetime case final p?)
          Plan(
            kind: PlanKind.lifetime,
            title: 'Lifetime',
            price: p.storeProduct.priceString,
            detail: 'Pay once, yours forever',
            badge: 'BEST VALUE',
            rcPackage: p,
          ),
        if (current.annual case final p?)
          Plan(
            kind: PlanKind.annual,
            title: 'Annual',
            price: '${p.storeProduct.priceString}/yr',
            detail: 'Try free for 7 days',
            badge: 'FREE TRIAL',
            rcPackage: p,
          ),
        if (current.monthly case final p?)
          Plan(
            kind: PlanKind.monthly,
            title: 'Monthly',
            price: '${p.storeProduct.priceString}/mo',
            detail: 'Cancel any time',
            rcPackage: p,
          ),
      ], current.metadata['highlight']);
    } on PlatformException catch (e) {
      debugPrint('RevenueCat getOfferings failed: $e');
      return _fallbackPlans;
    }
  }

  @override
  Future<PurchaseOutcome> purchase(Plan plan) async {
    final pkg = plan.rcPackage;
    if (pkg == null) return PurchaseOutcome.failed;
    try {
      final result = await Purchases.purchase(PurchaseParams.package(pkg));
      _apply(result.customerInfo);
      return _isPro.value ? PurchaseOutcome.success : PurchaseOutcome.failed;
    } on PlatformException catch (e) {
      final code = PurchasesErrorHelper.getErrorCode(e);
      return code == PurchasesErrorCode.purchaseCancelledError
          ? PurchaseOutcome.cancelled
          : PurchaseOutcome.failed;
    }
  }

  @override
  Future<bool> restore() async {
    try {
      _apply(await Purchases.restorePurchases());
    } on PlatformException catch (e) {
      debugPrint('RevenueCat restore failed: $e');
    }
    return _isPro.value;
  }
}
