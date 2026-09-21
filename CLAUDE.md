# Nagly — instructions for Claude

**Start every session by reading `CLAUDE_RESUME.md`** (current state + pending list) and `docs/claude-memory/` if present.

- Active code: Flutter app in `app/` on branch `flutter` (the Kotlin `androidApp/`, `shared/`, `iosApp/` folders are the old version — don't edit them).
- Before shipping: `cd app && flutter test`, then `flutter build appbundle --release`. Bump `version:` in `app/pubspec.yaml` (build number must increase every upload).
- Keys: only public IDs live in `app/lib/config/integrations.dart`. Never commit `app/android/key.properties`, keystores, service-account JSON or secret API keys.
- The owner (Manoj) prefers Claude to do the work end-to-end, asking only when needed. Ask first before: accepting terms, submitting for review/publishing, setting OneSignal Journeys live, pushing to GitHub.
- In the Work Chrome profile, only touch the Nagly app in Play Console.
- Deadline: Shipaton submission by **Sep 30, 2026, 11:45 pm PDT** (see SHIPATON.md).
