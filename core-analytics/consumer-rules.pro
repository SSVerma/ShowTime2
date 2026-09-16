# Consumer ProGuard rules for :core-analytics

# Preserve line numbers and source file names for Crashlytics stack trace de-obfuscation
-keepattributes SourceFile,LineNumberTable
-keepattributes *Annotation*,Signature,InnerClasses,EnclosingMethod

# Keep Firebase Crashlytics and Analytics components
-keep class com.google.firebase.crashlytics.** { *; }
-dontwarn com.google.firebase.crashlytics.**
-keep class com.google.firebase.analytics.** { *; }
-dontwarn com.google.firebase.analytics.**
