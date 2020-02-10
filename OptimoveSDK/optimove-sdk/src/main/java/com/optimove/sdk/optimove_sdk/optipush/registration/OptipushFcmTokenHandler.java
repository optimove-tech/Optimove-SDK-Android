package com.optimove.sdk.optimove_sdk.optipush.registration;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;

import com.google.firebase.FirebaseApp;
import com.google.firebase.iid.FirebaseInstanceId;
import com.optimove.sdk.optimove_sdk.main.Optimove;
import com.optimove.sdk.optimove_sdk.main.tools.opti_logger.OptiLogger;
import com.optimove.sdk.optimove_sdk.optipush.OptipushConstants;
import com.optimove.sdk.optimove_sdk.optipush.OptipushHandler;
//import com.optimove.sdk.optimove_sdk.optipush.firebase.OptimoveFirebaseInteractor;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicBoolean;

public class OptipushFcmTokenHandler {

  // Don't forget to set back to false once processing is done
  private static AtomicBoolean isRunning = new AtomicBoolean(false);

  private RegistrationDao registrationDao;
  private String lastToken;
  private String newToken;

  public OptipushFcmTokenHandler() {
    this(Optimove.getInstance().getApplicationContext());
  }

  public OptipushFcmTokenHandler(Context context) {
    registrationDao = new RegistrationDao(context.getApplicationContext());
    lastToken = null;
    newToken = null;
  }

  public void completeLastTokenRefreshIfFailed() {
    if (registrationDao.isTokenRefreshMarkedAsFailed()) {
      onTokenRefresh();
    }
  }

  /**
   * A callback informing the Optimove SDK that a new Instance ID Token is available.
   */
  public void onTokenRefresh() {
    if (!isRunning.compareAndSet(false, true)) {
      OptiLogger.optipushFcmTokenIsAlreadyRefreshing();
      return;
    }
    processToken();
  }

  private void processToken(){
    FirebaseInstanceId.getInstance().getInstanceId()
            .addOnSuccessListener(instanceIdResult -> {
              registrationDao.editFlags().unmarkTokenRefreshAsFailed().save();
              this.newToken = instanceIdResult.getToken();
              OptiLogger.optipushLogNewToken(newToken);

              lastToken = registrationDao.getLastToken();
              Context context = Optimove.getInstance().getApplicationContext();
              boolean clientHasDefaultFirebaseApp = FirebaseApp.getApps(context).size() > 1;
              if (clientHasDefaultFirebaseApp) {
                fetchNewSecondaryToken();
              } else {
                proceedOnlyIfTokenWasChanged();
              }
            })
            .addOnFailureListener(e -> {
              OptiLogger.optipushFailedToGetNewToken(e.getMessage());
              registrationDao.editFlags().markTokenRefreshAsFailed().save();
              isRunning.set(false);
            });
  }


  private void fetchNewSecondaryToken() {
    FirebaseApp firebaseApp;
    try {
      firebaseApp = FirebaseApp.getInstance(OptipushConstants.Firebase.APP_CONTROLLER_PROJECT_NAME);
    } catch (Exception e) {
      OptiLogger.optipushFailedToGetSecondaryToken(e.getMessage());
      registrationDao.editFlags().markTokenRefreshAsFailed().save();
      isRunning.set(false);
      return;
    }
    String gcmSenderId = firebaseApp.getOptions().getGcmSenderId();
    new Thread(() -> {
      try {
        newToken = FirebaseInstanceId.getInstance().getToken(gcmSenderId, "FCM");
        new Handler(Looper.getMainLooper()).post(this::proceedOnlyIfTokenWasChanged);
      } catch (IOException e) {
        OptiLogger.optipushFailedToGetSecondaryToken(e.getMessage());
        registrationDao.editFlags().markTokenRefreshAsFailed().save();
        isRunning.set(false);
      }
    }).start();
  }

  private void proceedOnlyIfTokenWasChanged() {
    // New token member must be updated before calling this method
    boolean isNewToken = lastToken == null || !lastToken.equals(newToken);
    if (!isNewToken) {
      isRunning.set(false);
      return;
    }

    OptipushHandler optipushHandler = Optimove.getInstance().getOptipushHandlerProvider().getOptipushHandler();

    // First update token so that any call to "getLastToken" will work
    registrationDao.editFlags().putNewToken(newToken).save();
    optipushHandler.tokenWasChanged();
    isRunning.set(false);
  }



}
