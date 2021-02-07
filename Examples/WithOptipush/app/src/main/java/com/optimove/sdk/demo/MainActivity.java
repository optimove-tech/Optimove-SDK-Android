package com.optimove.sdk.demo;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager.widget.ViewPager;

public class MainActivity extends AppCompatActivity {

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);
    ViewPager viewPager = findViewById(R.id.mainViewPager);
    MainPagerAdapter mainPagerAdapter = new MainPagerAdapter(getSupportFragmentManager());
    viewPager.setAdapter(mainPagerAdapter);
  }
}
