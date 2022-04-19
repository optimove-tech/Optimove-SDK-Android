package com.optimove.sdk.optimove_sdk.main.common;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.optimove.sdk.optimove_sdk.BuildConfig;
import com.optimove.sdk.optimove_sdk.main.events.OptimoveEvent;
import com.optimove.sdk.optimove_sdk.main.tools.opti_logger.OptiLoggerStreamsContainer;
import com.optimove.sdk.optimove_sdk.optistream.OptistreamEvent;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

public class OptistreamEventBuilder {

    private final int tenantId;
    @NonNull
    private final UserInfo userInfo;
    private final boolean airshipEnabled;

    @Nullable
    private OptistreamEvent.AirshipMetadata airshipMetadata;

    public final static class Constants {
        private static final String CATEGORY_TRACK = "track";
        private static final String PLATFORM = "Android";
        private static final String ORIGIN = "sdk";
    }

    public OptistreamEventBuilder(int tenantId, @NonNull UserInfo userInfo,
                                  boolean airshipEnabled) {
        this.tenantId = tenantId;
        this.userInfo = userInfo;
        this.airshipEnabled = airshipEnabled;
    }

    public OptistreamEvent convertOptimoveToOptistreamEvent(OptimoveEvent optimoveEvent, boolean isRealtime) {
        SimpleDateFormat simpleDateFormat;
        try {
            simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssXXX", Locale.US);
        } catch (Throwable throwable) {
            simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZZZZZ", Locale.US);
        }
        simpleDateFormat.setTimeZone(TimeZone.getTimeZone("GMT"));

        if (airshipEnabled && airshipMetadata == null) {
            airshipMetadata = getAirshipMetadata();
        }

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

        if (airshipMetadata != null) {
            metadata.setAirship(airshipMetadata);
        }

        return iMetadata.withMetadata(metadata)
                .build();
    }

    private @Nullable
    OptistreamEvent.AirshipMetadata getAirshipMetadata() {
        try {
            Class<?> airshipClass = Class.forName("com.urbanairship.UAirship");

            Method sharedMethod = airshipClass.getMethod("shared");
            Object uAirshipClassInstance = sharedMethod.invoke(null);

            Method getChannelMethod = airshipClass.getMethod("getChannel");
            Object airShipChannelInstance = getChannelMethod.invoke(uAirshipClassInstance);

            Class<?> airshipChannelClass = Class.forName("com.urbanairship.channel.AirshipChannel");
            Method getIdMethod = airshipChannelClass.getMethod("getId");
            Object channelId = getIdMethod.invoke(airShipChannelInstance);

            Method getAirshipConfigOptionsMethod = airshipClass.getMethod("getAirshipConfigOptions");
            Object airshipUrlConfigInstance = getAirshipConfigOptionsMethod.invoke(uAirshipClassInstance);


            Class<?> airshipConfigOptionsClass = Class.forName("com.urbanairship.AirshipConfigOptions");

            Field field = airshipConfigOptionsClass.getDeclaredField("appKey");

            Object appKey = field.get(airshipUrlConfigInstance);

            String appKeyString = String.valueOf(appKey);
            String channelIdString = String.valueOf(channelId);

            if (appKeyString.equals("null") || (channelIdString.equals("null"))) {
                OptiLoggerStreamsContainer.error("Airship not available - either appKey or channelId were not found");
                return null;
            } else {
                return new OptistreamEvent.AirshipMetadata(channelIdString, appKeyString);
            }

        } catch (Exception e) {
            OptiLoggerStreamsContainer.error("Airship not available - %s", e.getMessage());
            return null;
        }

    }
}
