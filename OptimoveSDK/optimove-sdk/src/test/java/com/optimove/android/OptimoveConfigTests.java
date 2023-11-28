package com.optimove.android;

import org.junit.Assert;
import org.junit.Test;

import static org.junit.Assert.assertThrows;

import android.content.Context;

import androidx.annotation.Nullable;

import com.optimove.android.main.tools.opti_logger.LogLevel;
import com.optimove.android.optimobile.DeferredDeepLinkHandlerInterface;
import com.optimove.android.optimobile.DeferredDeepLinkHelper;
import com.optimove.android.optimobile.UrlBuilder;

import java.util.Map;

public class OptimoveConfigTests {
    private final String VALID_OPTIMOVE_CREDS = "WyIxIiwgIjgwYTRhMjI0ZWVlMTRhNDQ4MTNlYzIwZmNkMjAzMjE2IiwgIm1vYmlsZS1jb25maWd1cmF0aW9uLjEuMC4wIl0=";
    private final String VALID_OPTIMOBILE_CREDS = "WzEsInVrLTEiLCI5NDM2YmI4OC1lY2MzLTQ5NDAtOGU1OC0wMDk5Y2I1NDI1NWIiLCJGbEQ0Q0tKRjRXR1grRVE4M21nTFM1WDV3R20rNTFJUDR1dVUiXQ==";

    // ============================================= STANDARD INITIALISATION =============================================

    @Test
    public void shouldSucceedWithMaximalConfigAndImmediateInitialisation() {
        OptimoveConfig config = new OptimoveConfig.Builder(VALID_OPTIMOVE_CREDS, VALID_OPTIMOBILE_CREDS)
                .enableInAppMessaging(OptimoveConfig.InAppConsentStrategy.EXPLICIT_BY_USER, OptimoveConfig.InAppDisplayMode.AUTOMATIC)
                .enableDeepLinking("https://my.c.name", new DeferredDeepLinkHandlerInterface() {
                    @Override
                    public void handle(Context context, DeferredDeepLinkHelper.DeepLinkResolution resolution, String link, @Nullable DeferredDeepLinkHelper.DeepLink data) {
                        // do nothing
                    }
                })
                .setPushSmallIconId(123)
                .setMinLogLevel(LogLevel.FATAL)
                .setSessionIdleTimeoutSeconds(4321)
                .build();

        Assert.assertNotNull(config.getRegion());
        Assert.assertNotNull(config.getApiKey());
        Assert.assertNotNull(config.getSecretKey());
        Assert.assertNotNull(config.getOptimoveToken());
        Assert.assertNotNull(config.getConfigFileName());

        Assert.assertEquals(OptimoveConfig.InAppConsentStrategy.EXPLICIT_BY_USER, config.getInAppConsentStrategy());
        Assert.assertEquals(OptimoveConfig.InAppDisplayMode.AUTOMATIC, config.getInAppDisplayMode());

        Assert.assertEquals("https://my.c.name", config.getDeepLinkCname().toString());
        Assert.assertNotNull(config.getDeferredDeepLinkHandler());

        Assert.assertEquals(123, config.getNotificationSmallIconId());
        Assert.assertEquals(4321, config.getSessionIdleTimeoutSeconds());
        Assert.assertEquals(LogLevel.FATAL, config.getCustomMinLogLevel());
    }

    // ============================================= DELAYED CONFIGURATION =============================================


    @Test
    public void shouldFailIfFeatureSetIsEmpty() {
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> {
                    OptimoveConfig.FeatureSet desiredFeatures = new OptimoveConfig.FeatureSet();
                    new OptimoveConfig.Builder(OptimoveConfig.Region.DEV, desiredFeatures).build();
                }
        );

