package com.optimove.android.preferencecenter;

import androidx.annotation.NonNull;

public enum Channel {
    MOBILE_PUSH(489), WEB_PUSH(490), SMS(493), IN_APP(427), WHATSAPP(498), MAIL(15), INBOX(495);
    private final int channel;

    Channel(int channel) {
        this.channel = channel;
    }

    @NonNull
    public int getValue() {
        return channel;
    }

    public static Channel getChannelByValue(int value) {
        switch (value) {
            case 489:
                return MOBILE_PUSH;
            case 490:
                return WEB_PUSH;
            case 493:
                return SMS;
            case 427:
                return IN_APP;
            case 498:
                return WHATSAPP;
            case 15:
                return MAIL;
            case 495:
                return INBOX;
            default:
                throw new IllegalArgumentException("Preference center does not support channel " + value);
        }
    }
}
