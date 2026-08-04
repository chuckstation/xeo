// Copyright 2026 Xeo Contributors. All rights reserved.
// Released under the BSD license - see LICENSE in the root for more details.

#include "xenia_jni_utils.h"
#include <sys/system_properties.h>
#include <cstdlib>

namespace xe {
namespace android {
namespace jni {

std::string JStringToStdString(JNIEnv* env, jstring jstr) {
  if (!jstr) return {};
  const char* chars = env->GetStringUTFChars(jstr, nullptr);
  if (!chars) return {};
  std::string result(chars);
  env->ReleaseStringUTFChars(jstr, chars);
  return result;
}

std::string BundleGetString(JNIEnv* env, jobject bundle, const char* key) {
  if (!bundle || !key) return {};

  jclass bundleClass = env->FindClass("android/os/Bundle");
  if (!bundleClass) return {};

  jmethodID getString = env->GetMethodID(
      bundleClass, "getString",
      "(Ljava/lang/String;)Ljava/lang/String;");
  env->DeleteLocalRef(bundleClass);
  if (!getString) return {};

  jstring jKey = env->NewStringUTF(key);
  if (!jKey) return {};

  auto jValue = static_cast<jstring>(
      env->CallObjectMethod(bundle, getString, jKey));
  env->DeleteLocalRef(jKey);

  if (!jValue) return {};
  std::string result = JStringToStdString(env, jValue);
  env->DeleteLocalRef(jValue);
  return result;
}

int32_t GetDeviceApiLevel() {
  char sdk_str[PROP_VALUE_MAX] = {};
  if (__system_property_get("ro.build.version.sdk", sdk_str) > 0) {
    return std::atoi(sdk_str);
  }
  return 24;  // Minimum supported API
}

}  // namespace jni
}  // namespace android
}  // namespace xe
