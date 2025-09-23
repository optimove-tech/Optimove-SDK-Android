package com.optimove.android.optimobile;

import androidx.annotation.NonNull;

/**
 * Interface for intercepting in-app messages when display mode is set to INTERCEPTED.
 * 
 * This interface allows you to intercept in-app messages before they are displayed
 * and decide whether to show or suppress them based on your own custom logic.
 * 
 * Use cases:
 * - Suppress messages during sensitive user flows (e.g., checkout, onboarding)
 * - Only show messages when user meets certain conditions (e.g., milestone reached)
 * - Check with external APIs before displaying messages
 * 
 * Example usage:
 * <pre>
 * // Set display mode to INTERCEPTED
 * OptimoveInApp.getInstance().setDisplayMode(OptimoveConfig.InAppDisplayMode.INTERCEPTED);
 * 
 * // Set your interceptor
 * OptimoveInApp.getInstance().setInAppMessageInterceptor(new InAppMessageInterceptor() {
 *     @Override
 *     public void shouldDisplayMessage(@NonNull InAppMessageInfo message, @NonNull InAppMessageInterceptorCallback callback) {
 *         // Your logic here
 *         if (isUserInCheckoutFlow()) {
 *             callback.onInterceptResult(InterceptResult.SUPPRESS);
 *             return;
 *         }
 *         
 *         // Async API call example
 *         checkUserEligibilityAsync(message, new ApiCallback() {
 *             @Override
 *             public void onResult(boolean eligible) {
 *                 callback.onInterceptResult(eligible ? InterceptResult.SHOW : InterceptResult.SUPPRESS);
 *             }
 *         });
 *     }
 * });
 * </pre>
 */
public interface InAppMessageInterceptor {
    
    /**
     * Intercept result options for message display decision.
     */
    enum InterceptResult {
        /** Show the message to the user */
        SHOW,
        /** Suppress the message (do not display) */
        SUPPRESS
    }
    
    /**
     * Called when an in-app message is about to be displayed and display mode is INTERCEPTED.
     * 
     * <p>This method is called on a background thread to avoid blocking the UI.
     * You can perform synchronous or asynchronous operations here.</p>
     * 
     * <p><strong>Important:</strong> You MUST call the callback within a reasonable time frame
     * (typically within 5 seconds). If you don't call the callback, the message will be
     * automatically suppressed after a timeout.</p>
     * 
     * @param message Information about the in-app message that is about to be displayed
     * @param callback Callback to invoke with your decision. Can be called from any thread.
     */
    void shouldDisplayMessage(@NonNull InAppMessageInfo message, @NonNull InAppMessageInterceptorCallback callback);
}
