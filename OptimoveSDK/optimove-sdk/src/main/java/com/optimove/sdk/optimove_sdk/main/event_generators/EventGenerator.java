package com.optimove.sdk.optimove_sdk.main.event_generators;

import android.content.Context;
import android.content.SharedPreferences;

import com.optimove.sdk.optimove_sdk.main.EventContext;
import com.optimove.sdk.optimove_sdk.main.EventHandlerProvider;
import com.optimove.sdk.optimove_sdk.main.TenantInfo;
import com.optimove.sdk.optimove_sdk.main.UserInfo;
import com.optimove.sdk.optimove_sdk.main.events.core_events.OptipushOptIn;
import com.optimove.sdk.optimove_sdk.main.events.core_events.OptipushOptOut;
import com.optimove.sdk.optimove_sdk.main.events.core_events.SdkMetadataEvent;
import com.optimove.sdk.optimove_sdk.main.events.core_events.SetAdvertisingIdEvent;
import com.optimove.sdk.optimove_sdk.main.events.core_events.UserAgentHeaderEvent;
import com.optimove.sdk.optimove_sdk.main.sdk_configs.configs.Configs;
import com.optimove.sdk.optimove_sdk.main.tools.OptiUtils;
import com.optimove.sdk.optimove_sdk.main.tools.RequirementProvider;

import org.matomo.sdk.tools.BuildInfo;
import org.matomo.sdk.tools.DeviceHelper;
import org.matomo.sdk.tools.PropertySource;

import java.util.concurrent.TimeUnit;

import static com.optimove.sdk.optimove_sdk.optitrack.OptitrackConstants.LAST_OPT_REPORTED_KEY;
import static com.optimove.sdk.optimove_sdk.optitrack.OptitrackConstants.LAST_REPORTED_OPT_IN;
import static com.optimove.sdk.optimove_sdk.optitrack.OptitrackConstants.LAST_REPORTED_OPT_OUT;
import static com.optimove.sdk.optimove_sdk.optitrack.OptitrackConstants.OPTITRACK_SP_NAME;
import static java.util.concurrent.TimeUnit.SECONDS;

public class EventGenerator {

    private UserInfo userInfo;
    private String packageName;
    private String encryptedDeviceId;
    private RequirementProvider requirementProvider;
    private TenantInfo tenantInfo;
    private EventHandlerProvider eventHandlerProvider;
    private Context context;


    private EventGenerator(Builder builder) {
        userInfo = builder.userInfo;
        packageName = builder.packageName;
        encryptedDeviceId = builder.encryptedDeviceId;
        requirementProvider = builder.requirementProvider;
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

        if (requirementProvider.canReportAdId()) {
            String advertisingId = userInfo.getAdvertisingId();
            if (advertisingId == null) {
                return;
            }

            eventHandlerProvider.getEventHandler()
                    .reportEvent(new EventContext(new SetAdvertisingIdEvent(advertisingId,
                            packageName, encryptedDeviceId)));
        }
    }


    private void reportMetadataEvent() {
        eventHandlerProvider.getEventHandler()
                .reportEvent(new EventContext(new SdkMetadataEvent(tenantInfo, packageName)));
    }

    private void reportUserAgent() {
        eventHandlerProvider.getEventHandler()
                .reportEvent(new EventContext(new UserAgentHeaderEvent(new DeviceHelper(context, new PropertySource(),
                        new BuildInfo()).getUserAgent())));
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
        ITenantInfo withRequirementProvider(RequirementProvider val);
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
        private RequirementProvider requirementProvider;
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
        public ITenantInfo withRequirementProvider(RequirementProvider val) {
            requirementProvider = val;
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