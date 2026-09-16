# ==============================================================================
# ShowTime ProGuard / R8 Configuration
# ==============================================================================

# ------------------------------------------------------------------------------
# 1. Firebase Crashlytics & Stack Trace De-Obfuscation
# ------------------------------------------------------------------------------
# Crucial: Preserves source file names and line numbers so Crashlytics can
# map obfuscated stack traces back to the exact line of code in release builds.
-keepattributes SourceFile,LineNumberTable

# Keep all runtime and compile-time annotations (required by Crashlytics, Hilt, Room, Gson, Serialization)
-keepattributes *Annotation*

# Prevent R8 from stripping generic signatures and thrown exceptions
-keepattributes Signature
-keepattributes Exceptions

# Preserve enclosing methods and inner class attributes for coroutine & lambda hierarchy
-keepattributes InnerClasses
-keepattributes EnclosingMethod

# Prevent Exception class names and constructors from being obfuscated to a.b.c.
# This enables Crashlytics to group crashes accurately by their real exception type.
-keep public class * extends java.lang.Throwable {
    public <init>(...);
}
-keep public class * extends java.lang.Exception {
    public <init>(...);
}
-keep public class * extends java.lang.RuntimeException {
    public <init>(...);
}

# Keep Firebase Crashlytics and Analytics SDK classes from aggressive optimization
-keep class com.google.firebase.crashlytics.** { *; }
-dontwarn com.google.firebase.crashlytics.**
-keep class com.google.firebase.analytics.** { *; }
-dontwarn com.google.firebase.analytics.**

# ------------------------------------------------------------------------------
# 2. Kotlin Coroutines & Asynchronous Stack Traces
# ------------------------------------------------------------------------------
# Restores asynchronous stack traces through Kotlin Continuation machinery
-keepclassmembernames class kotlinx.coroutines.internal.DispatchedContinuation {
    java.lang.Object _reusableCancellableContinuation;
}
-keepclassmembers class kotlin.coroutines.jvm.internal.BaseContinuationImpl {
    <fields>;
}

# ------------------------------------------------------------------------------
# 3. Serialization & Networking Models
# ------------------------------------------------------------------------------
-keepclassmembers,allowobfuscation class * {
    @com.google.gson.annotations.SerializedName <fields>;
}

-keepclassmembers class * extends com.google.protobuf.GeneratedMessageLite* {
    <fields>;
}

# Keep ApiResponse sealed class hierarchy for Retrofit call adapter
-keep class com.ssverma.core.networking.adapter.ApiResponse { *; }
-keep class com.ssverma.core.networking.adapter.ApiResponse$** { *; }

# Keep Retrofit service interfaces intact
-keepclasseswithmembers interface * {
    @retrofit2.http.* <methods>;
}

# ------------------------------------------------------------------------------
# 4. Jetpack Compose & UI Runtime
# ------------------------------------------------------------------------------
-dontwarn androidx.compose.**
