# Changelog

All notable changes to **Xeo** are documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.1.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [0.5.1] — 2026-08-04

First stable, production-ready release of Xeo. There are no beta, alpha, or
canary phases in the Xeo release line — every tag on `main` is a shippable
release.

### Project identity

- Application ID and Java package both set to `org.adars.xeo`. Native JNI
  symbols follow `Java_org_adars_xeo_*` and are compiled into `libxeo.so`.
- App label "Xeo", theme `Theme.Xeo`, color resources `xeo_green` /
  `xeo_accent`, log tag `XeoJNI`, CI artifact prefix `xeo-`.
- `rootProject.name` set to `Xeo`.
- Game-patch search paths in `patches/kernel/user_module.cc` use the
  `Xeo/patches` and `Android/data/org.adars.xeo/patches` directories.

### Versioning

- `versionName`: `0.5.1` (stable).
- `versionCode`: `4`.
- `gradle.properties` exposes `xeo.version`, `xeo.versionCode`, and
  `xeo.applicationId` for consumption by build scripts and CI.

### Production readiness

- `compileSdk`/`targetSdk` set to 35 (Android 15).
- Android Gradle Plugin 8.5.2 (required for SDK 35).
- Gradle wrapper 8.9.
- Current AndroidX releases:
  - `androidx.appcompat:appcompat` 1.7.0
  - `com.google.android.material:material` 1.12.0
  - `androidx.constraintlayout:constraintlayout` 2.2.0
  - `androidx.recyclerview:recyclerview` 1.3.4
  - `androidx.documentfile:documentfile` 1.1.0
  - `androidx.test.ext:junit` 1.2.1
  - `androidx.test.espresso:espresso-core` 3.6.1
- Release builds are signed with the debug key for CI compatibility.

### Code quality

- `EXTRA_CVARS` intent-extra key aligned between Java and native code:
  both sides use `org.adars.xeo.WindowedAppActivity.EXTRA_CVARS`.
- All hardcoded user-facing strings extracted from `EmulatorActivity.java`
  and `WindowedAppActivity.java` into `res/values/strings.xml` with proper
  formatting placeholders (`%1$d`, `%1$s`). The savestate menu, toast
  messages, and fatal-error dialog are now translatable.
- Orphaned JNI exports removed from `patches/ui/windowed_app_context_android.cc`.

### Documentation

- `README.md` with build, project-layout, and upstream-relationship
  sections.
- `NOTICE.md` with upstream and third-party attributions.
- `CONTRIBUTING.md` describing the contribution workflow.
