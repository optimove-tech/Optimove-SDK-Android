-keep class com.optimove.sdk.optimove_sdk.main.sdk_configs.** { <fields>; }
-keep class com.optimove.sdk.optimove_sdk.optipush.registration.requests.* { <fields>; }
-keep class com.optimove.sdk.optimove_sdk.optitrack.OptistreamEvent { <fields>; }
-keep class com.optimove.sdk.optimove_sdk.optipush.messaging.NotificationData { <fields>; }

-keep class * implements com.google.gson.TypeAdapterFactory
-keep class * implements com.google.gson.JsonDeserializer

-keepclassmembers,allowobfuscation class * {
  @com.google.gson.annotations.SerializedName <fields>;
}

-keep,allowobfuscation,allowshrinking class com.google.gson.reflect.TypeToken
-keep,allowobfuscation,allowshrinking class * extends com.google.gson.reflect.TypeToken