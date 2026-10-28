package com.optimove.android.gamifywidgetsdk;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.robolectric.Shadows.shadowOf;

import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Looper;
import android.webkit.WebResourceRequest;
import android.webkit.WebView;

import androidx.annotation.Nullable;

import com.optimove.android.AuthManager;
import com.optimove.android.AuthTokenProvider;
import com.optimove.android.R;

import org.json.JSONObject;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.shadows.ShadowDialog;
import org.robolectric.shadows.ShadowWebView;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

@RunWith(RobolectricTestRunner.class)
public class GamifyWidgetAuthTest {

    private static final int TENANT_ID = 593;
    private static final String WIDGET_URL = "https://opti-ls-widget-eu.optimove.net";

    private Activity activity;

    @Before
    public void setUp() {
        activity = Robolectric.buildActivity(Activity.class).setup().get();
        ShadowDialog.reset();
    }

    @After
    public void tearDown() {
        GamifyWidgetSDK.getInstance().closeWidget();
        GamifyWidgetSDK.resetForTesting(null);
    }

    @Test
    public void openWidget_postsInitWithJwtMintedForGivenUser() throws Exception {
        List<String> requestedFor = new ArrayList<>();
        configure("sdk-user", authManager((userId, callback) -> {
            requestedFor.add(userId);
            callback.onComplete("jwt-token", null);
        }));

        GamifyWidgetSDK.getInstance().openWidget(activity, new OpenLoyaltyWidgetParams("customer-1", null));
        WebView webView = webView();
        fireReady(webView);

        assertEquals(WIDGET_URL + "/593/customer-1", shadowOf(webView).getLastLoadedUrl());
        assertEquals(1, requestedFor.size());
        assertEquals("customer-1", requestedFor.get(0));
        JSONObject init = lastInit(webView);
        assertEquals("customer-1", init.getString("userId"));
        assertEquals("jwt-token", init.getString("token"));
    }

    @Test
    public void openWidget_fallsBackToIdentifiedSdkUser() throws Exception {
        List<String> requestedFor = new ArrayList<>();
        configure("sdk-user", authManager((userId, callback) -> {
            requestedFor.add(userId);
            callback.onComplete("jwt-token", null);
        }));

        GamifyWidgetSDK.getInstance().openWidget(activity);
        WebView webView = webView();
        fireReady(webView);

        assertEquals(WIDGET_URL + "/593/sdk-user", shadowOf(webView).getLastLoadedUrl());
        assertEquals("sdk-user", requestedFor.get(0));
        assertEquals("jwt-token", lastInit(webView).getString("token"));
    }

    @Test
    public void openWidget_postsInitWithoutTokenWhenAuthNotEnabled() throws Exception {
        configure("sdk-user", null);

        GamifyWidgetSDK.getInstance().openWidget(activity);
        WebView webView = webView();
        fireReady(webView);

        JSONObject init = lastInit(webView);
        assertEquals("sdk-user", init.getString("userId"));
        assertFalse(init.has("token"));
    }

    @Test
    public void openWidget_doesNothingWithoutAUser() {
        configure(null, null);

        GamifyWidgetSDK.getInstance().openWidget(activity);

        assertNull(ShadowDialog.getLatestDialog());
    }

    @Test
    public void openWidget_closesWhenTokenCannotBeMinted() {
        configure("sdk-user", authManager((userId, callback) -> callback.onComplete(null, new Exception("no session"))));

        GamifyWidgetSDK.getInstance().openWidget(activity);
        shadowOf(Looper.getMainLooper()).idle();

        Dialog dialog = ShadowDialog.getLatestDialog();
        assertNotNull(dialog);
        assertFalse(dialog.isShowing());
    }

    @Test
    public void openWidget_doesNotPostInitWhenClosedWhileTokenInFlight() {
        AtomicReference<AuthTokenProvider.Callback> pending = new AtomicReference<>();
        configure("sdk-user", authManager((userId, callback) -> pending.set(callback)));

        GamifyWidgetSDK.getInstance().openWidget(activity);
        WebView webView = webView();
        fireReady(webView);
        GamifyWidgetSDK.getInstance().closeWidget();
        pending.get().onComplete("late-token", null);
        shadowOf(Looper.getMainLooper()).idle();

        assertNull(shadowOf(webView).getLastEvaluatedJavascript());
    }

    @Test
    public void openWidget_trimsUserIdSoUrlInitAndTokenSubjectMatch() throws Exception {
        List<String> requestedFor = new ArrayList<>();
        configure(null, authManager((userId, callback) -> {
            requestedFor.add(userId);
            callback.onComplete("jwt-token", null);
        }));

        GamifyWidgetSDK.getInstance().openWidget(activity, new OpenLoyaltyWidgetParams(" customer-1 ", null));
        WebView webView = webView();
        fireReady(webView);

        assertEquals(WIDGET_URL + "/593/customer-1", shadowOf(webView).getLastLoadedUrl());
        assertEquals("customer-1", requestedFor.get(0));
        assertEquals("customer-1", lastInit(webView).getString("userId"));
    }

    @Test
    public void openWidget_doesNothingForWhitespaceUserId() {
        configure(null, null);

        GamifyWidgetSDK.getInstance().openWidget(activity, new OpenLoyaltyWidgetParams("   ", null));

        assertNull(ShadowDialog.getLatestDialog());
    }

