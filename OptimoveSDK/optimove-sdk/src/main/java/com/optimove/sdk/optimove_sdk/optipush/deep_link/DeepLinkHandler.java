package com.optimove.sdk.optimove_sdk.optipush.deep_link;

import android.content.Intent;
import android.net.Uri;

import com.optimove.sdk.optimove_sdk.main.tools.OptiUtils;

import java.util.HashMap;
import java.util.Map;

/**
 * Responsible for extracting {@code deep links} sent via <b>OptiPush</b>.
 */
public class DeepLinkHandler {

  private Intent intent;

  /**
   * Create a new instance with the {@link Intent} that was used to start the targeted {@code screen}.
   *
   * @param intent the {@code Intent} from the targeted {@code screen}.
   */
  public DeepLinkHandler(Intent intent) {
    this.intent = intent;
  }

  /**
   * Extracts and parses the {@code deep link}.<br>
   * The <i>output</i> is passed to the {@link LinkDataExtractedListener} as a callback.
   *
   * @param linkDataExtractedListener the {@code listener} that receives the result of the {@code deep link} extraction. Called from the <b>main</b> thread.
   */
  public void extractLinkData(LinkDataExtractedListener linkDataExtractedListener) {
    Uri dl = intent.getData();
    if (dl == null)
      OptiUtils.runOnMainThreadIfOnWorker(() -> linkDataExtractedListener.onErrorOccurred(LinkDataError.NO_DEEP_LINK));
    else
      buildCarryOverData(linkDataExtractedListener, dl);
  }

  private void buildCarryOverData(LinkDataExtractedListener linkDataExtractedListener, Uri deepLink) {
    Map<String, String> resultData = new HashMap<>();
    for (String name : deepLink.getQueryParameterNames())
      resultData.put(name, deepLink.getQueryParameter(name));
    String screenName = deepLink.getLastPathSegment();
    OptiUtils.runOnMainThreadIfOnWorker(() -> linkDataExtractedListener.onDataExtracted(screenName, resultData));
  }
}
