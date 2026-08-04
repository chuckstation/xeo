// Copyright 2026 Xeo Contributors. All rights reserved.
// Released under the BSD license - see LICENSE in the root for more details.
//
// JNI bridge — com.xenia.android.emulator.WindowedAppActivity  →  Xeo native.
//
// Design:
//   The Xenia upstream already implements everything we need inside
//   AndroidWindowedAppContext.  Its Jni* static/instance methods are the
//   canonical entry points for all lifecycle, surface, input, and paint events.
//
//   This file is a thin shim that:
//     1. Re-exports those methods under the Java package name
//        `com.xenia.android` (kept for ABI compatibility with the native
//        Java_com_xenia_android_* symbols; the user-facing Application ID
//        is `org.adars.xeo`).
//     2. Passes the EXTRA_CVARS Bundle to the emulator so cvars like
//        "target" (game path) and "target_trace_file" reach the native side.
//
// All heavy lifting — CPU JIT, GPU Vulkan renderer, file system, audio —
// is compiled in from the vendored src/xenia/ tree via CMakeLists.txt.

#include <jni.h>
#include <android/log.h>

// Xenia platform headers (vendored src/ tree).
#include "xenia/ui/windowed_app_context_android.h"
#include "xenia/base/cvar.h"
#include "xenia/base/main_android.h"
#include "xenia/emulator.h"
#include "xenia/hid/android_input_driver.h"

#define XTAG  "XeoJNI"
#define XLOGI(...) __android_log_print(ANDROID_LOG_INFO,  XTAG, __VA_ARGS__)
#define XLOGE(...) __android_log_print(ANDROID_LOG_ERROR, XTAG, __VA_ARGS__)

using xe::ui::AndroidWindowedAppContext;

// Cast the opaque jlong handle stored in the Java object back to a pointer.
static inline AndroidWindowedAppContext* toCtx(jlong h) {
  return reinterpret_cast<AndroidWindowedAppContext*>(
      static_cast<uintptr_t>(h));
}

