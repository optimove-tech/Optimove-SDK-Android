package com.optimove.sdk.optimove_sdk.events_tests;


import com.optimove.sdk.optimove_sdk.main.events.core_events.SetAdvertisingIdEvent;

import org.junit.Assert;
import org.junit.Test;

import java.util.Map;

public class SetAdvertisingIdEventTests {

    @Test
    public void setAdvertisingIdShouldContainTheRightName(){
        String advertisingId = "advertising_id";
        String fullPackageName = "full_package_name";
        String encryptedDeviceId = "encrypted_device_id";

        SetAdvertisingIdEvent setAdvertisingIdEvent = new SetAdvertisingIdEvent(advertisingId,fullPackageName,
                encryptedDeviceId);

        Assert.assertEquals(SetAdvertisingIdEvent.EVENT_NAME,setAdvertisingIdEvent.getName());
    }
    @Test
    public void setAdvertisingIdShouldContainRightParams(){
        String advertisingId = "advertising_id";
        String fullPackageName = "full_package_name";
        String encryptedDeviceId = "encrypted_device_id";

        SetAdvertisingIdEvent setAdvertisingIdEvent = new SetAdvertisingIdEvent(advertisingId,fullPackageName,
                encryptedDeviceId);

        Map<String, Object> parameters = setAdvertisingIdEvent.getParameters();

        Assert.assertEquals(parameters.get(SetAdvertisingIdEvent.APP_NS_PARAM_KEY),fullPackageName);
        Assert.assertEquals(parameters.get(SetAdvertisingIdEvent.DEVICE_ID_PARAM_KEY),encryptedDeviceId);
        Assert.assertEquals(parameters.get(SetAdvertisingIdEvent.ADVERTISING_ID_PARAM_KEY),advertisingId);
    }


}
