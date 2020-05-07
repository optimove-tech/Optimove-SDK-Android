package com.optimove.sdk.optimove_sdk.main.events.core_events;

import com.optimove.sdk.optimove_sdk.main.events.OptimoveEvent;

import java.util.HashMap;
import java.util.Map;

public class SetPageVisitEvent extends OptimoveEvent implements OptimoveCoreEvent {

  public static final String EVENT_NAME = "set_page_visit";

  public static final String CUSTOM_URL_PARAM_KEY = "customURL";
  public static final String PAGE_TITLE_PARAM_KEY = "pageTitle";
  public static final String CATEGORY_PARAM_KEY = "category";

  private String customUrl;
  private String pageTitle;
  private String pageCategory;

  public SetPageVisitEvent(String pageTitle, String pageCategory) {
    this.customUrl = "/";
    this.pageTitle = pageTitle;
    this.pageCategory = pageCategory;
  }
  @Override
  public String getName() {
    return EVENT_NAME;
  }

  @Override
  public Map<String, Object> getParameters() {
    Map<String, Object> params = new HashMap<>();
    params.put(CUSTOM_URL_PARAM_KEY, customUrl);
    params.put(PAGE_TITLE_PARAM_KEY, pageTitle);
    params.put(CATEGORY_PARAM_KEY, pageCategory);
    return params;
  }

  public String getCustomUrl() {
    return customUrl;
  }

  public void setCustomUrl(String customUrl) {
    this.customUrl = customUrl;
  }

  public String getPageTitle() {
    return pageTitle;
  }

  public void setPageTitle(String pageTitle) {
    this.pageTitle = pageTitle;
  }

  public String getPageCategory() {
    return pageCategory;
  }

  public void setPageCategory(String pageCategory) {
    this.pageCategory = pageCategory;
  }
}
