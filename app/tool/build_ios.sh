#!/bin/bash
# Builds the iOS app with NO ad SDK: swaps google_mobile_ads for the empty stand-in
# in tool/ios_no_ads, runs the given flutter command, then restores the real package.
# Usage: tool/build_ios.sh build ipa --release    |    tool/build_ios.sh run -d <sim>
set -euo pipefail
cd "$(dirname "$0")/.."
cat > pubspec_overrides.yaml <<'YAML'
dependency_overrides:
  google_mobile_ads:
    path: tool/ios_no_ads/google_mobile_ads
YAML
trap 'rm -f pubspec_overrides.yaml; flutter pub get >/dev/null' EXIT
flutter pub get
flutter "$@"
