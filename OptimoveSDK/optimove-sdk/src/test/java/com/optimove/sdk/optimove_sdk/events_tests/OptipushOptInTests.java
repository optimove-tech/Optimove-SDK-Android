package com.optimove.sdk.optimove_sdk.events_tests;


import com.optimove.sdk.optimove_sdk.main.events.core_events.OptipushOptIn;

import org.junit.Assert;
import org.junit.Test;

import java.util.Map;

public class OptipushOptInTests {

    @Test
    public void optipushOptInShouldContainTheRightName(){
        String fullPackageName = "full_package_name";
        String encryptedDeviceId = "encrypted_device_id";
        long timeInSeconds = 213221321;

        OptipushOptIn optipushOptIn = new OptipushOptIn(fullPackageName ,encryptedDeviceId,timeInSeconds);

        Assert.assertEquals(OptipushOptIn.EVENT_NAME,optipushOptIn.getName());
    }
    @Test
    public void optipushOptInShouldContainRightPackageAndDeviceIdParams(){
        String fullPackageName = "full_package_name";
        String encryptedDeviceId = "encrypted_device_id";
        long timeInSeconds = 213221321;

        OptipushOptIn optipushOptIn = new OptipushOptIn(fullPackageName ,encryptedDeviceId,timeInSeconds);
        Map<String, Object> parameters = optipushOptIn.getParameters();

        Assert.assertEquals(parameters.get(OptipushOptIn.APP_NS_PARAM_KEY),fullPackageName);
        Assert.assertEquals(parameters.get(OptipushOptIn.DEVICE_ID_PARAM_KEY),encryptedDeviceId);
        Assert.assertEquals(parameters.get(OptipushOptIn.TIMESTAMP_PARAM_KEY),timeInSeconds);
    }

}
