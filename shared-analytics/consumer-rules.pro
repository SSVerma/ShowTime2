# Consumer ProGuard rules for :shared-analytics

# Prevent obfuscation of custom telemetry and audit exceptions for Crashlytics grouping
-keep public class com.ssverma.shared.analytics.RemoteServerException { *; }
-keep public class com.ssverma.shared.analytics.community.FirestoreQuotaExhaustedException { *; }
-keep public class com.ssverma.shared.analytics.community.FirestorePermissionDeniedException { *; }
-keep public class com.ssverma.shared.analytics.community.FirestoreOutageException { *; }
