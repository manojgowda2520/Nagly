import 'package:flutter/material.dart';
import 'package:in_app_review/in_app_review.dart';
import 'package:package_info_plus/package_info_plus.dart';
import 'package:provider/provider.dart';
import 'package:share_plus/share_plus.dart';
import 'package:url_launcher/url_launcher.dart';

import '../../config/integrations.dart';
import '../../domain/models.dart';
import '../../domain/mood_engine.dart';
import '../../state/app_controller.dart';
import '../app.dart';
import '../theme.dart';
import '../widgets/common.dart';
import 'home_screen.dart';

class SettingsScreen extends StatelessWidget {
  const SettingsScreen({super.key});

  @override
  Widget build(BuildContext context) {
    final c = context.watch<AppController>();
    final p = c.profile;
    final a = c.access;
    return DayBackground(
      hour: c.now.hour,
      intensity: 0.45,
      child: SafeArea(
        bottom: false,
        child: ListView(
          padding: const EdgeInsets.fromLTRB(20, 12, 20, 32),
          children: [
            Text('Settings', style: Theme.of(context).textTheme.headlineMedium),
            const SizedBox(height: 12),
            NCard(
              color: a.isPro
                  ? const Color(0xFFEFFAF1)
                  : const Color(0xFFF3FAFD),
              onTap: a.isPro
                  ? null
                  : () => openPaywall(context, placement: 'settings'),
              child: Row(
                children: [
                  Text(
                    a.isPro ? '💛' : '✨',
                    style: const TextStyle(fontSize: 28),
                  ),
                  const SizedBox(width: 12),
                  Expanded(
                    child: Column(
                      crossAxisAlignment: CrossAxisAlignment.start,
                      children: [
                        Text(
                          a.isPro
                              ? 'Nagly Pro'
                              : a.inTrial
                              ? 'Free trial · ${a.trialDaysLeft} day${a.trialDaysLeft == 1 ? '' : 's'} left'
                              : 'Free plan',
                          style: const TextStyle(
                            fontSize: 17,
                            fontWeight: FontWeight.w900,
                          ),
                        ),
                        Text(
                          a.isPro
                              ? 'Every voice, unlimited meds. Thank you!'
                              : Integrations.purchasesEnabled
                              ? 'Mom, water & 1 medication free forever. Tap for Pro.'
                              : 'Mom, water & 1 medication free forever. Tap to unlock more with an ad.',
                          style: const TextStyle(
                            fontWeight: FontWeight.w700,
                            color: NaglyColors.textSecondary,
                          ),
                        ),
                      ],
                    ),
                  ),
                  if (!a.isPro) const Icon(Icons.chevron_right_rounded),
                ],
              ),
            ),
            const SectionLabel('Your goal'),
            _Group(
              children: [
                _Row(
                  emoji: '💧',
                  title: 'Daily goal',
                  value: formatVolume(p.dailyMl, p.volumeUnit),
                  onTap: () => _editGoal(context),
                ),
                _Row(
                  emoji: '📏',
                  title: 'Units',
                  trailing: SegmentedButton<VolumeUnit>(
                    showSelectedIcon: false,
                    style: const ButtonStyle(
                      visualDensity: VisualDensity.compact,
                    ),
                    segments: const [
                      ButtonSegment(value: VolumeUnit.ml, label: Text('ml')),
                      ButtonSegment(value: VolumeUnit.oz, label: Text('oz')),
                    ],
                    selected: {p.volumeUnit},
                    onSelectionChanged: (s) =>
                        c.updateProfile((x) => x.copyWith(volumeUnit: s.first)),
                  ),
                ),
                _Row(
                  emoji: '🌿',
                  title: 'Care mode',
                  trailing: SegmentedButton<CareMode>(
                    showSelectedIcon: false,
                    style: const ButtonStyle(
                      visualDensity: VisualDensity.compact,
                    ),
                    segments: const [
                      ButtonSegment(
                        value: CareMode.hydration,
                        label: Text('💧 Water'),
                      ),
                      ButtonSegment(
                        value: CareMode.medication,
                        label: Text('💊 Meds'),
                      ),
                    ],
                    selected: {p.careMode},
                    onSelectionChanged: (s) => c.setCareMode(s.first),
                  ),
                ),
                _Row(
                  emoji: '🌅',
                  title: 'Awake hours',
                  value:
                      '${formatHourLabel(p.wakeHour)} – ${formatHourLabel(p.sleepHour)}',
                  onTap: () => _editHours(context),
                ),
              ],
            ),
            if (p.careMode == CareMode.medication) ...[
              const SectionLabel('Medication'),
              for (final m in c.meds) ...[
                MedicationCard(med: m),
                const SizedBox(height: 10),
              ],
              OutlinedButton.icon(
                onPressed: () => addMedicationFlow(context),
                icon: const Icon(Icons.add_rounded),
                label: const Text('Add medication'),
              ),
            ],
            const SectionLabel('Nagging'),
            _Group(
              children: [
                _Row(
                  emoji: c.persona.emoji,
                  title: 'Persona',
                  value: c.persona.displayName,
                  onTap: () =>
                      context.read<TabSwitcher>().value = MainTab.personas,
                ),
                _Row(
                  emoji: '🔔',
                  title: 'Reminders',
                  trailing: Switch(
                    value: c.notificationsEnabled,
                    onChanged: c.setNotificationsEnabled,
                  ),
                ),
                if (!c.permissionGranted)
                  _Row(
                    emoji: '⚠️',
                    title: 'Allow notifications',
                    value: 'Off',
                    onTap: c.requestNotificationPermission,
                  ),
              ],
            ),
            if (Integrations.purchasesEnabled) ...[
              const SectionLabel('Purchases'),
              _Group(
                children: [
                  _Row(
                    emoji: '♻️',
                    title: 'Restore purchases',
                    onTap: () async {
                      final messenger = ScaffoldMessenger.of(context);
                      final ok = await c.restore();
                      messenger.showSnackBar(
                        SnackBar(
                          content: Text(
                            ok
                                ? 'Pro restored 💛'
                                : 'No previous purchase found.',
                          ),
                        ),
                      );
                    },
                  ),
                  _Row(
                    emoji: '🧾',
                    title: 'Manage subscription',
                    onTap: () => launchUrl(
                      Uri.parse(
                        'https://play.google.com/store/account/subscriptions?package=com.manojbuilds.nagly',
                      ),
                      mode: LaunchMode.externalApplication,
                    ),
                  ),
                ],
              ),
            ],
            const SectionLabel('About'),
            _Group(
              children: [
                _Row(
                  emoji: '📣',
                  title: 'Tell a friend',
                  onTap: () => SharePlus.instance.share(
                    ShareParams(text: Integrations.shareMessage),
                  ),
                ),
                _Row(
                  emoji: '⭐',
                  title: 'Rate Nagly',
                  onTap: () => InAppReview.instance.openStoreListing(),
                ),
                _Row(
                  emoji: '✉️',
                  title: 'Contact support',
                  onTap: () => launchUrl(
                    Uri.parse(
                      'mailto:${Integrations.supportEmail}?subject=Nagly%20feedback',
                    ),
                  ),
                ),
                _Row(
                  emoji: '🔒',
                  title: 'Privacy policy',
                  onTap: () => launchUrl(Uri.parse(Integrations.privacyUrl)),
                ),
                _Row(
                  emoji: '📄',
                  title: 'Terms of use',
                  onTap: () => launchUrl(Uri.parse(Integrations.termsUrl)),
                ),
              ],
            ),
            const SizedBox(height: 16),
            const Text(
              'Your water & medication log stays on this phone. No account needed.',
              textAlign: TextAlign.center,
              style: TextStyle(
                fontWeight: FontWeight.w700,
                color: NaglyColors.textSecondary,
              ),
            ),
            FutureBuilder(
              future: PackageInfo.fromPlatform(),
              builder: (_, s) => Text(
                s.hasData
                    ? 'Nagly ${s.data!.version} (${s.data!.buildNumber})'
                    : '',
                textAlign: TextAlign.center,
                style: const TextStyle(
                  fontSize: 12,
                  color: NaglyColors.textSecondary,
                ),
              ),
            ),
            if (Integrations.sandboxMode) ...[
              const SectionLabel('Sandbox (demo tools)'),
              _Group(
                children: [
                  _Row(
                    emoji: '🔔',
                    title: 'Send a test nudge in 5s',
                    onTap: () {
                      c.notifications.sendTestNudge(c.db);
                      ScaffoldMessenger.of(context).showSnackBar(
                        const SnackBar(
                          content: Text('Lock your phone — she\'s coming 👀'),
                        ),
                      );
                    },
                  ),
                  _Row(
                    emoji: '👑',
                    title: a.isPro ? 'Remove Pro' : 'Grant Pro',
                    onTap: () => c.sandboxSetPro(!a.isPro),
                  ),
                  _Row(
                    emoji: '⏳',
                    title: 'Trial ends in 1 hour',
                    onTap: c.sandboxEndTrialSoon,
                  ),
                  _Row(
                    emoji: '⌛',
                    title: 'Expire trial now',
                    onTap: c.sandboxExpireTrial,
                  ),
                  _Row(
                    emoji: '📅',
                    title: 'Seed a week of history',
                    onTap: c.sandboxSeedWeek,
                  ),
                ],
              ),
            ],
          ],
        ),
      ),
    );
  }

