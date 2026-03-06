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

# Shizuku — AIDL stub ve user service sınıf adları çalışma zamanında
# ComponentName ile aranır, obfuscate edilirse servis bulunamaz.
-keep class com.godofcodes.simappblocker.IAppManager { *; }
-keep class com.godofcodes.simappblocker.IAppManager$Stub { *; }
-keep class com.godofcodes.simappblocker.IAppManager$Stub$Proxy { *; }
-keep class com.godofcodes.simappblocker.AppManagerService { *; }

# Shizuku kütüphanesi
-keep class rikka.shizuku.** { *; }
-keep interface rikka.shizuku.** { *; }