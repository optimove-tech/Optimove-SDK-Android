package com.optimove.android.main.common;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.optimove.android.Optimove;
import com.optimove.android.main.tools.OptiUtils;

import java.util.UUID;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import static android.content.Context.MODE_PRIVATE;
import static com.optimove.android.main.common.UserInfo.UserInfoConstants.INITIAL_VISITOR_ID_KEY;
import static com.optimove.android.main.common.UserInfo.UserInfoConstants.INSTALLATION_ID_KEY;
import static com.optimove.android.main.common.UserInfo.UserInfoConstants.USER_EMAIL_KEY;
import static com.optimove.android.main.common.UserInfo.UserInfoConstants.USER_IDS_SP;
import static com.optimove.android.main.common.UserInfo.UserInfoConstants.USER_ID_KEY;
import static com.optimove.android.main.common.UserInfo.UserInfoConstants.VISITOR_ID_KEY;
import static com.optimove.android.realtime.RealtimeConstants.FIRST_VISIT_TIMESTAMP_KEY;
import static com.optimove.android.realtime.RealtimeConstants.REALTIME_SP_NAME;

/**
 * Represents the current device user.
 * Stored as member of the {@link Optimove} <b>Singleton</b>, thus must have its members managed in a thread safe manner.
 */
public class UserInfo {

  private final ReentrantReadWriteLock userInfoLock = new ReentrantReadWriteLock();
  private final ReentrantReadWriteLock.ReadLock readLock = userInfoLock.readLock();
  private final ReentrantReadWriteLock.WriteLock writeLock = userInfoLock.writeLock();

  @Nullable
  private String userId;
  private String visitorId;
  private String initialVisitorId;
  private String userEmail;
  private String installationId;
  private long firstVisitorDate;
  private SharedPreferences userIdsSp;

  private UserInfo() {
    this.userId = null;
    this.visitorId = null;
    this.initialVisitorId = null;
    this.userEmail = null;
    this.installationId = null;
  }

  public static UserInfo newInstance(Context context) {
    UserInfo userInfo = new UserInfo();
    userInfo.userIdsSp = context.getSharedPreferences(USER_IDS_SP, Context.MODE_PRIVATE);

    userInfo.userId = userInfo.userIdsSp.getString(USER_ID_KEY, null);
    userInfo.userEmail = userInfo.userIdsSp.getString(USER_EMAIL_KEY,null);

    userInfo.visitorId = userInfo.userIdsSp.getString(VISITOR_ID_KEY, null);
    if (userInfo.visitorId == null) { // The very first session
      userInfo.setVisitorId(UUID.randomUUID().toString().replaceAll("-", ""));
    }

    userInfo.initialVisitorId = userInfo.userIdsSp.getString(INITIAL_VISITOR_ID_KEY, null);
    if (userInfo.initialVisitorId == null) { // The very first session
      userInfo.initialVisitorId = userInfo.visitorId;
      userInfo.userIdsSp.edit().putString(INITIAL_VISITOR_ID_KEY, userInfo.initialVisitorId).apply();
    }

    String deviceIdAsStoredInUserInfoSp = userInfo.userIdsSp.getString(INSTALLATION_ID_KEY, null);
    if (deviceIdAsStoredInUserInfoSp != null){
      userInfo.installationId = deviceIdAsStoredInUserInfoSp;
    } else {
      //get installationId as generated in registration sp
      //todo - remove this in few months after 02.20
      SharedPreferences registrationPreferences =
              context.getSharedPreferences(Registration.REGISTRATION_PREFERENCES_NAME,
              MODE_PRIVATE);
      String installationIdAsAlreadyStoredInRegistration = registrationPreferences.getString(Registration.DEVICE_ID_KEY, null);
      if(installationIdAsAlreadyStoredInRegistration != null) {
        userInfo.userIdsSp.edit().putString(INSTALLATION_ID_KEY, installationIdAsAlreadyStoredInRegistration).apply();
        userInfo.installationId = installationIdAsAlreadyStoredInRegistration;
      } else {
        String newGeneratedInstallationId = UUID.randomUUID().toString();

        userInfo.userIdsSp.edit().putString(INSTALLATION_ID_KEY, newGeneratedInstallationId).apply();
        userInfo.installationId = newGeneratedInstallationId;
      }
    }

    //first visit for realtime
    SharedPreferences backwardCompatibleSharedPrefs  = context.getSharedPreferences(REALTIME_SP_NAME,
            Context.MODE_PRIVATE);
    if (backwardCompatibleSharedPrefs.contains(FIRST_VISIT_TIMESTAMP_KEY)) {
      userInfo.firstVisitorDate = backwardCompatibleSharedPrefs.getLong(FIRST_VISIT_TIMESTAMP_KEY,
              OptiUtils.currentTimeSeconds());
    } else {
      userInfo.firstVisitorDate = OptiUtils.currentTimeSeconds();
      backwardCompatibleSharedPrefs.edit()
              .putLong(FIRST_VISIT_TIMESTAMP_KEY, userInfo.firstVisitorDate)
              .apply();
    }

    return userInfo;
  }



  public String getInstallationId(){
    return installationId;
  }
  public long getFirstVisitorDate(){
    return firstVisitorDate;
  }

  @Nullable
  public String getUserId() {
    readLock.lock();
    String userId = this.userId;
    readLock.unlock();
    return userId;
  }
  @Nullable
  public String getEmail() {
    readLock.lock();
    String userEmail = this.userEmail;
    readLock.unlock();
    return userEmail;
  }

  /**
   * Sets the current {@code user ID} of the device user in a thread safe manner.
   *
   * @param userId the user ID to set
   */
  public void setUserId(@Nullable String userId) {
    writeLock.lock();
    try {
      this.userId = userId;
      userIdsSp.edit().putString(USER_ID_KEY, userId).apply();
    } finally {
      writeLock.unlock();
    }
  }
  /**
   * Sets the current {@code email} of the user in a thread safe manner.
   *
   * @param userEmail the email to set
   */
  public void setEmail(@NonNull String userEmail) {
    writeLock.lock();
    try {
      this.userEmail = userEmail;
      userIdsSp.edit().putString(USER_EMAIL_KEY, userEmail).apply();
    } finally {
      writeLock.unlock();
    }
  }

  public String getVisitorId() {
    readLock.lock();
    String visitorId = this.visitorId;
    readLock.unlock();
    return visitorId;
  }

  /**
   * Sets the current {@code visitor ID} of the device user in a thread safe manner.
   *
   * @param visitorId the visitor ID to set
   */
  public void setVisitorId(String visitorId) {
    writeLock.lock();
    try {
      this.visitorId = visitorId;
      userIdsSp.edit().putString(VISITOR_ID_KEY, visitorId).apply();
    } finally {
      writeLock.unlock();
    }
  }

  /**
   * The very first visitorId to be assigned to the end user
   *
   * @return the initial visitor ID
   */
  public String getInitialVisitorId() {
    return initialVisitorId;
  }

  interface UserInfoConstants {

    String USER_IDS_SP = "com.optimove.sdk.user_ids";
    String USER_ID_KEY = "userId";
    String VISITOR_ID_KEY = "visitorId";
    String INITIAL_VISITOR_ID_KEY = "initial_visitor_id";
    String USER_EMAIL_KEY = "userEmail";
    String INSTALLATION_ID_KEY = "installationIdKey";
  }

  interface Registration {
    String REGISTRATION_PREFERENCES_NAME = "com.optimove.sdk.registration_preferences";
    String DEVICE_ID_KEY = "deviceIdKey";
  }

}
