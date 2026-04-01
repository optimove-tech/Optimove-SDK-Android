package com.optimove.android.optimobile;

import androidx.annotation.Nullable;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

class OverlayMessagingRendererEvent {
    final String type;
    final boolean immediateFlush;
    @Nullable final JSONObject data;

    private OverlayMessagingRendererEvent(String type, boolean immediateFlush, @Nullable JSONObject data) {
        this.type = type;
        this.immediateFlush = immediateFlush;
        this.data = data;
    }

    static List<OverlayMessagingRendererEvent> parseAll(@Nullable JSONArray raw) {
        List<OverlayMessagingRendererEvent> result = new ArrayList<>();
        if (raw == null) return result;

        for (int i = 0; i < raw.length(); i++) {
            JSONObject obj = raw.optJSONObject(i);
            if (obj == null) continue;
            result.add(new OverlayMessagingRendererEvent(
                obj.optString("type"),
                obj.optBoolean("immediateFlush", true),
                obj.optJSONObject("data")
            ));
        }
        return result;
    }
}
