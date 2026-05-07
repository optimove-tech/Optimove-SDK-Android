package com.optimove.android.optistream;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.Collections;
import java.util.List;

public interface OptistreamPersistanceAdapter {

    boolean insertEvent(String eventJson);

    void removeEvents(String lastId);

    void removeEventsByIds(@NonNull List<Long> rowIds);

    @Nullable EventsBulk getFirstEvents(int numberOfEvents);

    final class QueuedEvent {
        private final long rowId;
        private final String eventJson;

        public QueuedEvent(long rowId, @NonNull String eventJson) {
            this.rowId = rowId;
            this.eventJson = eventJson;
        }

        public long getRowId() {
            return rowId;
        }

        @NonNull
        public String getEventJson() {
            return eventJson;
        }
    }

    class EventsBulk {

        private final List<QueuedEvent> events;

        public EventsBulk(@NonNull List<QueuedEvent> events) {
            this.events = events;
        }

        @NonNull
        public List<QueuedEvent> getEvents() {
            return events;
        }

        public boolean isEmpty() {
            return events.isEmpty();
        }

        @Nullable
        public String getLastIdLegacy() {
            if (events.isEmpty()) {
                return null;
            }
            return String.valueOf(events.get(events.size() - 1).getRowId());
        }

        @NonNull
        public List<String> getEventJsons() {
            if (events.isEmpty()) {
                return Collections.emptyList();
            }
            java.util.ArrayList<String> jsons = new java.util.ArrayList<>(events.size());
            for (QueuedEvent e : events) {
                jsons.add(e.getEventJson());
            }
            return jsons;
        }
    }
}
