# Keep Xeo JNI bridge classes - native code references these by name.
# The Java package is `org.adars.xeo` and matches the native
# Java_org_adars_xeo_* symbols in libxeo.so.
-keep class org.adars.xeo.XeoRuntimeException { *; }
-keep class org.adars.xeo.emulator.WindowedAppActivity { *; }
-keep class org.adars.xeo.emulator.WindowedAppActivity$* { *; }
-keep class org.adars.xeo.emulator.WindowSurfaceView { *; }
-keep class org.adars.xeo.emulator.EmulatorActivity { *; }
-keep class org.adars.xeo.emulator.GpuTraceViewerActivity { *; }

# Keep native method signatures
-keepclasseswithmembernames class * {
    native <methods>;
}

# AndroidX
-dontwarn androidx.**

# General Android rules
-keepattributes *Annotation*
-keepattributes SourceFile,LineNumberTable