        Assert.assertEquals("Feature set cannot be empty", exception.getMessage());
    }

    @Test
    public void shouldConfigureWithOptimoveOnly() {
        OptimoveConfig.FeatureSet desiredFeatures = new OptimoveConfig.FeatureSet().withOptimove();
        OptimoveConfig config = new OptimoveConfig.Builder(OptimoveConfig.Region.US, desiredFeatures).build();

        Assert.assertNull(config.getApiKey());
        Assert.assertNull(config.getSecretKey());
        Assert.assertNull(config.getOptimoveToken());
        Assert.assertNull(config.getConfigFileName());

        Assert.assertEquals(OptimoveConfig.Region.US.toString(), config.getRegion());

        Assert.assertTrue(config.isOptimoveConfigured());
        Assert.assertFalse(config.isOptimobileConfigured());

        Assert.assertTrue(config.usesDelayedConfiguration());
        Assert.assertFalse(config.usesDelayedOptimobileConfiguration());
        Assert.assertTrue(config.usesDelayedOptimoveConfiguration());
    }

    @Test
    public void shouldConfigureWithOptimobileOnly() {
        OptimoveConfig.FeatureSet desiredFeatures = new OptimoveConfig.FeatureSet().withOptimobile();
        OptimoveConfig config = new OptimoveConfig.Builder(OptimoveConfig.Region.DEV, desiredFeatures).build();

        Assert.assertNull(config.getApiKey());
        Assert.assertNull(config.getSecretKey());
        Assert.assertNull(config.getOptimoveToken());
        Assert.assertNull(config.getConfigFileName());

        Assert.assertEquals(OptimoveConfig.Region.DEV.toString(), config.getRegion());

        Assert.assertFalse(config.isOptimoveConfigured());
        Assert.assertTrue(config.isOptimobileConfigured());

        Assert.assertTrue(config.usesDelayedConfiguration());
        Assert.assertTrue(config.usesDelayedOptimobileConfiguration());
        Assert.assertFalse(config.usesDelayedOptimoveConfiguration());
    }

    @Test
    public void shouldConfigureWithCompleteFeatureSet() {
        OptimoveConfig.FeatureSet desiredFeatures = new OptimoveConfig.FeatureSet().withOptimobile().withOptimove();
        OptimoveConfig config = new OptimoveConfig.Builder(OptimoveConfig.Region.DEV, desiredFeatures).build();

        Assert.assertNull(config.getApiKey());
        Assert.assertNull(config.getSecretKey());
        Assert.assertNull(config.getOptimoveToken());
        Assert.assertNull(config.getConfigFileName());

        Assert.assertEquals(OptimoveConfig.Region.DEV.toString(), config.getRegion());

        Assert.assertTrue(config.isOptimoveConfigured());
        Assert.assertTrue(config.isOptimobileConfigured());

        Assert.assertTrue(config.usesDelayedConfiguration());
        Assert.assertTrue(config.usesDelayedOptimobileConfiguration());
        Assert.assertTrue(config.usesDelayedOptimoveConfiguration());

        Map<UrlBuilder.Service, String> map = config.getBaseUrlMap();
        Assert.assertEquals("https://i-" + OptimoveConfig.Region.DEV + ".app.delivery", map.get(UrlBuilder.Service.MEDIA));
    }

    @Test
    public void whenLateSettingCredentialsShouldFailIfBothCredentialsAreNull() {
        OptimoveConfig.FeatureSet desiredFeatures = new OptimoveConfig.FeatureSet().withOptimove().withOptimobile();
        OptimoveConfig config = new OptimoveConfig.Builder(OptimoveConfig.Region.DEV, desiredFeatures).build();

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> {
                    config.setCredentials(null, null);
                }
        );

        Assert.assertEquals("Should provide at least optimove or optimobile credentials", exception.getMessage());
    }

    @Test
    public void whenLateSettingCredentialsShouldFailIfCredentialsAreAlreadySet() {
        OptimoveConfig.FeatureSet desiredFeatures = new OptimoveConfig.FeatureSet().withOptimove().withOptimobile();
        OptimoveConfig config = new OptimoveConfig.Builder(OptimoveConfig.Region.DEV, desiredFeatures).build();


        IllegalStateException exception = assertThrows(
                IllegalStateException.class,
                () -> {
                    config.setCredentials(VALID_OPTIMOVE_CREDS, VALID_OPTIMOBILE_CREDS);
                    config.setCredentials(VALID_OPTIMOVE_CREDS, VALID_OPTIMOBILE_CREDS);
                }
        );
        Assert.assertEquals("OptimoveConfig: credentials are already set", exception.getMessage());
    }

    @Test
    public void whenLateSettingCredentialsShouldFailIfProvidedCredentialsForNonRequestedFeature() {
        // optimove
        OptimoveConfig.FeatureSet desiredFeatures = new OptimoveConfig.FeatureSet().withOptimove();
        OptimoveConfig config = new OptimoveConfig.Builder(OptimoveConfig.Region.DEV, desiredFeatures).build();

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> {
                    config.setCredentials(VALID_OPTIMOVE_CREDS, VALID_OPTIMOBILE_CREDS);
                }
        );
        Assert.assertEquals("Cannot set credentials for optimobile as it is not in the desired feature set", exception.getMessage());

        // optimobile
        OptimoveConfig.FeatureSet desiredFeatures2 = new OptimoveConfig.FeatureSet().withOptimobile();
        OptimoveConfig config2 = new OptimoveConfig.Builder(OptimoveConfig.Region.DEV, desiredFeatures2).build();

        IllegalArgumentException exception2 = assertThrows(
                IllegalArgumentException.class,
                () -> {
                    config2.setCredentials(VALID_OPTIMOVE_CREDS, VALID_OPTIMOBILE_CREDS);

                }
        );
        Assert.assertEquals("Cannot set credentials for optimove as it is not in the desired feature set", exception2.getMessage());
    }

    @Test
    public void shouldSucceedSettingCredentialsLate() {
        OptimoveConfig.FeatureSet desiredFeatures = new OptimoveConfig.FeatureSet().withOptimove().withOptimobile();
        OptimoveConfig config = new OptimoveConfig.Builder(OptimoveConfig.Region.DEV, desiredFeatures).build();

        config.setCredentials(VALID_OPTIMOVE_CREDS, VALID_OPTIMOBILE_CREDS);

        Assert.assertNotNull(config.getApiKey());
        Assert.assertNotNull(config.getSecretKey());

        Assert.assertNotNull(config.getOptimoveToken());
        Assert.assertNotNull(config.getConfigFileName());
    }
}
