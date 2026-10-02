#!/bin/bash
# Builds the iOS app. Nagly has no ad SDK on any platform, so this is now a plain
# flutter call, kept so existing commands keep working.
# Usage: tool/build_ios.sh build ipa --release    |    tool/build_ios.sh run -d <sim>
set -euo pipefail
cd "$(dirname "$0")/.."
flutter pub get
flutter "$@"
