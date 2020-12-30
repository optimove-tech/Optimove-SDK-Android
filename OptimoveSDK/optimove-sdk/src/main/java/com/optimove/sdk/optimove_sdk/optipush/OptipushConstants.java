package com.optimove.sdk.optimove_sdk.optipush;

public interface OptipushConstants {

  interface PushSchemaKeys {

    String DEEP_LINK = "dl";

    String IS_OPTIPUSH = "is_optipush";

    String MEDIA_TYPE_IMAGE = "image";

  }

  interface Notifications {

    int PENDING_INTENT_OPEN_RC = 100;
    int NOTIFICATION_ID = 1001;
    String SDK_NOTIFICATION_CHANNEL_ID = "optimove.sdk.notifications";
    String IS_DELETE_KEY = "is_delete";
    String DYNAMIC_LINK = "dynamic_link";
    String SCHEDULED_IDENTITY_TOKEN = "scheduled_identity_token";
    String TRIGGERED_IDENTITY_TOKEN = "triggered_identity_token";
    String REQUEST_ID = "request_id";


    String CUSTOM_ICON_META_DATA_KEY = "com.optimove.sdk.custom-notification-icon";
    String CUSTOM_COLOR_META_DATA_KEY = "com.optimove.sdk.custom-notification-color";
    int INVALID_CUSTOM_COLOR_VALUE = -1;
  }


  interface Registration {
    String REGISTRATION_PREFERENCES_NAME = "com.optimove.sdk.registration_preferences";

    String LAST_TOKEN_KEY = "lastToken";
    String LAST_NOTIFICATION_PERMISSION_STATUS = "lastNotificationPermissionStatus";
    //the key value remains the same to maintain users that have this value from previous versions
    String SET_INSTALLATION_FAILED_KEY = "tokenUpdateFailedKey";
    String API_V3_SYNCED_KEY = "apiV3SyncedKey";
    String PUSH_CAMPAIGNS_DISABLED_KEY = "pushCampaignsDisabledKey";
    String FAILED_USER_IDS_KEY ="failedUserIdsKey";
    String DEVICE_ID_KEY = "deviceIdKey";



  }
}