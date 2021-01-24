package com.optimove.sdk.optimove_sdk.optipush.registration.requests;

import com.google.gson.annotations.SerializedName;

public class InstallationRequest {
    @SerializedName("installation_id")
    private String installationId;
    @SerializedName("visitor_id")
    private String visitorId;
    @SerializedName("customer_id")
    private String customerId;
    @SerializedName("device_token")
    private String deviceToken;
    @SerializedName("is_tenant_token")
    private boolean isTenantToken;
    @SerializedName("push_provider")
    private String pushProvider;
    @SerializedName("app_ns")
    private String packageName;
    @SerializedName("os")
    private String os;
    @SerializedName("opt_in")
    private boolean optIn;
    @SerializedName("is_dev")
    private boolean isDev;
    @SerializedName("is_push_campaigns_disabled")
    private boolean isPushCampaignsDisabled;
    @SerializedName("metadata")
    private Metadata metadata;

    private InstallationRequest(Builder builder) {
        installationId = builder.installationId;
        visitorId = builder.visitorId;
        customerId = builder.customerId;
        deviceToken = builder.deviceToken;
        isTenantToken = builder.isTenantToken;
        pushProvider = builder.pushProvider;
        packageName = builder.packageName;
        os = builder.os;
        optIn = builder.optIn;
        isDev = builder.isDev;
        isPushCampaignsDisabled = builder.isPushCampaignsDisabled;
        metadata = builder.metadata;
    }

    public static IInstallationId builder() {
        return new Builder();
    }

    public interface IBuild {
        InstallationRequest build();
    }

    public interface IMetadata {
        IBuild withMetadata(Metadata val);
    }

    public interface IIsPushCampaignsDisabled {
        IMetadata withIsPushCampaignsDisabled(boolean val);
    }

    public interface IIsDev {
        IIsPushCampaignsDisabled withIsDev(boolean val);
    }

    public interface IOptIn {
        IIsDev withOptIn(boolean val);
    }

    public interface IOs {
        IOptIn withOs(String val);
    }

    public interface IPackageName {
        IOs withPackageName(String val);
    }

    public interface IPushProvider {
        IPackageName withPushProvider(String val);
    }

    public interface IDeviceToken {
        IIsTenantToken withDeviceToken(String val);
    }

    public interface IIsTenantToken {
        IPushProvider withIsTenantToken(boolean val);
    }

    public interface ICustomerId {
        IDeviceToken withCustomerId(String val);
    }

    public interface IVisitorId {
        ICustomerId withVisitorId(String val);
    }

    public interface IInstallationId {
        IVisitorId withInstallationId(String val);
    }

    public static final class Builder implements IMetadata, IIsPushCampaignsDisabled, IIsDev, IOptIn, IOs,
            IPackageName, IPushProvider, IDeviceToken, IIsTenantToken, ICustomerId, IVisitorId, IInstallationId,
            IBuild {
        private Metadata metadata;
        private boolean isPushCampaignsDisabled;
        private boolean isDev;
        private boolean optIn;
        private String os;
        private String packageName;
        private String pushProvider;
        private String deviceToken;
        private boolean isTenantToken;
        private String customerId;
        private String visitorId;
        private String installationId;

        private Builder() {
        }

        @Override
        public IBuild withMetadata(Metadata val) {
            metadata = val;
            return this;
        }

        @Override
        public IMetadata withIsPushCampaignsDisabled(boolean val) {
            isPushCampaignsDisabled = val;
            return this;
        }

        @Override
        public IIsPushCampaignsDisabled withIsDev(boolean val) {
            isDev = val;
            return this;
        }

        @Override
        public IIsDev withOptIn(boolean val) {
            optIn = val;
            return this;
        }

        @Override
        public IOptIn withOs(String val) {
            os = val;
            return this;
        }

        @Override
        public IOs withPackageName(String val) {
            packageName = val;
            return this;
        }

        @Override
        public IPackageName withPushProvider(String val) {
            pushProvider = val;
            return this;
        }

        @Override
        public IIsTenantToken withDeviceToken(String val) {
            deviceToken = val;
            return this;
        }

        @Override
        public IPushProvider withIsTenantToken(boolean val) {
            isTenantToken = val;
            return this;
        }

        @Override
        public IDeviceToken withCustomerId(String val) {
            customerId = val;
            return this;
        }

        @Override
        public ICustomerId withVisitorId(String val) {
            visitorId = val;
            return this;
        }

        @Override
        public IVisitorId withInstallationId(String val) {
            installationId = val;
            return this;
        }

        public InstallationRequest build() {
            return new InstallationRequest(this);
        }
    }
}

