# Xeo

**Xeo** is a production-ready Android port of [Xenia Canary](https://github.com/xenia-canary/xenia-canary), the open-source Xbox 360 emulator.

## Requirements

- Android 7.0+ (API 24), Vulkan 1.0
- ARM64 or x86_64 device (32-bit ABIs are unsupported — Xenia's JIT requires a 64-bit address space)
- Game files: `.iso`, `.xex`, `.stfs`, `.zar` — **legal dumps only**

## Building

```bash
git clone https://github.com/ChuckStation/xeo.git
cd xeo
git submodule update --init --depth 1 xenia-upstream
bash setup.sh
./gradlew assembleRelease
```

**Tools required:** JDK 17, Android NDK r26 (`26.1.10909125`), CMake 3.22, Gradle 8.9 (bundled via the wrapper).

The resulting APK is written to `app/build/outputs/apk/release/app-release.apk`.

## Project layout

```
xeo/
├── app/                       # Android application module (Gradle)
│   ├── build.gradle           # Version 0.5.2, applicationId org.adars.xeo
│   ├── proguard-rules.pro     # Keeps JNI bridge symbols
│   └── src/main/
│       ├── AndroidManifest.xml
│       ├── java/org/adars/xeo/            # Java sources (package org.adars.xeo)
│       ├── cpp/                # CMakeLists.txt → libxeo.so
│       │   ├── CMakeLists.txt
│       │   ├── jni/            # JNI bridge → Java_org_adars_xeo_*
│       │   └── custom_include/ # Build-time stubs
│       └── res/                # Strings, themes, colors, layouts
├── patches/                    # C++ sources layered on top of xenia-upstream
├── xenia-upstream/             # Git submodule → xenia-canary/xenia-canary
├── setup.sh                    # Symlinks src/ and third_party/ from upstream
├── .github/workflows/ci.yml    # Xeo Android CI
└── gradle/                     # Wrapper, properties
```

## Relationship to upstream

Xeo pulls in `xenia-canary/xenia-canary` (branch `canary_experimental`) as a git
submodule at `xenia-upstream/`. The `patches/` directory contains Android-specific
overrides that are layered on top of the upstream tree by `setup.sh` before the
native build runs. Xeo never modifies the submodule in place — every Android
adaptation lives in this repository.

The Java package is `org.adars.xeo` and matches the user-facing Application ID
set in `app/build.gradle`. Native JNI symbols follow the `Java_org_adars_xeo_*`
prefix and are compiled into `libxeo.so`.

## License

BSD 3-Clause. See [LICENSE](LICENSE).
Xenia Canary upstream: [BSD 3-Clause](https://github.com/xenia-canary/xenia-canary/blob/canary_experimental/LICENSE).

## Acknowledgements

See [NOTICE.md](NOTICE.md) for the full list of upstream projects and
third-party libraries vendored into `libxeo.so`.
