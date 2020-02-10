package com.optimove.sdk.optimove_sdk.optipush;

public interface OptipushConstants {

  String OPTIPUSH_SP_NAME = "com.optimove.sdk.optipush_sp_name";


  interface PushSchemaKeys {

    String TITLE = "title";
    String BODY = "content";
    String DEEP_LINK_PERSONALIZATION_VALUES = "deep_link_personalization_values";
    String DYNAMIC_LINKS = "dynamic_links";
    String ANDROID_DYNAMIC_LINKS = "android";

    String IS_OPTIPUSH = "is_optipush";

    String MEDIA_TYPE_IMAGE = "image";

  }

  interface Notifications {

    int PENDING_INTENT_OPEN_RC = 100;
    int PENDING_INTENT_DELETE_RC = 101;
    int NOTIFICATION_ID = 1001;
    String SDK_NOTIFICATION_CHANNEL_ID = "optimove.sdk.notifications";
    String IS_DELETE_KEY = "is_delete";
    String DYNAMIC_LINK = "dynamic_link";
    //String CAMPAIGN_ID_CARD = "campaign_id_card";
    String SCHEDULED_CAMPAIGN_CARD = "scheduled_campaign_card";
    String TRIGGERED_CAMPAIGN_CARD = "triggered_campaign_card";


    String CUSTOM_ICON_META_DATA_KEY = "com.optimove.sdk.custom-notification-icon";
    String CUSTOM_COLOR_META_DATA_KEY = "com.optimove.sdk.custom-notification-color";
    int INVALID_CUSTOM_COLOR_VALUE = -1;
  }

  interface Firebase {

    String TOPICS_PREFERENCES_NAME = "com.optimove.sdk.topics_preferences";
    String SDK_CONTROLLER_PROJECT_NAME = "optimove.sdk.sdk_controller";
    String APP_CONTROLLER_PROJECT_NAME = "optimove.sdk.app_controller";
  }

  interface Registration {
    String REGISTRATION_PREFERENCES_NAME = "com.optimove.sdk.registration_preferences";

    String LAST_TOKEN_KEY = "lastToken";
   // String LAST_OPT_STATUS_KEY = "lastOptStatus";
    String LAST_NOTIFICATION_PERMISSION_STATUS = "lastNotificationPermissionStatus";
    String TOKEN_REFRESH_FAILED_KEY = "last_refresh_failed_key";
    //the key value remains the same to maintain users that have this value from previous versions
    String SET_INSTALLATION_FAILED_KEY = "tokenUpdateFailedKey";
    String FAILED_USER_IDS_KEY ="failedUserIdsKey";
    String DEVICE_ID_KEY = "deviceIdKey";



  }
}