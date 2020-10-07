package com.optimove.sdk.optimove_sdk.main.common;

/**
 * Describes the <b>Tenant Configurations</b> as provided by <i>Optimove</i>.
 */
public class TenantInfo {

  private int tenantId;
  private String tenantToken;
  private String configName;

  public TenantInfo(String tenantToken, String configName) {
    this(-1, tenantToken, configName);
  }

  TenantInfo(int tenantId, String tenantToken, String configName) {
    this.tenantId = tenantId;
    this.tenantToken = tenantToken;
    this.configName = configName;
  }

  public int getTenantId() {
    return tenantId;
  }

  public void setTenantId(int tenantId) {
    this.tenantId = tenantId;
  }

  public String getTenantToken() {
    return tenantToken;
  }

  public void setTenantToken(String tenantToken) {
    this.tenantToken = tenantToken;
  }

  public String getConfigName() {
    return configName;
  }

  public void setConfigName(String configName) {
    this.configName = configName;
  }
}
