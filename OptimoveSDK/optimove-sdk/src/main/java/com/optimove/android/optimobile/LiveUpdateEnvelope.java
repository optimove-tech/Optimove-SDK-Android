package com.optimove.android.optimobile;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.json.JSONException;
import org.json.JSONObject;

final class LiveUpdateEnvelope {

    static final String KEY = "k.liveUpdate";

    final @NonNull LiveUpdateEvent event;
    final @NonNull String name;
    final @Nullable String type;
    final long timestamp;
    final @NonNull JSONObject contentState;
    final boolean hasContentState;
    final @Nullable String topic;
    final @Nullable String alertTitle;
    final @Nullable String alertBody;
    final @Nullable Long dismissalDateSeconds;

    private LiveUpdateEnvelope(@NonNull LiveUpdateEvent event,
                               @NonNull String name,
                               @Nullable String type,
                               long timestamp,
                               @Nullable JSONObject contentState,
                               @Nullable String topic,
                               @Nullable String alertTitle,
                               @Nullable String alertBody,
                               @Nullable Long dismissalDateSeconds) {
        this.event = event;
        this.name = name;
        this.type = type;
        this.timestamp = timestamp;
        this.hasContentState = contentState != null;
        this.contentState = contentState != null ? contentState : new JSONObject();
        this.topic = topic;
        this.alertTitle = alertTitle;
        this.alertBody = alertBody;
        this.dismissalDateSeconds = dismissalDateSeconds;
    }

    static boolean looksLikeLiveUpdate(@Nullable String customStr) {
        return customStr != null && customStr.contains(KEY);
    }

    static @Nullable Object extract(@Nullable String customStr) {
        if (!looksLikeLiveUpdate(customStr)) {
            return null;
        }

        try {
            JSONObject data = new JSONObject(customStr).optJSONObject("a");
            if (data == null || !data.has(KEY)) {
                return null;
            }
            return data.get(KEY);
        } catch (JSONException e) {
            return null;
        }
    }

    static @NonNull LiveUpdateEnvelope parse(@Nullable Object raw) throws JSONException {
        if (!(raw instanceof JSONObject)) {
            throw new JSONException(KEY + " is not an object");
        }
        JSONObject json = (JSONObject) raw;

        String eventValue = optNullableString(json, "event");
        LiveUpdateEvent event = LiveUpdateEvent.fromWireValue(eventValue);
        if (event == null) {
            throw new JSONException("unknown event: " + eventValue);
        }

        String name = optNullableString(json, "name");
        if (name == null || name.isEmpty()) {
            throw new JSONException("missing name");
        }

        String type = optNonEmptyString(json, "type");
        if (event == LiveUpdateEvent.START && type == null) {
            throw new JSONException("missing type on start");
        }

        if (!json.has("timestamp") || json.isNull("timestamp")) {
            throw new JSONException("missing timestamp");
        }
        long timestamp = json.getLong("timestamp");
        if (timestamp < 0) {
            throw new JSONException("negative timestamp: " + timestamp);
        }

        JSONObject contentState = null;
        if (json.has("content_state") && !json.isNull("content_state")) {
            contentState = json.getJSONObject("content_state");
        } else if (event != LiveUpdateEvent.END) {
            throw new JSONException("missing content_state on " + eventValue);
        }

        String topic = optNonEmptyString(json, "topic");
        if (event == LiveUpdateEvent.START && topic == null) {
            throw new JSONException("missing topic on start");
        }

        String alertTitle = null;
        String alertBody = null;
        JSONObject alert = json.optJSONObject("alert");
        if (alert != null) {
            alertTitle = optNullableString(alert, "title");
            alertBody = optNullableString(alert, "body");
        }

        Long dismissalDateSeconds = null;
        if (json.has("dismissal_date") && !json.isNull("dismissal_date")) {
            dismissalDateSeconds = json.getLong("dismissal_date");
        }

        return new LiveUpdateEnvelope(
                event,
                name,
                type,
                timestamp,
                contentState,
                topic,
                alertTitle,
                alertBody,
                dismissalDateSeconds);
    }

    static @Nullable String optNullableString(@NonNull JSONObject object, @NonNull String key) {
        if (!object.has(key) || object.isNull(key)) {
            return null;
        }
        return object.optString(key);
    }

    private static @Nullable String optNonEmptyString(@NonNull JSONObject object, @NonNull String key) {
        String value = optNullableString(object, key);
        return value == null || value.isEmpty() ? null : value;
    }
}
