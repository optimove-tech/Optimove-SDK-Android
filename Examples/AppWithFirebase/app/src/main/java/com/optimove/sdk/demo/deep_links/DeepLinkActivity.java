package com.optimove.sdk.demo.deep_links;

import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import com.optimove.sdk.demo.R;
import com.optimove.sdk.optimove_sdk.optipush.deep_link.DeepLinkHandler;
import com.optimove.sdk.optimove_sdk.optipush.deep_link.LinkDataError;
import com.optimove.sdk.optimove_sdk.optipush.deep_link.LinkDataExtractedListener;

import java.util.Map;

import androidx.appcompat.app.AppCompatActivity;

public class DeepLinkActivity extends AppCompatActivity implements LinkDataExtractedListener {

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_promo);

    // The DeepLinkHandler doesn't hold strong reference to the Activity so this example is safe
    new DeepLinkHandler(getIntent()).extractLinkData(this);
  }

  @Override
  public void onDataExtracted(String screenName, Map<String, String> map) {
    TextView outputTv = findViewById(R.id.outputTextView);
    StringBuilder builder = new StringBuilder(screenName).append(":\n");
    for (String key : map.keySet()) {
      builder.append(key).append("=").append(map.get(key)).append("\n");
    }
    outputTv.setText(builder.toString());
  }

  @Override
  public void onErrorOccurred(LinkDataError error) {
    // This callback will also be called if no deep link was found, that's why it's just an INFO level log and not ERROR
    Log.i("OPTIPUSH_DEEP_LINK", String.format("Failed to get deep link due to: %s", error));
  }
}
