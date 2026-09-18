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
# 3. Serialization, Gson & Reflection Models
# ------------------------------------------------------------------------------
# Preserve generic signatures & annotations for Gson reflection and Kotlin reflection
-keepattributes Signature
-keepattributes *Annotation*

# Keep Gson library and TypeToken subclasses
-keep class com.google.gson.** { *; }
-keep class * extends com.google.gson.reflect.TypeToken
-keepclassmembers class * extends com.google.gson.reflect.TypeToken {
    *;
}

# Keep all domain, data, entity, database and backup models
-keep class com.ssverma.shared.domain.model.** { *; }
-keep class com.ssverma.shared.data.model.** { *; }
-keep class com.ssverma.shared.data.backup.** { *; }
-keep class com.ssverma.shared.data.local.db.entity.** { *; }
-keep class com.ssverma.shared.data.entity.** { *; }
-keep class com.ssverma.feature.**.domain.model.** { *; }
-keep class com.ssverma.feature.**.data.model.** { *; }
-keep class com.ssverma.feature.**.data.entity.** { *; }
-keep @androidx.room.Entity class * { *; }
-keep class * extends androidx.room.RoomDatabase

# Keep Parcelable CREATORs and Navigation NavKeys
-keepclassmembers class * implements android.os.Parcelable {
    static ** CREATOR;
}
-keep class com.ssverma.**.navigation.**NavKey* { *; }
-keep class * implements androidx.navigation3.runtime.NavKey { *; }

# Keep enums used in serialization and reflection
-keepclassmembers enum * {
    public static **[] values();
    public static ** valueOf(java.lang.String);
}

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

# Keep Hilt EntryPoints and injected classes
-keep @dagger.hilt.EntryPoint interface * { *; }
-keep @dagger.hilt.InstallIn interface * { *; }

# Keep WorkManager workers instantiated via reflection
-keep public class * extends androidx.work.ListenableWorker {
    public <init>(android.content.Context, androidx.work.WorkerParameters);
}
-keep public class * extends androidx.work.CoroutineWorker {
    public <init>(android.content.Context, androidx.work.WorkerParameters);
}

# Keep AppWidgetProvider and Glance Receivers
-keep class * extends android.appwidget.AppWidgetProvider { *; }
-keep class * extends androidx.glance.appwidget.GlanceAppWidgetReceiver { *; }

# ------------------------------------------------------------------------------
# 4. Google Mobile Ads (AdMob) SDK
# ------------------------------------------------------------------------------
-keep public class com.google.android.gms.ads.** {
    public *;
}
-keep public class com.google.ads.** {
    public *;
}

# ------------------------------------------------------------------------------
# 5. Jetpack Compose & UI Runtime
# ------------------------------------------------------------------------------
-dontwarn androidx.compose.**
