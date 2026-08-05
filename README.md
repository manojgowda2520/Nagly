# Nagly

Kotlin Multiplatform hydration app (Android + iOS) with shared Compose Multiplatform UI.

## Modules

- `shared` — commonMain Compose UI + domain (target: maximize shared code)
- `androidApp` — Android entry point
- `iosApp` — iOS entry point hosting Compose via `MainViewController`

## Build

```bash
# Android
./gradlew :androidApp:assembleDebug

# iOS framework (used by Xcode)
./gradlew :shared:linkDebugFrameworkIosSimulatorArm64
```

Open `iosApp/iosApp.xcodeproj` in Xcode to run on simulator/device.
