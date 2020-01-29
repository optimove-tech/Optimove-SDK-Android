package com.optimove.sdk.optimove_sdk.main;

import com.optimove.sdk.optimove_sdk.main.events.OptimoveEvent;

import java.util.Date;

public class EventContext {

    private OptimoveEvent optimoveEvent;
    private long timestampInMillis;
    private int processingTimeout;

    public EventContext(OptimoveEvent optimoveEvent) {
        this.optimoveEvent = optimoveEvent;
        this.timestampInMillis = System.currentTimeMillis();
    }

    public EventContext(OptimoveEvent optimoveEvent, int processingTimeout) {
        this.optimoveEvent = optimoveEvent;
        this.processingTimeout = processingTimeout;
        this.timestampInMillis = System.currentTimeMillis();
    }


    public int getExecutionTimeout() {
        return processingTimeout;
    }

    public void setExecutionTimeout(int executionTimeout) {
        this.processingTimeout = executionTimeout;
    }

    public OptimoveEvent getOptimoveEvent() {
        return optimoveEvent;
    }

    public void setOptimoveEvent(OptimoveEvent optimoveEvent) {
        this.optimoveEvent = optimoveEvent;
    }

    public long getTimestampInMillis() {
        return timestampInMillis;
    }

    public void setTimestampInMillis(long timestampInMillis) {
        this.timestampInMillis = timestampInMillis;
    }
}
