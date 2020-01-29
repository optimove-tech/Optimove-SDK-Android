package com.optimove.sdk.optimove_sdk.optipush.registration.requests;

import com.google.gson.annotations.SerializedName;

public class SetUserRequest {

    @SerializedName("opt_in")
    private boolean optIn;
    @SerializedName("device_id")
    private String deviceId;
    @SerializedName("app_ns")
    private String packageName;
    @SerializedName("os")
    private String os;
    @SerializedName("device_token")
    private String deviceToken;
    @SerializedName("is_dev")
    private boolean isDev;

    private SetUserRequest(Builder builder) {
        optIn = builder.optIn;
        deviceId = builder.deviceId;
        packageName = builder.packageName;
        os = builder.os;
        deviceToken = builder.deviceToken;
    }

    public static IOptIn builder() {
        return new Builder();
    }


    public interface IBuild {
        SetUserRequest build();
    }

    public interface IDeviceToken {
        IBuild withDeviceToken(String val);
    }

    public interface IOs {
        IDeviceToken withOs(String val);
    }

    public interface IPackageName {
        IOs withPackageName(String val);
    }

    public interface IDeviceId {
        IPackageName withDeviceId(String val);
    }

    public interface IOptIn {
        IDeviceId withOptIn(boolean val);
    }


    public static final class Builder implements IDeviceToken, IOs, IPackageName, IDeviceId, IOptIn, IBuild {
        private String deviceToken;
        private String os;
        private String packageName;
        private String deviceId;
        private boolean optIn;

        private Builder() {
        }

        @Override
        public IBuild withDeviceToken(String val) {
            deviceToken = val;
            return this;
        }

        @Override
        public IDeviceToken withOs(String val) {
            os = val;
            return this;
        }

        @Override
        public IOs withPackageName(String val) {
            packageName = val;
            return this;
        }

        @Override
        public IPackageName withDeviceId(String val) {
            deviceId = val;
            return this;
        }

        @Override
        public IDeviceId withOptIn(boolean val) {
            optIn = val;
            return this;
        }

        public SetUserRequest build() {
            SetUserRequest setUserRequest = new SetUserRequest(this);
            setUserRequest.isDev = false;
            return new SetUserRequest(this);
        }
    }
}
