package com.optimove.sdk.demo

import android.os.Bundle
import android.widget.Toast

import com.optimove.sdk.optimove_sdk.main.Optimove
import com.optimove.sdk.optimove_sdk.main.constants.SdkRequiredPermission

import androidx.appcompat.app.AppCompatActivity
import androidx.viewpager.widget.ViewPager

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val viewPager = findViewById<ViewPager>(R.id.mainViewPager)
        val mainPagerAdapter = MainPagerAdapter(supportFragmentManager)
        viewPager.adapter = mainPagerAdapter
        if (BuildConfig.DEBUG) {
            Optimove.getInstance().startTestMode { success -> Toast.makeText(this, "Test Mode Started", Toast.LENGTH_SHORT).show() }
        }
    }
}
