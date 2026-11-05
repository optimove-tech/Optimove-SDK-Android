package com.optimove.android.optimobile;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.json.JSONObject;

interface EventTracker {

    void track(@NonNull String eventType, @Nullable JSONObject properties);
}
