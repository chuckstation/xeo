cmake_minimum_required(VERSION 3.22.1)
project(xeo CXX C ASM)

set(CMAKE_CXX_STANDARD 20)
set(CMAKE_CXX_STANDARD_REQUIRED ON)
set(CMAKE_CXX_EXTENSIONS OFF)

# ---------------------------------------------------------------------------
# Xenia root — the upstream repo is expected as a submodule at the repo root.
# Override XENIA_ROOT if your layout differs.
# ---------------------------------------------------------------------------
if(NOT DEFINED XENIA_ROOT)
  # app/src/main/cpp → app/src/main → app → <repo root>
  get_filename_component(XENIA_ROOT "${CMAKE_CURRENT_SOURCE_DIR}/../../../.." ABSOLUTE)
endif()
set(XE_SRC "${XENIA_ROOT}/src")
set(XE_3P  "${XENIA_ROOT}/third_party")

message(STATUS "XENIA_ROOT : ${XENIA_ROOT}")
message(STATUS "XE_SRC     : ${XE_SRC}")
message(STATUS "XE_3P      : ${XE_3P}")

# Detect capabilities dynamically from upstream headers to support multiple versions of xenia-upstream
if(EXISTS "${XE_SRC}/xenia/base/threading.h")
  file(READ "${XE_SRC}/xenia/base/threading.h" THREADING_H_CONTENT)
  if(THREADING_H_CONTENT MATCHES "EventInfo")
    add_compile_definitions(XE_HAS_EVENT_INFO=1)
    message(STATUS "Detected EventInfo in threading.h, defining XE_HAS_EVENT_INFO=1")
  endif()
endif()

if(EXISTS "${XE_SRC}/xenia/base/logging.h")
  file(READ "${XE_SRC}/xenia/base/logging.h" LOGGING_H_CONTENT)
  if(LOGGING_H_CONTENT MATCHES "log_mask")
    add_compile_definitions(XE_LOGGING_HAS_LOG_MASK=1)
    message(STATUS "Detected log_mask in logging.h, defining XE_LOGGING_HAS_LOG_MASK=1")
  endif()
endif()

if(EXISTS "${XE_SRC}/xenia/kernel/util/gameinfo_utils.h")
  add_compile_definitions(XE_HAS_GAMEINFO_UTILS=1)
  message(STATUS "Detected gameinfo_utils.h, defining XE_HAS_GAMEINFO_UTILS=1")
endif()

if(EXISTS "${XE_SRC}/xenia/kernel/util/xdbf_utils.h")
  add_compile_definitions(XE_HAS_XDBF_UTILS=1)
  message(STATUS "Detected xdbf_utils.h, defining XE_HAS_XDBF_UTILS=1")
endif()

if(EXISTS "${XE_SRC}/xenia/vfs/devices/stfs_container_device.h")
  add_compile_definitions(XE_HAS_STFS_CONTAINER_DEVICE=1)
  message(STATUS "Detected stfs_container_device.h, defining XE_HAS_STFS_CONTAINER_DEVICE=1")
endif()

# ---------------------------------------------------------------------------
# Compiler flags
# ---------------------------------------------------------------------------
add_compile_options(
  -Wall -Wextra -Wno-unused-parameter -Wno-unused-variable
  -Wno-missing-field-initializers -Wno-sign-compare
  -fvisibility=hidden -ffunction-sections -fdata-sections
  -fexceptions -frtti
)
add_link_options(-Wl,--gc-sections)

# ---------------------------------------------------------------------------
# Global definitions & architecture detection
# ---------------------------------------------------------------------------
if(ANDROID_ABI STREQUAL "x86_64")
  set(XE_ARCH_AMD64_VAL 1)
  set(XE_ARCH_ARM64_VAL 0)
else()
  set(XE_ARCH_AMD64_VAL 0)
  set(XE_ARCH_ARM64_VAL 1)
endif()

