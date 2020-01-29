package com.optimove.sdk.optimove_sdk.main.event_generators;

import com.optimove.sdk.optimove_sdk.main.EventContext;
import com.optimove.sdk.optimove_sdk.main.EventHandlerProvider;
import com.optimove.sdk.optimove_sdk.main.LifecycleObserver;
import com.optimove.sdk.optimove_sdk.main.UserInfo;
import com.optimove.sdk.optimove_sdk.main.events.core_events.AppOpenEvent;
import com.optimove.sdk.optimove_sdk.main.tools.InstallationIDProvider;
import com.optimove.sdk.optimove_sdk.optitrack.OptitrackConstants;

public class OptimoveLifecycleEventGenerator implements LifecycleObserver.ActivityStopped, LifecycleObserver.ActivityStarted {

    private EventHandlerProvider eventHandlerProvider;
    private long foregroundSessionEndTime;
    private UserInfo userInfo;
    private String fullPackageName;
    private InstallationIDProvider installationIDProvider;

    public OptimoveLifecycleEventGenerator(EventHandlerProvider eventHandlerProvider, UserInfo userInfo,
                                           String fullPackageName, InstallationIDProvider installationIDProvider) {
        this.eventHandlerProvider = eventHandlerProvider;
        this.userInfo = userInfo;
        this.fullPackageName = fullPackageName;
        this.installationIDProvider = installationIDProvider;
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
                        , installationIDProvider.getInstallationID());
        eventHandlerProvider.getEventHandler().reportEvent(new EventContext(appOpenEvent));
        foregroundSessionEndTime = System.currentTimeMillis(); //Reset the timer to prevent duplicate reports
    }
}
