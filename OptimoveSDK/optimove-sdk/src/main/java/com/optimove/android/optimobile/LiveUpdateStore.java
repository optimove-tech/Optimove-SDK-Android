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

/**
 * Persists active Live Updates, plus a short-lived record of ended ones so a late or re-delivered
 * push cannot bring an ended activity back. Not thread-safe; callers synchronise.
 */
final class LiveUpdateStore {

    private static final String TAG = LiveUpdateStore.class.getName();

    static final long ENDED_RETENTION_MS = 24 * 60 * 60 * 1000L;

    private static final String KEY_ACTIVE = "active";
    private static final String KEY_ENDED = "ended";

    private final SharedPreferences prefs;
    private @Nullable Map<String, LiveUpdate> active;
    private @Nullable Map<String, Long> endedAt;

    LiveUpdateStore(@NonNull SharedPreferences prefs) {
        this.prefs = prefs;
    }

    @Nullable LiveUpdate getActive(@NonNull String activityId) {
        load();
        return active.get(activityId);
    }

    @NonNull List<LiveUpdate> getAllActive() {
        load();
        return new ArrayList<>(active.values());
    }

    boolean isEnded(@NonNull String activityId) {
        load();
        return endedAt.containsKey(activityId);
    }

    void putActive(@NonNull LiveUpdate liveUpdate) {
        load();
        endedAt.remove(liveUpdate.getActivityId());
        active.put(liveUpdate.getActivityId(), liveUpdate);
        save();
    }

    void markEnded(@NonNull String activityId, long now) {
        load();
        active.remove(activityId);
        endedAt.put(activityId, now);
        pruneEnded(now);
        save();
    }

    void clear() {
        active = new LinkedHashMap<>();
        endedAt = new LinkedHashMap<>();
        prefs.edit().remove(SharedPrefs.KEY_LIVE_UPDATES).commit();
    }

    private void pruneEnded(long now) {
        Iterator<Long> it = endedAt.values().iterator();
        while (it.hasNext()) {
            if (now - it.next() > ENDED_RETENTION_MS) {
                it.remove();
            }
        }
    }

    private void load() {
        if (active != null && endedAt != null) {
            return;
        }

        active = new LinkedHashMap<>();
        endedAt = new LinkedHashMap<>();

        String stored = prefs.getString(SharedPrefs.KEY_LIVE_UPDATES, null);
        if (stored == null) {
            return;
        }

        try {
            JSONObject root = new JSONObject(stored);

            JSONObject activeJson = root.optJSONObject(KEY_ACTIVE);
            if (activeJson != null) {
                Iterator<String> ids = activeJson.keys();
                while (ids.hasNext()) {
                    String id = ids.next();
                    active.put(id, LiveUpdate.fromJson(activeJson.getJSONObject(id)));
                }
            }

            JSONObject endedJson = root.optJSONObject(KEY_ENDED);
            if (endedJson != null) {
                Iterator<String> ids = endedJson.keys();
                while (ids.hasNext()) {
                    String id = ids.next();
                    endedAt.put(id, endedJson.getLong(id));
                }
            }
        } catch (JSONException e) {
            Optimobile.log(TAG, "Live Updates: stored state is corrupt, discarding: " + e.getMessage());
            active.clear();
            endedAt.clear();
        }

        pruneEnded(System.currentTimeMillis());
    }

    private void save() {
        try {
            JSONObject activeJson = new JSONObject();
            for (LiveUpdate liveUpdate : active.values()) {
                activeJson.put(liveUpdate.getActivityId(), liveUpdate.toJson());
            }

            JSONObject endedJson = new JSONObject();
            for (Map.Entry<String, Long> entry : endedAt.entrySet()) {
                endedJson.put(entry.getKey(), entry.getValue().longValue());
            }

            JSONObject root = new JSONObject();
            root.put(KEY_ACTIVE, activeJson);
            root.put(KEY_ENDED, endedJson);

            // commit(): the push service may be torn down as soon as onMessageReceived returns
            prefs.edit().putString(SharedPrefs.KEY_LIVE_UPDATES, root.toString()).commit();
        } catch (JSONException e) {
            Optimobile.log(TAG, "Live Updates: failed to persist state: " + e.getMessage());
        }
    }
}
