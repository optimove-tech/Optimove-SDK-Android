package com.optimove.android.optimovemobilesdk

import android.content.Context
import android.util.Log
import android.widget.Toast
import com.optimove.android.optimobile.DeferredDeepLinkHandlerInterface
import com.optimove.android.optimobile.DeferredDeepLinkHelper

class MyDeferredDeepLinkHandler : DeferredDeepLinkHandlerInterface {

    override fun handle(
        context: Context,
        resolution: DeferredDeepLinkHelper.DeepLinkResolution,
        link: String,
        data: DeferredDeepLinkHelper.DeepLink?
    ) {
        val output = "DDl resolution: $resolution ${data?.data?.toString() ?: "null data"}"
        Log.d(TAG, output)
        Toast.makeText(context, output, Toast.LENGTH_SHORT).show()
    }

    companion object {
        private val TAG = MyDeferredDeepLinkHandler::class.java.name
    }
}
