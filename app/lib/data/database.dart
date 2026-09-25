import 'dart:convert';

import 'package:path/path.dart' as p;
import 'package:sqflite/sqflite.dart';

import '../domain/models.dart';

/// Local-only storage. No accounts, no backend — works offline by design.
class NaglyDatabase {
  NaglyDatabase._(this.db);

  final Database db;

  static const _version = 1;

  static Future<NaglyDatabase> open({String? path}) async {
    final dbPath = path ?? p.join(await getDatabasesPath(), 'nagly.db');
    final db = await openDatabase(
      dbPath,
      version: _version,
      onCreate: (db, _) async {
        final batch = db.batch()
          ..execute('''
            CREATE TABLE drink_log (
              id INTEGER PRIMARY KEY AUTOINCREMENT,
              timestamp_ms INTEGER NOT NULL,
              amount_ml INTEGER NOT NULL
            )''')
          ..execute('CREATE INDEX drink_log_ts ON drink_log(timestamp_ms)')
          ..execute('''
            CREATE TABLE medication (
              id INTEGER PRIMARY KEY AUTOINCREMENT,
              name TEXT NOT NULL,
              dose TEXT NOT NULL DEFAULT '',
              hour INTEGER NOT NULL,
              minute INTEGER NOT NULL,
              active INTEGER NOT NULL DEFAULT 1
            )''')
          ..execute('''
            CREATE TABLE med_log (
              id INTEGER PRIMARY KEY AUTOINCREMENT,
              med_id INTEGER NOT NULL,
              date_key TEXT NOT NULL,
              status TEXT NOT NULL,
              at_ms INTEGER NOT NULL,
              UNIQUE(med_id, date_key)
            )''')
          ..execute('''
            CREATE TABLE unlock (
              key TEXT PRIMARY KEY,
              expires_at_ms INTEGER NOT NULL
            )''')
          ..execute('''
            CREATE TABLE kv (
              key TEXT PRIMARY KEY,
              value TEXT NOT NULL
            )''');
        await batch.commit(noResult: true);
      },
    );
    return NaglyDatabase._(db);
  }

  Future<void> close() => db.close();

  // ── Key/value ─────────────────────────────────────────────
  Future<String?> getString(String key) async {
    final rows = await db.query(
      'kv',
      where: 'key = ?',
      whereArgs: [key],
      limit: 1,
    );
    return rows.isEmpty ? null : rows.first['value'] as String;
  }

  Future<void> setString(String key, String value) => db.insert('kv', {
    'key': key,
    'value': value,
  }, conflictAlgorithm: ConflictAlgorithm.replace);

  Future<void> remove(String key) =>
      db.delete('kv', where: 'key = ?', whereArgs: [key]);

  Future<int?> getInt(String key) async =>
      int.tryParse(await getString(key) ?? '');

  Future<void> setInt(String key, int value) => setString(key, '$value');

  Future<bool> getBool(String key, {bool fallback = false}) async {
    final v = await getString(key);
    return v == null ? fallback : v == '1';
  }

  Future<void> setBool(String key, bool value) =>
      setString(key, value ? '1' : '0');

  // ── Profile ───────────────────────────────────────────────
  Future<Profile> profile() async {
    final raw = await getString(Keys.profile);
    if (raw == null) return const Profile();
    return Profile.fromJson((jsonDecode(raw) as Map).cast<String, Object?>());
  }

  Future<void> saveProfile(Profile profile) =>
      setString(Keys.profile, jsonEncode(profile.toJson()));

  // ── Drinks ────────────────────────────────────────────────
  Future<int> addDrink(int amountMl, {int? atMs}) => db.insert('drink_log', {
    'timestamp_ms': atMs ?? DateTime.now().millisecondsSinceEpoch,
    'amount_ml': amountMl,
  });

  Future<List<DrinkLog>> drinksBetween(int fromMs, int toMs) async {
    final rows = await db.query(
      'drink_log',
      where: 'timestamp_ms >= ? AND timestamp_ms < ?',
      whereArgs: [fromMs, toMs],
      orderBy: 'timestamp_ms ASC, id ASC',
    );
    return [
      for (final r in rows)
        DrinkLog(
          id: r['id'] as int,
          timestampMs: r['timestamp_ms'] as int,
          amountMl: r['amount_ml'] as int,
        ),
    ];
  }

