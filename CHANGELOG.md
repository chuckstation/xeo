# Changelog

All notable changes to **Xeo** are documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.1.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [0.5.1] — 2026-08-04

First stable, production-ready release of the Xeo fork. There are no beta,
alpha, or canary phases in the Xeo release line — every tag on `main` is a
shippable release.

### Project identity

- Forked from `xenia-android/xenia-android` and rebranded as **Xeo** under
  `adars-org/xeo`.
- Application ID and Java package both set to `org.adars.xeo`. Native JNI
  symbols follow `Java_org_adars_xeo_*` and are compiled into `libxeo.so`.
- Native library renamed from `libxenia-android.so` to `libxeo.so`.
- Java class renames: `XeniaRuntimeException` → `XeoRuntimeException`,
  `XeniaPreferenceFragment` → `XeoPreferenceFragment`.
- App label, theme name, color resources, log tags, and CI artifact names
  all updated to the Xeo identity.
- `rootProject.name` changed from `XeniaAndroid` to `Xeo`.
- `patches/kernel/user_module.cc` now searches for game patches under both
  the new `Xeo/patches` and `Android/data/org.adars.xeo/patches` paths and
  the legacy `Xenia/...` and `Android/data/com.xenia.android/...` paths,
  so existing user patch directories keep working.

### Versioning

- `versionName`: `0.5.0-beta` → `0.5.1` (stable).
- `versionCode`: `3` → `4`.
- `gradle.properties` now exposes `xeo.version`, `xeo.versionCode`, and
  `xeo.applicationId` for consumption by build scripts and CI.

### Production readiness

- `compileSdk`/`targetSdk` raised from 34 to 35 (Android 15).
- Android Gradle Plugin bumped from 8.2.2 to 8.5.2 (required for SDK 35).
- Gradle wrapper bumped from 8.4 to 8.9.
- Dependency refresh to current AndroidX releases:
  - `androidx.appcompat:appcompat` 1.6.1 → 1.7.0
  - `com.google.android.material:material` 1.11.0 → 1.12.0
  - `androidx.constraintlayout:constraintlayout` 2.1.4 → 2.2.0
  - `androidx.recyclerview:recyclerview` 1.3.2 → 1.3.4
  - `androidx.documentfile:documentfile` 1.0.1 → 1.1.0
  - `androidx.test.ext:junit` 1.1.5 → 1.2.1
  - `androidx.test.espresso:espresso-core` 3.5.1 → 3.6.1
- Release signing is documented in `README.md` as a manual edit of the
  `signingConfig` line in `app/build.gradle`. CI builds are signed with
  the debug key by default.

### Code quality fixes

- Removed the orphaned `Java_jp_xenia_emulator_WindowedAppActivity_*` JNI
  exports from `patches/ui/windowed_app_context_android.cc`. These functions
  referenced a `jp.xenia.emulator` Java package that does not exist in this
  codebase — the real JNI bindings live in
  `app/src/main/cpp/jni/windowed_app_jni.cc` under the
  `Java_org_adars_xeo_*` symbol prefix. The dead exports were bloating
  `libxeo.so` and confusing maintainers.
- Aligned the `EXTRA_CVARS` intent-extra key between Java and native code.
  Both sides now use `org.adars.xeo.WindowedAppActivity.EXTRA_CVARS`. The
  previous `jp.xenia.emulator.*` literal was a historical leftover.
- Extracted all hardcoded user-facing strings from `EmulatorActivity.java`
  and `WindowedAppActivity.java` into `res/values/strings.xml` with proper
  formatting placeholders (`%1$d`, `%1$s`). The savestate menu, toast
  messages, and fatal-error dialog are now translatable.

### Documentation

- Rewrote `README.md` for the Xeo identity with build, signing, and layout
  sections.
- Added `NOTICE.md` with upstream and third-party attributions.
- Added `CONTRIBUTING.md` describing the contribution workflow.

### Removed

- "Beta (0.5.0-beta)" status banner from `README.md`.
- `beta` / `alpha` / `canary` markers from user-visible project metadata.
  (Internal references to shader alpha channels and `canary_experimental`
  upstream branch names are preserved — these are unrelated to release phase.)

## [0.5.0-beta] — historical baseline

The last release of `xenia-android/xenia-android` before the Xeo fork.
Not shipped under the Xeo name.