add_compile_definitions(
  # Platform
  XE_PLATFORM_ANDROID=1
  XE_PLATFORM_LINUX=1        # Android inherits POSIX layer
  XE_ARCH_AMD64=${XE_ARCH_AMD64_VAL}
  XE_ARCH_ARM64=${XE_ARCH_ARM64_VAL}

  # Feature gates
  XE_VULKAN_ENABLED=1
  XE_DISCORD_ENABLED=0       # No Discord OAuth on Android
  XE_ENABLE_SDL=0            # No SDL on Android

  # Third-party lib config
  FMT_HEADER_ONLY=1
  VULKAN_HPP_NO_TO_STRING=1
  CAPSTONE_X86_ATT_DISABLE=1
  CAPSTONE_DIET_NO=1
  CAPSTONE_X86_REDUCE_NO=1
  CAPSTONE_HAS_ARM64=1
  CAPSTONE_USE_SYS_DYN_MEM=1
  XBYAK_NO_OP_NAMES=1
  XBYAK_ENABLE_OMITTED_OPERAND=1

  # Snappy
  SNAPPY_HAVE_SSSE3=0
  SNAPPY_HAVE_BMI2=0

  # VMA
  VMA_STATIC_VULKAN_FUNCTIONS=0
  VMA_DYNAMIC_VULKAN_FUNCTIONS=1
)

# ---------------------------------------------------------------------------
# Android system libraries
# ---------------------------------------------------------------------------
find_library(LIB_LOG     log)
find_library(LIB_ANDROID android)
find_library(LIB_VULKAN  vulkan)
find_library(LIB_EGL     EGL)
find_library(LIB_DL      dl)
find_library(LIB_M       m)

# ---------------------------------------------------------------------------
# Include directories (global — applied to all targets)
# ---------------------------------------------------------------------------
include_directories(
  "${XE_SRC}"
  "${XENIA_ROOT}"
  "${XE_3P}"
  "${XE_3P}/fmt/include"
  "${XE_3P}/Vulkan-Headers/include"
  "${XE_3P}/VulkanMemoryAllocator/include"
  "${XE_3P}/glslang"
  "${XE_3P}/SPIRV-Tools/include"
  "${XE_3P}/xxhash"
  "${XE_3P}/capstone/include"
  "${XE_3P}/xbyak"
  "${XE_3P}/xbyak_aarch64"
  "${XE_3P}/xbyak_aarch64/xbyak_aarch64"
  "${XE_3P}/imgui"
  "${XE_3P}/cpptoml/include"
  "${XE_3P}/tomlplusplus/include"
  "${XE_3P}/zarchive/include"
  "${XE_3P}/zstd/lib"
  "${XE_3P}/snappy"
  "${XE_3P}/cxxopts/include"
  "${XE_3P}/dxbc"
  "${XE_3P}/mspack"
  "${XE_3P}/aes_128"
  "${XE_3P}/microprofile"
  "${XE_3P}/stb"
  "${XE_3P}/half/include"
  "${XE_3P}/rapidjson/include"
  "${XE_3P}/utfcpp/source"
  "${XE_3P}/date/include"
  "${XE_3P}/llvm/include"
  "${XE_3P}/FFmpeg"
  "${XE_3P}/ffmpeg-xenia"
  "${CMAKE_CURRENT_SOURCE_DIR}/jni"
  "${CMAKE_CURRENT_SOURCE_DIR}/custom_include"
)

# ---------------------------------------------------------------------------
# Helper macro: glob .cc files in a directory (no recursion)
# ---------------------------------------------------------------------------
macro(xe_src_dir VAR DIR)
  file(GLOB ${VAR} "${XE_SRC}/${DIR}/*.cc")
endmacro()

macro(xe_src_dir_recurse VAR DIR)
  file(GLOB_RECURSE ${VAR} "${XE_SRC}/${DIR}/*.cc")
endmacro()

# ---------------------------------------------------------------------------
# xenia/base — Platform-abstraction layer
# Includes POSIX + Android overrides; excludes Win32, mac, GTK.
# ---------------------------------------------------------------------------
file(GLOB XE_BASE_SRCS "${XE_SRC}/xenia/base/*.cc")
list(FILTER XE_BASE_SRCS EXCLUDE REGEX "_(win|mac|gtk|linux|gnulinux)\\.cc$")
list(FILTER XE_BASE_SRCS EXCLUDE REGEX "test|_demo|debugging_mac|console_app_main")
if(NOT XE_ARCH_AMD64_VAL)
  list(FILTER XE_BASE_SRCS EXCLUDE REGEX "_x64\\.cc$")
  list(FILTER XE_BASE_SRCS EXCLUDE REGEX "_amd64\\.cc$")
endif()
if(NOT XE_ARCH_ARM64_VAL)
  list(FILTER XE_BASE_SRCS EXCLUDE REGEX "_arm64\\.cc$")
endif()
# Add POSIX + Android platform files explicitly
set(XE_BASE_PLATFORM_SRCS
  ${XE_SRC}/xenia/base/clock_posix.cc
  ${XE_SRC}/xenia/base/console_posix.cc
  ${XE_SRC}/xenia/base/cvar_android.cc
  ${XE_SRC}/xenia/base/debugging_posix.cc
  ${XE_SRC}/xenia/base/exception_handler_posix.cc
  ${XE_SRC}/xenia/base/filesystem_android.cc
  ${XE_SRC}/xenia/base/filesystem_posix.cc
  ${XE_SRC}/xenia/base/main_android.cc
  ${XE_SRC}/xenia/base/mapped_memory_posix.cc
  ${XE_SRC}/xenia/base/memory_posix.cc
  ${XE_SRC}/xenia/base/system_android.cc
  ${XE_SRC}/xenia/base/threading_posix.cc
  ${XE_SRC}/xenia/base/threading_timer_queue.cc
)
# Remove any duplicates already in glob
foreach(_f ${XE_BASE_PLATFORM_SRCS})
  list(REMOVE_ITEM XE_BASE_SRCS ${_f})
endforeach()

# ---------------------------------------------------------------------------
# xenia/cpu — PowerPC JIT (processor, HIR, compiler, PPC frontend)
# Compile architecture-dependent backend files selectively.
# ---------------------------------------------------------------------------
file(GLOB XE_CPU_SRCS
  "${XE_SRC}/xenia/cpu/*.cc"
  "${XE_SRC}/xenia/cpu/backend/*.cc"
  "${XE_SRC}/xenia/cpu/compiler/*.cc"
  "${XE_SRC}/xenia/cpu/compiler/passes/*.cc"
  "${XE_SRC}/xenia/cpu/hir/*.cc"
  "${XE_SRC}/xenia/cpu/ppc/*.cc"
)
list(FILTER XE_CPU_SRCS EXCLUDE REGEX "test|_win\\.cc$|stack_walker_win")
# Add posix-specific CPU files
list(APPEND XE_CPU_SRCS
  ${XE_SRC}/xenia/cpu/stack_walker_posix.cc
)
if(XE_ARCH_AMD64_VAL)
  list(APPEND XE_CPU_SRCS
    ${XE_SRC}/xenia/cpu/backend/x64/x64_code_cache_posix.cc
  )
  file(GLOB XE_CPU_X64_SRCS "${XE_SRC}/xenia/cpu/backend/x64/*.cc")
  list(FILTER XE_CPU_X64_SRCS EXCLUDE REGEX "_win\\.cc$|test")
  list(APPEND XE_CPU_SRCS ${XE_CPU_X64_SRCS})
endif()
list(REMOVE_DUPLICATES XE_CPU_SRCS)

