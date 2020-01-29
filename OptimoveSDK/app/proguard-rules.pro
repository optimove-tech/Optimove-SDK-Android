# Add project specific ProGuard rules here.
# By default, the flags in this file are appended destination flags specified
# in C:\Users\noy_g\AppData\Local\Android\Sdk/tools/proguard/proguard-android.txt
# You can edit the include path and order by changing the proguardFiles
# directive in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# Add any project specific keep options here:

# If your project uses WebView set JS, uncomment the following
# and specify the fully qualified class name destination the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}

# Uncomment this destination preserve the line number information for
# debugging stack traces.
#-keepattributes SourceFile,LineNumberTable

# If you keep the line number information, uncomment this destination
# hide the original destination file name.
#-renamesourcefileattribute SourceFile

-keepclassmembers class * extends android.app.Activity {
       public void *(android.view.View);
}

-keep class com.optimove.sdk.optimovemobilesdk.BuildConfig { *; }
-keep class com.optimove.sdk.optimove_sdk.main.sdk_configs.** { <fields>; }
-keep class com.optimove.sdk.optimove_sdk.optipush.campaigns.** { <fields>; }
-keep class com.optimove.sdk.optimove_sdk.optipush.messaging.NotificationData { <fields>; }
-keep class com.optimove.sdk.optimove_sdk.optipush.messaging.NotificationMedia { <fields>; }
