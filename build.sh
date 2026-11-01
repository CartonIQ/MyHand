#!/data/data/com.termux/files/usr/bin/bash
# MyHand build script for Termux.  Usage:  bash build.sh
set -e
cd "$(dirname "$0")"

if [ -z "$ANDROID_HOME" ]; then
  echo "ANDROID_HOME is not set. Example: export ANDROID_HOME=\$HOME/qrapp/android-sdk"
  exit 1
fi
echo "sdk.dir=$ANDROID_HOME" > local.properties

# Use the ARM aapt2 from Termux (the SDK one is x86 and crashes on phones)
AAPT2="$(command -v aapt2 || true)"
if [ -z "$AAPT2" ]; then echo "aapt2 not found. Run: pkg install aapt2"; exit 1; fi
grep -v "aapt2FromMavenOverride" gradle.properties > gradle.properties.tmp || true
echo "android.aapt2FromMavenOverride=$AAPT2" >> gradle.properties.tmp
mv gradle.properties.tmp gradle.properties

# Use Gradle 8.9 (newer Gradle 9 can break the Android plugin used here)
G="$HOME/gradle-8.9/bin/gradle"
if [ ! -x "$G" ]; then
  echo "Downloading Gradle 8.9 ..."
  curl -L -o "$HOME/gradle-8.9-bin.zip" https://services.gradle.org/distributions/gradle-8.9-bin.zip
  unzip -q -o "$HOME/gradle-8.9-bin.zip" -d "$HOME"
  rm -f "$HOME/gradle-8.9-bin.zip"
fi

termux-wake-lock 2>/dev/null || true
"$G" assembleDebug --no-daemon

mkdir -p "$HOME/storage/downloads"
cp app/build/outputs/apk/debug/app-debug.apk "$HOME/storage/downloads/MyHand.apk"
echo ""
echo "DONE. Install: Downloads/MyHand.apk"
