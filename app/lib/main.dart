import 'package:flutter/material.dart';
import 'package:flutter/services.dart';
import 'package:provider/provider.dart';

import 'config/integrations.dart';
import 'data/database.dart';
import 'services/ads.dart';
import 'services/billing.dart';
import 'services/notifications.dart';
import 'services/push.dart';
import 'state/app_controller.dart';
import 'ui/app.dart';

Future<void> main() async {
  WidgetsFlutterBinding.ensureInitialized();
  await SystemChrome.setPreferredOrientations([DeviceOrientation.portraitUp]);
  SystemChrome.setSystemUIOverlayStyle(const SystemUiOverlayStyle(
    statusBarColor: Colors.transparent,
    statusBarIconBrightness: Brightness.dark,
    systemNavigationBarColor: Colors.white,
    systemNavigationBarIconBrightness: Brightness.dark,
  ));

  final db = await NaglyDatabase.open();
  final notifications = NotificationService();
  final controller = AppController(
    db: db,
    notifications: notifications,
    billing: Integrations.useRevenueCat ? RevenueCatBillingService() : FakeBillingService(db),
    ads: Integrations.useAdMob
        ? AdMobAdService(trackWithRevenueCat: Integrations.useRevenueCat)
        : FakeAdService(),
    push: Integrations.useOneSignal ? OneSignalPushService() : FakePushService(),
  );
  await notifications.init(onResponse: controller.handleNotificationResponse);
  await controller.init();

  runApp(ChangeNotifierProvider.value(value: controller, child: const NaglyApp()));
}
