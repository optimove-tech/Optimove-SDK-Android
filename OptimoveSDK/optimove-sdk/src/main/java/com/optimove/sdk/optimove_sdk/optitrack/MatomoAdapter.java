package com.optimove.sdk.optimove_sdk.optitrack;

import android.content.Context;

import org.matomo.sdk.Matomo;
import org.matomo.sdk.TrackMe;
import org.matomo.sdk.Tracker;
import org.matomo.sdk.TrackerBuilder;
import org.matomo.sdk.extra.TrackHelper;

import java.util.List;

import static com.optimove.sdk.optimove_sdk.optitrack.OptitrackConstants.DEFAULT_DISPATCH_TIMEOUT;


public class MatomoAdapter implements OptitrackAdapter {

    private Tracker mainTracker;

    @Override
    public void setup(String apiUrl, int siteId, Context context) {
        //String optitrackEndpoint = optitrackMetadata.getOptitrackEndpoint();
        if (!apiUrl.endsWith("/piwik.php")) {
            if (apiUrl.endsWith("/")) {
                // "optitrackEndpoint" here is https://sometracker.optimove.net/
                apiUrl = String.format("%s%s", apiUrl, "piwik.php");
            } else {
                // "optitrackEndpoint" here is https://sometracker.optimove.net
                apiUrl = String.format("%s/%s", apiUrl, "piwik.php");
            }
        }
        // "optitrackEndpoint" here is https://sometracker.optimove.net/piwik.php
        mainTracker = TrackerBuilder.createDefault(apiUrl, siteId)
                .build(Matomo.getInstance(context));
        mainTracker.setDispatchTimeout(DEFAULT_DISPATCH_TIMEOUT);
    }
    @Override
    public void dispatch() {
        mainTracker.dispatch();
    }

    @Override
    public void reportScreenVisit(String initialVisitorId, String screenPath, String screenTitle){
        TrackHelper.track(new TrackMe()).screen(screenPath).title(screenTitle).with(mainTracker);

    }
    @Override
    public void setDispatchTimeout(int timeout ) {
        mainTracker.setDispatchTimeout(timeout);
    }
    @Override
    public void track(String category, String action, List<CustomDimension> customDimensions, String initialVisitorId){
        TrackHelper trackHelper = TrackHelper.track(new TrackMe());

        for(CustomDimension customDimension : customDimensions) {
            trackHelper.dimension(customDimension.getId(),customDimension.getValue());
        }
        trackHelper.event(category,action).with(mainTracker);

    }

    @Override
    public void setUserId(String userId){
        mainTracker.setUserId(userId);
    }
    @Override
    public void setVisitorId(String visitorId){
        mainTracker.setVisitorId(visitorId);
    }

}
