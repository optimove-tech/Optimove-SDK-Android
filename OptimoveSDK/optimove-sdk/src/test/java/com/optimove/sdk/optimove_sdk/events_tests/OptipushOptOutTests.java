package com.optimove.sdk.optimove_sdk.events_tests;

import com.optimove.sdk.optimove_sdk.main.events.core_events.OptipushOptOut;

import org.junit.Assert;
import org.junit.Test;

import java.util.Map;

public class OptipushOptOutTests {

    @Test
    public void optipushOptInShouldContainTheRightName(){
        String fullPackageName = "full_package_name";
        String encryptedDeviceId = "encrypted_device_id";
        long timestamp = 45645645;

        OptipushOptOut optipushOptOut = new OptipushOptOut(fullPackageName,encryptedDeviceId, timestamp);

        Assert.assertEquals(OptipushOptOut.EVENT_NAME,optipushOptOut.getName());
    }
    @Test
    public void optipushOptInShouldContainRightPackageAndDeviceIdParams(){
        String fullPackageName = "full_package_name";
        String encryptedDeviceId = "encrypted_device_id";
        long timestamp = 45645645;

        OptipushOptOut optipushOptOut = new OptipushOptOut(fullPackageName,encryptedDeviceId, timestamp);

        Map<String, Object> parameters = optipushOptOut.getParameters();

        Assert.assertEquals(parameters.get(OptipushOptOut.APP_NS_PARAM_KEY),fullPackageName);
        Assert.assertEquals(parameters.get(OptipushOptOut.DEVICE_ID_PARAM_KEY),encryptedDeviceId);
        Assert.assertEquals(parameters.get(OptipushOptOut.TIMESTAMP_PARAM_KEY),timestamp);
    }

}
