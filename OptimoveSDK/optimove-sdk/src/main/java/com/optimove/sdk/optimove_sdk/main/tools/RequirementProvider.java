package com.optimove.sdk.optimove_sdk.main.tools;

import android.content.Context;
import android.support.v4.app.NotificationManagerCompat;

import com.google.android.gms.ads.identifier.AdvertisingIdClient;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.optimove.sdk.optimove_sdk.main.tools.opti_logger.OptiLogger;

import java.io.IOException;

public class RequirementProvider {

    private Context context;

    public RequirementProvider(Context context) {
        this.context = context;
    }

    public boolean canReportAdId() {
        AdvertisingIdClient.Info adInfo = null;
        try {
            adInfo = AdvertisingIdClient.getAdvertisingIdInfo(context);
        } catch (GooglePlayServicesNotAvailableException | GooglePlayServicesRepairableException | IOException e) {
            OptiLogger.adIdFetcherFailedFetching(e.getMessage());
        }
        return adInfo != null && !adInfo.isLimitAdTrackingEnabled();
    }

    public boolean notificaionsAreEnabled() {
        return NotificationManagerCompat.from(context)
                .areNotificationsEnabled();
    }

    public boolean isGooglePlayServicesAvailable() {
        int servicesAvailable = GoogleApiAvailability.getInstance()
                .isGooglePlayServicesAvailable(context);
        return servicesAvailable == ConnectionResult.SUCCESS;
    }



}
