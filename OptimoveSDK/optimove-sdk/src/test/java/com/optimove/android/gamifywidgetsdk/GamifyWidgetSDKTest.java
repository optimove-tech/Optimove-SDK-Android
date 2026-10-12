package com.optimove.android.gamifywidgetsdk;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

@RunWith(RobolectricTestRunner.class)
@Config(manifest = Config.NONE)
public class GamifyWidgetSDKTest {

    private static final int TENANT_ID = 593;

    @Before
    public void setUp() {
        GamifyWidgetSDK.resetForTesting(null);
    }

    @Test
    public void getInstance_createsInstance() {
        assertNotNull(GamifyWidgetSDK.getInstance());
        assertSame(GamifyWidgetSDK.getInstance(), GamifyWidgetSDK.getInstance());
    }

    @Test
    public void getAdactUrl_returnsNormalizedTrailingSlashFromTenantConfig() {
        GamifyWidgetSDK.applyTenantConfiguration("https://campaign.adact.me/", null, TENANT_ID);
        assertEquals("https://campaign.adact.me/", GamifyWidgetSDK.getInstance().getAdactUrl());
    }

    @Test
    public void getAdactUrl_emptyWhenNotConfigured() {
        assertEquals("", GamifyWidgetSDK.getInstance().getAdactUrl());
    }

    @Test
    public void buildAdactCampaignUrl_buildsEmbeddedPath() {
        GamifyWidgetSDK.applyTenantConfiguration("https://campaign.adact.me/", null, TENANT_ID);
        assertEquals(
                "https://campaign.adact.me/embedded/179",
                GamifyWidgetSDK.getInstance().buildAdactCampaignUrl(new OpenAdactParams(179, null, null)));
    }

    @Test
    public void buildAdactCampaignUrl_addsCidAndCustomerIdToken() {
        GamifyWidgetSDK.applyTenantConfiguration("https://campaign.adact.me/", null, TENANT_ID);
        String url = GamifyWidgetSDK.getInstance().buildAdactCampaignUrl(
                new OpenAdactParams(179, "customer@example.com", "jwt-token"));
        assertTrue(url.startsWith("https://campaign.adact.me/embedded/179?"));
        assertTrue(url.contains("cid=customer%40example.com") || url.contains("cid=customer@example.com"));
        assertTrue(url.contains("customerIdToken=jwt-token"));
    }

    @Test
    public void buildAdactCampaignUrl_emptyWhenAdactUrlOrCampaignIdMissing() {
        assertEquals("", GamifyWidgetSDK.getInstance().buildAdactCampaignUrl(new OpenAdactParams(179, null, null)));

        GamifyWidgetSDK.applyTenantConfiguration("https://campaign.adact.me/", null, TENANT_ID);
        assertEquals("", GamifyWidgetSDK.getInstance().buildAdactCampaignUrl(new OpenAdactParams(null, "cid", null)));
    }

    @Test
    public void buildAdactCampaignUrl_rejectsNonHttps() {
        GamifyWidgetSDK.applyTenantConfiguration("http://campaign.adact.me/", null, TENANT_ID);
        assertEquals("", GamifyWidgetSDK.getInstance().buildAdactCampaignUrl(new OpenAdactParams(179, null, null)));
    }

    @Test
    public void buildLoyaltyWidgetUrl_buildsTenantAndUserPath() {
        GamifyWidgetSDK.applyTenantConfiguration(null, "https://opti-ls-widget-eu.optimove.net/", TENANT_ID);
        assertEquals("https://opti-ls-widget-eu.optimove.net/593/customer-1",
                GamifyWidgetSDK.getInstance().buildLoyaltyWidgetUrl("customer-1", null));
    }

    @Test
    public void buildLoyaltyWidgetUrl_addsBrandSegmentAndEncodes() {
        GamifyWidgetSDK.applyTenantConfiguration(null, "https://opti-ls-widget-eu.optimove.net", TENANT_ID);
        assertEquals("https://opti-ls-widget-eu.optimove.net/593/brand/my%20brand/a%2Fb%40c",
                GamifyWidgetSDK.getInstance().buildLoyaltyWidgetUrl("a/b@c", "my brand"));
    }

    @Test
    public void buildLoyaltyWidgetUrl_emptyWhenUrlTenantOrUserMissing() {
        assertEquals("", GamifyWidgetSDK.getInstance().buildLoyaltyWidgetUrl("u1", null));

        GamifyWidgetSDK.applyTenantConfiguration(null, "https://widget.example.com", -1);
        assertEquals("", GamifyWidgetSDK.getInstance().buildLoyaltyWidgetUrl("u1", null));

        GamifyWidgetSDK.applyTenantConfiguration(null, "https://widget.example.com", TENANT_ID);
        assertEquals("", GamifyWidgetSDK.getInstance().buildLoyaltyWidgetUrl(null, null));
        assertEquals("", GamifyWidgetSDK.getInstance().buildLoyaltyWidgetUrl("", null));
    }

    @Test
    public void buildLoyaltyWidgetUrl_rejectsNonHttps() {
        GamifyWidgetSDK.applyTenantConfiguration(null, "http://widget.example.com", TENANT_ID);
        assertEquals("", GamifyWidgetSDK.getInstance().buildLoyaltyWidgetUrl("u1", null));
    }
}