  Future<void> deleteDrink(int id) =>
      db.delete('drink_log', where: 'id = ?', whereArgs: [id]);

  // ── Medications ───────────────────────────────────────────
  Future<List<Medication>> medications() async {
    final rows = await db.query(
      'medication',
      where: 'active = 1',
      orderBy: 'hour, minute, id',
    );
    return [
      for (final r in rows)
        Medication(
          id: r['id'] as int,
          name: r['name'] as String,
          dose: r['dose'] as String,
          hour: r['hour'] as int,
          minute: r['minute'] as int,
        ),
    ];
  }

  Future<int> addMedication(
    String name,
    int hour,
    int minute, {
    String dose = '',
  }) => db.insert('medication', {
    'name': name,
    'dose': dose,
    'hour': hour,
    'minute': minute,
  });

  Future<void> updateMedication(Medication m) => db.update(
    'medication',
    {'name': m.name, 'dose': m.dose, 'hour': m.hour, 'minute': m.minute},
    where: 'id = ?',
    whereArgs: [m.id],
  );

  /// Soft-delete so history keeps the medication's name.
  Future<void> removeMedication(int id) =>
      db.update('medication', {'active': 0}, where: 'id = ?', whereArgs: [id]);

  Future<Map<int, Medication>> allMedicationsById() async {
    final rows = await db.query('medication');
    return {
      for (final r in rows)
        r['id'] as int: Medication(
          id: r['id'] as int,
          name: r['name'] as String,
          dose: r['dose'] as String,
          hour: r['hour'] as int,
          minute: r['minute'] as int,
        ),
    };
  }

  Future<void> logMed(
    int medId,
    String dateKey,
    MedStatus status, {
    int? atMs,
  }) => db.insert('med_log', {
    'med_id': medId,
    'date_key': dateKey,
    'status': status.name,
    'at_ms': atMs ?? DateTime.now().millisecondsSinceEpoch,
  }, conflictAlgorithm: ConflictAlgorithm.replace);

  Future<void> clearMedLog(int medId, String dateKey) => db.delete(
    'med_log',
    where: 'med_id = ? AND date_key = ?',
    whereArgs: [medId, dateKey],
  );

  Future<List<MedLog>> medLogsSince(String fromDateKey) async {
    final rows = await db.query(
      'med_log',
      where: 'date_key >= ?',
      whereArgs: [fromDateKey],
      orderBy: 'at_ms',
    );
    return [
      for (final r in rows)
        MedLog(
          id: r['id'] as int,
          medId: r['med_id'] as int,
          dateKey: r['date_key'] as String,
          status: MedStatus.values.byName(r['status'] as String),
          atMs: r['at_ms'] as int,
        ),
    ];
  }

  // ── Unlocks ───────────────────────────────────────────────
  Future<Map<String, int>> activeUnlocks(int nowMs) async {
    final rows = await db.query(
      'unlock',
      where: 'expires_at_ms > ?',
      whereArgs: [nowMs],
    );
    return {
      for (final r in rows) r['key'] as String: r['expires_at_ms'] as int,
    };
  }

  Future<void> grantUnlock(String key, int expiresAtMs) => db.insert('unlock', {
    'key': key,
    'expires_at_ms': expiresAtMs,
  }, conflictAlgorithm: ConflictAlgorithm.replace);

  // ── Nudge history (for "ignored" counting) ────────────────
  Future<List<int>> nudgeHistory() async {
    final raw = await getString(Keys.nudgeHistory);
    if (raw == null) return [];
    return (jsonDecode(raw) as List).cast<int>();
  }

  Future<void> setNudgeHistory(List<int> times) =>
      setString(Keys.nudgeHistory, jsonEncode(times));
}

abstract final class Keys {
  static const profile = 'profile';
  static const isPro = 'is_pro';
  static const trialEndsAt = 'trial_ends_at';
  static const notificationsEnabled = 'notifications_enabled';
  static const nudgeHistory = 'nudge_history';
  static const trialSheetShownOn = 'trial_sheet_shown_on';
  static const trialEndedShown = 'trial_ended_shown';
  static const upsellShownAt = 'upsell_shown_at';
  static const installId = 'install_id';
  static const tourSeen = 'tour_seen';
}
