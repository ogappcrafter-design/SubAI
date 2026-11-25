# --- SubAI ProGuard / R8 rules ---
# Kotlinx Serialization
-keepattributes *Annotation*, InnerClasses
-dontnote kotlinx.serialization.**
-keepclassmembers class **$$serializer { *; }
-keepclasseswithmembers class com.ogapp.subai.** {
    kotlinx.serialization.KSerializer serializer(...);
}
-keep,includedescriptorclasses class com.ogapp.subai.**$$serializer { *; }
-keep class com.ogapp.subai.data.** { *; }
-keep class com.ogapp.subai.domain.model.** { *; }

# Ktor / OkHttp
-dontwarn org.slf4j.**
-dontwarn io.ktor.**
-keep class io.ktor.** { *; }
-keepclassmembers class io.ktor.** { volatile <fields>; }
-dontwarn okhttp3.**
-dontwarn okio.**

# Tink / androidx.security-crypto reference error-prone + javax annotations that
# aren't on the Android runtime classpath. Safe to ignore (compile-time only).
-dontwarn com.google.errorprone.annotations.**
-dontwarn javax.annotation.**
-dontwarn com.google.crypto.tink.**
-keep class com.google.crypto.tink.** { *; }
-keep class androidx.security.crypto.** { *; }

# Keep line numbers for readable crash reports; hide original file name.
-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile
