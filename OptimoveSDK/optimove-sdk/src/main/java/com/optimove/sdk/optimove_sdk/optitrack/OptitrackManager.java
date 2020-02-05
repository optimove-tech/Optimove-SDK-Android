package com.optimove.sdk.optimove_sdk.optitrack;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.annotation.NonNull;

import com.optimove.sdk.optimove_sdk.main.LifecycleObserver;
import com.optimove.sdk.optimove_sdk.main.UserInfo;
import com.optimove.sdk.optimove_sdk.main.events.OptimoveEvent;
import com.optimove.sdk.optimove_sdk.main.events.core_events.SetPageVisitEvent;
import com.optimove.sdk.optimove_sdk.main.events.core_events.SetUserIdEvent;
import com.optimove.sdk.optimove_sdk.main.events.decorators.OptimoveEventDecorator;
import com.optimove.sdk.optimove_sdk.main.sdk_configs.configs.OptitrackConfigs;
import com.optimove.sdk.optimove_sdk.main.sdk_configs.reused_configs.CustomDimensionIds;
import com.optimove.sdk.optimove_sdk.main.sdk_configs.reused_configs.EventConfigs;
import com.optimove.sdk.optimove_sdk.main.tools.opti_logger.OptiLoggerStreamsContainer;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static com.optimove.sdk.optimove_sdk.optitrack.OptitrackConstants.OPTITRACK_SP_NAME;
import static com.optimove.sdk.optimove_sdk.optitrack.OptitrackConstants.OPTITRACK_USER_ID_KEY;


public final class OptitrackManager implements LifecycleObserver.ActivityStopped  {

    @NonNull
    private OptitrackAdapter optitrackAdapter;
    @NonNull
    private OptitrackConfigs optitrackConfigs;
    @NonNull
    private SharedPreferences optitrackPreferences;
    @NonNull
    private UserInfo userInfo;
    @NonNull
    private Map<String, EventConfigs> eventConfigsMap;
    @NonNull
    private LifecycleObserver lifecycleObserver;
    @NonNull
    private Context context;


    private boolean initialized = false;

    public OptitrackManager(@NonNull OptitrackAdapter optitrackAdapter,
                            @NonNull OptitrackConfigs optitrackConfigs,
                            @NonNull UserInfo userInfo,
                            @NonNull  Map<String, EventConfigs> eventConfigsMap,
                            @NonNull LifecycleObserver lifecycleObserver,
                            @NonNull Context context) {
        this.optitrackAdapter = optitrackAdapter;
        this.optitrackPreferences = context.getSharedPreferences(OPTITRACK_SP_NAME, Context.MODE_PRIVATE);
        this.userInfo = userInfo;
        this.optitrackConfigs = optitrackConfigs;
        this.eventConfigsMap = eventConfigsMap;
        this.lifecycleObserver = lifecycleObserver;
        this.context = context;
    }

    private synchronized void ensureInitialization(){
        if (!initialized) {
            this.initialized = true;
            this.optitrackAdapter.setup(optitrackConfigs.getOptitrackEndpoint(), optitrackConfigs.getSiteId(),
                    context);
            this.syncTrackerUserIdWithSdk();
            this.syncTrackerVisitorIdWithSdk();
            //this will not cause a leak because this component is tied to the app context
            this.lifecycleObserver.addActivityStoppedListener(this);
        }
    }

    @Override
    public void activityStopped() {
        this.sendAllEventsNow();
    }

    /**
     * Matomo's SDK will generate a new visitorId for each session, causing inconsistent <b>Visitor Tracking</b>.
     * For that reason each session the SDK will set the tracker's {@code visitorId} to be its <b>consistent</b> {@code visitorId}
     */
    private void syncTrackerVisitorIdWithSdk() {
        optitrackAdapter.setVisitorId(userInfo.getVisitorId());
    }

