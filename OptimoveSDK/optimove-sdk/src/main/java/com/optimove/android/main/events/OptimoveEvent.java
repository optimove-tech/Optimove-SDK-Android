package com.optimove.android.main.events;

import java.util.Map;
import java.util.UUID;

/**
 * Defines a <i><b>Custom Event</b></i> that can be validated by the {@code SDK} and reported to <b>OptiTrack</b>.
 */
public abstract class OptimoveEvent {

    private long timestamp;
    private String requestId;

    public OptimoveEvent() {
        this.timestamp = System.currentTimeMillis();
        this.requestId = UUID.randomUUID().toString();
    }

    public OptimoveEvent(long timestamp, String requestId) {
        this.timestamp = timestamp;
        this.requestId = requestId == null ? UUID.randomUUID().toString() : requestId;
    }

    public OptimoveEvent(String requestId) {
        this.timestamp = System.currentTimeMillis();
        this.requestId = requestId;
    }

    /**
     * <b>Mandatory</b>: Override this method to declare the Event's {@code name}.<br>
     * <b>Note</b>: The event's name is the <b>key</b> that is set in the event's {@code configurations}, <b>not</b> its display name.
     *
     * @return the Event's {@code name}
     */
    public abstract String getName();

    /**
     * <b>Mandatory</b>: Override this method to declare the Event's {@code parameters}
     *
     * @return the Event's {@code parameters}
     */
    public abstract Map<String, Object> getParameters();

    public long getTimestamp() {
        return timestamp;
    }

    public String getRequestId() {
        return requestId;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

}
