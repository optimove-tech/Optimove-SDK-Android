package com.optimove.android.embeddedmessaging;
public class EmbeddedMessagingConfig {
        private final String region;
        private final int tenantId;
        private final String brandId;

        public EmbeddedMessagingConfig(String region, int tenantId, String brandId) {
            this.region = region;
            this.brandId = brandId;
            this.tenantId = tenantId;
        }
        public String getRegion() { return region; }
        public String getBrandId() {
            return brandId;
        }

        public int getTenantId() {
            return tenantId;
        }
}
