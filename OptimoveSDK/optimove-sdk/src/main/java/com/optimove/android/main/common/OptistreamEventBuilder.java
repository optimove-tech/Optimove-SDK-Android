package com.optimove.android.main.common;

import androidx.annotation.NonNull;

import com.optimove.android.BuildConfig;
import com.optimove.android.main.events.OptimoveEvent;
import com.optimove.android.optistream.OptistreamEvent;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

public class OptistreamEventBuilder {

    private final int tenantId;
    @NonNull
    private final UserInfo userInfo;

    public final static class Constants {
        private static final String CATEGORY_TRACK = "track";
        private static final String PLATFORM = "Android";
        private static final String ORIGIN = "sdk";
    }

    public OptistreamEventBuilder(int tenantId, @NonNull UserInfo userInfo) {
        this.tenantId = tenantId;
        this.userInfo = userInfo;
    }

    public OptistreamEvent convertOptimoveToOptistreamEvent(OptimoveEvent optimoveEvent, boolean isRealtime) {
        SimpleDateFormat simpleDateFormat;
        try {
            simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssXXX", Locale.US);
        } catch (Throwable throwable) {
            simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZZZZZ", Locale.US);
        }
        simpleDateFormat.setTimeZone(TimeZone.getTimeZone("GMT"));

        OptistreamEvent.IMetadata iMetadata = OptistreamEvent.builder()
                .withTenantId(tenantId)
                .withCategory(OptistreamEventBuilder.Constants.CATEGORY_TRACK)
                .withName(optimoveEvent.getName())
                .withOrigin(OptistreamEventBuilder.Constants.ORIGIN)
                .withUserId(userInfo.getUserId())
                .withVisitorId(userInfo.getVisitorId())
                .withTimestamp(simpleDateFormat.format(new Date()))
                .withContext(optimoveEvent.getParameters());

        OptistreamEvent.Metadata metadata = new OptistreamEvent.Metadata(isRealtime, userInfo.getFirstVisitorDate(),
                Constants.PLATFORM, BuildConfig.OPTIMOVE_VERSION_NAME, optimoveEvent.getRequestId());

        return iMetadata.withMetadata(metadata)
                .build();
    }
}
