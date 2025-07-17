package com.optimove.android.embeddedmessaging;

import androidx.annotation.NonNull;

public enum EventType {
    CLICKED ("embedded-message.clicked"),
    READ("embedded-message.read"),
    UNREAD("embedded-message.unread"),
    DELETED("embedded-message.deleted");

    private final String name;

    EventType(String name) {
        this.name = name;
    }

    @NonNull
    public String toString() {
        return this.name;
    }
}