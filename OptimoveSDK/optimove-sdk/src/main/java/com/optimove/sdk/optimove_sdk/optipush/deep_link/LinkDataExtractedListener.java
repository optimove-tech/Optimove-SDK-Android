package com.optimove.sdk.optimove_sdk.optipush.deep_link;

import java.util.Map;

/**
 * Callback for receiving the result of {@code deep link extraction}.
 */
public interface LinkDataExtractedListener {

  /**
   * Called when the {@code deep link} was extracted <b>successfully</b>
   *
   * @param screenName the name of the targeted screen
   * @param parameters the {@code deep link} data
   */
  void onDataExtracted(String screenName, Map<String, String> parameters);

  /**
   * Called when the {@code deep link} extraction <b>failed</b>
   *
   * @param error the {@code deep link} extraction error
   */
  void onErrorOccurred(LinkDataError error);
}
