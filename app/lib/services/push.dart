import 'package:flutter/foundation.dart';
import 'package:onesignal_flutter/onesignal_flutter.dart';

import '../config/integrations.dart';

/// Where a cloud message or in-app message should take the user.
/// Set `route` in the OneSignal message's additional data (or as the IAM button action id).
enum PushRoute { home, paywall, personas, history, insights }

/// Cloud re-engagement only (win-back, streak milestones, trial ending, upsell).
/// The hourly nags are local notifications — they never go through push.
abstract class PushService {
  Future<void> init(String externalId, {required void Function(PushRoute) onRoute});
  Future<void> setTags(Map<String, String> tags);

  /// In-app message triggers, e.g. `streak_milestone=7` or `trial_days_left=1`.
  Future<void> setTriggers(Map<String, String> triggers);
}

PushRoute? parseRoute(Object? raw) =>
    PushRoute.values.where((r) => r.name == raw).firstOrNull;

class FakePushService implements PushService {
  final Map<String, String> tags = {};
  final Map<String, String> triggers = {};
  String? externalId;

  @override
  Future<void> init(String externalId, {required void Function(PushRoute) onRoute}) async =>
      this.externalId = externalId;

  @override
  Future<void> setTags(Map<String, String> tags) async {
    this.tags.addAll(tags);
    if (kDebugMode) debugPrint('[push:fake] tags $tags');
  }

  @override
  Future<void> setTriggers(Map<String, String> triggers) async => this.triggers.addAll(triggers);
}

class OneSignalPushService implements PushService {
  @override
  Future<void> init(String externalId, {required void Function(PushRoute) onRoute}) async {
    OneSignal.initialize(Integrations.oneSignalAppId);
    // Anonymous install id — no personal data, no login screen.
    await OneSignal.login(externalId);
    OneSignal.Notifications.addClickListener((event) {
      final route = parseRoute(event.notification.additionalData?['route']);
      if (route != null) onRoute(route);
    });
    OneSignal.InAppMessages.addClickListener((event) {
      final route = parseRoute(event.result.actionId);
      if (route != null) onRoute(route);
    });
  }

  @override
  Future<void> setTags(Map<String, String> tags) => OneSignal.User.addTags(tags);

  @override
  Future<void> setTriggers(Map<String, String> triggers) => OneSignal.InAppMessages.addTriggers(triggers);
}
