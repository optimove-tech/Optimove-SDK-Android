package com.optimove.android.optistream;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.List;

public interface OptistreamPersistanceAdapter {

    boolean insertEvent(String eventJson);

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
    }
}