# ---------------------------------------------------------------------------
# xenia/gpu — Xenos GPU emulation + Vulkan backend + null backend
# Excludes D3D12, Windows-only DXBC translator, trace dumping, trace playing.
# ---------------------------------------------------------------------------
file(GLOB XE_GPU_SRCS "${XE_SRC}/xenia/gpu/*.cc")
list(FILTER XE_GPU_SRCS EXCLUDE REGEX "dxbc_shader_translator|shader_compiler_main|trace_dump|trace_player|_win\\.cc$|test")

file(GLOB XE_GPU_VULKAN_SRCS "${XE_SRC}/xenia/gpu/vulkan/*.cc")
list(FILTER XE_GPU_VULKAN_SRCS EXCLUDE REGEX "trace_dump_main|trace_viewer_main|test")

file(GLOB XE_GPU_NULL_SRCS "${XE_SRC}/xenia/gpu/null/*.cc")

# Pre-compiled Vulkan SPIR-V shader bytecode headers (included by vulkan/*.cc)
# These are .h files that are #included — no separate compilation needed.

# ---------------------------------------------------------------------------
# xenia/apu — Audio Processing Unit (XMA decoder + nop backend)
# Excludes SDL audio and XAudio2.
# ---------------------------------------------------------------------------
file(GLOB XE_APU_SRCS "${XE_SRC}/xenia/apu/*.cc")
list(FILTER XE_APU_SRCS EXCLUDE REGEX "test")
file(GLOB XE_APU_NOP_SRCS "${XE_SRC}/xenia/apu/nop/*.cc")

# ---------------------------------------------------------------------------
# xenia/hid — Human Interface Device (controller) layer + nop backend
# Excludes WinKey, XInput, SDL.
# ---------------------------------------------------------------------------
file(GLOB XE_HID_SRCS "${XE_SRC}/xenia/hid/*.cc")
list(FILTER XE_HID_SRCS EXCLUDE REGEX "_demo|test")
file(GLOB XE_HID_NOP_SRCS "${XE_SRC}/xenia/hid/nop/*.cc")

# ---------------------------------------------------------------------------
# xenia/kernel — Xbox 360 OS kernel HLE (xboxkrnl + xam + xbdm)
# ---------------------------------------------------------------------------
file(GLOB_RECURSE XE_KERNEL_SRCS "${XE_SRC}/xenia/kernel/*.cc")
list(FILTER XE_KERNEL_SRCS EXCLUDE REGEX "test")

# ---------------------------------------------------------------------------
# xenia/vfs — Virtual File System (disc image, STFS, host path)
# ---------------------------------------------------------------------------
file(GLOB_RECURSE XE_VFS_SRCS "${XE_SRC}/xenia/vfs/*.cc")
list(FILTER XE_VFS_SRCS EXCLUDE REGEX "test|vfs_dump")

# ---------------------------------------------------------------------------
# xenia/ui — UI framework, window abstraction, Vulkan presenter
# Android-specific windowing + Vulkan presenter.
# Excludes GTK, Win32, XCB, X11.
# ---------------------------------------------------------------------------
file(GLOB XE_UI_SRCS "${XE_SRC}/xenia/ui/*.cc")
list(FILTER XE_UI_SRCS EXCLUDE REGEX "_win\\.cc$|_gtk\\.cc$|_gnulinux\\.cc$|_linux\\.cc$|_demo|windowed_app_main_posix|test")
list(APPEND XE_UI_SRCS
  ${XE_SRC}/xenia/ui/surface_android.cc
  ${XE_SRC}/xenia/ui/window_android.cc
  ${XE_SRC}/xenia/ui/windowed_app_context_android.cc
  ${XE_SRC}/xenia/ui/windowed_app_main_android.cc
  ${XE_SRC}/xenia/ui/file_picker_android.cc
)

file(GLOB XE_UI_VULKAN_SRCS
  "${XE_SRC}/xenia/ui/vulkan/*.cc"
  "${XE_SRC}/xenia/ui/vulkan/functions/*.cc"
)
list(FILTER XE_UI_VULKAN_SRCS EXCLUDE REGEX "_win\\.cc$|test|_demo")
list(REMOVE_DUPLICATES XE_UI_VULKAN_SRCS)

