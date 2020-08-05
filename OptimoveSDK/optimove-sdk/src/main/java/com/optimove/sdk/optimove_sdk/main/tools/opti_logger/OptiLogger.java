package com.optimove.sdk.optimove_sdk.main.tools.opti_logger;

public final class OptiLogger {

  /* ************************************************
   * OptiPush Module
   * ************************************************/


  public static void optipushFirebaseProjectInitFailed(String projectName, String reason) {
    OptiLoggerStreamsContainer.error("Failed to init Firebase project %s due to: %s", projectName, reason);
  }

  public static void optipushReceivedUserResponse(boolean isDelete) {
    OptiLoggerStreamsContainer.info("User has responded to Optipush Notification with %s", isDelete ? "dismiss" : "open");
  }

  public static void optipushFailedToDeepLinkToScreen(String deepLink, String reason) {
    OptiLoggerStreamsContainer.error("Failed to redirect user to deep link %s due to: %s", deepLink, reason);
  }

  public static void optipushReceivedNewPushMessage(String pushPayloadJson) {
    OptiLoggerStreamsContainer.debug("New incoming push message with payload: %s", pushPayloadJson);
  }

  public static void optipushFailedToProcessNotification_WhenNotificationManagerIsNull() {
    OptiLoggerStreamsContainer.fatal("Failed to process an Optipush push notification because the Device's Notifications Manager is null");
  }

  public static void optipushNotificationNotPresented_WhenUserIdOptOut() {
    OptiLoggerStreamsContainer.warn("Optipush Notification blocked since the user is opt out");
  }

  public static void optipushNoCustomNotificationIconWasFound() {
    OptiLoggerStreamsContainer.debug("No custom notification icon was found, using default");
  }

  public static void optipushNoCustomNotificationColorWasFound() {
    OptiLoggerStreamsContainer.debug("No custom notification color was found, using default");
  }

  public static void optipushFcmTokenIsAlreadyRefreshing() {
    OptiLoggerStreamsContainer.info("Skipping on token refresh as someone else is already refreshing the token");
  }


  public static void optipushLogNewToken(String newToken) {
    OptiLoggerStreamsContainer.debug("Got new FCM token %s", newToken);
  }

  public static void optipushFailedToGetNewToken(String reason) {
    OptiLoggerStreamsContainer.error("Failed to get new token from InstanceId due to: %s. Will retry later", reason);
  }

  public static void optipushFailedToGetSecondaryToken(String reason) {
    OptiLoggerStreamsContainer.error("Failed to get FCM token for AppController as a secondary app due to: %s. Will retry later", reason);
  }

  public static void configurationsAreAlreadySet() {
    OptiLoggerStreamsContainer.debug("Configuration file was already set, no need to set again");
  }

  public static void utilsFailedToCreateNewFile(String absolutePath, String reason) {
    OptiLoggerStreamsContainer.error("Failed to create file %s due to: %s", absolutePath, reason);
  }

  public static void f157() {
    OptiLoggerStreamsContainer.error("Missing file name to read from");
  }

  public static void f158(String fileName) {
    OptiLoggerStreamsContainer.error("The cache directory has no %s file", fileName);
  }

  public static void f159(String reason) {
    OptiLoggerStreamsContainer.error(reason);
  }

  public static void f160(String reason) {
    OptiLoggerStreamsContainer.error(reason);
  }

  public static void f161(String reason) {
    OptiLoggerStreamsContainer.error(reason);
  }

  public static void f162(String reason) {
    OptiLoggerStreamsContainer.error(reason);
  }

  public static void f163(String reason) {
    OptiLoggerStreamsContainer.error(reason);
  }

  public static void f164() {
    OptiLoggerStreamsContainer.error("Missing a file name to write to");
  }

  public static void f165(String fileName) {
    OptiLoggerStreamsContainer.error("File name %s couldn't be created for write operation", fileName);
  }

  public static void f166(String reason) {
    OptiLoggerStreamsContainer.error(reason);
  }

  public static void f167(String reason) {
    OptiLoggerStreamsContainer.error(reason);
  }

  public static void f168(String reason) {
    OptiLoggerStreamsContainer.error(reason);
  }

  public static void f169(String reason) {
    OptiLoggerStreamsContainer.error(reason);
  }

  public static void f170() {
    OptiLoggerStreamsContainer.error("Parent dir wasn't set when attempting to delete");
  }

  public static void f171() {
    OptiLoggerStreamsContainer.error("Missing a file name to delete");
  }

  public static void f172() {
    OptiLoggerStreamsContainer.fatal("getBuildConfig failed due to: failed to find App BuildConfig class");
  }

  public static void f173(String key) {
    OptiLoggerStreamsContainer.warn("getBuildConfig failed due to: failed to find Optimove SDK flag %s in BuildConfig class", key);
  }

  public static void f174() {
    OptiLoggerStreamsContainer.warn("getBuildConfig failed due to: failed to get value of optimove flag");
  }

  public static void f175(String error) {
    OptiLoggerStreamsContainer.error("getBuildConfig failed due to: %s", error);
  }

  public static void f176() {
    OptiLoggerStreamsContainer.error("SHA1");
  }

  public static void optipushNotificationBitmapFailedToLoad(String url){
    OptiLoggerStreamsContainer.error("Failed to get bitmap from url - %s", url);
  }
  public static void optipushMediaTypeNotImage(String actualType) {
    OptiLoggerStreamsContainer.debug("Notification payload contains media that is not image, image type is: %s",
            actualType);
  }
  public static void providedEmailWasAlreadySet(String email){
    OptiLoggerStreamsContainer.warn("The provided email %s, was already set", email);
  }

  public static void failedToGetRemoteConfigurationFile(String reason){
    OptiLoggerStreamsContainer.error("Failed to get remote configuration file due to - %s", reason);
  }
  public static void failedToGetConfigurationFile(String reason){
    OptiLoggerStreamsContainer.error("Failed to get configuration file due to - %s", reason);
  }

  public static void optimoveInitializationFailedDueToCorruptedTenantInfo(){
    OptiLoggerStreamsContainer.error("Optimove initialization failed due to corrupted tenant info");
  }

  public static void adIdFetcherFailedFetching(String reason) {
    OptiLoggerStreamsContainer.warn("Failed to get AdvertisingId due to: %s", reason);
  }

}