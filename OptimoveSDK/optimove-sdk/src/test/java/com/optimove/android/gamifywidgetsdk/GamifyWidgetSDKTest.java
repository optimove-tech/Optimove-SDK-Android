package com.optimove.android.gamifywidgetsdk;

import static org.junit.Assert.assertEquals;

import org.junit.Before;
import org.junit.Test;

public class GamifyWidgetSDKTest {

    @Before
    public void setUp() {
        GamifyWidgetSDK.init("");
    }

    @Test
    public void init_storesWidgetUrl() {
        String url = "https://gamify-widget.vercel.app";
        GamifyWidgetSDK.init(url);
        assertEquals(url, GamifyWidgetSDK.getWidgetUrl());
    }

    @Test
    public void init_overwritesPreviousUrl() {
        GamifyWidgetSDK.init("https://first.example.com");
        GamifyWidgetSDK.init("https://second.example.com");
        assertEquals("https://second.example.com", GamifyWidgetSDK.getWidgetUrl());
    }
}
