package com.optimove.sdk.optimove_sdk.optipush.firebase;

import com.google.firebase.FirebaseOptions;

class FirebaseKeys {

  private String apiKey;
  private String appId;
  private String dbUrl;
  private String senderId;
  private String storageBucket;
  private String projectId;

  private FirebaseKeys() {
  }

  FirebaseKeys(String apiKey, String appId, String dbUrl, String senderId, String projectId) {
    this.apiKey = apiKey;
    this.appId = appId;
    this.dbUrl = dbUrl;
    this.senderId = senderId;
    this.projectId = projectId;
  }

  String getApiKey() {
    return apiKey;
  }

  String getApplicationId() {
    return appId;
  }

  String getDatabaseUrl() {
    return dbUrl;
  }

  String getGcmSenderId() {
    return senderId;
  }

  public String getStorageBucket() {
    return storageBucket;
  }

  String getProjectId() {
    return projectId;
  }

  FirebaseOptions toFirebaseOptions() {
    return new FirebaseOptions.Builder()
        .setApiKey(apiKey)
        .setApplicationId(appId)
        .setDatabaseUrl(dbUrl)
        .setGcmSenderId(senderId)
        .setStorageBucket(storageBucket)
        .setProjectId(projectId)
        .build();
  }

  static class Builder {

    private FirebaseKeys keysInBuilding;

    Builder() {
      keysInBuilding = new FirebaseKeys();
    }

    Builder setApiKey(String apiKey) {
      keysInBuilding.apiKey = apiKey;
      return this;
    }

    Builder setApplicationId(String applicationId) {
      keysInBuilding.appId = applicationId;
      return this;
    }

    Builder setDatabaseUrl(String databaseUrl) {
      keysInBuilding.dbUrl = databaseUrl;
      return this;
    }

    Builder setGcmSenderId(String gcmSenderId) {
      keysInBuilding.senderId = gcmSenderId;
      return this;
    }

    Builder setStorageBucket(String storageBucket) {
      keysInBuilding.storageBucket = storageBucket;
      return this;
    }

    Builder setProjectId(String projectId) {
      keysInBuilding.projectId = projectId;
      return this;
    }

    FirebaseKeys build() {
      return keysInBuilding;
    }
  }
}
