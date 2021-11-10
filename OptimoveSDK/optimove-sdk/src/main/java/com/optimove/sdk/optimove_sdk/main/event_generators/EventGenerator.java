package com.optimove.sdk.optimove_sdk.main.event_generators;

import android.content.Context;
import android.location.Location;

import com.optimove.sdk.optimove_sdk.BuildConfig;
import com.optimove.sdk.optimove_sdk.main.common.EventHandlerProvider;
import com.optimove.sdk.optimove_sdk.main.common.TenantInfo;
import com.optimove.sdk.optimove_sdk.main.common.UserInfo;
import com.optimove.sdk.optimove_sdk.main.events.core_events.SdkMetadataEvent;
import com.optimove.sdk.optimove_sdk.main.events.core_events.SetAdvertisingIdEvent;
import com.optimove.sdk.optimove_sdk.main.events.core_events.UserAgentHeaderEvent;
import com.optimove.sdk.optimove_sdk.main.sdk_configs.ConfigsFetcher;
import com.optimove.sdk.optimove_sdk.main.tools.DeviceInfoProvider;

import java.util.Collections;

public class EventGenerator {

    private UserInfo userInfo;
    private String packageName;
    private String encryptedDeviceId;
    private DeviceInfoProvider deviceInfoProvider;
    private TenantInfo tenantInfo;
    private EventHandlerProvider eventHandlerProvider;
    private Context context;


    private EventGenerator(Builder builder) {
        userInfo = builder.userInfo;
        packageName = builder.packageName;
        encryptedDeviceId = builder.encryptedDeviceId;
        deviceInfoProvider = builder.deviceInfoProvider;
        tenantInfo = builder.tenantInfo;
        eventHandlerProvider = builder.eventHandlerProvider;
        context = builder.context;
    }


    public void generateStartEvents(boolean advertisingIdReportEnabled) {
        new Thread(() -> {
            reportAdId(advertisingIdReportEnabled);
            reportMetadataEvent();
            reportUserAgent();
        }).start();
    }

    private void reportAdId(boolean isEnableAdvertisingIdReport) {
        if (!isEnableAdvertisingIdReport) {
            return;
        }

        if (deviceInfoProvider.canReportAdId()) {
            String advertisingId = userInfo.getAdvertisingId();
            if (advertisingId == null) {
                return;
            }

            eventHandlerProvider.getEventHandler()
                    .reportEvent(Collections.singletonList(new SetAdvertisingIdEvent(advertisingId,
                            packageName, encryptedDeviceId)));
        }
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


    public static IUserInfo builder() {
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

    public interface IDeviceId {
        IRequirementProvider withDeviceId(String val);
    }

    public interface IPackageName {
        IDeviceId withPackageName(String val);
    }

    public interface IUserInfo {
        IPackageName withUserInfo(UserInfo val);
    }

    public static final class Builder implements IContext, IEventHandlerProvider,
            ITenantInfo, IRequirementProvider, IDeviceId, IPackageName, IUserInfo, IBuild {
        private Context context;
        private EventHandlerProvider eventHandlerProvider;
        private TenantInfo tenantInfo;
        private DeviceInfoProvider deviceInfoProvider;
        private String encryptedDeviceId;
        private String packageName;
        private UserInfo userInfo;

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
        public IRequirementProvider withDeviceId(String val) {
            encryptedDeviceId = val;
            return this;
        }

        @Override
        public IDeviceId withPackageName(String val) {
            packageName = val;
            return this;
        }

        @Override
        public IPackageName withUserInfo(UserInfo val) {
            userInfo = val;
            return this;
        }

        public EventGenerator build() {
            return new EventGenerator(this);
        }
    }
}