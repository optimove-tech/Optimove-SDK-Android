package com.optimove.sdk.optimove_sdk.events_tests;

import com.optimove.sdk.optimove_sdk.main.TenantInfo;
import com.optimove.sdk.optimove_sdk.main.events.core_events.SdkMetadataEvent;
import com.optimove.sdk.optimove_sdk.main.sdk_configs.ConfigsFetcher;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import java.util.Map;

public class SdkMetadataEventTests {

    @Mock
    TenantInfo tenantInfo;
    String tenantToken = "tenant_token";
    String tenantCofigName = "config_name";
    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);

        Mockito.when(tenantInfo.getTenantToken()).thenReturn(tenantToken);
        Mockito.when(tenantInfo.getConfigName()).thenReturn(tenantCofigName);
    }

    @Test
    public void sdkMetadataShouldContainTheRightName() {
        String packageName = "package_name";

        SdkMetadataEvent sdkMetadataEvent = new SdkMetadataEvent(tenantInfo, packageName);

        Assert.assertEquals(SdkMetadataEvent.EVENT_NAME, sdkMetadataEvent.getName());
    }

    @Test
    public void sdkMetadataShouldContainRightParams() {
        String packageName = "package_name";

        SdkMetadataEvent sdkMetadataEvent = new SdkMetadataEvent(tenantInfo, packageName);
        Map<String, Object> parameters = sdkMetadataEvent.getParameters();

        Assert.assertEquals(parameters.get(SdkMetadataEvent.APP_NS_PARAM_KEY), packageName);
        Assert.assertEquals(parameters.get(SdkMetadataEvent.SDK_PLATFORM_PARAM_KEY), SdkMetadataEvent.NATIVE_SDK_PLATFORM);
        Assert.assertEquals(parameters.get(SdkMetadataEvent.SDK_VERSION_PARAM_KEY), SdkMetadataEvent.NATIVE_SDK_VERSION);
        Assert.assertEquals(parameters.get(SdkMetadataEvent.CONFIG_FILE_URL_PARAM_KEY),
                String.format("%s%s/%s.json", ConfigsFetcher.TENANT_CONFIG_FILE_BASE_URL, tenantInfo.getTenantToken(),
                        tenantInfo.getConfigName()));


    }

}