# ---------------------------------------------------------------------------
# xenia/app — Main entry point & emulator window
# Excludes Discord (disabled), Win32 rc files.
# ---------------------------------------------------------------------------
set(XE_APP_SRCS
  ${XE_SRC}/xenia/app/xenia_main.cc
  ${XE_SRC}/xenia/app/emulator_window.cc
  ${XE_SRC}/xenia/app/discord/discord_presence.cc
  ${XE_SRC}/xenia/app/console_settings_dialog.cc
  ${XE_SRC}/xenia/app/profile_dialogs.cc
)

# ---------------------------------------------------------------------------
# xenia/config + xenia/memory + xenia/emulator (core)
# ---------------------------------------------------------------------------
set(XE_CORE_SRCS
  ${XE_SRC}/xenia/config.cc
  ${XE_SRC}/xenia/emulator.cc
  ${XE_SRC}/xenia/memory.cc
)

# ---------------------------------------------------------------------------
# Third-party: snappy (compression, used by GPU trace system)
# ---------------------------------------------------------------------------
set(XE_SNAPPY_SRCS
  ${XE_3P}/snappy/snappy.cc
  ${XE_3P}/snappy/snappy-sinksource.cc
  ${XE_3P}/snappy/snappy-c.cc
)
# Filter to only files that exist (snappy layout varies)
foreach(_sf IN LISTS XE_SNAPPY_SRCS)
  if(NOT EXISTS ${_sf})
    list(REMOVE_ITEM XE_SNAPPY_SRCS ${_sf})
  endif()
endforeach()

# ---------------------------------------------------------------------------
# Third-party: aes_128 (kernel crypto)
# ---------------------------------------------------------------------------
file(GLOB XE_AES_SRCS "${XE_3P}/aes_128/*.cc" "${XE_3P}/aes_128/*.c")
list(FILTER XE_AES_SRCS EXCLUDE REGEX "main\\.c$")

# ---------------------------------------------------------------------------
# Third-party: mspack (XEX lzx decompression)
# ---------------------------------------------------------------------------
file(GLOB XE_MSPACK_SRCS "${XE_3P}/mspack/*.c" "${XE_3P}/mspack/*.cc")
list(FILTER XE_MSPACK_SRCS EXCLUDE REGEX "test")

# ---------------------------------------------------------------------------
# Third-party: dxbc (DXBC container reader, used by GPU)
# ---------------------------------------------------------------------------
file(GLOB XE_DXBC_SRCS "${XE_3P}/dxbc/*.cc" "${XE_3P}/dxbc/*.c")

# ---------------------------------------------------------------------------
# Third-party: xxhash (fast hashing, texture cache)
# ---------------------------------------------------------------------------
file(GLOB XE_XXHASH_SRCS "${XE_3P}/xxhash/*.cc" "${XE_3P}/xxhash/*.c")
list(FILTER XE_XXHASH_SRCS EXCLUDE REGEX "xxh_x86dispatch\\.c$|xxhsum\\.c$")
if(NOT XE_XXHASH_SRCS)
  # xxhash is often header-only — create an empty compilation unit
  file(WRITE "${CMAKE_BINARY_DIR}/xxhash_impl.cc"
    "#define XXH_INLINE_ALL 1\n#include \"xxhash.h\"\n")
  set(XE_XXHASH_SRCS "${CMAKE_BINARY_DIR}/xxhash_impl.cc")
endif()

# ---------------------------------------------------------------------------
# Third-party: imgui (in-emulator overlay UI)
# ---------------------------------------------------------------------------
set(XE_IMGUI_SRCS
  ${XE_3P}/imgui/imgui.cpp
  ${XE_3P}/imgui/imgui_demo.cpp
  ${XE_3P}/imgui/imgui_draw.cpp
  ${XE_3P}/imgui/imgui_tables.cpp
  ${XE_3P}/imgui/imgui_widgets.cpp
)
foreach(_sf IN LISTS XE_IMGUI_SRCS)
  if(NOT EXISTS ${_sf})
    list(REMOVE_ITEM XE_IMGUI_SRCS ${_sf})
  endif()
