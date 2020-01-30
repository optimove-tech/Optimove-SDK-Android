package com.optimove.sdk.demo.deep_links

import android.os.Bundle
import android.util.Log
import android.widget.TextView

import com.optimove.sdk.demo.R
import com.optimove.sdk.optimove_sdk.optipush.deep_link.DeepLinkHandler
import com.optimove.sdk.optimove_sdk.optipush.deep_link.LinkDataError
import com.optimove.sdk.optimove_sdk.optipush.deep_link.LinkDataExtractedListener

import androidx.appcompat.app.AppCompatActivity

class DeepLinkActivity : AppCompatActivity(), LinkDataExtractedListener {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_promo)

        // The DeepLinkHandler doesn't hold strong reference to the Activity so this example is safe
        DeepLinkHandler(intent).extractLinkData(this)
    }

    override fun onDataExtracted(screenName: String, map: Map<String, String>) {
        val outputTv = findViewById<TextView>(R.id.outputTextView)
        val builder = StringBuilder(screenName).append(":\n")
        for (key in map.keys) {
            builder.append(key).append("=").append(map[key]).append("\n")
        }
        outputTv.text = builder.toString()
    }

    override fun onErrorOccurred(error: LinkDataError) {
        // This callback will also be called if no deep link was found, that's why it's just an INFO level log and not ERROR
        Log.i("OPTIPUSH_DEEP_LINK", String.format("Failed to get deep link due to: %s", error))
    }
}
