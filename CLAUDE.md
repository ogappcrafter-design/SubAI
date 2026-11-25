# SubAI — project notes for Claude

Native Android app (Kotlin + Jetpack Compose) that helps wire car-audio subs,
amps, LOCs, second batteries and aftermarket head units, with AI photo diagnosis.

## ‼️ DELIVERY RULE — how to hand the user every build / download

**Always deliver APKs (and any file the user downloads) by uploading to the
user's Google Drive, then give them the Drive link.** Do NOT rely on the in-chat
file card for APKs — on their phone (Motorola, Android 16, ThinkShield) that path
caused "App not installed" / "problem parsing the package" failures. The Drive
link is the method that works (same as their "Rhythm Riot" app).

How to do it:
1. Build the **release** APK (small ~2.6MB, signed): `./gradlew :app:assembleRelease`
2. `sha256sum app/build/outputs/apk/release/app-release.apk`
3. Google Drive connector `create_file`:
   - `title: "SubAI.apk"`
   - `contentMimeType: "application/vnd.android.package-archive"`
   - `disableConversionToGoogleType: true`
   - `base64Content: "sha256:<hash>:/absolute/path/to/app-release.apk"`
4. Give the user the returned `viewUrl`. It lands in THEIR Drive (their account),
   so no sharing step is needed — they open the Google Drive app and install.

Then remind them: if install still fails, it's **Play Protect** — Play Store →
profile → Play Protect → gear → turn OFF "Scan apps with Play Protect", install,
turn back on.

## Build environment (cloud sessions)

- The Android SDK is NOT preinstalled and `dl.google.com` is blocked on the
  Default environment. This repo has a dedicated **SubAI** cloud environment that
  allows `dl.google.com`. Install SDK if missing:
  cmdline-tools → `sdkmanager "platform-tools" "platforms;android-35" "build-tools;35.0.0"`,
  set `ANDROID_HOME=/home/user/android-sdk`, write `local.properties` with `sdk.dir`.
- Maven Central (`repo.maven.apache.org`) often returns **429** through the shared
  proxy. Builds retry-loop with backoff; re-running resumes from cache.
- Build: `./gradlew :app:assembleRelease` (or `assembleDebug`). Tests:
  `./gradlew :app:testDebugUnitTest` (pure-Kotlin engine, 23 tests).

## App facts

- Package id: `com.ogapp.subai` (no `.debug` suffix — keeps one signature so
  sideload updates don't clash). minSdk 24, targetSdk/compileSdk 35.
- Signed with the committed dev keystore `app/subai-debug.keystore`
  (store/key pass `subai123`, alias `subai`). This is a NON-SECRET sideload key;
  a separate release key is needed for an actual Play Store upload.
- The wiring **math is deterministic and unit-tested — never let AI compute it**
  (fuse/gauge/impedance errors can cause fires). AI is only for photo diagnosis.
- AI photo diagnosis is bring-your-own-key (Gemini / Claude / OpenAI), stored
  encrypted via `SecurePrefs`. Model ids are constants in `AiDiagnostician.kt`.

## Branch

Develop on `claude/subwoofer-amp-wiring-assistant-djukf7`. Don't open a PR unless asked.
