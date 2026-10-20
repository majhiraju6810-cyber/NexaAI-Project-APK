# Building NEXA AI

**Honest status:** this project was written in an environment that has no Android SDK, Gradle,
Kotlin compiler or internet. It has **never been compiled or run**. Expect to fix a few
compile errors on the first build; send me the first error text and I will fix it.

## Option A: GitHub Actions (most reliable from a phone)
1. In Termux: `pkg install git`, create an empty GitHub repo, then
   `cd NexaAI && git init && git add . && git commit -m "NEXA AI" && git branch -M main`
   `git remote add origin <your repo url> && git push -u origin main`
2. GitHub > Actions > "Build debug APK" runs automatically (`.github/workflows/build-apk.yml`).
   It runs the unit tests and builds the APK on a PC-class machine.
3. Download the artifact `nexa-ai-debug-apk`, unzip, install `app-debug.apk`.

## Option B: build on the phone in Termux (works only if every step cooperates)
```
pkg install unzip
unzip NexaAI-project.zip -d ~ && cd ~/NexaAI
bash tools/termux_setup.sh
```
It installs JDK 17 + Termux's `aapt2`, downloads the Android command-line tools,
`platforms;android-34`, `build-tools;34.0.0` and Gradle 8.9, then runs
`gradle :app:testDebugUnitTest :app:assembleDebug`. Needs ~3 GB free and a good connection.

Known Termux trap: if you see `failed to load include path ... android.jar` or an aapt2
version error, Termux's `aapt2` is older than platform 34. Use Option A instead.

## Option C: Android Studio on a PC
Open the `NexaAI` folder, let it sync, press Run. (JDK 17, Android SDK 34.)

## Requirements this project asks for
JDK 17, Gradle 8.9, AGP 8.5.2, Kotlin 2.0.20, compileSdk/targetSdk 34, minSdk 26,
Compose BOM 2024.06.00. No permissions. No network access.

## Regenerating assets (optional, needs a PC with `pip install pillow numpy scipy`)
`python3 tools/prepare_assets.py` (logo variants + launcher icons from the original logo)
`python3 tools/make_sound.py` (the 3 s opening sound)
