#!/usr/bin/env bash
set -euo pipefail
cd "$(dirname "$0")"

usage() {
  cat <<'EOF'
soufiT: build the mod from an official TikTok apk, sign it, and check it.

  ./build.sh build path/to/tiktok.apk      build, then sign with the debug key
  ./build.sh build tiktok.apk --unsigned   build only: build/soufit-unsigned.apk
  ./build.sh build-sign tiktok.apk --keystore release.p12
                                           build and sign; only build/soufit.apk is kept
  ./build.sh sign build/soufit-unsigned.apk --keystore release.p12
  ./build.sh verify build/soufit.apk       check that an apk is signed
  ./build.sh tools hashes                  print the SHA-256 of each cached tool

The TikTok apk is not in this repository: bring a universal one (type APK, not
BUNDLE), arm64-v8a, nodpi. Output goes to build/. Each command takes -h.

A keystore's password comes from SOUFIT_KEYSTORE_PASSWORD, from --password-file,
--keystore-pass (visible to other users, so avoid it on shared machines), or by asking. A separate key password comes from SOUFIT_KEY_PASSWORD and defaults
to the keystore password.

smali, d8, android.jar and the signer are downloaded into tools/ the first time
and reused after that. You need a JDK (java and keytool) and Python 3.7 or newer.
EOF
}

case "${1:-}" in
  "" | -h | --help | help)
    usage
    exit 0
    ;;
esac

if ! command -v python3 >/dev/null 2>&1; then
  echo "python3 is needed (3.7 or newer)" >&2
  exit 1
fi
if ! python3 -c 'import sys; sys.exit(sys.version_info < (3, 7))'; then
  echo "python3 is too old: 3.7 or newer is needed" >&2
  exit 1
fi

case "$1" in
  verify | tools) ;;
  *)
    if ! command -v java >/dev/null 2>&1; then
      echo "a JDK is needed (java and keytool): install one and run this again" >&2
      exit 1
    fi
    ;;
esac

exec python3 -m soufit "$@"
