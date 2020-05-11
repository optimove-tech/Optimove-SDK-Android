package com.optimove.sdk.optimove_sdk.main.event_generators;

import android.content.SharedPreferences;

import com.optimove.sdk.optimove_sdk.main.EventHandlerProvider;
import com.optimove.sdk.optimove_sdk.main.LifecycleObserver;
import com.optimove.sdk.optimove_sdk.main.UserInfo;
import com.optimove.sdk.optimove_sdk.main.events.core_events.AppOpenEvent;
import com.optimove.sdk.optimove_sdk.main.events.core_events.OptipushOptIn;
import com.optimove.sdk.optimove_sdk.main.events.core_events.OptipushOptOut;
import com.optimove.sdk.optimove_sdk.main.tools.DeviceInfoProvider;
import com.optimove.sdk.optimove_sdk.main.tools.OptiUtils;
import com.optimove.sdk.optimove_sdk.optitrack.OptitrackConstants;

import java.util.Collections;
import java.util.concurrent.TimeUnit;

import static com.optimove.sdk.optimove_sdk.optitrack.OptitrackConstants.LAST_OPT_REPORTED_KEY;
import static com.optimove.sdk.optimove_sdk.optitrack.OptitrackConstants.LAST_REPORTED_OPT_IN;
import static com.optimove.sdk.optimove_sdk.optitrack.OptitrackConstants.LAST_REPORTED_OPT_OUT;

public class OptimoveLifecycleEventGenerator implements LifecycleObserver.ActivityStopped, LifecycleObserver.ActivityStarted {

    private int optInOutExecutionTimeout = (int) TimeUnit.SECONDS.toMillis(5);

    private EventHandlerProvider eventHandlerProvider;
    private long foregroundSessionEndTime;
    private UserInfo userInfo;
    private String fullPackageName;
    private SharedPreferences optitrackPreferences;
    private DeviceInfoProvider deviceInfoProvider;


    public OptimoveLifecycleEventGenerator(EventHandlerProvider eventHandlerProvider, UserInfo userInfo,
                                           String fullPackageName,
                                           SharedPreferences optitrackPreferences,
                                           DeviceInfoProvider deviceInfoProvider) {
        this.eventHandlerProvider = eventHandlerProvider;
        this.userInfo = userInfo;
        this.fullPackageName = fullPackageName;
        this.optitrackPreferences = optitrackPreferences;
        this.deviceInfoProvider = deviceInfoProvider;
        this.foregroundSessionEndTime = -1;
    }

    @Override
    public void activityStopped() {
        foregroundSessionEndTime = System.currentTimeMillis();
    }

    @Override
    public void activityStarted() {
        if (isNewForegroundSession()) {
            reportAppOpenEvent();
        }
        reportOptInOrOut();
    }
    private boolean isNewForegroundSession() {
        if (foregroundSessionEndTime == -1) {
            return true;
        }
        long backgroundSaturationDeadline = OptitrackConstants.SESSION_DURATION_MILLIS + foregroundSessionEndTime;
        return System.currentTimeMillis() > backgroundSaturationDeadline;
    }

    private void reportAppOpenEvent() {

        AppOpenEvent appOpenEvent =
                new AppOpenEvent(userInfo.getUserId(), userInfo.getVisitorId(), fullPackageName
                        , userInfo.getInstallationId());
        eventHandlerProvider.getEventHandler().reportEvent(Collections.singletonList(appOpenEvent));
        foregroundSessionEndTime = System.currentTimeMillis(); //Reset the timer to prevent duplicate reports
    }
    private void reportOptInOrOut() {
        int lastReportedOpt = optitrackPreferences.getInt(LAST_OPT_REPORTED_KEY, -1);
        if (lastReportedOpt == -1) {
            eventHandlerProvider.getEventHandler()
                    .reportEvent(Collections.singletonList(new OptipushOptIn(fullPackageName,
                            userInfo.getInstallationId(), OptiUtils.currentTimeSeconds())));
            optitrackPreferences.edit()
                    .putInt(LAST_OPT_REPORTED_KEY, LAST_REPORTED_OPT_IN)
                    .apply();
        } else {
            boolean wasOptIn = lastReportedOpt == LAST_REPORTED_OPT_IN;

            boolean currentlyOptIn = deviceInfoProvider.notificaionsAreEnabled();
            if (wasOptIn == currentlyOptIn) {
                return;
            }
            eventHandlerProvider.getEventHandler()
                    .reportEvent(currentlyOptIn ?
                            Collections.singletonList(new OptipushOptIn(fullPackageName, userInfo.getInstallationId(),
                                    OptiUtils.currentTimeSeconds())) :
                            Collections.singletonList(new OptipushOptOut(fullPackageName,
                                    userInfo.getInstallationId(), OptiUtils.currentTimeSeconds())));
            optitrackPreferences.edit()
                    .putInt(LAST_OPT_REPORTED_KEY, currentlyOptIn ? LAST_REPORTED_OPT_IN : LAST_REPORTED_OPT_OUT)
                    .apply();
        }
    }
}
