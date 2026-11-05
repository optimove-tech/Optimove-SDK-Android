package com.optimove.android.optimobile;

import android.content.Context;
import android.net.Uri;

import androidx.annotation.Nullable;

import com.optimove.android.AuthJwtResolver;
import com.optimove.android.AuthManager;
import com.optimove.android.Optimove;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import okhttp3.Response;

class OverlayMessagingRequestService {

    private static final String TAG = OverlayMessagingRequestService.class.getName();
    // After a failed scheduled fetch: 1 minute, doubling up to 1 hour, reset by a success
    private static final long MIN_RETRY_SECONDS = 60L;
    private static final long MAX_RETRY_SECONDS = 3600L;

    private static long retrySeconds = MIN_RETRY_SECONDS;

    /**
     * The result of a scheduled fetch. When the fetch failed, {@code nextFetchInSeconds} is the retry backoff
     * instead of the server's, so the caller handles both the same way.
     */
    static final class ScheduledFetchResult {
        @Nullable
        final OverlayMessagingMessage message;
        final long nextFetchInSeconds;

        ScheduledFetchResult(@Nullable OverlayMessagingMessage message, long nextFetchInSeconds) {
            this.message = message;
            this.nextFetchInSeconds = nextFetchInSeconds;
        }
    }

    static @Nullable OverlayMessagingMessage readOverlayMessage(Context c, OverlayMessagingMessage.MessageType type) {
        OptimobileHttpClient httpClient = OptimobileHttpClient.getInstance();
        String userIdentifier = Optimobile.getCurrentUserIdentifier(c);

        String encodedIdentifier = Uri.encode(userIdentifier);

        try {
            String messageType = type == OverlayMessagingMessage.MessageType.SESSION ? "session" : "immediate";

            String url = Optimobile.urlForService(UrlBuilder.Service.OVERLAY_MESSAGING, "/api/v1/users/" + encodedIdentifier + "/messages/mobile?messageType=" + messageType);

            try (Response response = httpClient.getSync(url, userJwt(c))) {

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
        } catch (Optimobile.PartialInitialisationException | MissingJwtException e) {
            // noop -- will skip loading until credentials are set
        }

        return null;
    }

    /** Fetches from the v2 scheduled endpoint: at most one message, and when to fetch next. */
    static ScheduledFetchResult readScheduledMessage(Context c) {
        OptimobileHttpClient httpClient = OptimobileHttpClient.getInstance();
        String encodedIdentifier = Uri.encode(Optimobile.getCurrentUserIdentifier(c));

        try {
            String url = Optimobile.urlForService(UrlBuilder.Service.OVERLAY_MESSAGING, "/api/v2/users/" + encodedIdentifier + "/messages/scheduled/mobile");

            try (Response response = httpClient.getSync(url, userJwt(c))) {
                // 204 means the server couldn't read the messages in time
                if (!response.isSuccessful() || response.code() == 204) {
                    logFailedResponse(response);
                    return retryLater();
                }

                ScheduledFetchResult result = parseScheduledResponse(response.body().string());
                resetRetry();
                return result;
            }
        } catch (NullPointerException | JSONException | IOException e) {
            Optimobile.log(TAG, e.getMessage());
        } catch (Optimobile.PartialInitialisationException | MissingJwtException e) {
            // noop -- will retry until credentials are set
        }

        return retryLater();
    }

    static ScheduledFetchResult parseScheduledResponse(String body) throws JSONException {
        JSONObject json = new JSONObject(body);
        long nextFetchInSeconds = json.getLong("nextFetchInSeconds");
        JSONArray messages = json.optJSONArray("messages");
        if (messages == null || messages.length() == 0) {
            return new ScheduledFetchResult(null, nextFetchInSeconds);
        }

        JSONObject m = messages.getJSONObject(0);
        OverlayMessagingMessage message = new OverlayMessagingMessage(m.getLong("id"), m.getJSONObject("content"),
                m.optJSONObject("data"), OverlayMessagingMessage.MessageType.SESSION);
        return new ScheduledFetchResult(message, nextFetchInSeconds);
    }

    static synchronized ScheduledFetchResult retryLater() {
        long nextFetchInSeconds = retrySeconds;
        retrySeconds = Math.min(retrySeconds * 2, MAX_RETRY_SECONDS);
        return new ScheduledFetchResult(null, nextFetchInSeconds);
    }

    static synchronized void resetRetry() {
        retrySeconds = MIN_RETRY_SECONDS;
    }

    /** Thrown when the associated user needs a JWT that couldn't be fetched. */
    private static final class MissingJwtException extends Exception {
    }

    /** The JWT for the associated user, or null when none is needed. */
    private static @Nullable String userJwt(Context c) throws MissingJwtException {
        String jwt = null;
        String associatedUserId = Optimobile.getAssociatedUserIdentifier(c);
        AuthManager authManager = Optimove.getAuthManager();
        if (authManager != null && associatedUserId != null) {
            jwt = AuthJwtResolver.blockingJwt(authManager, associatedUserId, 30_000L);
        }
        if (AuthJwtResolver.isMissingRequiredJwt(authManager, associatedUserId, jwt)) {
            throw new MissingJwtException();
        }
        return jwt;
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
