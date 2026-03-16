# Prevent R8 from merging structurally identical data classes in DiscoverOption hierarchy
-keep class com.ssverma.shared.domain.DiscoverOption** { *; }
-keep class com.ssverma.shared.domain.OptionMode** { *; }
-keep class com.ssverma.shared.domain.MultiValueMode** { *; }

# Prevent R8 from merging structurally identical data classes in SortBy hierarchy
-keep class com.ssverma.shared.domain.SortBy** { *; }
-keep class com.ssverma.shared.domain.Order** { *; }

# Prevent R8 from merging structurally identical interceptors
-keep class com.ssverma.api.service.tmdb.interceptor.** { *; }
-keep interface com.ssverma.core.networking.interceptor.** { *; }
