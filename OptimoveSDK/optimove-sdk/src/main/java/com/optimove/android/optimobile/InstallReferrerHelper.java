package com.optimove.android.optimobile;

import android.content.Context;

import androidx.annotation.Nullable;

import com.android.installreferrer.api.InstallReferrerClient;
import com.android.installreferrer.api.InstallReferrerStateListener;
import com.android.installreferrer.api.ReferrerDetails;


/**
 * Helper class to interact with Google Play Install Referrer API using reflection.
 * This allows the SDK to work without requiring the install referrer library as a hard dependency.
 */
public class InstallReferrerHelper {

    private static final String INSTALL_REFERRER_CLIENT_CLASS = "com.android.installreferrer.api.InstallReferrerClient";

    public interface InstallReferrerCallback {
        void onInstallReferrerReceived(@Nullable String referrerUrl);

        void onInstallReferrerFailed();
    }

    /**
     * Checks if the Install Referrer library is available at runtime
     */
    public static boolean isInstallReferrerAvailable() {
        try {
            Class.forName(INSTALL_REFERRER_CLIENT_CLASS);
            return true;
        } catch (ClassNotFoundException e) {
            return false;
        }
    }

    /**
     * Attempts to retrieve the install referrer information
     */
    public static void getInstallReferrer(Context context, InstallReferrerCallback callback) {
        if (!isInstallReferrerAvailable()) {
            callback.onInstallReferrerFailed();
            return;
        }

        try {
            InstallReferrerClient client = InstallReferrerClient.newBuilder(context).build();

            client.startConnection(new InstallReferrerStateListener() {
                @Override
                public void onInstallReferrerSetupFinished(int responseCode) {
                    if (responseCode == InstallReferrerClient.InstallReferrerResponse.OK) {
                        try {
                            ReferrerDetails referrerDetails = client.getInstallReferrer();
                            String referrerUrl = referrerDetails.getInstallReferrer();

                            client.endConnection();
                            callback.onInstallReferrerReceived(referrerUrl);
                        } catch (Exception e) {
                            e.printStackTrace();
                            callback.onInstallReferrerFailed();
                        }
                    } else {
                        callback.onInstallReferrerFailed();
                    }
                }

                @Override
                public void onInstallReferrerServiceDisconnected() {
                    callback.onInstallReferrerFailed();
                }
            });

        } catch (Exception e) {
            e.printStackTrace();
            callback.onInstallReferrerFailed();
        }
    }

    /**
     * Extracts the deep link URL from the install referrer string if present.
     * The referrer string should only contain direct deep link URL or be empty.
     */
    @Nullable
    public static String extractDeepLinkFromReferrer(@Nullable String referrerString) {
        if (referrerString == null || referrerString.isEmpty()) {
            return null;
        }

        if (referrerString.startsWith("http://") || referrerString.startsWith("https://")) {
            return referrerString;
        }

        return null;
    }
}

