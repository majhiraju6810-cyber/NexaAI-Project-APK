#!/data/data/com.termux/files/usr/bin/bash
# One-time Termux setup + build for NEXA AI.
# STATUS: written but NOT executed by the author (no phone/Termux was available).
# If a step fails, the cloud route in docs/BUILD_TERMUX.md (GitHub Actions) is the fallback.
set -e
cd "$(dirname "$0")/.."            # project root

pkg update -y
pkg install -y openjdk-17 aapt2 curl unzip git zip
export JAVA_HOME="$PREFIX/lib/jvm/java-17-openjdk"

SDK="$HOME/android-sdk"
if [ ! -d "$SDK/cmdline-tools/latest" ]; then
  mkdir -p "$SDK/cmdline-tools"
  curl -L -o "$TMPDIR/cmdtools.zip" https://dl.google.com/android/repository/commandlinetools-linux-11076708_latest.zip
  unzip -q "$TMPDIR/cmdtools.zip" -d "$SDK/cmdline-tools"
  mv "$SDK/cmdline-tools/cmdline-tools" "$SDK/cmdline-tools/latest"
fi
yes | "$SDK/cmdline-tools/latest/bin/sdkmanager" --sdk_root="$SDK" --licenses > /dev/null || true
"$SDK/cmdline-tools/latest/bin/sdkmanager" --sdk_root="$SDK" "platforms;android-34" "build-tools;34.0.0"
echo "sdk.dir=$SDK" > local.properties

# AGP's downloaded aapt2 is x86-64 and cannot run on a phone: use Termux's own.
sed -i 's|^#android.aapt2FromMavenOverride=|android.aapt2FromMavenOverride=|' gradle.properties

# Gradle 8.9 (the version this project is pinned to)
if [ ! -x "$HOME/gradle-8.9/bin/gradle" ]; then
  curl -L -o "$TMPDIR/gradle.zip" https://services.gradle.org/distributions/gradle-8.9-bin.zip
  unzip -q "$TMPDIR/gradle.zip" -d "$HOME"
fi

"$HOME/gradle-8.9/bin/gradle" --no-daemon :app:testDebugUnitTest :app:assembleDebug
echo
echo "APK: app/build/outputs/apk/debug/app-debug.apk"
echo "Install:  cp app/build/outputs/apk/debug/app-debug.apk ~/storage/downloads/   (then tap it in Files)"
