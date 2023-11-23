package com.optimove.android.optimovemobilesdk;


import android.content.Context;
import android.util.Log;

import androidx.annotation.Nullable;

import com.optimove.android.optimobile.DeferredDeepLinkHandlerInterface;
import com.optimove.android.optimobile.DeferredDeepLinkHelper;


public class MyDeferredDeepLinkHandler implements DeferredDeepLinkHandlerInterface {
    private static final String TAG = MyDeferredDeepLinkHandler.class.getName();

    public void handle(Context context, DeferredDeepLinkHelper.DeepLinkResolution resolution, String link, @Nullable DeferredDeepLinkHelper.DeepLink data) {
        //- Inspect the data payload and run code as needed.

        String output = "DDl resolution: " + resolution + " ";
        if (data == null) {
            output += "null data";
        } else {
            output += data.data.toString();
        }
        Log.d(TAG, output);
    }

}