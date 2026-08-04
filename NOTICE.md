# Xeo — Third-Party Notices

Xeo is an Android port of [Xenia Canary](https://github.com/xenia-canary/xenia-canary),
the open-source Xbox 360 emulator. The full upstream license text is reproduced
in [LICENSE](LICENSE) (BSD 3-Clause).

## Upstream projects

| Project                                | License         | Used for                                  |
| -------------------------------------- | --------------- | ----------------------------------------- |
| xenia-canary/xenia-canary              | BSD 3-Clause    | Xbox 360 emulator core (submodule)        |

## Third-party libraries vendored via `xenia-upstream/third_party/`

The native `libxeo.so` is statically linked against the following third-party
libraries. Each subdirectory under `xenia-upstream/third_party/` contains the
upstream source and its own license file.

| Library                | License              | Purpose                                  |
| ---------------------- | -------------------- | ---------------------------------------- |
| capstone               | BSD 3-Clause         | Disassembler (x64 JIT trace output)      |
| cpptoml                | MIT                  | TOML config reader (legacy path)         |
| cxxopts                | MIT                  | Command-line option parser               |
| date                   | MIT                  | Howard Hinnant's date/time library       |
| dxbc                   | MIT                  | DXBC container reader                    |
| FFmpeg / ffmpeg-xenia  | LGPL 2.1+            | Audio/video decoding (stubbed on Android)|
| fmt                    | MIT                  | C++ formatting library                   |
| glslang                | BSD 3-Clause         | GLSL → SPIR-V frontend                   |
| half                   | MIT                  | IEEE 754 half-precision float type       |
| imgui                  | MIT                  | In-emulator overlay UI                   |
| LLVM (subset)          | Apache 2.0 + LLVM    | Header-only intrinsics                   |
| microprofile           | Unlicense            | Lightweight CPU/GPU profiler             |
| mspack                 | LGPL 2.1             | XEX LZX decompression                    |
| aes_128                | Public Domain        | AES-128 crypto (kernel)                  |
| pugixml                | MIT                  | XML parser (used by zarchive)            |
| rapidjson              | MIT + BSD            | JSON parser                              |
| SDL2                   | zlib                 | Stubbed out on Android                   |
| snappy                 | BSD 3-Clause         | Compression (GPU trace system)           |
| SPIRV-Tools            | Apache 2.0           | SPIR-V optimiser/validation              |
| stb                    | Public Domain        | Image loading helpers                    |
| tomliplus              | MIT                  | TOML config reader (modern path)         |
| utfcpp                 | Boost 1.0            | UTF-8 / UTF-16 conversion                |
| Vulkan-Headers         | Apache 2.0           | Vulkan API headers                       |
| VulkanMemoryAllocator  | MIT                  | Vulkan memory allocator                  |
| xbyak                  | BSD 3-Clause         | x86 JIT assembler (x64 backend)          |
| xbyak_aarch64          | BSD 3-Clause         | ARM64 JIT assembler (arm64 backend)      |
| xxhash                 | BSD 2-Clause         | Fast non-cryptographic hash              |
| zarchive               | BSD 3-Clause         | Zarchive reader                          |
| zlib-ng                | zlib                 | Compression (vendored header)            |
| zstd                   | BSD 3-Clause + GPLv2 | Zstandard compression                    |

## AndroidX & Material libraries (APK dependencies)

These libraries are linked dynamically from Google's Maven repository and
ship inside the APK as separate DEX entries / resources.

| Library                                   | License          |
| ----------------------------------------- | ---------------- |
| androidx.appcompat:appcompat              | Apache 2.0       |
| androidx.constraintlayout:constraintlayout | Apache 2.0       |
| androidx.documentfile:documentfile         | Apache 2.0       |
| androidx.preference:preference             | Apache 2.0       |
| androidx.recyclerview:recyclerview         | Apache 2.0       |
| com.google.android.material:material      | Apache 2.0       |

## Trademarks

Xbox, Xbox 360, and related marks are trademarks of Microsoft Corporation.
Xeo is not affiliated with or endorsed by Microsoft Corporation. Xeo is an
independent research project intended for use with legally-dumped game
software that you own.
