package com.optimove.android.optimobile;

import android.content.Context;
import android.net.Uri;

import androidx.annotation.Nullable;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import okhttp3.Response;

class OverlayMessagingRequestService {

    private static final String TAG = OverlayMessagingRequestService.class.getName();

    static @Nullable OverlayMessagingMessage readOverlayMessage(Context c, OverlayMessagingManager.MessageType type) {
        OptimobileHttpClient httpClient = OptimobileHttpClient.getInstance();
        String userIdentifier = Optimobile.getCurrentUserIdentifier(c);

        String encodedIdentifier = Uri.encode(userIdentifier);

        try {
            //TODO: derive region, tenantId, brandId from credentials + take from urlBuilder
            String region = "dev";
            int tenantId = 3013;
            String brandId = "9abb8d6d-62ed-42d1-97d1-c82d15f9c1fc";

            String messageType = type == OverlayMessagingManager.MessageType.SESSION ? "session-start" : "immediate";

            String url = String.format(
                    "http://optimobile-overlay-srv-%s.optimove.net/mobile/%s/messages?tenantId=%s&brandId=%s&messageType=%s",
                    region, encodedIdentifier, tenantId, brandId, messageType);

            try (Response response = httpClient.getSync(url)) {
                if (!response.isSuccessful()) {
                    logFailedResponse(response);
                    return null;
                }
                return buildMessage(response);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (Optimobile.PartialInitialisationException e) {
            // noop -- will skip loading until credentials are set
        }

        return null;
    }

    private static void logFailedResponse(Response response) {
        switch (response.code()) {
            case 404:
                Optimobile.log(TAG, "User not found");
                break;
            case 422:
                try {
                    Optimobile.log(TAG, response.body().string());
                } catch (NullPointerException | IOException e) {
                    Optimobile.log(TAG, e.getMessage());
                }
                break;
            default:
                Optimobile.log(TAG, response.message());
                break;
        }
    }

    private static @Nullable OverlayMessagingMessage buildMessage(Response response) {
        try {
            JSONObject json = new JSONObject(response.body().string());
            long id = json.getLong("id");
            String html = json.getString("html");
            return new OverlayMessagingMessage(id, html);
        } catch (NullPointerException | JSONException | IOException e) {
            Optimobile.log(TAG, e.getMessage());
            return null;
        }
    }
}
