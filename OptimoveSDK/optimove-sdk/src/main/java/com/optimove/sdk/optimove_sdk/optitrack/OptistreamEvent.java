package com.optimove.sdk.optimove_sdk.optitrack;

import com.google.gson.annotations.SerializedName;

import java.util.Map;

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
            metadata = metadata;
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
}