# Keep Xeo JNI bridge classes - native code references these by name.
# The Java package remains `com.xenia.android` for ABI compatibility with
# the native Java_com_xenia_android_* symbols in libxeo.so; the user-facing
# Application ID is `org.adars.xeo` and is set in app/build.gradle.
-keep class com.xenia.android.XeniaRuntimeException { *; }
-keep class com.xenia.android.emulator.WindowedAppActivity { *; }
-keep class com.xenia.android.emulator.WindowedAppActivity$* { *; }
-keep class com.xenia.android.emulator.WindowSurfaceView { *; }
-keep class com.xenia.android.emulator.EmulatorActivity { *; }
-keep class com.xenia.android.emulator.GpuTraceViewerActivity { *; }

# Keep native method signatures
-keepclasseswithmembernames class * {
    native <methods>;
}

# AndroidX
-dontwarn androidx.**

# General Android rules
-keepattributes *Annotation*
-keepattributes SourceFile,LineNumberTable
