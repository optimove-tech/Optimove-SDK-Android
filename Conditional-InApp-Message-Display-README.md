# Conditional In-App Message Display Feature

This document describes the new conditional in-app message display feature that allows developers to intercept and filter in-app messages before they are shown to users.

## Overview

The conditional display feature provides developers with the ability to:
- Intercept in-app messages before they are displayed
- Apply custom logic to decide whether to show or suppress messages
- Handle both synchronous and asynchronous filtering logic
- Make API calls or check external systems before displaying messages

This is similar to Braze's conditional message display functionality.

## Use Cases

- **User State Filtering**: Suppress messages during sensitive flows (e.g., checkout, onboarding)
- **Milestone-Based Display**: Only show messages when users reach certain achievements
- **External Validation**: Check with CRM systems or other APIs before displaying messages
- **Context-Aware Messaging**: Show messages based on current app context or user behavior

## API Reference

### Setting up a Message Filter

```java
OptimoveInApp.getInstance().setInAppMessageDisplayFilter(new InAppMessageDisplayFilter() {
    @Override
    public void shouldDisplayMessage(@NonNull InAppMessageInfo message, @NonNull InAppMessageFilterCallback callback) {
        // Your filtering logic here
        if (shouldSuppressMessage(message)) {
            callback.onFilterResult(InAppMessageDisplayFilter.FilterResult.SUPPRESS);
        } else {
            callback.onFilterResult(InAppMessageDisplayFilter.FilterResult.SHOW);
        }
    }
});
```

### InAppMessageDisplayFilter Interface

```java
public interface InAppMessageDisplayFilter {
    enum FilterResult {
        SHOW,      // Show the message
        SUPPRESS   // Suppress the message
    }
    
    void shouldDisplayMessage(@NonNull InAppMessageInfo message, @NonNull InAppMessageFilterCallback callback);
}
```

### InAppMessageInfo Interface

The `InAppMessageInfo` interface provides access to message data for filtering decisions:

```java
public interface InAppMessageInfo {
    int getMessageId();                    // Unique message identifier
    String getPresentedWhen();             // When message should be presented
    JSONObject getContent();               // Message content (title, body, etc.)
    JSONObject getData();                  // Custom data attached to message
    JSONObject getInboxConfig();           // Inbox configuration if applicable
    Date getUpdatedAt();                   // Last update timestamp
    Date getExpiresAt();                   // Expiration timestamp
    Date getSentAt();                      // Sent timestamp
}
```

## Examples

### 1. Simple State-Based Filtering

```java
OptimoveInApp.getInstance().setInAppMessageDisplayFilter(new InAppMessageDisplayFilter() {
    @Override
    public void shouldDisplayMessage(@NonNull InAppMessageInfo message, @NonNull InAppMessageFilterCallback callback) {
        // Suppress messages during checkout
        if (isUserInCheckoutFlow()) {
            callback.onFilterResult(FilterResult.SUPPRESS);
            return;
        }
        
        // Show all other messages
        callback.onFilterResult(FilterResult.SHOW);
    }
});
```

### 2. Custom Data-Based Filtering

```java
OptimoveInApp.getInstance().setInAppMessageDisplayFilter(new InAppMessageDisplayFilter() {
    @Override
    public void shouldDisplayMessage(@NonNull InAppMessageInfo message, @NonNull InAppMessageFilterCallback callback) {
        try {
            JSONObject data = message.getData();
            if (data != null && data.has("vip_only") && data.getBoolean("vip_only")) {
                // Only show VIP messages to VIP users
                boolean isVip = checkUserVipStatus();
                callback.onFilterResult(isVip ? FilterResult.SHOW : FilterResult.SUPPRESS);
                return;
            }
        } catch (JSONException e) {
            // Handle error
        }
        
        // Default: show message
        callback.onFilterResult(FilterResult.SHOW);
    }
});
```

### 3. Async API Validation

```java
OptimoveInApp.getInstance().setInAppMessageDisplayFilter(new InAppMessageDisplayFilter() {
    @Override
    public void shouldDisplayMessage(@NonNull InAppMessageInfo message, @NonNull InAppMessageFilterCallback callback) {
        // Check if message requires external validation
        if (requiresExternalValidation(message)) {
            // Make async API call
            checkUserEligibilityAsync(message.getMessageId(), new ApiCallback() {
                @Override
                public void onResult(boolean eligible) {
                    callback.onFilterResult(eligible ? FilterResult.SHOW : FilterResult.SUPPRESS);
                }
                
                @Override
                public void onError() {
                    // On error, suppress the message
                    callback.onFilterResult(FilterResult.SUPPRESS);
                }
            });
            return;
        }
        
        // No validation needed, show message
        callback.onFilterResult(FilterResult.SHOW);
    }
});
```

## Important Considerations

### Threading
- The filter method is called on a **background thread** to avoid blocking the UI
- The callback can be invoked from **any thread** - the SDK handles thread switching
- Perform heavy operations (API calls, database queries) safely in the filter method

### Timeout Handling
- You **must** call the callback within a reasonable time frame (default: 5 seconds)
- If the callback is not called within the timeout period, the message will be **automatically suppressed**
- The timeout prevents the SDK from waiting indefinitely for a response

### Error Handling
- If an exception occurs in your filter method, the message will be **automatically suppressed**
- Always handle exceptions gracefully and call the callback appropriately
- Consider fallback behavior for network errors or other issues

### Performance
- Keep filter logic lightweight for better user experience
- Consider caching results of expensive operations
- Use async operations for API calls or database queries

## Removing the Filter

To remove the filter and return to default behavior:

```java
OptimoveInApp.getInstance().setInAppMessageDisplayFilter(null);
```

## Migration Notes

This feature is **backward compatible**. Existing integrations will continue to work unchanged:
- If no filter is set, messages are displayed normally
- Existing display mode settings (`PAUSED`, etc.) continue to work as before
- The feature is opt-in and doesn't affect default SDK behavior

## Testing

The demo app (`MainActivity.java`) includes a comprehensive example that shows:
- State-based filtering (simulated checkout flow)
- Custom data filtering (VIP-only messages)
- Async API validation with simulated network calls
- Error handling and timeout scenarios

## Technical Implementation

### Architecture
- **Filter Execution**: Background thread with configurable timeout
- **Callback Handling**: Thread-safe with atomic operations to prevent duplicate callbacks
- **Integration Point**: Intercepts messages in `InAppMessagePresenter` before UI creation
- **Interface Design**: Clean separation between internal `InAppMessage` and public `InAppMessageInfo`

### Thread Safety
- Uses `AtomicBoolean` to ensure callbacks are only processed once
- Proper synchronization between background filter execution and UI thread
- Timeout handling via `ScheduledExecutorService`

## Support

For questions or issues related to this feature, please refer to the main SDK documentation or contact support.
