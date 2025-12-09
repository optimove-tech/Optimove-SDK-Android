package com.optimove.android.optimobile;

import android.content.Context;

import androidx.annotation.Nullable;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * Helper class to interact with Google Play Install Referrer API using reflection.
 * This allows the SDK to work without requiring the install referrer library as a hard dependency.
 */
public class InstallReferrerHelper {

    private static final String INSTALL_REFERRER_CLIENT_CLASS = "com.android.installreferrer.api.InstallReferrerClient";
    private static final String INSTALL_REFERRER_STATE_LISTENER_CLASS = "com.android.installreferrer.api.InstallReferrerStateListener";
    private static final String REFERRER_DETAILS_CLASS = "com.android.installreferrer.api.ReferrerDetails";

    private static Boolean isAvailable = null;

    public interface InstallReferrerCallback {
        void onInstallReferrerReceived(@Nullable String referrerUrl);

        void onInstallReferrerFailed();
    }

    /**
     * Checks if the Install Referrer library is available at runtime
     */
    public static boolean isInstallReferrerAvailable() {
        if (isAvailable != null) {
            return isAvailable;
        }

        try {
            Class.forName(INSTALL_REFERRER_CLIENT_CLASS);
            isAvailable = true;
            return true;
        } catch (ClassNotFoundException e) {
            isAvailable = false;
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
            // Use reflection to create InstallReferrerClient
            Class<?> clientClass = Class.forName(INSTALL_REFERRER_CLIENT_CLASS);
            Class<?> listenerClass = Class.forName(INSTALL_REFERRER_STATE_LISTENER_CLASS);
            Class<?> detailsClass = Class.forName(REFERRER_DETAILS_CLASS);

            // InstallReferrerClient.newBuilder(context).build()
            Method newBuilderMethod = clientClass.getMethod("newBuilder", Context.class);
            Object builder = newBuilderMethod.invoke(null, context);
            Method buildMethod = builder.getClass().getMethod("build");
            Object client = buildMethod.invoke(builder);

            // Create listener using proxy
            Object listener = java.lang.reflect.Proxy.newProxyInstance(
                    listenerClass.getClassLoader(),
                    new Class[]{listenerClass},
                    (proxy, method, args) -> {
                        String methodName = method.getName();

                        if ("onInstallReferrerSetupFinished".equals(methodName)) {
                            int responseCode = (int) args[0];

                            // InstallReferrerClient.InstallReferrerResponse.OK = 0
                            if (responseCode == 0) {
                                try {
                                    // Get referrer details
                                    Method getInstallReferrerMethod = clientClass.getMethod("getInstallReferrer");
                                    Object referrerDetails = getInstallReferrerMethod.invoke(client);

                                    // Get install referrer string
                                    Method getInstallReferrerUrlMethod = detailsClass.getMethod("getInstallReferrer");
                                    String referrerUrl = (String) getInstallReferrerUrlMethod.invoke(referrerDetails);

                                    // End connection
                                    Method endConnectionMethod = clientClass.getMethod("endConnection");
                                    endConnectionMethod.invoke(client);

                                    callback.onInstallReferrerReceived(referrerUrl);
                                } catch (Exception e) {
                                    e.printStackTrace();
                                    callback.onInstallReferrerFailed();
                                }
                            } else {
                                callback.onInstallReferrerFailed();
                            }
                        } else if ("onInstallReferrerServiceDisconnected".equals(methodName)) {
                            // Service disconnected - could retry, but for simplicity we'll just fail
                            callback.onInstallReferrerFailed();
                        }

                        return null;
                    }
            );

            // Start connection
            Method startConnectionMethod = clientClass.getMethod("startConnection", listenerClass);
            startConnectionMethod.invoke(client, listener);

        } catch (ClassNotFoundException | NoSuchMethodException | IllegalAccessException |
                 InvocationTargetException e) {
            e.printStackTrace();
            callback.onInstallReferrerFailed();
        }
    }

    /**
     * Extracts the deep link URL from the install referrer string if present.
     * The referrer might contain a URL parameter like: utm_source=...&deep_link_url=...
     * Or it might be a direct deep link URL.
     */
    @Nullable
    public static String extractDeepLinkFromReferrer(@Nullable String referrerString) {
        if (referrerString == null || referrerString.isEmpty()) {
            return null;
        }

        // TODO: Won't need this if referrer from deeplink-service will always be referrer=https://... and no other params
        // Try to parse as query parameters
        String[] params = referrerString.split("&");
        for (String param : params) {
            String[] keyValue = param.split("=", 2);
            if (keyValue.length == 2) {
                String key = keyValue[0];
                String value = keyValue[1];

                // Check for common deep link parameter names
                if ("deep_link_url".equals(key) || "deeplink".equals(key) ||
                        "deep_link".equals(key) || "link".equals(key)) {
                    try {
                        return java.net.URLDecoder.decode(value, "UTF-8");
                    } catch (Exception e) {
                        return value;
                    }
                }
            }
        }

        // If no parameter found, check if the entire referrer string is a valid URL
        if (referrerString.startsWith("http://") || referrerString.startsWith("https://")) {
            return referrerString;
        }

        return null;
    }
}

