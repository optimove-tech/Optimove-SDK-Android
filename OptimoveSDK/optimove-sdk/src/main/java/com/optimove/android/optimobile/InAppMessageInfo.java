package com.optimove.android.optimobile;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.json.JSONObject;

import java.util.Date;

/**
 * Information about an in-app message for filtering purposes.
 * 
 * This interface provides access to message data that can be used to make
 * filtering decisions in {@link InAppMessageDisplayFilter}.
 */
public interface InAppMessageInfo {
    
    /**
     * Gets the unique identifier for this message.
     * 
     * @return The message ID
     */
    int getMessageId();
    
    /**
     * Gets when this message should be presented.
     * 
     * @return The presentation timing (e.g., "immediately", "next_open")
     */
    @Nullable
    String getPresentedWhen();
    
    /**
     * Gets the message content as a JSON object.
     * 
     * <p>This contains the full message content including title, body, images, etc.
     * The exact structure depends on the message type and configuration.</p>
     * 
     * @return The message content JSON, never null
     */
    @NonNull
    JSONObject getContent();
    
    /**
     * Gets custom data attached to this message.
     * 
     * <p>This includes any custom key-value pairs that were attached to the message
     * when it was created in the Optimove platform.</p>
     * 
     * @return The custom data JSON, or null if no custom data
     */
    @Nullable
    JSONObject getData();
    
    /**
     * Gets inbox configuration for this message.
     * 
     * @return The inbox configuration JSON, or null if not an inbox message
     */
    @Nullable
    JSONObject getInboxConfig();
    
    /**
     * Gets when this message was last updated.
     * 
     * @return The update timestamp, or null if not available
     */
    @Nullable
    Date getUpdatedAt();
    
    /**
     * Gets when this message expires.
     * 
     * @return The expiration timestamp, or null if the message doesn't expire
     */
    @Nullable
    Date getExpiresAt();
    
    /**
     * Gets when this message was sent to the user.
     * 
     * @return The sent timestamp, or null if not available
     */
    @Nullable
    Date getSentAt();
}
