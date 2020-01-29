package com.optimove.sdk.optimove_sdk.events_tests;

import com.optimove.sdk.optimove_sdk.main.events.core_events.SdkPingEvent;

import org.junit.Assert;
import org.junit.Test;

import java.util.Map;

public class SdkPingTests {
    @Test
    public void sdkPingShouldContainTheRightName(){
        String userId = "user_id";
        String visitorId = "visitor_id";
        String fullPackageName = "full_package_name";
        String encryptedDeviceId = "encrypted_device_id";

        SdkPingEvent sdkPingEvent = new SdkPingEvent(userId,visitorId,fullPackageName,encryptedDeviceId);
        Assert.assertEquals(SdkPingEvent.EVENT_NAME,sdkPingEvent.getName());
    }
    @Test
    public void sdkPingShouldContainRightPackageAndDeviceIdParams(){
        String userId = "user_id";
        String visitorId = "visitor_id";
        String fullPackageName = "full_package_name";
        String encryptedDeviceId = "encrypted_device_id";

        SdkPingEvent sdkPingEvent = new SdkPingEvent(userId,visitorId,fullPackageName,encryptedDeviceId);
        Map<String, Object> parameters = sdkPingEvent.getParameters();

        Assert.assertEquals(parameters.get(SdkPingEvent.APP_NS_PARAM_KEY),fullPackageName);
        Assert.assertEquals(parameters.get(SdkPingEvent.DEVICE_ID_PARAM_KEY),encryptedDeviceId);
    }
    @Test
    public void sdkPingShouldContainOnlyUserIdParamIfExists(){
        String userId = "user_id";
        String visitorId = "visitor_id";
        String fullPackageName = "full_package_name";
        String encryptedDeviceId = "encrypted_device_id";

        SdkPingEvent sdkPingEvent = new SdkPingEvent(userId,visitorId,fullPackageName,encryptedDeviceId);
        Map<String, Object> parameters = sdkPingEvent.getParameters();

        Assert.assertEquals(parameters.get(SdkPingEvent.USER_ID_PARAM_KEY),userId);
        Assert.assertFalse(parameters.containsKey(SdkPingEvent.VISITOR_ID_PARAM_KEY));
    }
    @Test
    public void sdkPingShouldContainOnlyVisitorIdIfUserAbsent(){
        String visitorId = "visitor_id";
        String fullPackageName = "full_package_name";
        String encryptedDeviceId = "encrypted_device_id";

        SdkPingEvent sdkPingEvent = new SdkPingEvent(null,visitorId,fullPackageName,encryptedDeviceId);
        Map<String, Object> parameters = sdkPingEvent.getParameters();

        Assert.assertEquals(parameters.get(SdkPingEvent.VISITOR_ID_PARAM_KEY),visitorId);
        Assert.assertFalse(parameters.containsKey(SdkPingEvent.USER_ID_PARAM_KEY));
    }
}
