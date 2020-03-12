package com.optimove.sdk.optimove_sdk.events_tests;

import com.optimove.sdk.optimove_sdk.main.events.core_events.SdkMetadataEvent;
import com.optimove.sdk.optimove_sdk.main.sdk_configs.ConfigsFetcher;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.MockitoAnnotations;

import java.util.Map;

public class SdkMetadataEventTests {

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void sdkMetadataShouldContainTheRightName() {
        SdkMetadataEvent sdkMetadataEvent =
                SdkMetadataEvent.builder()
                        .withSdkPlatform(null)
                        .withSdkVersion(null)
                        .withAppNs(null)
                        .withLocation(null)
                        .withLocationLongitude(null)
                        .withLocationLatitude(null)
                        .withIp(null)
                        .withLanguage(null)
                        .withConfigFileUrl(null)
                        .build();

        Assert.assertEquals(SdkMetadataEvent.EVENT_NAME, sdkMetadataEvent.getName());
    }

    @Test
    public void sdkMetadataShouldContainRightParams() {
        String sdkPlatform = "some_platform";
        String sdkVersion = "some_sdk_version";
        String appNs = "some_app_ns";
        String location = "some_location";
        String locationLongitude = "some_location_longitude";
        String locationLatitude = "some_location_latitude";
        String ip = "some_ip";
        String language = "some_language";
        String configFileUrl = "some_config_file_url";

        SdkMetadataEvent sdkMetadataEvent =
                SdkMetadataEvent.builder()
                        .withSdkPlatform(sdkPlatform)
                        .withSdkVersion(sdkVersion)
                        .withAppNs(appNs)
                        .withLocation(location)
                        .withLocationLongitude(locationLongitude)
                        .withLocationLatitude(locationLatitude)
                        .withIp(ip)
                        .withLanguage(language)
                        .withConfigFileUrl(configFileUrl)
                        .build();

        Map<String, Object> parameters = sdkMetadataEvent.getParameters();

        Assert.assertEquals(parameters.get(SdkMetadataEvent.SDK_PLATFORM_PARAM_KEY), sdkPlatform);
        Assert.assertEquals(parameters.get(SdkMetadataEvent.SDK_VERSION_PARAM_KEY), sdkVersion);
        Assert.assertEquals(parameters.get(SdkMetadataEvent.APP_NS_PARAM_KEY), appNs);
        Assert.assertEquals(parameters.get(SdkMetadataEvent.LOCATION_PARAM_KEY), location);
        Assert.assertEquals(parameters.get(SdkMetadataEvent.LOCATION_LONGITUDE_PARAM_KEY), locationLongitude);
        Assert.assertEquals(parameters.get(SdkMetadataEvent.LOCATION_LATITUDE_PARAM_KEY), locationLatitude);
        Assert.assertEquals(parameters.get(SdkMetadataEvent.IP_PARAM_KEY), ip);
        Assert.assertEquals(parameters.get(SdkMetadataEvent.LANGUAGE_PARAM_KEY), language);
        Assert.assertEquals(parameters.get(SdkMetadataEvent.CONFIG_FILE_URL_PARAM_KEY), configFileUrl);
    }

}
