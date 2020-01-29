package com.optimove.sdk.optimove_sdk.main.tools;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.util.UUID;

import static android.content.Context.MODE_PRIVATE;
import static com.optimove.sdk.optimove_sdk.optipush.OptipushConstants.Registration.DEVICE_ID_KEY;
import static com.optimove.sdk.optimove_sdk.optipush.OptipushConstants.Registration.REGISTRATION_PREFERENCES_NAME;

public class InstallationIDProvider {

    public static String INSTALLATION_SP_FILE = "com.optimove.sdk.installation_id";
    public static String INSTALLATION_ID_KEY = "installationIdKey";


    @Nullable
    private String cachedInstallationId;
    @NonNull
    private Context context;

    public InstallationIDProvider(@NonNull Context context) {
        this.context = context;
    }

    public synchronized String getInstallationID(){
        if (cachedInstallationId != null){
            return cachedInstallationId;
        }
        SharedPreferences installationSharedPrefs = context.getSharedPreferences(INSTALLATION_SP_FILE,
                Context.MODE_PRIVATE);
        String deviceIdAsStoredInInstallationSP = installationSharedPrefs.getString(INSTALLATION_ID_KEY, null);

        if (deviceIdAsStoredInInstallationSP != null){
            this.cachedInstallationId = deviceIdAsStoredInInstallationSP;
            return deviceIdAsStoredInInstallationSP;
        }
        SharedPreferences registrationPreferences = context.getSharedPreferences(REGISTRATION_PREFERENCES_NAME,
                MODE_PRIVATE);
        String deviceIdAsAlreadyStoredInOptipush = registrationPreferences.getString(DEVICE_ID_KEY, null);

        if (deviceIdAsAlreadyStoredInOptipush != null) {
            this.cachedInstallationId = deviceIdAsAlreadyStoredInOptipush;
            return deviceIdAsAlreadyStoredInOptipush;
        }
        String newGeneratedInstallationId = UUID.randomUUID().toString();

        installationSharedPrefs.edit().putString(INSTALLATION_ID_KEY, newGeneratedInstallationId).apply();
        this.cachedInstallationId = newGeneratedInstallationId;
        return newGeneratedInstallationId;
    }
}
