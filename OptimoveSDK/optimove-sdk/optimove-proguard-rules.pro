-keep class com.optimove.sdk.optimove_sdk.main.sdk_configs.** { <fields>; }
-keep class com.optimove.sdk.optimove_sdk.optipush.registration.requests.* { <fields>; }
-keep class com.optimove.sdk.optimove_sdk.optistream.OptistreamEvent { <fields>; }
-keep class com.optimove.sdk.optimove_sdk.optipush.messaging.NotificationData { <fields>; }

-keep class * implements com.google.gson.TypeAdapterFactory
-keep class * implements com.google.gson.JsonDeserializer

-keepclassmembers,allowobfuscation class * {
  @com.google.gson.annotations.SerializedName <fields>;
}

-keep,allowobfuscation,allowshrinking class com.google.gson.reflect.TypeToken
-keep,allowobfuscation,allowshrinking class * extends com.google.gson.reflect.TypeToken

-keep class com.google.android.gms.** { *; }
-dontwarn com.google.android.gms.
-keep class com.google.firebase.** { *; }
-dontwarn com.google.firebase.
-keep class android.support.v7.widget.** { *; }
-dontwarn android.support.v7.widget.
-keep class android.support.v4.widget.Space { *; }
-dontwarn android.support.v4.widget.Space
-keep class com.kumulos.** { *; }
-dontwarn com.kumulos.**
-keep class okhttp3.** { *;}
-dontwarn okhttp3.**
-keep class oikio.** { *;}
-dontwarn okio.**
-keep class com.huawei.hms.** { *; }
-dontwarn com.huawei.hms.