  Future<void> _editGoal(BuildContext context) async {
    final c = context.read<AppController>();
    var ml = c.profile.dailyMl;
    await showModalBottomSheet<void>(
      context: context,
      builder: (ctx) => StatefulBuilder(
        builder: (ctx, set) => Padding(
          padding: const EdgeInsets.fromLTRB(24, 0, 24, 24),
          child: Column(
            mainAxisSize: MainAxisSize.min,
            children: [
              Text('Daily goal', style: Theme.of(ctx).textTheme.titleLarge),
              const SizedBox(height: 8),
              Text(
                formatVolume(ml, c.profile.volumeUnit),
                style: Theme.of(ctx).textTheme.displaySmall
                    ?.copyWith(color: NaglyColors.primaryDeep),
              ),
              Slider(
                value: ml.toDouble(),
                min: 1000,
                max: 5000,
                divisions: 80,
                onChanged: (v) => set(() => ml = v.round()),
              ),
              TextButton(
                onPressed: () => set(
                  () => ml = recommendedDailyMl(
                    c.profile.weightKg,
                    c.profile.activity,
                  ),
                ),
                child: Text(
                  'Use recommended (${formatVolume(recommendedDailyMl(c.profile.weightKg, c.profile.activity), c.profile.volumeUnit)})',
                ),
              ),
              FilledButton(
                onPressed: () {
                  Navigator.pop(ctx);
                  c.updateProfile((p) => p.copyWith(dailyMl: ml));
                },
                child: const Text('Save'),
              ),
            ],
          ),
        ),
      ),
    );
  }

