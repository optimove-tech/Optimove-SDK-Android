package com.optimove.sdk.optimove_sdk.main.sdk_configs.configs;

public class OptipushConfigs {


    private String registrationServiceEndpoint;

    private FirebaseConfigs appControllerProjectConfigs;


    public String getRegistrationServiceEndpoint() {
        return registrationServiceEndpoint;
    }

    public void setRegistrationServiceEndpoint(String registrationServiceEndpoint) {
        this.registrationServiceEndpoint = registrationServiceEndpoint;
    }

    public FirebaseConfigs getAppControllerProjectConfigs() {
        return appControllerProjectConfigs;
    }

    public void setAppControllerProjectConfigs(
            FirebaseConfigs appControllerProjectConfigs) {
        this.appControllerProjectConfigs = appControllerProjectConfigs;
    }

    public class FirebaseConfigs {

        private String appId;
        private String webApiKey;
        private String dbUrl;
        private String senderId;
        private String storageBucket;
        private String projectId;

        public String getWebApiKey() {
            return webApiKey;
        }

        public void setWebApiKey(String webApiKey) {
            this.webApiKey = webApiKey;
        }

        public String getDbUrl() {
            return dbUrl;
        }

        public void setDbUrl(String dbUrl) {
            this.dbUrl = dbUrl;
        }

        public String getSenderId() {
            return senderId;
        }

        public void setSenderId(String senderId) {
            this.senderId = senderId;
        }

        public String getStorageBucket() {
            return storageBucket;
        }

        public void setStorageBucket(String storageBucket) {
            this.storageBucket = storageBucket;
        }

        public String getProjectId() {
            return projectId;
        }

        public void setProjectId(String projectId) {
            this.projectId = projectId;
        }

        public String getAppId() {
            return appId;
        }

        public void setAppId(String appId) {
            this.appId = appId;
        }
    }

}
