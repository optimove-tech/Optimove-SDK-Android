package com.optimove.sdk.optimove_sdk.main.tools.opti_logger;

import com.optimove.sdk.optimove_sdk.main.events.EventValidationResult;

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

  public static void optipushFailedToGetDeepLinkFromDynamicLink(String dynamicLink, String reason) {
    OptiLoggerStreamsContainer.error("Failed to get deep link out of an Optipush dynamic link url %s due to: %s", dynamicLink, reason);
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


  public static void f82() {
    OptiLoggerStreamsContainer.debug("Optimove.configure() is starting");
  }

  public static void f83() {
    OptiLoggerStreamsContainer.debug("Optimove.configure() was called from a worker thread, moving call to main thread");
  }

  public static void f84() {
    OptiLoggerStreamsContainer.debug("Optimove.configureUrgently() is starting");
  }

  public static void f89(String email) {
    OptiLoggerStreamsContainer.error("Invalid email was received: %s", email);
  }

  public static void f90(String userId) {
    OptiLoggerStreamsContainer.error("Invalid user ID was received: %s, not even going to retry", userId);
  }

  public static void f91(String userId) {
    OptiLoggerStreamsContainer.warn("The provided user ID %s, was already set", userId);
  }

  public static void f95() {
    OptiLoggerStreamsContainer.error("Cannot report event with name null");
  }


  public static void f97(String screenPath) {
    OptiLoggerStreamsContainer.error("Tried to pass illegal screenPath %s to report Screen Visit", screenPath);
  }

  public static void f110() {
    OptiLoggerStreamsContainer.debug("Saving fetched configurations file");
  }


  public static void f115(String configName) {
    OptiLoggerStreamsContainer.debug("Deleted local configurations named %s", configName);
  }

  public static void f116() {
    OptiLoggerStreamsContainer.debug("Configuration file was already loaded, no need to load again");
  }
  public static void configurationsAreAlreadySet() {
    OptiLoggerStreamsContainer.debug("Configuration file was already set, no need to set again");
  }

  public static void f117(int tenantId) {
    OptiLoggerStreamsContainer.debug("Updating the configurations for tenant ID %d", tenantId);
  }


  public static void f122(String e) {
    OptiLoggerStreamsContainer.warn("Failed to get AdvertisingId due to: %s", e);
  }

  public static void f123(String error) {
    OptiLoggerStreamsContainer.warn("Can't report Ad-ID due to: %s", error);
  }

  public static void f124(String action) {
    OptiLoggerStreamsContainer.error("Suspicious action %s was received by the AppUpdateReceiver", action);
  }

  public static void f125() {
    OptiLoggerStreamsContainer.info("Starting on-update background SDK initialization");
  }

  public static void f129(String paramKey, String eventName) {
    OptiLoggerStreamsContainer.error("Mandatory parameter %s in event %s is missing", paramKey, eventName);
  }


  public static void f149() {
    OptiLoggerStreamsContainer.warn("Thread.sleep after dispatching event was interrupted");
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

  public static void f177() {
    OptiLoggerStreamsContainer.error("MD5");
  }

  public static void optipushDeepLinkPersonalizationValuesBadJson() {
    OptiLoggerStreamsContainer.error("Failed to extract deep link values due to bad JSON format");
  }

  public static void optipushDeepLinkFailedToDecodeLinkParam() {
    OptiLoggerStreamsContainer.error("Failed to decode personalized deep link param value");
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
  public static void providedEmailIsNull() {
    OptiLoggerStreamsContainer.error("Email is null");
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
  public static void eventDoesntAppearInConfigs(String eventName){
    OptiLoggerStreamsContainer.error("Event %s doesn't appear in configuration file", eventName);
  }
  public static void eventIsInvalid(String eventName, EventValidationResult validationResult){
    OptiLoggerStreamsContainer.error("Event %s is invalid, %s",eventName, validationResult);
  }
  public static void adIdFetcherFailedFetching(String reason) {
    OptiLoggerStreamsContainer.warn("Failed to get AdvertisingId due to: %s", reason);
  }

}