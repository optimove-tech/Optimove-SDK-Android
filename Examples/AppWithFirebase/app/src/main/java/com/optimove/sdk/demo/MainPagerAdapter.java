package com.optimove.sdk.demo;

import com.optimove.sdk.demo.custom_events.CustomEventsDemoFragment;
import com.optimove.sdk.demo.screen_visit.ScreenVisitFragment;
import com.optimove.sdk.demo.set_user_id.SetUserIdFragment;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;

public class MainPagerAdapter extends FragmentPagerAdapter {

  private List<Fragment> fragments;

  public MainPagerAdapter(FragmentManager fm) {
    super(fm);
    this.fragments = new ArrayList<>(Arrays.asList(
      new CustomEventsDemoFragment(),
      new ScreenVisitFragment(),
      new SetUserIdFragment()
    ));
  }

  @Override
  public Fragment getItem(int position) {
    return fragments.get(position);
  }

  @Override
  public CharSequence getPageTitle(int position) {
    switch (position) {
      case 0:
        return "Custom Events";
      case 1:
        return "Screen Event";
      case 2:
        return "Set User ID";
    }
    return super.getPageTitle(position);
  }

  @Override
  public int getCount() {
    return fragments.size();
  }
}
