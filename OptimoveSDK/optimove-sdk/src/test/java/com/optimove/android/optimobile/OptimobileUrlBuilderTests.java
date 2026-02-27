package com.optimove.android.optimobile;

import org.junit.Assert;
import org.junit.Test;

import static org.junit.Assert.assertThrows;

import java.util.HashMap;
import java.util.Map;

public class OptimobileUrlBuilderTests {

    // ========================= defaultMapping =========================

    @Test
    public void defaultMappingShouldContainAllServices() {
        Map<UrlBuilder.Service, String> map = UrlBuilder.defaultMapping("eu-central-2");

        for (UrlBuilder.Service service : UrlBuilder.Service.values()) {
            Assert.assertNotNull("Missing mapping for " + service, map.get(service));
        }
    }

    @Test
    public void defaultMappingShouldIncludeRegionInRegionalServices() {
        String region = "us-east-1";
        Map<UrlBuilder.Service, String> map = UrlBuilder.defaultMapping(region);

        Assert.assertTrue(map.get(UrlBuilder.Service.PUSH).contains(region));
        Assert.assertTrue(map.get(UrlBuilder.Service.CRM).contains(region));
        Assert.assertTrue(map.get(UrlBuilder.Service.EVENTS).contains(region));
        Assert.assertTrue(map.get(UrlBuilder.Service.DDL).contains(region));
        Assert.assertTrue(map.get(UrlBuilder.Service.MEDIA).contains(region));
    }

    @Test
    public void defaultMappingIarShouldNotContainRegion() {
        String region = "us-east-1";
        Map<UrlBuilder.Service, String> map = UrlBuilder.defaultMapping(region);

        Assert.assertFalse(map.get(UrlBuilder.Service.IAR).contains(region));
    }

    @Test
    public void defaultMappingShouldProduceCorrectDomains() {
        Map<UrlBuilder.Service, String> map = UrlBuilder.defaultMapping("eu-central-2");

        Assert.assertEquals("https://push-eu-central-2.kumulos.com", map.get(UrlBuilder.Service.PUSH));
        Assert.assertEquals("https://crm-eu-central-2.kumulos.com", map.get(UrlBuilder.Service.CRM));
        Assert.assertEquals("https://events-eu-central-2.kumulos.com", map.get(UrlBuilder.Service.EVENTS));
        Assert.assertEquals("https://links-eu-central-2.kumulos.com", map.get(UrlBuilder.Service.DDL));
        Assert.assertEquals("https://i-eu-central-2.app.delivery", map.get(UrlBuilder.Service.MEDIA));
        Assert.assertEquals("https://iar.app.delivery", map.get(UrlBuilder.Service.IAR));
    }

    // ========================= UrlBuilder constructor =========================

    @Test
    public void constructorShouldThrowIfServiceMissing() {
        Map<UrlBuilder.Service, String> incompleteMap = new HashMap<>();
        incompleteMap.put(UrlBuilder.Service.EVENTS, "https://events.example.com");

        assertThrows(
                IllegalArgumentException.class,
                () -> new UrlBuilder(incompleteMap)
        );
    }

    @Test
    public void constructorShouldThrowIfMapIsEmpty() {
        assertThrows(
                IllegalArgumentException.class,
                () -> new UrlBuilder(new HashMap<>())
        );
    }

    @Test
    public void constructorShouldSucceedWithCompleteMap() {
        Map<UrlBuilder.Service, String> map = UrlBuilder.defaultMapping("eu-central-2");
        UrlBuilder builder = new UrlBuilder(map);
        Assert.assertNotNull(builder);
    }

    // ========================= urlForService =========================

    @Test
    public void urlForServiceShouldAppendPath() {
        UrlBuilder builder = new UrlBuilder(UrlBuilder.defaultMapping("eu-central-2"));

        String url = builder.urlForService(UrlBuilder.Service.EVENTS, "/v1/test");

        Assert.assertNotNull(url);
        Assert.assertEquals("https://events-eu-central-2.kumulos.com/v1/test", url);
    }

    @Test
    public void urlForServiceShouldWorkWithEmptyPath() {
        UrlBuilder builder = new UrlBuilder(UrlBuilder.defaultMapping("eu-central-2"));

        String url = builder.urlForService(UrlBuilder.Service.IAR, "");

        Assert.assertEquals("https://iar.app.delivery", url);
    }

    @Test
    public void urlForServiceShouldBuildCorrectUrlForEachService() {
        UrlBuilder builder = new UrlBuilder(UrlBuilder.defaultMapping("us-east-1"));

        Assert.assertEquals(
                "https://events-us-east-1.kumulos.com/v1/app-installs/abc/events",
                builder.urlForService(UrlBuilder.Service.EVENTS, "/v1/app-installs/abc/events"));

        Assert.assertEquals(
                "https://push-us-east-1.kumulos.com/v1/users/user123/messages",
                builder.urlForService(UrlBuilder.Service.PUSH, "/v1/users/user123/messages"));

        Assert.assertEquals(
                "https://links-us-east-1.kumulos.com/v1/deeplinks/my-slug?wasDeferred=1",
                builder.urlForService(UrlBuilder.Service.DDL, "/v1/deeplinks/my-slug?wasDeferred=1"));

        Assert.assertEquals(
                "https://i-us-east-1.app.delivery/300x/images/pic.png",
                builder.urlForService(UrlBuilder.Service.MEDIA, "/300x/images/pic.png"));

        Assert.assertEquals(
                "https://iar.app.delivery",
                builder.urlForService(UrlBuilder.Service.IAR, ""));
    }

    @Test
    public void urlForServiceShouldWorkWithCustomBaseUrlMap() {
        Map<UrlBuilder.Service, String> custom = new HashMap<>();
        for (UrlBuilder.Service s : UrlBuilder.Service.values()) {
            custom.put(s, "https://custom-" + s.name().toLowerCase() + ".example.com");
        }
        UrlBuilder builder = new UrlBuilder(custom);

        String url = builder.urlForService(UrlBuilder.Service.EVENTS, "/v1/test");
        Assert.assertEquals("https://custom-events.example.com/v1/test", url);
    }

    // ========================= Region variations =========================

    @Test
    public void defaultMappingShouldWorkWithDifferentRegions() {
        String[] regions = {"eu-central-2", "us-east-1", "ap-southeast-1"};

        for (String region : regions) {
            Map<UrlBuilder.Service, String> map = UrlBuilder.defaultMapping(region);
            UrlBuilder builder = new UrlBuilder(map);

            String eventsUrl = builder.urlForService(UrlBuilder.Service.EVENTS, "/v1/test");
            Assert.assertTrue("Events URL should contain region " + region, eventsUrl.contains(region));

            String iarUrl = builder.urlForService(UrlBuilder.Service.IAR, "");
            Assert.assertFalse("IAR URL should not contain region", iarUrl.contains(region));
        }
    }
}
