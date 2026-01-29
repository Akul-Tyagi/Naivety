# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# WebView with JavaScript interface - required for EPUB reader
-keepclassmembers class com.abundance.naivety.EpubReaderActivity$EpubJsInterface {
   public *;
}

# Preserve line number information for debugging stack traces
-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile

# Base attributes to keep
-keepattributes Signature, *Annotation*, EnclosingMethod, InnerClasses
-keepattributes Exceptions, RuntimeVisibleAnnotations, RuntimeVisibleParameterAnnotations
-keepattributes MethodParameters, SourceFile, LineNumberTable
-keepattributes Deprecated, AnnotationDefault

# Gson and JSON models
-dontwarn sun.misc.**
-keep class com.google.gson.** { *; }
-keep class * implements java.lang.reflect.Type
-keep class * extends com.google.gson.TypeAdapter
-keep class * implements com.google.gson.TypeAdapterFactory
-keep class * implements com.google.gson.JsonSerializer
-keep class * implements com.google.gson.JsonDeserializer
-keepclassmembers,allowobfuscation class * {
  @com.google.gson.annotations.SerializedName <fields>;
}

# Keep your models
-keep class com.abundance.naivety.network.models.** { *; }
-keep class com.abundance.naivety.models.** { *; }
-keep class com.abundance.naivety.data.** { *; }

# Retrofit
-keep class retrofit2.** { *; }
-keep class retrofit2.converter.gson.** { *; }
-keep class retrofit2.Response { *; }
-keep class retrofit2.HttpServiceMethod { *; }
-keep class retrofit2.ServiceMethod { *; }
-keepclasseswithmembers interface * {
    @retrofit2.http.* <methods>;
}

# Java Reflection
-keep class java.lang.reflect.** { *; }

# Kotlin Coroutines
-keep class kotlinx.coroutines.** { *; }
-keepclassmembers class kotlinx.** {
    volatile <fields>;
}
-keepnames class kotlinx.coroutines.internal.MainDispatcherFactory {}
-keepnames class kotlinx.coroutines.CoroutineExceptionHandler {}
-keep class kotlin.coroutines.Continuation

# HTTP Client
-dontwarn okhttp3.**
-dontwarn okio.**
-dontwarn javax.annotation.**
-keep class okhttp3.** { *; }
-keep class okio.** { *; }

# Google & Firebase Auth
-keep class com.google.android.gms.auth.** { *; }
-keep class com.google.android.gms.common.** { *; }
-keep class com.google.android.gms.tasks.** { *; }
-keep class com.google.firebase.auth.** { *; }

# Keep specific Google Sign-In classes
-keep class com.google.android.gms.auth.api.signin.** { *; }
-keep class com.google.android.gms.common.api.GoogleApiClient { *; }
-keep class com.google.android.gms.auth.api.signin.GoogleSignInClient { *; }
-keep class com.google.android.gms.auth.api.signin.GoogleSignInAccount { *; }
-keep class com.google.android.gms.auth.api.signin.GoogleSignInOptions { *; }

# Other Firebase services
-keep class com.google.firebase.** { *; }

# Android Architecture Components
-keep class androidx.paging.** { *; }
-keep class * implements androidx.paging.PagingSource
-keep class com.abundance.naivety.data.BookPagingSource { *; }

# Room Database
-keep class androidx.room.** { *; }
-keep class * extends androidx.room.RoomDatabase
-keep @androidx.room.Entity class *
-keepclassmembers @androidx.room.Entity class * { <fields>; }
-keepclassmembers class * { @androidx.room.Query <methods>; }

# APIs and Interfaces
-keep interface com.abundance.naivety.network.api.** { *; }
-keep interface com.abundance.naivety.network.OpenLibraryApi { *; }

# Dependency Injection
-keep class dagger.** { *; }
-keep class javax.inject.** { *; }
-keep class * extends dagger.hilt.android.lifecycle.HiltViewModel

# Third-party libraries
-keep class com.github.mhiew.** { *; }
-keep class com.tom_roush.pdfbox.** { *; }
-keep class io.coil.** { *; }
-dontwarn com.gemalto.jp2.**

# Android components
-keep public class com.abundance.naivety.**.Activity
-keep public class com.abundance.naivety.**.Fragment
-keep public class com.abundance.naivety.ui.** { *; }
-keep class * extends androidx.lifecycle.ViewModel


# Navigation
-keep class com.abundance.naivety.navigation.Destinations { *; }
-keepclassmembers class com.abundance.naivety.navigation.Destinations$* { *; }
-keep class androidx.navigation.** { *; }

# Android System
-keep class android.database.sqlite.** { *; }
-keep class android.net.Uri { *; }
-keep class * extends android.content.ContentProvider { *; }
-keepclassmembers class * implements android.os.Parcelable { public static final ** CREATOR; }

# Enums
-keepclassmembers class * extends java.lang.Enum {
    <fields>;
    public static **[] values();
    public static ** valueOf(java.lang.String);
}

# ==================== READIUM EPUB SUPPORT ====================
# Readium Kotlin Toolkit
-keep class org.readium.** { *; }
-keepclassmembers class org.readium.** { *; }
-dontwarn org.readium.**

# Jsoup (used by Readium for HTML parsing)
-keep class org.jsoup.** { *; }
-keepclassmembers class org.jsoup.** { *; }
-dontwarn org.jsoup.**

# WebKit (for EPUB WebView rendering)
-keep class androidx.webkit.** { *; }
-dontwarn androidx.webkit.**

# EPUB reader state and settings classes
-keep class com.abundance.naivety.epub.** { *; }
-keep class com.abundance.naivety.ui.components.epub.** { *; }

# ==================== R8 WARNING SUPPRESSIONS ====================
# Suppress R8 warnings for Google Play Services Auth internal classes
# These warnings are caused by obfuscated code in Google's AAR files
# and do not affect app functionality
-dontwarn com.google.android.gms.auth.api.identity.**
-dontwarn com.google.android.gms.auth.api.signin.**
-dontwarn com.google.android.gms.auth.api.signin.internal.**
-dontwarn com.google.android.gms.internal.auth.**
-keep class com.google.android.gms.auth.api.identity.** { *; }
-keep class com.google.android.gms.auth.api.signin.internal.** { *; }
