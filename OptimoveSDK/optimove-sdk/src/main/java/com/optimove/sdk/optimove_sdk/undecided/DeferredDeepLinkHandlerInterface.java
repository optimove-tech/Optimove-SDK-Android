package com.optimove.sdk.optimove_sdk.undecided;

import android.content.Context;

import androidx.annotation.Nullable;

public interface DeferredDeepLinkHandlerInterface {
    /**
     * Override to change the behaviour of deep link. Default none
     *
     * @param data deep link
     * @return
     */
    void handle(Context context, DeferredDeepLinkHelper.DeepLinkResolution resolution, String link, @Nullable DeferredDeepLinkHelper.DeepLink data);
}
