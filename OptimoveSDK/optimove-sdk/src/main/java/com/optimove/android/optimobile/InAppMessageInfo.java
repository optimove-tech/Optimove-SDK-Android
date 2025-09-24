package com.optimove.android.optimobile;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.json.JSONObject;

import java.util.Date;

/**
 * Information about an in-app message for interceptor evaluation.
 */
public interface InAppMessageInfo {
    
    int getMessageId();
    
    @Nullable
    String getPresentedWhen();
    
    @NonNull
    JSONObject getContent();
    
    @Nullable
    JSONObject getData();
    
    @Nullable
    JSONObject getInboxConfig();
    
    @Nullable
    Date getUpdatedAt();
    
    @Nullable
    Date getExpiresAt();
    
    @Nullable
    Date getSentAt();
}
