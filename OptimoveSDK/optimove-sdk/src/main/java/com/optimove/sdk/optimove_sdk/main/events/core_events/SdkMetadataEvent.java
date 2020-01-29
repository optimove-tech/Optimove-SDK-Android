package com.optimove.sdk.optimove_sdk.main.events.core_events;

import com.optimove.sdk.optimove_sdk.main.sdk_configs.ConfigsFetcher;
import com.optimove.sdk.optimove_sdk.main.TenantInfo;
import com.optimove.sdk.optimove_sdk.main.events.OptimoveEvent;

import java.util.HashMap;
import java.util.Map;

public class SdkMetadataEvent implements OptimoveEvent {

  public static final String EVENT_NAME = "optimove_sdk_metadata";
  
  public static final String SDK_PLATFORM_PARAM_KEY = "sdk_platform";
  public static final String SDK_VERSION_PARAM_KEY = "sdk_version";
  public static final String CONFIG_FILE_URL_PARAM_KEY = "config_file_url";
  public static final String APP_NS_PARAM_KEY = "app_ns";
  
  public static final String NATIVE_SDK_PLATFORM = "Android";
  public static final String NATIVE_SDK_VERSION = "2.7.0";


  private String sdkPlatform;
  private String sdkVersion;
  private String configFileUrl;
  private String appNs;

  public SdkMetadataEvent(TenantInfo tenantInfo, String packageName) {
    this(NATIVE_SDK_PLATFORM, NATIVE_SDK_VERSION,tenantInfo,packageName);
  }

  public SdkMetadataEvent(String sdkPlatform, String sdkVersion, TenantInfo tenantInfo, String packageName) {
    this.sdkPlatform = sdkPlatform;
    this.sdkVersion = sdkVersion;
    this.configFileUrl = String.format("%s%s/%s.json", ConfigsFetcher.TENANT_CONFIG_FILE_BASE_URL, tenantInfo.getTenantToken(), tenantInfo.getConfigName());
    this.appNs = packageName;
  }

  @Override
  public String getName() {
    return EVENT_NAME;
  }

  @Override
  public Map<String, Object> getParameters() {
    Map<String, Object> params = new HashMap<>(3);
    params.put(SDK_PLATFORM_PARAM_KEY, this.sdkPlatform);
    params.put(SDK_VERSION_PARAM_KEY, this.sdkVersion);
    params.put(CONFIG_FILE_URL_PARAM_KEY, configFileUrl);
    params.put(APP_NS_PARAM_KEY, appNs);
    return params;
  }
}
