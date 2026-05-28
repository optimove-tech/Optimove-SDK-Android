package com.optimove.android.gamifywidgetsdk;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertThrows;

import org.junit.Test;

import java.lang.reflect.Field;

public class GamifyWidgetSDKTest {

    @Test
    public void getInstance_throwsWhenNotInitialized() throws Exception {
        resetShared();
        assertThrows(IllegalStateException.class, GamifyWidgetSDK::getInstance);
    }

    @Test
    public void initialize_createsInstance() throws Exception {
        resetShared();
        GamifyWidgetSDK.initialize("https://example.com");
        assertNotNull(GamifyWidgetSDK.getInstance());
    }

    @Test
    public void initialize_replacesPreviousInstance() throws Exception {
        resetShared();
        GamifyWidgetSDK.initialize("https://first.example.com");
        GamifyWidgetSDK.initialize("https://second.example.com");
        assertSame(GamifyWidgetSDK.getInstance(), GamifyWidgetSDK.getInstance());
    }

    private static void resetShared() throws Exception {
        Field f = GamifyWidgetSDK.class.getDeclaredField("shared");
        f.setAccessible(true);
        f.set(null, null);
    }
}
