package com.optimove.sdk.optimove_sdk.optitrack;

import android.content.Context;

import java.util.List;

public interface OptitrackAdapter {

    void setup(String apiUrl, int siteId, Context context);

    void dispatch();

    void reportScreenVisit(String initialVisitorId, String screenPath, String screenTitle);

    void setDispatchTimeout(int timeout);

    void track(String category, String action, List<CustomDimension> customDimensions, String initialVisitorId);

    void setUserId(String userId);

    void setVisitorId(String visitorId);

}
