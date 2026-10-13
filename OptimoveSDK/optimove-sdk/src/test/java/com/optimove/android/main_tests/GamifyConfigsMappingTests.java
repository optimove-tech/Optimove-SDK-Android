package com.optimove.android.main_tests;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.optimove.android.fixtures.ConfigProvider;
import com.optimove.android.main.sdk_configs.FetchedLocalConfigsMapper;
import com.optimove.android.main.sdk_configs.configs.Configs;
import com.optimove.android.main.sdk_configs.fetched_configs.FetchedGlobalConfig;
import com.optimove.android.main.sdk_configs.fetched_configs.FetchedTenantConfigs;

import org.junit.Assert;
import org.junit.Test;

public class GamifyConfigsMappingTests {

    @Test
    public void gamifyIsNullWhenAbsentFromTenantConfig() {
        Assert.assertNull(map(null).getGamifyConfigs());
    }

    @Test
    public void gamifyFieldsAreMappedFromTenantConfig() {
        JsonObject gamify = new JsonObject();
        gamify.addProperty("adactUrl", "https://adact-campaign-dev.optimove.net/");
        gamify.addProperty("loyaltyWidgetUrl", "https://opti-ls-widget-dev.optimove.net/");

        Configs configs = map(gamify);

        Assert.assertNotNull(configs.getGamifyConfigs());
        Assert.assertEquals("https://adact-campaign-dev.optimove.net/", configs.getGamifyConfigs().getAdactUrl());
        Assert.assertEquals("https://opti-ls-widget-dev.optimove.net/",
                configs.getGamifyConfigs().getLoyaltyWidgetUrl());
    }

    @Test
    public void eachGamifyFieldIsIndependentlyOptional() {
        JsonObject gamify = new JsonObject();
        gamify.addProperty("loyaltyWidgetUrl", "https://opti-ls-widget-dev.optimove.net/");

        Configs configs = map(gamify);

        Assert.assertNotNull(configs.getGamifyConfigs());
        Assert.assertNull(configs.getGamifyConfigs().getAdactUrl());
        Assert.assertEquals("https://opti-ls-widget-dev.optimove.net/",
                configs.getGamifyConfigs().getLoyaltyWidgetUrl());
    }

    @Test
    public void gamifyBlockIsMappedWhenItCarriesNoKnownFields() {
        JsonObject gamify = new JsonObject();
        gamify.addProperty("someFutureField", "value");

        Configs configs = map(gamify);

        Assert.assertNotNull(configs.getGamifyConfigs());
        Assert.assertNull(configs.getGamifyConfigs().getAdactUrl());
        Assert.assertNull(configs.getGamifyConfigs().getLoyaltyWidgetUrl());
    }

    private static Configs map(JsonObject gamify) {
        Gson gson = new Gson();
        JsonObject tenantJson = JsonParser.parseString(ConfigProvider.getTenantConfigJsonString()).getAsJsonObject();
        if (gamify != null) {
            tenantJson.add("gamify", gamify);
        }

        return FetchedLocalConfigsMapper.mapFetchedConfigsToLocal(
                gson.fromJson(ConfigProvider.getGlobalConfigJsonString(), FetchedGlobalConfig.class),
                gson.fromJson(tenantJson, FetchedTenantConfigs.class));
    }
}
