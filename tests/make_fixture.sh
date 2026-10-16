#!/usr/bin/env bash
set -euo pipefail
cd "$(dirname "$0")"
AAPT2="${1:-aapt2}"
ANDROID_JAR="${2:-../tools/android.jar}"
TMP="$(mktemp -d)"
trap 'rm -rf "$TMP"' EXIT
"$AAPT2" compile --dir fixture/res -o "$TMP/res.zip"
"$AAPT2" link -o data/fixture.apk -I "$ANDROID_JAR" \
    --manifest fixture/AndroidManifest.xml "$TMP/res.zip"
echo "tests/data/fixture.apk rebuilt"
