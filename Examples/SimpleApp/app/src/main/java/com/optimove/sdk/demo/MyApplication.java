package com.optimove.sdk.demo;

import android.app.Application;

import com.optimove.sdk.optimove_sdk.main.Optimove;
import com.optimove.sdk.optimove_sdk.main.common.TenantInfo;

public class MyApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        Optimove.configure(this, new TenantInfo(BuildConfig.OPTIMOVE_TENANT_TOKEN, BuildConfig.OPTIMOVE_CONFIG_NAME));
    }
}
