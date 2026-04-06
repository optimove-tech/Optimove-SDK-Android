package com.optimove.android.optimobile;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.HashMap;
import java.util.Map;

public class UrlBuilder {

    public enum Service {
        CRM,
        DDL,
        EVENTS,
        IAR,
        MEDIA,
        PUSH,
        OVERLAY_MESSAGING
    }

    private final Map<Service, String> baseUrlMap;

    UrlBuilder(Map<Service, String> baseUrlMap) {
        for (Service s : Service.values()) {
            if (!baseUrlMap.containsKey(s)) {
                throw new IllegalArgumentException("baseUrlMap must contain an entry for every Service entry");
            }
        }

        this.baseUrlMap = baseUrlMap;
    }

    String urlForService(Service service, String path) {
        String baseUrl = baseUrlMap.get(service);

        return baseUrl + path;
    }

    public static Map<Service, String> defaultMapping(@NonNull String region) {
        Map<Service, String> baseUrlMap = new HashMap<>(Service.values().length);

        //baseUrlMap.put(Service.IAR, "https://iar.app.delivery");
        // TODO
        baseUrlMap.put(Service.IAR, "https://optimobile-iar-dev.optimove.net");



        baseUrlMap.put(Service.PUSH, "https://push-" + region + ".kumulos.com");
        baseUrlMap.put(Service.CRM, "https://crm-" + region + ".kumulos.com");
        baseUrlMap.put(Service.EVENTS, "https://events-" + region + ".kumulos.com");
        baseUrlMap.put(Service.DDL, "https://links-" + region + ".kumulos.com");
        baseUrlMap.put(Service.MEDIA, "https://i-" + region + ".app.delivery");

        // TODO: http -> https
        String omRegion = mapRegionForOverlayMessaging(region);
        if (omRegion != null) {
            baseUrlMap.put(Service.OVERLAY_MESSAGING, "http://optimobile-overlay-srv-" + omRegion + ".optimove.net");
        }

        return baseUrlMap;
    }


    // TODO:  region. mapping? crashing app?
    @Nullable
    private static String mapRegionForOverlayMessaging(@NonNull String region) {
        switch (region) {
            case "eu-central-2": return "eu";
            case "us-east-1":    return "us";
            case "uk-1":         return "dev";
            default:             return null;
        }
    }

}
