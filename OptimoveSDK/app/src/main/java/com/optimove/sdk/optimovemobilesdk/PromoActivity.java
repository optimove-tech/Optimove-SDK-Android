package com.optimove.sdk.optimovemobilesdk;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.TextView;

import com.optimove.sdk.optimove_sdk.optipush.deep_link.DeepLinkHandler;
import com.optimove.sdk.optimove_sdk.optipush.deep_link.LinkDataError;
import com.optimove.sdk.optimove_sdk.optipush.deep_link.LinkDataExtractedListener;

import java.util.Map;

public class PromoActivity extends AppCompatActivity implements LinkDataExtractedListener {

  private TextView outputTv;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_promo);
    outputTv = findViewById(R.id.promoOutputTextView);
    new DeepLinkHandler(getIntent()).extractLinkData(this);
  }

  @Override
  public void onDataExtracted(String screenName, Map<String, String> parameters) {
    StringBuilder paramsString = new StringBuilder();
    for (String key : parameters.keySet()) {
      paramsString.append(key).append("=").append(parameters.get(key)).append("\n");
    }
    outputTv.setText(String.format("%s:\n%s", screenName, paramsString.toString()));
  }

  @Override
  public void onErrorOccurred(LinkDataError error) {
    outputTv.setText("Where is the data?");
  }
}
