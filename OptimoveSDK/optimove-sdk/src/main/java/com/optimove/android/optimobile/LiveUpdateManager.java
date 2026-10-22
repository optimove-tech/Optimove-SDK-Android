package com.optimove.android.optimobile;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

final class LiveUpdateManager {

    private static final String TAG = LiveUpdateManager.class.getName();

    static final long INACTIVITY_MS = 12 * 60 * 60 * 1000L;

    static final String PROP_ACTIVITY_ID = "optimoveActivityId";
    static final String PROP_TYPE = "attributesType";
    static final String PROP_REASON = "reason";
    static final String REASON_CLEARED = "cleared";

    private static final class Outcome {
        final LiveUpdateEvent event;
        final LiveUpdate liveUpdate;
        final @Nullable String trackEventType;
        final @Nullable String trackReason;

        Outcome(LiveUpdateEvent event, LiveUpdate liveUpdate, @Nullable String trackEventType, @Nullable String trackReason) {
            this.event = event;
            this.liveUpdate = liveUpdate;
            this.trackEventType = trackEventType;
            this.trackReason = trackReason;
        }
    }

    private final Context context;
    private final LiveUpdateHandler handler;
    private final LiveUpdateStore store;
    private final TopicSubscriber topics;
    private final EventTracker events;
    private final Clock clock;
    private final Object lock = new Object();

    LiveUpdateManager(@NonNull Context context,
                      @NonNull LiveUpdateHandler handler,
                      @NonNull LiveUpdateStore store,
                      @NonNull TopicSubscriber topics,
                      @NonNull EventTracker events,
                      @NonNull Clock clock) {
        this.context = context.getApplicationContext();
        this.handler = handler;
        this.store = store;
        this.topics = topics;
        this.events = events;
        this.clock = clock;
    }

    void handlePush(@Nullable Object rawEnvelope) {
        LiveUpdateEnvelope envelope;
        try {
            envelope = LiveUpdateEnvelope.parse(rawEnvelope);
        } catch (JSONException e) {
            Optimobile.log(TAG, "Live Updates: dropping malformed " + LiveUpdateEnvelope.KEY + ": " + e.getMessage());
            return;
        }

        Optimobile.log(TAG, "Live Updates: " + envelope.event + " for " + envelope.name
                + " (timestamp " + envelope.timestamp + ")");
        handle(envelope);
    }

    void handle(@NonNull LiveUpdateEnvelope envelope) {
        List<Outcome> outcomes = new ArrayList<>();

        synchronized (lock) {
            long now = clock.nowMillis();
            expireInactiveLocked(now, outcomes);

            LiveUpdateStore.EndedRecord ended = store.getEnded(envelope.name);
            if (ended != null && !(envelope.event == LiveUpdateEvent.START && envelope.timestamp > ended.timestamp)) {
                Optimobile.log(TAG, "Live Updates: " + envelope.name + " has ended, ignoring " + envelope.event);
                return;
            }

            LiveUpdateStore.ActiveRecord active = store.getActive(envelope.name);
            if (active != null) {
                applyToActiveLocked(active, envelope, now, outcomes);
            } else if (envelope.event == LiveUpdateEvent.START) {
                startLocked(envelope, now, outcomes);
            } else {
                Optimobile.log(TAG, "Live Updates: " + envelope.event + " for unknown " + envelope.name + ", ignoring");
                if (envelope.event == LiveUpdateEvent.END && envelope.topic != null) {
                    topics.unsubscribe(envelope.topic);
                }
            }
        }

        dispatch(outcomes);
    }

    void end(@NonNull String name) {
        List<Outcome> outcomes = new ArrayList<>();

        synchronized (lock) {
            LiveUpdateStore.ActiveRecord active = store.getActive(name);
            if (active == null) {
                Optimobile.log(TAG, "Live Updates: end for inactive " + name + ", ignoring");
                return;
            }
            endLocked(active, null, LiveUpdateEndReason.LOCAL, null, clock.nowMillis(), outcomes);
        }

        dispatch(outcomes);
    }

