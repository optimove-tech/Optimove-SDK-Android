package com.optimove.android.optimobile;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * The {@code k.liveUpdate} object carried in a push's {@code custom.a} map.
 */
final class LiveUpdateEnvelope {

    static final String KEY = "k.liveUpdate";

    final @NonNull LiveUpdateEvent event;
    final @NonNull String activityId;
    final @Nullable String type;
    final @Nullable String name;
    final long sequence;
    final @NonNull JSONObject content;
    final @Nullable String alertTitle;
    final @Nullable String alertBody;

    private LiveUpdateEnvelope(@NonNull LiveUpdateEvent event,
                               @NonNull String activityId,
                               @Nullable String type,
                               @Nullable String name,
                               long sequence,
                               @NonNull JSONObject content,
                               @Nullable String alertTitle,
                               @Nullable String alertBody) {
        this.event = event;
        this.activityId = activityId;
        this.type = type;
        this.name = name;
        this.sequence = sequence;
        this.content = content;
        this.alertTitle = alertTitle;
        this.alertBody = alertBody;
    }

    /**
     * @return the raw {@code k.liveUpdate} value, or null if the payload is not a Live Update
     */
    static @Nullable Object extract(@Nullable String customStr) {
        if (customStr == null) {
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

        String activityId = optNullableString(json, "activity_id");
        if (activityId == null || activityId.isEmpty()) {
            throw new JSONException("missing activity_id");
        }

        String type = optNullableString(json, "type");
        if (type != null && type.isEmpty()) {
            type = null;
        }
        if (event == LiveUpdateEvent.START && type == null) {
            throw new JSONException("missing type on start");
        }

        if (!json.has("sequence") || json.isNull("sequence")) {
            throw new JSONException("missing sequence");
        }
        long sequence = json.getLong("sequence");
        if (sequence < 0) {
            throw new JSONException("negative sequence: " + sequence);
        }

        JSONObject content = json.getJSONObject("content");

        String alertTitle = null;
        String alertBody = null;
        JSONObject alert = json.optJSONObject("alert");
        if (alert != null) {
            alertTitle = optNullableString(alert, "title");
            alertBody = optNullableString(alert, "body");
        }

        return new LiveUpdateEnvelope(
                event,
                activityId,
                type,
                optNullableString(json, "name"),
                sequence,
                content,
                alertTitle,
                alertBody);
    }

    static @Nullable String optNullableString(@NonNull JSONObject object, @NonNull String name) {
        if (!object.has(name) || object.isNull(name)) {
            return null;
        }
        return object.optString(name);
    }
}
