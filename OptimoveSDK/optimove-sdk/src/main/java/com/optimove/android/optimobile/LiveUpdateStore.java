package com.optimove.android.optimobile;

import android.content.SharedPreferences;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

final class LiveUpdateStore {

    private static final String TAG = LiveUpdateStore.class.getName();

    static final int VERSION = 1;
    static final long ENDED_RETENTION_MS = 24 * 60 * 60 * 1000L;

    private static final String KEY_VERSION = "v";
    private static final String KEY_ACTIVE = "active";
    private static final String KEY_ENDED = "ended";

    static final class ActiveRecord {
        final @NonNull String name;
        final @NonNull String type;
        final @Nullable String topic;
        final long timestamp;
        final @NonNull JSONObject contentState;
        final @Nullable Long dismissalDateSeconds;
        final long startedAtMillis;
        final long lastUpdatedAtMillis;

        ActiveRecord(@NonNull String name,
                     @NonNull String type,
                     @Nullable String topic,
                     long timestamp,
                     @NonNull JSONObject contentState,
                     @Nullable Long dismissalDateSeconds,
                     long startedAtMillis,
                     long lastUpdatedAtMillis) {
            this.name = name;
            this.type = type;
            this.topic = topic;
            this.timestamp = timestamp;
            this.contentState = contentState;
            this.dismissalDateSeconds = dismissalDateSeconds;
            this.startedAtMillis = startedAtMillis;
            this.lastUpdatedAtMillis = lastUpdatedAtMillis;
        }

        @NonNull ActiveRecord applying(long timestamp,
                                       @NonNull JSONObject contentState,
                                       @Nullable String topic,
                                       @Nullable Long dismissalDateSeconds,
                                       long nowMillis) {
            return new ActiveRecord(
                    name,
                    type,
                    this.topic != null ? this.topic : topic,
                    timestamp,
                    contentState,
                    dismissalDateSeconds != null ? dismissalDateSeconds : this.dismissalDateSeconds,
                    startedAtMillis,
                    nowMillis);
        }

        @NonNull ActiveRecord touched(long nowMillis) {
            return applying(timestamp, contentState, null, null, nowMillis);
        }

        @NonNull JSONObject toJson() throws JSONException {
            return new JSONObject()
                    .put("name", name)
                    .put("type", type)
                    .put("topic", topic)
                    .put("timestamp", timestamp)
                    .put("content_state", contentState)
                    .put("dismissal_date", dismissalDateSeconds)
                    .put("started_at", startedAtMillis)
                    .put("last_updated_at", lastUpdatedAtMillis);
        }

        static @NonNull ActiveRecord fromJson(@NonNull JSONObject json) throws JSONException {
            return new ActiveRecord(
                    json.getString("name"),
                    json.getString("type"),
                    LiveUpdateEnvelope.optNullableString(json, "topic"),
                    json.getLong("timestamp"),
                    json.getJSONObject("content_state"),
                    json.has("dismissal_date") && !json.isNull("dismissal_date") ? json.getLong("dismissal_date") : null,
                    json.getLong("started_at"),
                    json.getLong("last_updated_at"));
        }
    }

    static final class EndedRecord {
        final long timestamp;
        final long endedAtMillis;

        EndedRecord(long timestamp, long endedAtMillis) {
            this.timestamp = timestamp;
            this.endedAtMillis = endedAtMillis;
        }
    }

    private final SharedPreferences prefs;
    private final Map<String, ActiveRecord> active = new LinkedHashMap<>();
    private final Map<String, EndedRecord> ended = new LinkedHashMap<>();

    LiveUpdateStore(@NonNull SharedPreferences prefs) {
        this.prefs = prefs;
        load();
    }

    @Nullable ActiveRecord getActive(@NonNull String name) {
        return active.get(name);
    }

    @NonNull List<ActiveRecord> getAllActive() {
        return new ArrayList<>(active.values());
    }

    @Nullable EndedRecord getEnded(@NonNull String name) {
        return ended.get(name);
    }

    void putActive(@NonNull ActiveRecord record) {
        ended.remove(record.name);
        active.put(record.name, record);
        save();
    }

    void markEnded(@NonNull String name, long timestamp, long nowMillis) {
        active.remove(name);
        ended.put(name, new EndedRecord(timestamp, nowMillis));
        pruneEnded(nowMillis);
        save();
    }

    void pruneEnded(long nowMillis) {
        boolean changed = false;
        Iterator<EndedRecord> it = ended.values().iterator();
        while (it.hasNext()) {
            if (nowMillis - it.next().endedAtMillis > ENDED_RETENTION_MS) {
                it.remove();
                changed = true;
            }
        }
        if (changed) {
            save();
        }
    }

    void clear() {
        active.clear();
        ended.clear();
        prefs.edit().remove(SharedPrefs.KEY_LIVE_UPDATES).commit();
    }

    private void load() {
        String stored = prefs.getString(SharedPrefs.KEY_LIVE_UPDATES, null);
        if (stored == null) {
            return;
        }

        try {
            JSONObject root = new JSONObject(stored);

            JSONObject activeJson = root.optJSONObject(KEY_ACTIVE);
            if (activeJson != null) {
                Iterator<String> names = activeJson.keys();
                while (names.hasNext()) {
                    String name = names.next();
                    active.put(name, ActiveRecord.fromJson(activeJson.getJSONObject(name)));
                }
            }

            JSONObject endedJson = root.optJSONObject(KEY_ENDED);
            if (endedJson != null) {
                Iterator<String> names = endedJson.keys();
                while (names.hasNext()) {
                    String name = names.next();
                    JSONObject record = endedJson.getJSONObject(name);
                    ended.put(name, new EndedRecord(record.getLong("timestamp"), record.getLong("ended_at")));
                }
            }
        } catch (JSONException e) {
            Optimobile.log(TAG, "Live Updates: stored state is corrupt, discarding: " + e.getMessage());
            active.clear();
            ended.clear();
        }
    }

    private void save() {
        try {
            JSONObject activeJson = new JSONObject();
            for (ActiveRecord record : active.values()) {
                activeJson.put(record.name, record.toJson());
            }

            JSONObject endedJson = new JSONObject();
            for (Map.Entry<String, EndedRecord> entry : ended.entrySet()) {
                endedJson.put(entry.getKey(), new JSONObject()
                        .put("timestamp", entry.getValue().timestamp)
                        .put("ended_at", entry.getValue().endedAtMillis));
            }

            JSONObject root = new JSONObject()
                    .put(KEY_VERSION, VERSION)
                    .put(KEY_ACTIVE, activeJson)
                    .put(KEY_ENDED, endedJson);

            prefs.edit().putString(SharedPrefs.KEY_LIVE_UPDATES, root.toString()).commit();
        } catch (JSONException e) {
            Optimobile.log(TAG, "Live Updates: failed to persist state: " + e.getMessage());
        }
    }
}
