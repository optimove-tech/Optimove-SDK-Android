package com.optimove.android.optimobile;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * The current state of a Live Update on this device.
 * <p>
 * {@link #getContent()} is the opaque payload sent with the latest start / update / end, passed through
 * verbatim. The host app decides how to render it.
 */
public final class LiveUpdate {

    static final long NO_SEQUENCE = -1;

    private final @NonNull String activityId;
    private final @NonNull String type;
    private final @Nullable String name;
    private final long sequence;
    private final @NonNull JSONObject content;
    private final @Nullable String alertTitle;
    private final @Nullable String alertBody;
    private final long startedAt;
    private final long updatedAt;

    LiveUpdate(@NonNull String activityId,
               @NonNull String type,
               @Nullable String name,
               long sequence,
               @NonNull JSONObject content,
               @Nullable String alertTitle,
               @Nullable String alertBody,
               long startedAt,
               long updatedAt) {
        this.activityId = activityId;
        this.type = type;
        this.name = name;
        this.sequence = sequence;
        this.content = content;
        this.alertTitle = alertTitle;
        this.alertBody = alertBody;
        this.startedAt = startedAt;
        this.updatedAt = updatedAt;
    }

    public @NonNull String getActivityId() {
        return activityId;
    }

    /**
     * @return the handler type this Live Update is routed to, e.g. {@code "notification"}
     */
    public @NonNull String getType() {
        return type;
    }

    /**
     * @return optional human-readable label for the activity. Not a substitute for {@link #getActivityId()}.
     */
    public @Nullable String getName() {
        return name;
    }

    /**
     * @return the server sequence of the last applied update, or -1 if only local updates have been applied
     */
    public long getSequence() {
        return sequence;
    }

    /**
     * @return a copy of the opaque content payload
     */
    public @NonNull JSONObject getContent() {
        try {
            return new JSONObject(content.toString());
        } catch (JSONException e) {
            return new JSONObject();
        }
    }

    /**
     * @return alert title sent with this event, if the sender wants the user alerted
     */
    public @Nullable String getAlertTitle() {
        return alertTitle;
    }

    /**
     * @return alert body sent with this event, if the sender wants the user alerted
     */
    public @Nullable String getAlertBody() {
        return alertBody;
    }

    public boolean hasAlert() {
        return alertTitle != null || alertBody != null;
    }

    /**
     * @return epoch millis when this device first started the Live Update
     */
    public long getStartedAt() {
        return startedAt;
    }

    /**
     * @return epoch millis when this device last applied an update
     */
    public long getUpdatedAt() {
        return updatedAt;
    }

    @NonNull JSONObject toJson() throws JSONException {
        JSONObject json = new JSONObject();
        json.put("activity_id", activityId);
        json.put("type", type);
        json.put("name", name);
        json.put("sequence", sequence);
        json.put("content", content);
        json.put("alert_title", alertTitle);
        json.put("alert_body", alertBody);
        json.put("started_at", startedAt);
        json.put("updated_at", updatedAt);
        return json;
    }

    static @NonNull LiveUpdate fromJson(@NonNull JSONObject json) throws JSONException {
        return new LiveUpdate(
                json.getString("activity_id"),
                json.getString("type"),
                LiveUpdateEnvelope.optNullableString(json, "name"),
                json.getLong("sequence"),
                json.getJSONObject("content"),
                LiveUpdateEnvelope.optNullableString(json, "alert_title"),
                LiveUpdateEnvelope.optNullableString(json, "alert_body"),
                json.getLong("started_at"),
                json.getLong("updated_at"));
    }
}