    /**
     * On the first session Matomo's SDK will assign a random {@code userId} to the current user.<br>
     * To enable correct <b>Visitor Tracking</b> the SDK must always keep its {@code userId} in sync with the Tracker's.
     * <p><b>Discussion</b>:<br>
     * When the user is still a visitor, his {@code userId} must be null. This is not the case in the current version of the Matomo SDK (v3.0.3).<br>
     * It sets the initial {@code userId} to be a UUID and even if we manually set the {@code userId} to be null, at the next creation of the {@code Tracker}
     * it will detect that the saved {@code userId} was null and <b>overwrite</b> it again to be a UUID.<br>
     * For that reason the {@code OptitrackManager} also manages a locally stored {@code userId} in its {@code SharedPreferences} that gets updated here and queried during each
     * initialization to check for a difference between Optitrack's {@code userId} and Optimove's {@code userId}.
     * </p>
     */
    private void syncTrackerUserIdWithSdk() {
        String userId = userInfo.getUserId();
        String trackerUserId = optitrackPreferences.getString(OPTITRACK_USER_ID_KEY, null);
        if (userId == null) { // The user has still not converted. Check the setUserId method in OptiTrack (above) for more info about why not using the mainTracker.getUserId()
            optitrackAdapter.setUserId(null);
        } else if (trackerUserId == null || !trackerUserId.equals(userId)) {
            String updatedVisitorId = userInfo.getVisitorId();
            EventConfigs setUserIdEventConfig = eventConfigsMap.get(SetUserIdEvent.EVENT_NAME);
            Objects.requireNonNull(setUserIdEventConfig);
            reportEvent(new OptimoveEventDecorator(new SetUserIdEvent(userInfo.getInitialVisitorId(), userId, updatedVisitorId),
                    setUserIdEventConfig),setUserIdEventConfig);
        }
    }



    public void reportEvent(OptimoveEvent event, EventConfigs eventConfig) {
        this.ensureInitialization();
        //Only predefined events are not dependent on OptiTrack SDK Permission
        OptiLoggerStreamsContainer.debug("Reporting event named: %s, with id: %s, with params: %s", event.getName(), String.valueOf(eventConfig.getId()),
                Arrays.deepToString(event.getParameters()
                        .values()
                        .toArray()));
        if (event.getName().equals(SetUserIdEvent.EVENT_NAME)) {
            String reportedUserId = String.valueOf(event.getParameters().get(SetUserIdEvent.USER_ID_PARAM_KEY));
            optitrackPreferences.edit()
                    .putString(OPTITRACK_USER_ID_KEY, reportedUserId)
                    .apply();
            optitrackAdapter.setUserId(reportedUserId);
        } else if (event.getName().equals(SetPageVisitEvent.EVENT_NAME)) {
            optitrackAdapter.reportScreenVisit(userInfo.getInitialVisitorId(),
                    String.valueOf(event.getParameters()
                            .get(SetPageVisitEvent.CUSTOM_URL_PARAM_KEY)),
                    String.valueOf(event.getParameters()
                            .get(SetPageVisitEvent.PAGE_TITLE_PARAM_KEY)));
        }
        sendEvent(event, eventConfig);

    }


    /**
     * Set the Optitrack event report timeout
     *
     * @param timeout the timeout in milliseconds
     */
    public void setTimeout(int timeout) {
        this.ensureInitialization();
        optitrackAdapter.setDispatchTimeout(timeout);
    }
    public void sendAllEventsNow() {
        this.ensureInitialization();
        optitrackAdapter.dispatch();
    }

    private void sendEvent(OptimoveEvent event, EventConfigs eventConfig) {
        // mapping params to custom dimensions
        List<CustomDimension> customDimensions = new ArrayList<>();
        CustomDimensionIds customDimensionIds = optitrackConfigs.getCustomDimensionIds();
        customDimensions.add(new CustomDimension(customDimensionIds.getEventIdCustomDimensionId(),
                String.valueOf(eventConfig.getId())));
        customDimensions.add(new CustomDimension(customDimensionIds.getEventNameCustomDimensionId(), event.getName()));
        Map<String, Object> eventParams = event.getParameters();
        for (String paramName : eventParams.keySet()) {
            EventConfigs.ParameterConfig parameterConfig = eventConfig.getParameterConfigs()
                    .get(paramName);
            Objects.requireNonNull(parameterConfig);
            Object paramValue = eventParams.get(paramName);
            if (paramValue == null) {
                continue;
            }
            int dimensionId = parameterConfig.getDimensionId();
            if (dimensionId <= customDimensionIds.getMaxVisitCustomDimensions() + customDimensionIds.getMaxActionCustomDimensions()) {
                // Insert params only within the allowed range of dimension IDs
                customDimensions.add(new CustomDimension(dimensionId, paramValue.toString()));
            }
        }

        optitrackAdapter.track(optitrackConfigs.getEventCategoryName(), event.getName(), customDimensions,
                userInfo.getInitialVisitorId());
    }

}
