package com.optimove.android.optistream;

import androidx.annotation.Nullable;

import java.util.List;

public interface OptistreamPersistanceAdapter {

    boolean insertEvent(String eventJson);

    void removeEvents(String lastId);

    @Nullable EventsBulk getFirstEvents(int numberOfEvents);

    class EventsBulk {

        private String lastId;
        private List<String> eventJsons;

        public EventsBulk(String lastId, List<String> eventJsons) {
            this.lastId = lastId;
            this.eventJsons = eventJsons;
        }

        public String getLastId() {
            return lastId;
        }

        public void setLastId(String lastId) {
            this.lastId = lastId;
        }

        public List<String> getEventJsons() {
            return eventJsons;
        }

        public void setEventJsons(List<String> eventJsons) {
            this.eventJsons = eventJsons;
        }
    }
}
