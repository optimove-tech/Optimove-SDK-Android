package com.optimove.sdk.optimove_sdk.main;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.optimove.sdk.optimove_sdk.main.events.OptimoveEvent;
import com.optimove.sdk.optimove_sdk.optitrack.OptistreamEvent;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

public class OptistreamEventBuilder {

    private int tenantId;
    @NonNull
    private UserInfo userInfo;

    @Nullable
    private OptistreamEvent.AirshipMetadata airshipMetadata;

    private final static class Constants {
        private static final String CATEGORY = "track";
        private static final String PLATFORM = "Android";
        private static final String ORIGIN = "sdk";
    }

    public OptistreamEventBuilder(int tenantId, @NonNull UserInfo userInfo) {
        this.tenantId = tenantId;
        this.userInfo = userInfo;
    }

    public OptistreamEventBuilder(int tenantId, @NonNull UserInfo userInfo,
                                  @Nullable OptistreamEvent.AirshipMetadata airshipMetadata) {
        this.tenantId = tenantId;
        this.userInfo = userInfo;
        this.airshipMetadata = airshipMetadata;
    }

    public OptistreamEvent convertOptimoveToOptistreamEvent(OptimoveEvent optimoveEvent, boolean isRealtime) {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssXXX", Locale.US);
        simpleDateFormat.setTimeZone(TimeZone.getTimeZone("GMT"));

        OptistreamEvent.IMetadata iMetadata = OptistreamEvent.builder()
                .withTenantId(tenantId)
                .withCategory(OptistreamEventBuilder.Constants.CATEGORY)
                .withName(optimoveEvent.getName())
                .withOrigin(OptistreamEventBuilder.Constants.ORIGIN)
                .withUserId(userInfo.getUserId())
                .withVisitorId(userInfo.getVisitorId())
                .withTimestamp(simpleDateFormat.format(new Date()))
                .withContext(optimoveEvent.getParameters());

        if (airshipMetadata != null) {
            return iMetadata.withMetadata(new OptistreamEvent.Metadata(isRealtime, userInfo.getFirstVisitorDate(), airshipMetadata))
                    .build();
        } else {
            return iMetadata.withMetadata(new OptistreamEvent.Metadata(isRealtime, userInfo.getFirstVisitorDate()))
                    .build();
        }
    }
}
