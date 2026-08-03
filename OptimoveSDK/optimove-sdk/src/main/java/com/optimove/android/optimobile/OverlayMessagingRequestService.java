package com.optimove.android.optimobile;

import android.content.Context;
import android.net.Uri;

import androidx.annotation.Nullable;

import com.optimove.android.AuthJwtResolver;
import com.optimove.android.AuthManager;
import com.optimove.android.Optimove;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import okhttp3.Response;

class OverlayMessagingRequestService {

    private static final String TAG = OverlayMessagingRequestService.class.getName();

    static @Nullable OverlayMessagingMessage readOverlayMessage(Context c, OverlayMessagingMessage.MessageType type) {
        OptimobileHttpClient httpClient = OptimobileHttpClient.getInstance();
        String userIdentifier = Optimobile.getCurrentUserIdentifier(c);

        String encodedIdentifier = Uri.encode(userIdentifier);

        try {
            String messageType = type == OverlayMessagingMessage.MessageType.SESSION ? "session" : "immediate";

            String url = Optimobile.urlForService(UrlBuilder.Service.OVERLAY_MESSAGING, "/api/v1/users/" + encodedIdentifier + "/messages/mobile?messageType=" + messageType);

            String jwt = null;
            String associatedUserId = Optimobile.getAssociatedUserIdentifier(c);
            AuthManager authManager = Optimove.getAuthManager();
            if (authManager != null && associatedUserId != null) {
                jwt = AuthJwtResolver.blockingJwt(authManager, associatedUserId, 30_000L);
            }
            if (AuthJwtResolver.isMissingRequiredJwt(authManager, associatedUserId, jwt)) {
                return null;
            }

            try (Response response = httpClient.getSync(url, jwt)) {

                if (!response.isSuccessful()) {
                    logFailedResponse(response);
                    return null;
                }

                if (response.code() == 204) {
                    return null;
                }

                return buildMessage(response, type);
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

    private static @Nullable OverlayMessagingMessage buildMessage(Response response, OverlayMessagingMessage.MessageType type) {
        try {
            JSONObject json = new JSONObject(response.body().string());
            long id = json.getLong("id");
            JSONObject content = json.getJSONObject("content");
            JSONObject data = json.optJSONObject("data");

            return new OverlayMessagingMessage(id, content, data, type);
        } catch (NullPointerException | JSONException | IOException e) {
            Optimobile.log(TAG, e.getMessage());
            return null;
        }
    }
}
