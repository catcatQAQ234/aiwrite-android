# Hilt - keep generated components
-keep class dagger.hilt.** { *; }
-keep class javax.inject.** { *; }
-keep class * extends dagger.hilt.android.internal.managers.ViewComponentManager$FragmentContextWrapper { *; }
-keep class * implements dagger.hilt.internal.GeneratedComponent { *; }

# Retrofit - keep data classes used in API calls
-keepattributes Signature
-keepattributes Exceptions
-keepattributes *Annotation*
-keep class com.aiwrite.domain.model.** { *; }

# Room - keep entities and DAOs
-keep class com.aiwrite.data.local.entity.** { *; }
-keep class com.aiwrite.data.local.dao.** { *; }

# OkHttp - keep for runtime
-dontwarn okhttp3.**
-dontwarn okio.**

# Kotlin coroutines
-keepnames class kotlinx.coroutines.internal.MainDispatcherFactory {}
-keepnames class kotlinx.coroutines.CoroutineExceptionHandler {}

# Android Keystore (for encryption)
-keepclassmembers class * {
    @javax.crypto.spec.GCMParameterSpec <init>(...);
}