endforeach()

# ---------------------------------------------------------------------------
# Third-party: capstone (disassembler, used by x64 backend for trace output)
# ---------------------------------------------------------------------------
file(GLOB XE_CAPSTONE_SRCS "${XE_3P}/capstone/arch/AArch64/*.c"
                             "${XE_3P}/capstone/arch/ARM/*.c")
list(APPEND XE_CAPSTONE_SRCS
  ${XE_3P}/capstone/cs.c
  ${XE_3P}/capstone/MCInst.c
  ${XE_3P}/capstone/MCInstrDesc.c
  ${XE_3P}/capstone/MCRegisterInfo.c
  ${XE_3P}/capstone/SStream.c
  ${XE_3P}/capstone/utils.c
)
foreach(_sf IN LISTS XE_CAPSTONE_SRCS)
  if(NOT EXISTS ${_sf})
    list(REMOVE_ITEM XE_CAPSTONE_SRCS ${_sf})
  endif()
endforeach()

# ---------------------------------------------------------------------------
# Third-party: glslang-spirv (SPIR-V from the shader translator)
# Only the SPIRV/ output writer; not the full glslang frontend.
# ---------------------------------------------------------------------------
file(GLOB XE_GLSLANG_SPIRV_SRCS
  "${XE_3P}/glslang/SPIRV/*.cpp"
  "${XE_3P}/glslang/glslang/GenericCodeGen/*.cpp"
  "${XE_3P}/glslang/glslang/MachineIndependent/*.cpp"
  "${XE_3P}/glslang/glslang/MachineIndependent/preprocessor/*.cpp"
  "${XE_3P}/glslang/glslang/OSDependent/Unix/*.cpp"
)
list(FILTER XE_GLSLANG_SPIRV_SRCS EXCLUDE REGEX "test|doc\\.cpp|GlslangToSpv|StandAlone")
foreach(_sf IN LISTS XE_GLSLANG_SPIRV_SRCS)
  if(NOT EXISTS ${_sf})
    list(REMOVE_ITEM XE_GLSLANG_SPIRV_SRCS ${_sf})
  endif()
endforeach()

# ---------------------------------------------------------------------------
# Third-party: zarchive (archive decompression)
# ---------------------------------------------------------------------------
set(XE_ZARCHIVE_SRCS
  ${XE_3P}/zarchive/src/zarchivereader.cpp
  ${XE_3P}/zarchive/src/zarchivewriter.cpp
  ${XE_3P}/zarchive/src/sha_256.c
  ${XE_3P}/pugixml/src/pugixml.cpp
)

# ---------------------------------------------------------------------------
# xenia/patcher — Game patching system
# ---------------------------------------------------------------------------
set(XE_PATCHER_SRCS
  ${XE_SRC}/xenia/patcher/patch_db.cc
  ${XE_SRC}/xenia/patcher/patcher.cc
  ${XE_SRC}/xenia/patcher/plugin_loader.cc
)

# ---------------------------------------------------------------------------
# Third-party: xbyak_aarch64 (ARM64 JIT / CPU feature detection)
# ---------------------------------------------------------------------------
if(XE_ARCH_ARM64_VAL)
  set(XE_XBYAK_AARCH64_SRCS
    ${XE_3P}/xbyak_aarch64/src/xbyak_aarch64_impl.cpp
    ${XE_3P}/xbyak_aarch64/src/util_impl.cpp
  )
else()
  set(XE_XBYAK_AARCH64_SRCS)
endif()

