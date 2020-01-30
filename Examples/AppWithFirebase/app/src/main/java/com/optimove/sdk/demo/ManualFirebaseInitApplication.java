package com.optimove.sdk.demo;

import android.app.Application;

import com.google.firebase.FirebaseApp;
import com.optimove.sdk.optimove_sdk.main.Optimove;
import com.optimove.sdk.optimove_sdk.main.TenantInfo;

// Implement this class only if you manually initialize the Firebase SDK (if you don't use the google-service.json file)
public class ManualFirebaseInitApplication extends Application {

  @Override
  public void onCreate() {
    super.onCreate();
    // It's crucial that you maintain the following order:
    // First, initialize the Firebase SDK
    FirebaseApp.initializeApp(this);
    // Then, initialize the Optimove SDK
    Optimove.configure(this, new TenantInfo("<YOUR_OPTIMOVE_TENANT_TOKEN>", "<YOUR_OPTIMOVE_CONFIG_NAME>"));
  }
}
