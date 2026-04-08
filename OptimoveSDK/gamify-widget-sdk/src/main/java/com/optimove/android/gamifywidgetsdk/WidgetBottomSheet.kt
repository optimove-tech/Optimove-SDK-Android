package com.optimove.android.gamifywidgetsdk

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.FrameLayout
import android.widget.ProgressBar
import android.widget.TextView
import android.util.Log
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import org.json.JSONObject

internal class WidgetBottomSheet : BottomSheetDialogFragment() {

    private lateinit var webView: WebView
    private lateinit var progressBar: ProgressBar
    private lateinit var errorView: TextView

    private val widgetUrl: String get() = arguments?.getString(ARG_WIDGET_URL) ?: ""
    private val userId: String? get() = arguments?.getString(ARG_USER_ID)
    private val token: String? get() = arguments?.getString(ARG_TOKEN)

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val root = FrameLayout(requireContext())

        // WebView
        webView = WebView(requireContext()).apply {
            layoutParams = FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT
            )
            settings.apply {
                javaScriptEnabled = true
                domStorageEnabled = true
                loadWithOverviewMode = true
                useWideViewPort = true
            }
            addJavascriptInterface(
                AndroidBridge(
                    onClose = { activity?.runOnUiThread { dismiss() } },
                    onReady = { activity?.runOnUiThread { sendInit() } }
                ),
                "AndroidBridge"
            )
            webViewClient = object : WebViewClient() {
                override fun onPageFinished(view: WebView, url: String) {
                    activity?.runOnUiThread { progressBar.visibility = View.GONE }
                }

                override fun onReceivedError(
                    view: WebView,
                    request: WebResourceRequest,
                    error: WebResourceError
                ) {
                    if (request.isForMainFrame) {
                        activity?.runOnUiThread { showError() }
                    }
                }
            }
        }

        // Loading spinner
        progressBar = ProgressBar(requireContext()).apply {
            layoutParams = FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.WRAP_CONTENT,
                FrameLayout.LayoutParams.WRAP_CONTENT,
                android.view.Gravity.CENTER
            )
        }

        // Error state
        errorView = TextView(requireContext()).apply {
            layoutParams = FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.WRAP_CONTENT,
                FrameLayout.LayoutParams.WRAP_CONTENT,
                android.view.Gravity.CENTER
            )
            text = "Unable to load widget.\nCheck your connection and try again."
            textAlignment = View.TEXT_ALIGNMENT_CENTER
            visibility = View.GONE
        }

        root.addView(webView)
        root.addView(progressBar)
        root.addView(errorView)

        webView.loadUrl(widgetUrl)
        return root
    }

    override fun onStart() {
        super.onStart()
        // Expand the bottom sheet fully on open
        val bottomSheet = (dialog as? BottomSheetDialog)
            ?.findViewById<FrameLayout>(com.google.android.material.R.id.design_bottom_sheet)
        bottomSheet?.let {
            BottomSheetBehavior.from(it).apply {
                state = BottomSheetBehavior.STATE_EXPANDED
                skipCollapsed = true
            }
        }
    }

    /**
     * Called by AndroidBridge.onReady() when the widget fires its READY signal.
     * Posts the INIT payload back via window.postMessage so useBridge picks it up.
     */
    private fun sendInit() {
        val initJson = buildInitJson()
        Log.d(TAG, "sending INIT: $initJson")
        webView.evaluateJavascript("window.postMessage($initJson, '*');", null)
    }

    private fun buildInitJson(): String {
        val obj = JSONObject().apply {
            put("type", "INIT")
            userId?.let { put("userId", it) }
            token?.let { put("token", it) }
        }
        return obj.toString()
    }

    private fun showError() {
        progressBar.visibility = View.GONE
        webView.visibility = View.GONE
        errorView.visibility = View.VISIBLE
    }

    companion object {
        private const val TAG = "GamifyWidget"
        const val FRAGMENT_TAG = "WidgetBottomSheet"
        private const val ARG_WIDGET_URL = "widget_url"
        private const val ARG_USER_ID = "user_id"
        private const val ARG_TOKEN = "token"

        fun newInstance(widgetUrl: String, userId: String?, token: String?): WidgetBottomSheet {
            return WidgetBottomSheet().apply {
                arguments = Bundle().apply {
                    putString(ARG_WIDGET_URL, widgetUrl)
                    userId?.let { putString(ARG_USER_ID, it) }
                    token?.let { putString(ARG_TOKEN, it) }
                }
            }
        }
    }
}
