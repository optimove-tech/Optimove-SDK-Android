package com.optimove.sdk.optimove_sdk.main.event_generators;

import android.content.Context;
import android.location.Location;

import com.optimove.sdk.optimove_sdk.BuildConfig;
import com.optimove.sdk.optimove_sdk.main.common.EventHandlerProvider;
import com.optimove.sdk.optimove_sdk.main.common.TenantInfo;
import com.optimove.sdk.optimove_sdk.main.events.core_events.SdkMetadataEvent;
import com.optimove.sdk.optimove_sdk.main.events.core_events.UserAgentHeaderEvent;
import com.optimove.sdk.optimove_sdk.main.sdk_configs.ConfigsFetcher;
import com.optimove.sdk.optimove_sdk.main.tools.DeviceInfoProvider;

import java.util.Collections;
import java.util.concurrent.Executors;

public class EventGenerator {

    private final String packageName;
    private final DeviceInfoProvider deviceInfoProvider;
    private final TenantInfo tenantInfo;
    private final EventHandlerProvider eventHandlerProvider;
    private final Context context;


    private EventGenerator(Builder builder) {
        packageName = builder.packageName;
        deviceInfoProvider = builder.deviceInfoProvider;
        tenantInfo = builder.tenantInfo;
        eventHandlerProvider = builder.eventHandlerProvider;
        context = builder.context;
    }


    public void generateStartEvents() {
        Executors.newSingleThreadExecutor()
                .execute(() -> {
                    reportMetadataEvent();
                    reportUserAgent();
                });
    }

    private void reportMetadataEvent() {
        String language = deviceInfoProvider.getDeviceLanguage()
                .replace('_', '-').toLowerCase();
        Location location = deviceInfoProvider.getDeviceLocation(context);
        String cityName = null;
        String locationLongitude = null;
        String locationLatitude = null;

        if (location != null) {
            cityName = deviceInfoProvider.getCityNameFromLocation(context, location);
            locationLongitude = String.valueOf(location.getLongitude());
            locationLatitude = String.valueOf(location.getLatitude());
        }


        SdkMetadataEvent sdkMetadataEvent =
                SdkMetadataEvent.builder()
                        .withSdkPlatform("Android")
                        .withSdkVersion(BuildConfig.OPTIMOVE_VERSION_NAME)
                        .withAppNs(packageName)
                        .withLocation(cityName)
                        .withLocationLongitude(locationLongitude)
                        .withLocationLatitude(locationLatitude)
                        .withIp(deviceInfoProvider.getIP(context))
                        .withLanguage(language)
                        .withConfigFileUrl(String.format("%s%s/%s.json", ConfigsFetcher.TENANT_CONFIG_FILE_BASE_URL, tenantInfo.getTenantToken(), tenantInfo.getConfigName()))
                        .build();

        eventHandlerProvider.getEventHandler()
                .reportEvent(Collections.singletonList(sdkMetadataEvent));
    }

    private void reportUserAgent() {
        String userAgentToReport = deviceInfoProvider.getUserAgent();
        if (userAgentToReport == null) {
            userAgentToReport = "";
        }
        eventHandlerProvider.getEventHandler()
                .reportEvent(Collections.singletonList(new UserAgentHeaderEvent(userAgentToReport)));
    }


    public static IPackageName builder() {
        return new Builder();
    }


    public interface IBuild {
        EventGenerator build();
    }

    public interface IContext {
        IBuild withContext(Context val);
    }
    public interface IEventHandlerProvider {
        IContext withEventHandlerProvider(EventHandlerProvider val);
    }

    public interface ITenantInfo {
        IEventHandlerProvider withTenantInfo(TenantInfo val);
    }

    public interface IRequirementProvider {
        ITenantInfo withRequirementProvider(DeviceInfoProvider val);
    }

    public interface IPackageName {
        IRequirementProvider withPackageName(String val);
    }

    public static final class Builder implements IContext, IEventHandlerProvider,
            ITenantInfo, IRequirementProvider, IPackageName, IBuild {
        private Context context;
        private EventHandlerProvider eventHandlerProvider;
        private TenantInfo tenantInfo;
        private DeviceInfoProvider deviceInfoProvider;
        private String packageName;

        private Builder() {
        }

        @Override
        public IBuild withContext(Context val) {
            context = val;
            return this;
        }

        @Override
        public IContext withEventHandlerProvider(EventHandlerProvider val) {
            eventHandlerProvider = val;
            return this;
        }

        @Override
        public IEventHandlerProvider withTenantInfo(TenantInfo val) {
            tenantInfo = val;
            return this;
        }

        @Override
        public ITenantInfo withRequirementProvider(DeviceInfoProvider val) {
            deviceInfoProvider = val;
            return this;
        }

        @Override
        public IRequirementProvider withPackageName(String val) {
            packageName = val;
            return this;
        }

        public EventGenerator build() {
            return new EventGenerator(this);
        }
    }
}