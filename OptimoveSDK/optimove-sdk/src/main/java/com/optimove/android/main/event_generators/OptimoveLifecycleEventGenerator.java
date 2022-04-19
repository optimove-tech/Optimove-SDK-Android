package com.optimove.android.main.event_generators;

import com.optimove.android.main.common.EventHandlerProvider;
import com.optimove.android.main.common.LifecycleObserver;
import com.optimove.android.main.common.UserInfo;
import com.optimove.android.main.events.core_events.AppOpenEvent;
import com.optimove.android.optistream.OptitrackConstants;

import java.util.Collections;

public class OptimoveLifecycleEventGenerator implements LifecycleObserver.ActivityStopped, LifecycleObserver.ActivityStarted {

    private final EventHandlerProvider eventHandlerProvider;
    private long foregroundSessionEndTime;
    private final UserInfo userInfo;
    private final String fullPackageName;


    public OptimoveLifecycleEventGenerator(EventHandlerProvider eventHandlerProvider, UserInfo userInfo,
                                           String fullPackageName) {
        this.eventHandlerProvider = eventHandlerProvider;
        this.userInfo = userInfo;
        this.fullPackageName = fullPackageName;
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
                        , userInfo.getInstallationId());
        eventHandlerProvider.getEventHandler().reportEvent(Collections.singletonList(appOpenEvent));
        foregroundSessionEndTime = System.currentTimeMillis(); //Reset the timer to prevent duplicate reports
    }
}
