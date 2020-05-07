package com.optimove.sdk.optimove_sdk.main.events.core_events;

import com.optimove.sdk.optimove_sdk.main.events.OptimoveEvent;

import java.util.HashMap;
import java.util.Map;

public class SdkMetadataEvent extends OptimoveEvent {

  public static final String EVENT_NAME = "optimove_sdk_metadata";

  public static final String SDK_PLATFORM_PARAM_KEY = "sdk_platform";
  public static final String SDK_VERSION_PARAM_KEY = "sdk_version";
  public static final String CONFIG_FILE_URL_PARAM_KEY = "config_file_url";
  public static final String APP_NS_PARAM_KEY = "app_ns";

  public static final String LOCATION_PARAM_KEY = "location";
  public static final String IP_PARAM_KEY = "ip";
  public static final String LANGUAGE_PARAM_KEY = "language";
  public static final String LOCATION_LATITUDE_PARAM_KEY = "location_latitude";
  public static final String LOCATION_LONGITUDE_PARAM_KEY = "location_longitude";

  private String sdkPlatform;
  private String sdkVersion;
  private String configFileUrl;
  private String appNs;

  private String location;
  private String locationLongitude;
  private String locationLatitude;
  private String ip;
  private String language;

  private SdkMetadataEvent(Builder builder) {
    sdkPlatform = builder.sdkPlatform;
    sdkVersion = builder.sdkVersion;
    appNs = builder.appNs;
    location = builder.location;
    locationLongitude = builder.locationLongitude;
    locationLatitude = builder.locationLatitude;
    ip = builder.ip;
    language = builder.language;
    configFileUrl = builder.configFileUrl;
  }

  public static ISdkPlatform builder() {
    return new Builder();
  }


  @Override
  public String getName() {
    return EVENT_NAME;
  }

  @Override
  public Map<String, Object> getParameters() {
    Map<String, Object> params = new HashMap<>(3);
    params.put(SDK_PLATFORM_PARAM_KEY, sdkPlatform);
    params.put(SDK_VERSION_PARAM_KEY, sdkVersion);
    params.put(CONFIG_FILE_URL_PARAM_KEY, configFileUrl);
    params.put(APP_NS_PARAM_KEY, appNs);

    params.put(LOCATION_PARAM_KEY, location);
    params.put(IP_PARAM_KEY, ip);
    params.put(LANGUAGE_PARAM_KEY, language);
    params.put(LOCATION_LATITUDE_PARAM_KEY, locationLatitude);
    params.put(LOCATION_LONGITUDE_PARAM_KEY, locationLongitude);

    return params;
  }


  public interface IBuild {
    SdkMetadataEvent build();
  }
  public interface IConfigFileUrl {
    IBuild withConfigFileUrl(String val);
  }

  public interface ILanguage {
    IConfigFileUrl withLanguage(String val);
  }

  public interface IIp {
    ILanguage withIp(String val);
  }

  public interface ILocationLatitude {
    IIp withLocationLatitude(String val);
  }

  public interface ILocationLongitude {
    ILocationLatitude withLocationLongitude(String val);
  }

  public interface ILocation {
    ILocationLongitude withLocation(String val);
  }

  public interface IAppNs {
    ILocation withAppNs(String val);
  }

  public interface ISdkVersion {
    IAppNs withSdkVersion(String val);
  }

  public interface ISdkPlatform {
    ISdkVersion withSdkPlatform(String val);
  }

  public static final class Builder implements ILanguage, IIp, ILocationLatitude, ILocationLongitude, ILocation,
          IAppNs, ISdkVersion, ISdkPlatform, IBuild, IConfigFileUrl {
    private String configFileUrl;
    private String language;
    private String ip;
    private String locationLatitude;
    private String locationLongitude;
    private String location;
    private String appNs;
    private String sdkVersion;
    private String sdkPlatform;

    private Builder() {
    }

    @Override
    public IBuild withConfigFileUrl(String val) {
      configFileUrl = val;
      return this;
    }

    @Override
    public IConfigFileUrl withLanguage(String val) {
      language = val;
      return this;
    }

    @Override
    public ILanguage withIp(String val) {
      ip = val;
      return this;
    }

    @Override
    public IIp withLocationLatitude(String val) {
      locationLatitude = val;
      return this;
    }

    @Override
    public ILocationLatitude withLocationLongitude(String val) {
      locationLongitude = val;
      return this;
    }

    @Override
    public ILocationLongitude withLocation(String val) {
      location = val;
      return this;
    }

    @Override
    public ILocation withAppNs(String val) {
      appNs = val;
      return this;
    }

    @Override
    public IAppNs withSdkVersion(String val) {
      sdkVersion = val;
      return this;
    }

    @Override
    public ISdkVersion withSdkPlatform(String val) {
      sdkPlatform = val;
      return this;
    }

    public SdkMetadataEvent build() {
      return new SdkMetadataEvent(this);
    }
  }
}
