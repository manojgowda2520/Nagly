import 'package:flutter/foundation.dart';
import 'package:onesignal_flutter/onesignal_flutter.dart';

import '../config/integrations.dart';

/// Where a cloud message or in-app message should take the user.
/// Set `route` in the OneSignal message's additional data (or as the IAM button action id).
enum PushRoute { home, paywall, personas, history, insights }

/// Action-button ids a cloud push can carry. `add_250` / `add_500` log water straight
/// from the notification, exactly like the local nudges do.
abstract final class PushActionIds {
  static const add250 = 'add_250';
  static const add500 = 'add_500';
}

/// Outcome names reported to OneSignal, so the dashboard shows which messages actually
/// lead to drinking water (not just opens).
abstract final class PushOutcomes {
  static const waterLogged = 'water_logged';
  static const waterMl = 'water_ml';
  static const goalMet = 'goal_met';
  static const medTaken = 'med_taken';
}

/// Cloud re-engagement only (win-back, streak milestones, trial ending, upsell).
/// The hourly nags are local notifications — they never go through push.
abstract class PushService {
  Future<void> init(
    String externalId, {
    required void Function(PushRoute) onRoute,
    required void Function(String actionId) onAction,
  });
  Future<void> setTags(Map<String, String> tags);

  /// In-app message triggers, e.g. `streak=7` or `trial_days_left=1`.
  Future<void> setTriggers(Map<String, String> triggers);

  /// Report a conversion. With [unique], counted once per notification attribution.
  Future<void> outcome(String name, {double? value, bool unique = false});
}

PushRoute? parseRoute(Object? raw) =>
    PushRoute.values.where((r) => r.name == raw).firstOrNull;

class FakePushService implements PushService {
  final Map<String, String> tags = {};
  final Map<String, String> triggers = {};
  final List<String> outcomes = [];
  String? externalId;

  @override
  Future<void> init(
    String externalId, {
    required void Function(PushRoute) onRoute,
    required void Function(String actionId) onAction,
  }) async => this.externalId = externalId;

  @override
  Future<void> setTags(Map<String, String> tags) async {
    this.tags.addAll(tags);
    if (kDebugMode) debugPrint('[push:fake] tags $tags');
  }

  @override
  Future<void> setTriggers(Map<String, String> triggers) async =>
      this.triggers.addAll(triggers);

  @override
  Future<void> outcome(
    String name, {
    double? value,
    bool unique = false,
  }) async => outcomes.add(value == null ? name : '$name=$value');
}

class OneSignalPushService implements PushService {
  @override
  Future<void> init(
    String externalId, {
    required void Function(PushRoute) onRoute,
    required void Function(String actionId) onAction,
  }) async {
    OneSignal.initialize(Integrations.oneSignalAppId);
    // Anonymous install id — no personal data, no login screen.
    await OneSignal.login(externalId);
    OneSignal.Notifications.addClickListener((event) {
      final action = event.result.actionId;
      if (action != null && action.isNotEmpty) onAction(action);
      final route = parseRoute(event.notification.additionalData?['route']);
      if (route != null) onRoute(route);
    });
    OneSignal.InAppMessages.addClickListener((event) {
      final id = event.result.actionId;
      final route = parseRoute(id);
      if (route != null) {
        onRoute(route);
      } else if (id != null) {
        onAction(id);
      }
    });
  }

  @override
  Future<void> setTags(Map<String, String> tags) =>
      OneSignal.User.addTags(tags);

  @override
  Future<void> setTriggers(Map<String, String> triggers) =>
      OneSignal.InAppMessages.addTriggers(triggers);

  @override
  Future<void> outcome(String name, {double? value, bool unique = false}) {
    if (value != null)
      return OneSignal.Session.addOutcomeWithValue(name, value);
    return unique
        ? OneSignal.Session.addUniqueOutcome(name)
        : OneSignal.Session.addOutcome(name);
  }
}
