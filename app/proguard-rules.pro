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

# 🔮 NUEVO: Reglas ProGuard para Haze Library (Glassmorphism)
-keep class dev.chrisbanes.haze.** { *; }
-dontwarn dev.chrisbanes.haze.**

# 🔮 NUEVO: Reglas para RenderEffect (usado por Haze)
-keep class android.graphics.RenderEffect { *; }
-keep class android.graphics.RenderNode { *; }

# 🔮 NUEVO: Reglas para Compose Animation Graphics
-keep class androidx.compose.animation.graphics.** { *; }
-dontwarn androidx.compose.animation.graphics.**

# 🔮 NUEVO: Reglas para efectos de glassmorphism
-keep class androidx.compose.ui.graphics.** { *; }
-keep class androidx.compose.ui.draw.** { *; }

# 🎨 Reglas existentes para WisdomSpark
-keep class com.albertowisdom.wisdomspark.data.models.** { *; }
-keep class com.albertowisdom.wisdomspark.data.local.database.entities.** { *; }
-keep class com.albertowisdom.wisdomspark.data.remote.dto.** { *; }

# Retrofit y Gson
-keepattributes Signature
-keepattributes *Annotation*
-keep class retrofit2.** { *; }
-keep class com.google.gson.** { *; }

# Room Database
-keep class * extends androidx.room.RoomDatabase
-keep @androidx.room.Entity class *
-keep @androidx.room.Dao class *

# Hilt
-keep class dagger.hilt.** { *; }
-keep class javax.inject.** { *; }
-keep class **_HiltComponents$** { *; }
-keep class **_Impl { *; }
-keep class **_Factory { *; }