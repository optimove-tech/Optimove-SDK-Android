package com.optimove.sdk.optimovemobilesdk;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import android.widget.TextView;


public class PromoActivity extends AppCompatActivity {

  private TextView outputTv;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_promo);
    outputTv = findViewById(R.id.promoOutputTextView);

  }
}