    @Test
    public void refreshToken_postsFreshTokenToWidgetOrigin() throws Exception {
        List<String> tokens = new ArrayList<>();
        tokens.add("init-token");
        tokens.add("fresh-token");
        configure("sdk-user", authManager((userId, callback) -> callback.onComplete(tokens.remove(0), null)));

        GamifyWidgetSDK.getInstance().openWidget(activity);
        WebView webView = webView();
        fireReady(webView);
        fireRefreshToken(webView);

        JSONObject refresh = lastPosted(webView);
        assertEquals("TOKEN_REFRESHED", refresh.getString("type"));
        assertEquals("fresh-token", refresh.getString("token"));
    }

    @Test
    public void refreshToken_ignoresRequestsWhileFetchInFlight() {
        List<AuthTokenProvider.Callback> pending = new ArrayList<>();
        configure("sdk-user", authManager((userId, callback) -> pending.add(callback)));

        GamifyWidgetSDK.getInstance().openWidget(activity);
        WebView webView = webView();
        pending.remove(0).onComplete("init-token", null);
        fireReady(webView);

        fireRefreshToken(webView);
        fireRefreshToken(webView);
        assertEquals(1, pending.size());

        pending.remove(0).onComplete("fresh-token", null);
        shadowOf(Looper.getMainLooper()).idle();
        fireRefreshToken(webView);
        assertEquals(1, pending.size());
    }

    @Test
    public void navigation_staysInWebViewOnWidgetOrigin() {
        configure("sdk-user", null);
        GamifyWidgetSDK.getInstance().openWidget(activity);
        WebView webView = webView();

        assertFalse(shadowOf(webView).getWebViewClient()
                .shouldOverrideUrlLoading(webView, mainFrameRequest(WIDGET_URL + "/593/other-page")));
        assertNull(shadowOf(activity).getNextStartedActivity());
    }

    @Test
    public void navigation_offOriginOpensSystemBrowser() {
        configure("sdk-user", null);
        GamifyWidgetSDK.getInstance().openWidget(activity);
        WebView webView = webView();

        assertTrue(shadowOf(webView).getWebViewClient()
                .shouldOverrideUrlLoading(webView, mainFrameRequest("https://evil.example.com/steal")));
        Intent started = shadowOf(activity).getNextStartedActivity();
        assertNotNull(started);
        assertEquals(Intent.ACTION_VIEW, started.getAction());
        assertEquals("https://evil.example.com/steal", started.getDataString());
    }

    @Test
    public void originOf_normalizesDefaultPortAndCase() {
        assertEquals("https://widget.example.com",
                WidgetDialog.originOf(Uri.parse("HTTPS://Widget.Example.com:443/593/u")));
        assertEquals("https://widget.example.com:8443",
                WidgetDialog.originOf(Uri.parse("https://widget.example.com:8443/593/u")));
        assertNull(WidgetDialog.originOf(Uri.parse("about:blank")));
    }

    private void configure(@Nullable String sdkUserId, @Nullable AuthManager authManager) {
        GamifyWidgetSDK.resetForTesting(new GamifyWidgetSDK.Environment() {
            @Nullable
            @Override
            public String currentUserId() {
                return sdkUserId;
            }

            @Nullable
            @Override
            public AuthManager authManager() {
                return authManager;
            }
        });
        GamifyWidgetSDK.applyTenantConfiguration(null, WIDGET_URL + "/", TENANT_ID);
    }

    private static AuthManager authManager(AuthTokenProvider provider) {
        return new AuthManager(provider, 0);
    }

    private static WebView webView() {
        Dialog dialog = ShadowDialog.getLatestDialog();
        assertNotNull(dialog);
        return dialog.findViewById(R.id.gamify_widget_webview);
    }

    private static void fireReady(WebView webView) {
        AndroidBridge bridge = (AndroidBridge) shadowOf(webView).getJavascriptInterface("AndroidBridge");
        bridge.receiveMessage("{\"type\":\"READY\"}");
        shadowOf(Looper.getMainLooper()).idle();
    }

    private static void fireRefreshToken(WebView webView) {
        AndroidBridge bridge = (AndroidBridge) shadowOf(webView).getJavascriptInterface("AndroidBridge");
        bridge.receiveMessage("{\"type\":\"REFRESH_TOKEN\"}");
        shadowOf(Looper.getMainLooper()).idle();
    }

    private static WebResourceRequest mainFrameRequest(String url) {
        WebResourceRequest request = mock(WebResourceRequest.class);
        when(request.getUrl()).thenReturn(Uri.parse(url));
        when(request.isForMainFrame()).thenReturn(true);
        return request;
    }

    private static JSONObject lastInit(WebView webView) throws Exception {
        JSONObject init = lastPosted(webView);
        assertEquals("INIT", init.getString("type"));
        return init;
    }

    private static JSONObject lastPosted(WebView webView) throws Exception {
        ShadowWebView shadow = shadowOf(webView);
        String js = shadow.getLastEvaluatedJavascript();
        assertNotNull("nothing was posted", js);
        String prefix = "window.postMessage(";
        String suffix = ", " + JSONObject.quote(WIDGET_URL) + ");";
        assertTrue(js.startsWith(prefix) && js.endsWith(suffix));
        return new JSONObject(js.substring(prefix.length(), js.length() - suffix.length()));
    }
}
