package com.optimove.sdk.optimove_sdk.optimobile;

public class InAppInboxSummary {
    private final int totalCount;
    private final int unreadCount;

    InAppInboxSummary(int totalCount, int unreadCount) {
        this.totalCount = totalCount;
        this.unreadCount = unreadCount;
    }

    public int getTotalCount() {
        return totalCount;
    }

    public int getUnreadCount() {
        return unreadCount;
    }
}