  Future<void> _editHours(BuildContext context) async {
    final c = context.read<AppController>();
    var wake = c.profile.wakeHour, sleep = c.profile.sleepHour;
    await showModalBottomSheet<void>(
      context: context,
      builder: (ctx) => StatefulBuilder(
        builder: (ctx, set) {
          Widget row(String label, int h, ValueChanged<int> on) => ListTile(
            title: Text(
              label,
              style: const TextStyle(fontWeight: FontWeight.w900),
            ),
            trailing: Row(
              mainAxisSize: MainAxisSize.min,
              children: [
                IconButton(
                  tooltip: '$label earlier',
                  onPressed: () => set(() => on((h + 23) % 24)),
                  icon: const Icon(Icons.remove_circle_outline),
                ),
                SizedBox(
                  width: 70,
                  child: Text(
                    formatHourLabel(h),
                    textAlign: TextAlign.center,
                    style: const TextStyle(
                      fontSize: 17,
                      fontWeight: FontWeight.w900,
                    ),
                  ),
                ),
                IconButton(
                  tooltip: '$label later',
                  onPressed: () => set(() => on((h + 1) % 24)),
                  icon: const Icon(Icons.add_circle_outline),
                ),
              ],
            ),
          );
          return Padding(
            padding: const EdgeInsets.fromLTRB(16, 0, 16, 24),
            child: Column(
              mainAxisSize: MainAxisSize.min,
              children: [
                Text('Awake hours', style: Theme.of(ctx).textTheme.titleLarge),
                row('☀️ Wake', wake, (v) => wake = v),
                row('🌙 Sleep', sleep, (v) => sleep = v),
                const SizedBox(height: 12),
                FilledButton(
                  onPressed: wake == sleep
                      ? null
                      : () {
                          Navigator.pop(ctx);
                          c.updateProfile(
                            (p) => p.copyWith(wakeHour: wake, sleepHour: sleep),
                          );
                        },
                  child: const Text('Save'),
                ),
              ],
            ),
          );
        },
      ),
    );
  }
}

class _Group extends StatelessWidget {
  const _Group({required this.children});
  final List<Widget> children;

  @override
  Widget build(BuildContext context) => NCard(
    padding: EdgeInsets.zero,
    child: Column(
      children: [
        for (final (i, w) in children.indexed) ...[
          if (i > 0) const Divider(indent: 56),
          w,
        ],
      ],
    ),
  );
}

class _Row extends StatelessWidget {
  const _Row({
    required this.emoji,
    required this.title,
    this.value,
    this.trailing,
    this.onTap,
  });
  final String emoji, title;
  final String? value;
  final Widget? trailing;
  final VoidCallback? onTap;

  @override
  Widget build(BuildContext context) => ListTile(
    onTap: onTap,
    minTileHeight: 56,
    leading: Text(emoji, style: const TextStyle(fontSize: 20)),
    title: Text(title, style: const TextStyle(fontWeight: FontWeight.w800)),
    trailing:
        trailing ??
        Row(
          mainAxisSize: MainAxisSize.min,
          children: [
            if (value != null)
              Text(
                value!,
                style: const TextStyle(
                  fontWeight: FontWeight.w800,
                  color: NaglyColors.textSecondary,
                ),
              ),
            if (onTap != null)
              const Icon(
                Icons.chevron_right_rounded,
                color: NaglyColors.textSecondary,
              ),
          ],
        ),
  );
}
