package com.optimove.sdk.optimove_sdk.main;

import android.support.annotation.NonNull;

import com.google.gson.annotations.SerializedName;
import com.optimove.sdk.optimove_sdk.main.events.OptimoveEvent;
import com.optimove.sdk.optimove_sdk.optitrack.OptistreamEvent;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class OptistreamEventBuilder {

    private int tenantId;
    @NonNull
    private UserInfo userInfo;

    private final static class Constants {
        private static final String CATEGORY = "track";
        private static final String PLATFORM = "Android";
        private static final String ORIGIN = "sdk";
    }

    public OptistreamEventBuilder(int tenantId,@NonNull UserInfo userInfo) {
        this.tenantId = tenantId;
        this.userInfo = userInfo;

    }

    public OptistreamEvent convertOptimoveToOptistreamEvent(OptimoveEvent optimoveEvent, boolean isRealtime) {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ", Locale.US);
        //sdf.setTimeZone(TimeZone.getTimeZone("GMT"));


        return OptistreamEvent.builder()
                .withTenantId(tenantId)
                .withCategory(OptistreamEventBuilder.Constants.CATEGORY)
                .withName(optimoveEvent.getName())
                .withOrigin(OptistreamEventBuilder.Constants.ORIGIN)
                .withUserId(userInfo.getUserId())
                .withVisitorId(userInfo.getVisitorId())
                .withTimestamp(simpleDateFormat.format(new Date()))
                .withContext(optimoveEvent.getParameters())
                .withMetadata(new Metadata(isRealtime))
                .build();
    }

    public class Metadata {
        @SerializedName("realtime")
        private boolean realtime;

        Metadata(boolean realtime) {
            this.realtime = realtime;
        }
    }
}
