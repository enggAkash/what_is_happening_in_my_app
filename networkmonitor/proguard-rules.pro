# Add project specific ProGuard rules here.

# Keep NetworkMonitor SDK classes
-keep class com.engineerakash.networkmonitorsdk.** { *; }
-dontwarn com.engineerakash.networkmonitorsdk.**

# OkHttp
-dontwarn okhttp3.**
-dontwarn okio.**
-keepnames class okhttp3.internal.publicsuffix.PublicSuffixDatabase

# Socket.IO
-keep class io.socket.** { *; }
-dontwarn io.socket.**

# Gson
-keepattributes Signature
-keepattributes *Annotation*
-keep class sun.misc.Unsafe { *; }
-keep class com.google.gson.** { *; }
-keep class com.engineerakash.networkmonitorsdk.models.** { *; }

# Room
-keep class * extends androidx.room.RoomDatabase
-dontwarn androidx.room.paging.**

# Keep data classes
-keepclassmembers class com.engineerakash.networkmonitorsdk.models.** {
    <fields>;
}

# Coroutines
-keepnames class kotlinx.coroutines.internal.MainDispatcherFactory
-keepnames class kotlinx.coroutines.CoroutineExceptionHandler
-keepclassmembers class kotlinx.coroutines.** {
    volatile <fields>;
}
-keep class kotlinx.coroutines.** { *; }

# WorkManager
-keep class androidx.work.** { *; }
-dontwarn androidx.work.**

