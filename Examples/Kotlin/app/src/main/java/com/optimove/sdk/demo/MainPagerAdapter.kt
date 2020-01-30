package com.optimove.sdk.demo

import com.optimove.sdk.demo.custom_events.CustomEventsDemoFragment
import com.optimove.sdk.demo.screen_visit.ScreenVisitFragment
import com.optimove.sdk.demo.set_user_id.SetUserIdFragment

import java.util.ArrayList
import java.util.Arrays

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter

class MainPagerAdapter(fm: FragmentManager) : FragmentPagerAdapter(fm) {

    private val fragments: List<Fragment>

    init {
        this.fragments = ArrayList(Arrays.asList(
                CustomEventsDemoFragment(),
                ScreenVisitFragment(),
                SetUserIdFragment()
        ))
    }

    override fun getItem(position: Int): Fragment {
        return fragments[position]
    }

    override fun getPageTitle(position: Int): CharSequence? {
        when (position) {
            0 -> return "Custom Events"
            1 -> return "Screen Event"
            2 -> return "Set User ID"
        }
        return super.getPageTitle(position)
    }

    override fun getCount(): Int {
        return fragments.size
    }
}
