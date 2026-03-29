package com.optimove.android.optimobile;

import android.content.Context;

import androidx.annotation.UiThread;

class OverlayMessagingManager {

    private static final int SESSION_SLOT_CAPACITY = 1;
    private static final int IMMEDIATE_SLOT_CAPACITY = 1;

    enum MessageType {
        SESSION,
        IMMEDIATE
    }

    private final Context context;

    private int sessionSlotCount = 0;
    private int immediateSlotCount = 0;

    OverlayMessagingManager(Context context) {
        this.context = context.getApplicationContext();
    }

    @UiThread
    void onTriggerReceived(MessageType type) {
        switch (type) {
            case SESSION:
                if (sessionSlotCount >= SESSION_SLOT_CAPACITY) return;
                sessionSlotCount++;
                loadMessage(type);
                break;
            case IMMEDIATE:
                if (immediateSlotCount >= IMMEDIATE_SLOT_CAPACITY) return;
                immediateSlotCount++;
                loadMessage(type);
                break;
        }
    }

    @UiThread
    void onSlotCleared(MessageType type) {
        switch (type) {
            case SESSION:
                sessionSlotCount = Math.max(0, sessionSlotCount - 1);
                break;
            case IMMEDIATE:
                immediateSlotCount = Math.max(0, immediateSlotCount - 1);
                break;
        }
    }

    private void loadMessage(MessageType type) {
        Optimobile.executorService.submit(() -> {
            OverlayMessagingMessage message = OverlayMessagingRequestService.readOverlayMessage(context, type);
            Optimobile.handler.post(() -> onMessageLoaded(type, message));
        });
    }

    @UiThread
    private void onMessageLoaded(MessageType type, OverlayMessagingMessage message) {
        if (message == null) {
            onSlotCleared(type);
            return;
        }
        // TODO: run interceptor, add to display queue
    }
}
