# xenia-android

Android port of [Xenia Canary](https://github.com/xenia-canary/xenia-canary), an Xbox 360 emulator.

**Status:** Beta (0.5.0-beta). Requires Vulkan 1.0 (Android 7.0+, ARM64 or x86_64).

[![Android CI](https://github.com/xenia-android/xenia-android/actions/workflows/ci.yml/badge.svg)](https://github.com/xenia-android/xenia-android/actions/workflows/ci.yml)

## Requirements

- Android 7.0+ (API 24), Vulkan 1.0
- ARM64 or x86_64 device
- Game files: `.iso`, `.xex`, `.stfs` — legal dumps only

## Building

```bash
git clone https://github.com/xenia-android/xenia-android.git
cd xenia-android
git submodule update --init --depth 1 xenia-upstream
bash setup.sh
./gradlew assembleDebug
```

**Tools required:** JDK 17, Android NDK r26, CMake 3.22, Gradle 8.4


## License

BSD 3-Clause. See [LICENSE](LICENSE).  
Xenia Canary upstream: [BSD 3-Clause](https://github.com/xenia-canary/xenia-canary/blob/canary_experimental/LICENSE).