# ---------------------------------------------------------------------------
# JNI bridge & Stubs (this repo)
# ---------------------------------------------------------------------------
set(XE_JNI_SRCS
  ${CMAKE_CURRENT_SOURCE_DIR}/jni/windowed_app_jni.cc
  ${CMAKE_CURRENT_SOURCE_DIR}/jni/xenia_jni_utils.cc
  ${CMAKE_CURRENT_SOURCE_DIR}/discord_stubs.c
  ${CMAKE_CURRENT_SOURCE_DIR}/ffmpeg_stubs.c
  ${CMAKE_CURRENT_SOURCE_DIR}/debug_window_stubs.cc
  ${CMAKE_CURRENT_SOURCE_DIR}/stb_impl.cc
  ${XENIA_ROOT}/src/xenia/hid/android_input_driver.cc
)

# ---------------------------------------------------------------------------
# Main shared library target: libxeo.so
# ---------------------------------------------------------------------------
add_library(xeo SHARED
  ${XE_BASE_SRCS}
  ${XE_BASE_PLATFORM_SRCS}
  ${XE_CORE_SRCS}
  ${XE_CPU_SRCS}
  ${XE_GPU_SRCS}
  ${XE_GPU_VULKAN_SRCS}
  ${XE_GPU_NULL_SRCS}
  ${XE_APU_SRCS}
  ${XE_APU_NOP_SRCS}
  ${XE_HID_SRCS}
  ${XE_HID_NOP_SRCS}
  ${XE_KERNEL_SRCS}
  ${XE_PATCHER_SRCS}
  ${XE_VFS_SRCS}
  ${XE_UI_SRCS}
  ${XE_UI_VULKAN_SRCS}
  ${XE_APP_SRCS}
  # Third-party
  ${XE_SNAPPY_SRCS}
  ${XE_AES_SRCS}
  ${XE_MSPACK_SRCS}
  ${XE_DXBC_SRCS}
  ${XE_XXHASH_SRCS}
  ${XE_IMGUI_SRCS}
  ${XE_CAPSTONE_SRCS}
  ${XE_GLSLANG_SPIRV_SRCS}
  ${XE_ZARCHIVE_SRCS}
  ${XE_XBYAK_AARCH64_SRCS}
  # JNI bridge
  ${XE_JNI_SRCS}
)

# ---------------------------------------------------------------------------
# Per-file compile flags
# ---------------------------------------------------------------------------
# imgui uses C++ but is in third_party — suppress its warnings
if(XE_IMGUI_SRCS)
  set_source_files_properties(${XE_IMGUI_SRCS}
    PROPERTIES COMPILE_FLAGS "-Wno-error -w")
endif()
# capstone is C — suppress noisy warnings
if(XE_CAPSTONE_SRCS)
  set_source_files_properties(${XE_CAPSTONE_SRCS}
    PROPERTIES COMPILE_FLAGS "-w")
endif()
# glslang suppression
if(XE_GLSLANG_SPIRV_SRCS)
  set_source_files_properties(${XE_GLSLANG_SPIRV_SRCS}
    PROPERTIES COMPILE_FLAGS "-Wno-error -w")
endif()
# mspack is C
if(XE_MSPACK_SRCS)
  set_source_files_properties(${XE_MSPACK_SRCS}
    PROPERTIES COMPILE_FLAGS "-w")
endif()

# ---------------------------------------------------------------------------
# Link libraries
# ---------------------------------------------------------------------------
target_link_libraries(xeo
  ${LIB_LOG}
  ${LIB_ANDROID}
  ${LIB_VULKAN}
  ${LIB_EGL}
  ${LIB_DL}
  ${LIB_M}
  z
)

# ---------------------------------------------------------------------------
# Strip release builds
# ---------------------------------------------------------------------------
if(CMAKE_BUILD_TYPE STREQUAL "Release")
  add_custom_command(TARGET xeo POST_BUILD
    COMMAND ${CMAKE_STRIP} --strip-unneeded "$<TARGET_FILE:xeo>"
    COMMENT "Stripping libxeo.so")
endif()