    void endFromNotificationDismissal(@NonNull String name) {
        List<Outcome> outcomes = new ArrayList<>();

        synchronized (lock) {
            LiveUpdateStore.ActiveRecord active = store.getActive(name);
            if (active == null) {
                return;
            }
            long now = clock.nowMillis();
            LiveUpdateEndReason reason = now - active.lastUpdatedAtMillis > INACTIVITY_MS
                    ? LiveUpdateEndReason.EXPIRED
                    : LiveUpdateEndReason.LOCAL;
            endLocked(active, null, reason, null, now, outcomes);
        }

        dispatch(outcomes);
    }

    void sweep() {
        List<Outcome> outcomes = new ArrayList<>();

        synchronized (lock) {
            long now = clock.nowMillis();
            store.pruneEnded(now);
            expireInactiveLocked(now, outcomes);
        }

        dispatch(outcomes);
    }

    @NonNull List<LiveUpdate> getActive() {
        synchronized (lock) {
            List<LiveUpdate> result = new ArrayList<>();
            for (LiveUpdateStore.ActiveRecord record : store.getAllActive()) {
                result.add(toLiveUpdate(record, null, null, null));
            }
            return result;
        }
    }

    @NonNull List<String> clearAll() {
        List<Outcome> outcomes = new ArrayList<>();
        List<String> names = new ArrayList<>();

        synchronized (lock) {
            long now = clock.nowMillis();
            for (LiveUpdateStore.ActiveRecord active : store.getAllActive()) {
                names.add(active.name);
                endLocked(active, null, LiveUpdateEndReason.LOCAL, REASON_CLEARED, now, outcomes);
            }
            store.clear();
        }

        dispatch(outcomes);
        return names;
    }

    private void startLocked(@NonNull LiveUpdateEnvelope envelope, long now, @NonNull List<Outcome> outcomes) {
        LiveUpdateStore.ActiveRecord record = new LiveUpdateStore.ActiveRecord(
                envelope.name,
                envelope.type,
                envelope.topic,
                envelope.timestamp,
                envelope.contentState,
                envelope.dismissalDateSeconds,
                now,
                now);
        store.putActive(record);
        if (envelope.topic != null) {
            topics.subscribe(envelope.topic);
        }
        outcomes.add(new Outcome(
                LiveUpdateEvent.START,
                toLiveUpdate(record, envelope.alertTitle, envelope.alertBody, null),
                AnalyticsContract.EVENT_TYPE_LIVE_ACTIVITY_STARTED,
                null));
    }

    private void applyToActiveLocked(@NonNull LiveUpdateStore.ActiveRecord active,
                                     @NonNull LiveUpdateEnvelope envelope,
                                     long now,
                                     @NonNull List<Outcome> outcomes) {
        if (envelope.timestamp < active.timestamp) {
            Optimobile.log(TAG, "Live Updates: stale " + envelope.event + " for " + envelope.name
                    + " (" + envelope.timestamp + " < " + active.timestamp + "), ignoring");
            return;
        }

        switch (envelope.event) {
            case START:
                Optimobile.log(TAG, "Live Updates: " + envelope.name + " already started, ignoring start");
                return;
            case UPDATE:
                if (envelope.type != null && !envelope.type.equals(active.type)) {
                    Optimobile.log(TAG, "Live Updates: type changed for " + envelope.name + " from " + active.type
                            + " to " + envelope.type + ", keeping " + active.type);
                }
                LiveUpdateStore.ActiveRecord updated = new LiveUpdateStore.ActiveRecord(
                        active.name,
                        active.type,
                        active.topic != null ? active.topic : envelope.topic,
                        envelope.timestamp,
                        envelope.contentState,
                        envelope.dismissalDateSeconds != null ? envelope.dismissalDateSeconds : active.dismissalDateSeconds,
                        active.startedAtMillis,
                        now);
                store.putActive(updated);
                outcomes.add(new Outcome(
                        LiveUpdateEvent.UPDATE,
                        toLiveUpdate(updated, envelope.alertTitle, envelope.alertBody, null),
                        null,
                        null));
                return;
            case END:
                endLocked(active, envelope, LiveUpdateEndReason.SERVER, null, now, outcomes);
        }
    }

