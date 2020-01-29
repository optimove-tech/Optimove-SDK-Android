package com.optimove.sdk.optimove_sdk.optipush.firebase;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.Nullable;

import com.optimove.sdk.optimove_sdk.main.SdkOperationListener;
import com.optimove.sdk.optimove_sdk.optipush.OptipushConstants;

/**
 * Encapsulates Topic requests dispatching logic:
 * <ul>
 * <li>Request type: register/unregister</li>
 * <li>Does client have Firebase</li>
 * <li>Handling topics batches</li>
 * </ul>
 */
public abstract class TopicsRegistrar {

  protected Context context;
  protected SharedPreferences topicsPreferences;
  @Nullable
  protected SdkOperationListener operationListener;

  protected TopicsRegistrar(Context context, @Nullable SdkOperationListener operationListener) {
    this.context = context;
    this.topicsPreferences = context.getSharedPreferences(OptipushConstants.Firebase.TOPICS_PREFERENCES_NAME, Context.MODE_PRIVATE);
    this.operationListener = operationListener;
  }

  public abstract void registerToTopics(String... topics);

  public abstract void unregisterFromTopics(String... topics);

  protected void notifyOperationListener(boolean success) {
    if (operationListener != null)
      new Handler(Looper.getMainLooper()).post(() -> operationListener.onResult(success));
  }
}
