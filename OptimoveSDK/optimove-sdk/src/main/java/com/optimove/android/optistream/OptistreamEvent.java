package com.optimove.android.optistream;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.gson.annotations.SerializedName;

import java.util.Map;
import java.util.UUID;

public class OptistreamEvent {
    @SerializedName("tenant")
    private int tenantId;
    @SerializedName("category")
    private String category;
    @SerializedName("event")
    private String name;
    @SerializedName("origin")
    private String origin;
    @SerializedName("customer")
    private String userId;
    @SerializedName("visitor")
    private String visitorId;
    @SerializedName("timestamp")
    private String timestamp;
    @SerializedName("context")
    private Map<String, Object> context;
    @SerializedName("metadata")
    private Metadata metadata;

    public int getTenantId() {
        return tenantId;
    }

    public void setTenantId(int tenantId) {
        this.tenantId = tenantId;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getOrigin() {
        return origin;
    }

    public void setOrigin(String origin) {
        this.origin = origin;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getVisitorId() {
        return visitorId;
    }

    public void setVisitorId(String visitorId) {
        this.visitorId = visitorId;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    public Map<String, Object> getContext() {
        return context;
    }

    public void setContext(Map<String, Object> context) {
        this.context = context;
    }


    private OptistreamEvent(Builder builder) {
        tenantId = builder.tenantId;
        category = builder.category;
        name = builder.name;
        origin = builder.origin;
        userId = builder.userId;
        visitorId = builder.visitorId;
        timestamp = builder.timestamp;
        context = builder.context;
        metadata = builder.metadata;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Metadata getMetadata() {
        return metadata;
    }

    public void setMetadata(Metadata metadata) {
        this.metadata = metadata;
    }

    public static ITenantId builder() {
        return new Builder();
    }


    public interface IBuild {
        OptistreamEvent build();
    }

    public interface IMetadata {
        IBuild withMetadata(Metadata metadata);
    }

    public interface IContext {
        IMetadata withContext(Map<String, Object> val);
    }

    public interface ITimestamp {
        IContext withTimestamp(String val);
    }

    public interface IVisitorId {
        ITimestamp withVisitorId(String val);
    }

    public interface IUserId {
        IVisitorId withUserId(String val);
    }

    public interface IOrigin {
        IUserId withOrigin(String val);
    }

    public interface IName {
        IOrigin withName(String val);
    }

    public interface ICategory {
        IName withCategory(String val);
    }

    public interface ITenantId {
        ICategory withTenantId(int val);
    }

    public static final class Builder implements IMetadata, IContext, ITimestamp, IVisitorId, IUserId, IOrigin, IName, ICategory, ITenantId, IBuild {
        private Metadata metadata;
        private Map<String, Object> context;
        private String timestamp;
        private String visitorId;
        private String userId;
        private String origin;
        private String name;
        private String category;
        private int tenantId;

        private Builder() {
        }

        @Override
        public IBuild withMetadata(Metadata metadata) {
            this.metadata = metadata;
            return this;
        }

        @Override
        public IMetadata withContext(Map<String, Object> val) {
            context = val;
            return this;
        }

        @Override
        public IContext withTimestamp(String val) {
            timestamp = val;
            return this;
        }

        @Override
        public ITimestamp withVisitorId(String val) {
            visitorId = val;
            return this;
        }

        @Override
        public IVisitorId withUserId(String val) {
            userId = val;
            return this;
        }

        @Override
        public IUserId withOrigin(String val) {
            origin = val;
            return this;
        }

        @Override
        public IOrigin withName(String val) {
            name = val;
            return this;
        }

        @Override
        public IName withCategory(String val) {
            category = val;
            return this;
        }

        @Override
        public ICategory withTenantId(int val) {
            tenantId = val;
            return this;
        }

        public OptistreamEvent build() {
            return new OptistreamEvent(this);
        }
    }

    public static final class Metadata {
        @SerializedName("realtime")
        @NonNull
        private boolean realtime;
        @SerializedName("firstVisitorDate")
        @NonNull
        private long firstVisitorDate;
        @SerializedName("eventId")
        @Nullable
        private String eventId;
        @SerializedName("sdk_platform")
        private String sdkPlatform;
        @SerializedName("sdk_version")
        private String sdkVersion;
        @SerializedName("requestId")
        private String requestId;

        public Metadata(boolean realtime, long firstVisitorDate, String sdkPlatform, String sdkVersion, String requestId) {
            this.realtime = realtime;
            this.firstVisitorDate = firstVisitorDate;
            this.eventId = UUID.randomUUID().toString();
            this.sdkPlatform = sdkPlatform;
            this.sdkVersion = sdkVersion;
            this.requestId = requestId;
        }

        public boolean isRealtime() {
            return realtime;
        }

        public void setRealtime(boolean realtime) {
            this.realtime = realtime;
        }
    }
}