package com.optimove.optimobile;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.kumulos.android.Kumulos;
import com.kumulos.android.KumulosConfig;
import com.optimove.sdk.optimove_sdk.main.Optimove;
import com.optimove.sdk.optimove_sdk.main.common.TenantInfo;

import org.json.JSONObject;

import java.util.Map;

public class Optimobile {

    private static Optimobile shared;
    private static boolean initialized;

    @NonNull
    private final Application application;

    public static synchronized void initialize(Application application, OptimobileConfig optimobileConfig) {
        if (initialized) {
            return;
        }
        initialized = true;
        shared = new Optimobile(application);
        Optimove.configure(application.getApplicationContext(), new TenantInfo(optimobileConfig.getOptimoveToken(),
                optimobileConfig.getConfigFile()));
        Kumulos.initialize(application, new KumulosConfig.Builder(optimobileConfig.getApiKey(), optimobileConfig.getSecretKey()).build());
    }

    private Optimobile(@NonNull Application application) {
        this.application = application;
    }

    /* *******************
     * Public API
     ******************* */

    public static Optimobile getInstance() {
        if (shared == null) {
            throw new IllegalStateException("Optimobile.initialize() must be called");
        }
        return shared;
    }

    public void setUserId(String userId) {
        Optimove.getInstance().setUserId(userId);
        Kumulos.associateUserWithInstall(application.getApplicationContext(), userId);
    }

    public void reportEvent(String name, Map<String, Object> parameters) {
        Optimove.getInstance().reportEvent(name, parameters);
        Kumulos.trackEvent(application.getApplicationContext(), name, new JSONObject(parameters));
    }

    public void reportScreenView(@NonNull String screenName, @Nullable String screenCategory) {
        Optimove.getInstance().reportScreenVisit(screenName, screenCategory);
    }
}