    private void endLocked(@NonNull LiveUpdateStore.ActiveRecord active,
                           @Nullable LiveUpdateEnvelope envelope,
                           @NonNull LiveUpdateEndReason reason,
                           @Nullable String reasonOverride,
                           long now,
                           @NonNull List<Outcome> outcomes) {
        boolean fromServer = envelope != null;
        LiveUpdateStore.ActiveRecord last = new LiveUpdateStore.ActiveRecord(
                active.name,
                active.type,
                active.topic != null ? active.topic : (fromServer ? envelope.topic : null),
                fromServer ? envelope.timestamp : active.timestamp,
                fromServer && envelope.hasContentState ? envelope.contentState : active.contentState,
                fromServer && envelope.dismissalDateSeconds != null ? envelope.dismissalDateSeconds : active.dismissalDateSeconds,
                active.startedAtMillis,
                now);

        store.markEnded(last.name, last.timestamp, now);
        if (last.topic != null) {
            topics.unsubscribe(last.topic);
        }

        outcomes.add(new Outcome(
                LiveUpdateEvent.END,
                toLiveUpdate(last, fromServer ? envelope.alertTitle : null, fromServer ? envelope.alertBody : null, reason),
                AnalyticsContract.EVENT_TYPE_LIVE_ACTIVITY_ENDED,
                reasonOverride != null ? reasonOverride : reason.wireValue()));
    }

    private void expireInactiveLocked(long now, @NonNull List<Outcome> outcomes) {
        for (LiveUpdateStore.ActiveRecord active : store.getAllActive()) {
            if (now - active.lastUpdatedAtMillis > INACTIVITY_MS) {
                Optimobile.log(TAG, "Live Updates: " + active.name + " inactive for over 12h, ending");
                endLocked(active, null, LiveUpdateEndReason.EXPIRED, null, now, outcomes);
            }
        }
    }

    private void dispatch(@NonNull List<Outcome> outcomes) {
        for (Outcome outcome : outcomes) {
            try {
                handler.onUpdate(context, outcome.event, outcome.liveUpdate);
            } catch (RuntimeException e) {
                Optimobile.log(TAG, "Live Updates: handler threw on " + outcome.event + " for "
                        + outcome.liveUpdate.getName() + ": " + e);
            }

            if (outcome.trackEventType != null) {
                events.track(outcome.trackEventType, eventProps(outcome.liveUpdate, outcome.trackReason));
            }
        }
    }

    private static @NonNull LiveUpdate toLiveUpdate(@NonNull LiveUpdateStore.ActiveRecord record,
                                                    @Nullable String alertTitle,
                                                    @Nullable String alertBody,
                                                    @Nullable LiveUpdateEndReason endReason) {
        return new LiveUpdate(
                record.name,
                record.type,
                record.timestamp,
                record.contentState,
                alertTitle,
                alertBody,
                record.dismissalDateSeconds != null ? record.dismissalDateSeconds * 1000L : null,
                record.startedAtMillis,
                record.lastUpdatedAtMillis,
                endReason);
    }

    private static @NonNull JSONObject eventProps(@NonNull LiveUpdate liveUpdate, @Nullable String reason) {
        JSONObject props = new JSONObject();
        try {
            props.put(PROP_ACTIVITY_ID, liveUpdate.getName());
            props.put(PROP_TYPE, liveUpdate.getType());
            if (reason != null) {
                props.put(PROP_REASON, reason);
            }
        } catch (JSONException ignored) {
        }
        return props;
    }
}
