package com.optimove.android.optimobile;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * The state of a Live Update on this device. Content is the opaque {@code content_state} sent by the server.
 */
public final class LiveUpdate {

    private final @NonNull String name;
    private final @NonNull String type;
    private final long timestamp;
    private final @NonNull JSONObject contentState;
    private final @Nullable String alertTitle;
    private final @Nullable String alertBody;
    private final @Nullable Long dismissalDateMillis;
    private final long startedAtMillis;
    private final long lastUpdatedAtMillis;
    private final @Nullable LiveUpdateEndReason endReason;

    LiveUpdate(@NonNull String name,
               @NonNull String type,
               long timestamp,
               @NonNull JSONObject contentState,
               @Nullable String alertTitle,
               @Nullable String alertBody,
               @Nullable Long dismissalDateMillis,
               long startedAtMillis,
               long lastUpdatedAtMillis,
               @Nullable LiveUpdateEndReason endReason) {
        this.name = name;
        this.type = type;
        this.timestamp = timestamp;
        this.contentState = contentState;
        this.alertTitle = alertTitle;
        this.alertBody = alertBody;
        this.dismissalDateMillis = dismissalDateMillis;
        this.startedAtMillis = startedAtMillis;
        this.lastUpdatedAtMillis = lastUpdatedAtMillis;
        this.endReason = endReason;
    }

    public @NonNull String getName() {
        return name;
    }

    public @NonNull String getType() {
        return type;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public @NonNull JSONObject getContentState() {
        return copyOf(contentState);
    }

    public @Nullable String getAlertTitle() {
        return alertTitle;
    }

    public @Nullable String getAlertBody() {
        return alertBody;
    }

    public boolean hasAlert() {
        return alertTitle != null || alertBody != null;
    }

    public @Nullable Long getDismissalDateMillis() {
        return dismissalDateMillis;
    }

    public long getStartedAtMillis() {
        return startedAtMillis;
    }

    public long getLastUpdatedAtMillis() {
        return lastUpdatedAtMillis;
    }

    public @Nullable LiveUpdateEndReason getEndReason() {
        return endReason;
    }

    @Override
    public @NonNull String toString() {
        return "LiveUpdate{" + name + ", type=" + type + ", timestamp=" + timestamp
                + (endReason != null ? ", ended=" + endReason : "") + "}";
    }

    static @NonNull JSONObject copyOf(@NonNull JSONObject source) {
        try {
            return new JSONObject(source.toString());
        } catch (JSONException e) {
            return new JSONObject();
        }
    }
}
