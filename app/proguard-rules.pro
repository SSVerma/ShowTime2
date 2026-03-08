# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}

# Uncomment this to preserve the line number information for
# debugging stack traces.
#-keepattributes SourceFile,LineNumberTable

# If you keep the line number information, uncomment this to
# hide the original source file name.
#-renamesourcefileattribute SourceFile

-keepclassmembers,allowobfuscation class * {
  @com.google.gson.annotations.SerializedName <fields>;
}

-keepclassmembers class * extends com.google.protobuf.GeneratedMessageLite* {
   <fields>;
}

# 1. Prevent R8 from stripping generic types (the <S, E> parts)
-keepattributes Signature
-keepattributes Exceptions

# 2. Keep the ApiResponse class and ALL of its nested sealed classes
-keep class com.ssverma.core.networking.adapter.ApiResponse { *; }
-keep class com.ssverma.core.networking.adapter.ApiResponse$** { *; }

# 3. Keep Retrofit service interfaces intact
-keepclasseswithmembers interface * {
    @retrofit2.http.* <methods>;
}