extern "C" {

// ---------------------------------------------------------------------------
// nativeInitialize
// Called from WindowedAppActivity.onCreate().
//
// Delegates to AndroidWindowedAppContext::JniActivityInitializeWindowedAppOnCreate
// which:
//   - Calls InitializeAndroidAppFromMainThread (ref-counted, multi-activity safe)
//   - Parses the cvarBundle (sets "target", "gpu", "apu", "hid" cvars etc.)
//   - Creates the windowed app (EmulatorApp or GpuTraceViewer)
//   - Sets up the ALooper for UI-thread callbacks
//
// Returns an opaque pointer (as jlong) to the AndroidWindowedAppContext, or 0
// on failure.
// ---------------------------------------------------------------------------
JNIEXPORT jlong JNICALL
Java_com_xenia_android_emulator_WindowedAppActivity_nativeInitialize(
    JNIEnv* env, jobject activity,
    jstring windowedAppIdentifier,
    jobject assetManager,
    jobject cvarBundle) {

  XLOGI("nativeInitialize: entry");

  // JniActivityInitializeWindowedAppOnCreate internally calls
  // InitializeAndroidAppFromMainThread with the cvarBundle so all cvars
  // (including "target" and "target_trace_file") are parsed before the
  // emulator thread starts.
  AndroidWindowedAppContext* ctx =
      AndroidWindowedAppContext::JniActivityInitializeWindowedAppOnCreate(
          env, activity, windowedAppIdentifier, assetManager);

  if (!ctx) {
    XLOGE("nativeInitialize: JniActivityInitializeWindowedAppOnCreate returned null");
    return 0L;
  }

  // If a cvar bundle was supplied by the launching Intent, parse it now so
  // cvars set per-launch (e.g. "target") override any persisted config.
  // The upstream cvar system merges these on top of any already-set values.
  if (cvarBundle) {
    cvar::ParseLaunchArgumentsFromAndroidBundle(cvarBundle);
  }

  XLOGI("nativeInitialize: context=%p", static_cast<void*>(ctx));
  return static_cast<jlong>(reinterpret_cast<uintptr_t>(ctx));
}

// ---------------------------------------------------------------------------
// nativeDestroy — called from WindowedAppActivity.onDestroy()
// ---------------------------------------------------------------------------
JNIEXPORT void JNICALL
Java_com_xenia_android_emulator_WindowedAppActivity_nativeDestroy(
    JNIEnv* /*env*/, jobject /*activity*/, jlong handle) {
  XLOGI("nativeDestroy: handle=%p",
        reinterpret_cast<void*>(static_cast<uintptr_t>(handle)));
  AndroidWindowedAppContext* ctx_ = toCtx(handle);
  if (ctx_) ctx_->JniActivityOnDestroy();
}

// ---------------------------------------------------------------------------
// nativeSurfaceLayoutChange — forwarded from View.OnLayoutChangeListener
// ---------------------------------------------------------------------------
JNIEXPORT void JNICALL
Java_com_xenia_android_emulator_WindowedAppActivity_nativeSurfaceLayoutChange(
    JNIEnv* /*env*/, jobject /*activity*/, jlong handle,
    jint left, jint top, jint right, jint bottom) {
  AndroidWindowedAppContext* ctx_ = toCtx(handle);
  if (ctx_) ctx_->JniActivityOnWindowSurfaceLayoutChange(left, top, right, bottom);
}

// ---------------------------------------------------------------------------
// nativeSurfaceMotionEvent — touch + generic motion events
// Returns true if the event was consumed by the native layer.
// ---------------------------------------------------------------------------
JNIEXPORT jboolean JNICALL
Java_com_xenia_android_emulator_WindowedAppActivity_nativeSurfaceMotionEvent(
    JNIEnv* /*env*/, jobject /*activity*/, jlong handle, jobject event) {
  AndroidWindowedAppContext* ctx_ = toCtx(handle);
  if (!ctx_ || !event) return JNI_FALSE;
  return ctx_->JniActivityOnWindowSurfaceMotionEvent(event)
             ? JNI_TRUE : JNI_FALSE;
}

// ---------------------------------------------------------------------------
// nativeSurfaceChanged — Surface created / changed / destroyed (null → gone)
// ---------------------------------------------------------------------------
JNIEXPORT void JNICALL
Java_com_xenia_android_emulator_WindowedAppActivity_nativeSurfaceChanged(
    JNIEnv* /*env*/, jobject /*activity*/, jlong handle, jobject surface) {
  AndroidWindowedAppContext* ctx_ = toCtx(handle);
  if (ctx_) ctx_->JniActivityOnWindowSurfaceChanged(surface);
}

// ---------------------------------------------------------------------------
// nativePaint — called from WindowSurfaceView.onDraw and surfaceRedrawNeeded
// ---------------------------------------------------------------------------
JNIEXPORT void JNICALL
Java_com_xenia_android_emulator_WindowedAppActivity_nativePaint(
    JNIEnv* /*env*/, jobject /*activity*/, jlong handle, jboolean forcePaint) {
  AndroidWindowedAppContext* ctx_ = toCtx(handle);
  if (ctx_) ctx_->JniActivityPaintWindow(forcePaint == JNI_TRUE);
}

// ---------------------------------------------------------------------------
// nativeSetGamepadState — called from Java TouchControllerOverlay
// ---------------------------------------------------------------------------
JNIEXPORT void JNICALL
Java_com_xenia_android_emulator_EmulatorActivity_nativeSetGamepadState(
    JNIEnv* /*env*/, jobject /*activity*/, jint buttons, jint lt, jint rt,
    jint lx, jint ly, jint rx, jint ry) {
  xe::hid::AndroidInputDriver::SetButtonState(
      static_cast<uint16_t>(buttons),
      static_cast<uint8_t>(lt),
      static_cast<uint8_t>(rt),
      static_cast<int16_t>(lx),
      static_cast<int16_t>(ly),
      static_cast<int16_t>(rx),
      static_cast<int16_t>(ry)
  );
}

// ---------------------------------------------------------------------------
// nativeSaveState — called from EmulatorActivity to save state to file
// ---------------------------------------------------------------------------
JNIEXPORT jboolean JNICALL
Java_com_xenia_android_emulator_EmulatorActivity_nativeSaveState(
    JNIEnv* env, jobject activity, jstring filePath) {
  XLOGI("nativeSaveState: entry");
  xe::Emulator* emulator = xe::GetActiveEmulator();
  if (!emulator) {
    XLOGE("nativeSaveState: No active emulator instance!");
    return JNI_FALSE;
  }
  const char* file_path_c = env->GetStringUTFChars(filePath, nullptr);
  if (!file_path_c) {
    XLOGE("nativeSaveState: Failed to get path string!");
    return JNI_FALSE;
  }
  std::filesystem::path path(file_path_c);
  env->ReleaseStringUTFChars(filePath, file_path_c);

  bool success = emulator->SaveToFile(path);
  return success ? JNI_TRUE : JNI_FALSE;
}

// ---------------------------------------------------------------------------
// nativeRestoreState — called from EmulatorActivity to load state from file
// ---------------------------------------------------------------------------
JNIEXPORT jboolean JNICALL
Java_com_xenia_android_emulator_EmulatorActivity_nativeRestoreState(
    JNIEnv* env, jobject activity, jstring filePath) {
  XLOGI("nativeRestoreState: entry");
  xe::Emulator* emulator = xe::GetActiveEmulator();
  if (!emulator) {
    XLOGE("nativeRestoreState: No active emulator instance!");
    return JNI_FALSE;
  }
  const char* file_path_c = env->GetStringUTFChars(filePath, nullptr);
  if (!file_path_c) {
    XLOGE("nativeRestoreState: Failed to get path string!");
    return JNI_FALSE;
  }
  std::filesystem::path path(file_path_c);
  env->ReleaseStringUTFChars(filePath, file_path_c);

  bool success = emulator->RestoreFromFile(path);
  return success ? JNI_TRUE : JNI_FALSE;
}

}  // extern "C"

