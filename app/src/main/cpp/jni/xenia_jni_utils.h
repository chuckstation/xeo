// Copyright 2026 Xeo Contributors. All rights reserved.
// Released under the BSD license - see LICENSE in the root for more details.

#ifndef XENIA_ANDROID_JNI_XENIA_JNI_UTILS_H_
#define XENIA_ANDROID_JNI_XENIA_JNI_UTILS_H_

#include <jni.h>
#include <android/log.h>
#include <string>

#define XENIA_LOGI(...) __android_log_print(ANDROID_LOG_INFO,  "Xenia", __VA_ARGS__)
#define XENIA_LOGW(...) __android_log_print(ANDROID_LOG_WARN,  "Xenia", __VA_ARGS__)
#define XENIA_LOGE(...) __android_log_print(ANDROID_LOG_ERROR, "Xenia", __VA_ARGS__)
#define XENIA_LOGD(...) __android_log_print(ANDROID_LOG_DEBUG, "Xenia", __VA_ARGS__)

namespace xe {
namespace android {
namespace jni {

// Convert a Java jstring to a std::string (UTF-8). Returns "" if null.
std::string JStringToStdString(JNIEnv* env, jstring jstr);

// Read a named string entry from an android.os.Bundle. Returns "" if missing.
std::string BundleGetString(JNIEnv* env, jobject bundle, const char* key);

// Return the running device's Android API level (from system property).
int32_t GetDeviceApiLevel();

}  // namespace jni
}  // namespace android
}  // namespace xe

#endif  // XENIA_ANDROID_JNI_XENIA_JNI_UTILS_H_
