package com.optimove.android.gamifywidgetsdk;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import java.lang.reflect.Field;

@RunWith(RobolectricTestRunner.class)
@Config(manifest = Config.NONE)
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

    @Test
    public void getAdactUrl_returnsNormalizedTrailingSlash() throws Exception {
        resetShared();
        GamifyWidgetSDK.initialize("https://loyalty.example.com", "https://campaign.adact.me/");
        assertEquals("https://campaign.adact.me/", GamifyWidgetSDK.getInstance().getAdactUrl());
    }

    @Test
    public void getAdactUrl_emptyWhenNotConfigured() throws Exception {
        resetShared();
        GamifyWidgetSDK.initialize("https://loyalty.example.com");
        assertEquals("", GamifyWidgetSDK.getInstance().getAdactUrl());
    }

    @Test
    public void buildAdactCampaignUrl_buildsEmbeddedPath() throws Exception {
        resetShared();
        GamifyWidgetSDK.initialize(null, "https://campaign.adact.me/");
        assertEquals(
                "https://campaign.adact.me/embedded/179",
                GamifyWidgetSDK.getInstance().buildAdactCampaignUrl(new OpenAdactParams(179, null, null)));
    }

    @Test
    public void buildAdactCampaignUrl_addsCidAndCustomerIdToken() throws Exception {
        resetShared();
        GamifyWidgetSDK.initialize(null, "https://campaign.adact.me/");
        String url = GamifyWidgetSDK.getInstance().buildAdactCampaignUrl(
                new OpenAdactParams(179, "customer@example.com", "jwt-token"));
        assertTrue(url.startsWith("https://campaign.adact.me/embedded/179?"));
        assertTrue(url.contains("cid=customer%40example.com") || url.contains("cid=customer@example.com"));
        assertTrue(url.contains("customerIdToken=jwt-token"));
    }

    @Test
    public void buildAdactCampaignUrl_emptyWhenAdactUrlOrCampaignIdMissing() throws Exception {
        resetShared();
        GamifyWidgetSDK.initialize("https://loyalty.example.com");
        assertEquals("", GamifyWidgetSDK.getInstance().buildAdactCampaignUrl(new OpenAdactParams(179, null, null)));

        resetShared();
        GamifyWidgetSDK.initialize(null, "https://campaign.adact.me/");
        assertEquals("", GamifyWidgetSDK.getInstance().buildAdactCampaignUrl(new OpenAdactParams(null, "cid", null)));
    }

    @Test
    public void buildAdactCampaignUrl_rejectsNonHttps() throws Exception {
        resetShared();
        GamifyWidgetSDK.initialize(null, "http://campaign.adact.me/");
        assertEquals("", GamifyWidgetSDK.getInstance().buildAdactCampaignUrl(new OpenAdactParams(179, null, null)));
    }

    private static void resetShared() throws Exception {
        Field f = GamifyWidgetSDK.class.getDeclaredField("shared");
        f.setAccessible(true);
        f.set(null, null);
    }
}
