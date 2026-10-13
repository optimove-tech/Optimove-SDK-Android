package com.optimove.android.gamifywidgetsdk;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.robolectric.Shadows.shadowOf;

import android.app.Activity;
import android.app.Dialog;
import android.os.Looper;
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

    private static JSONObject lastInit(WebView webView) throws Exception {
        ShadowWebView shadow = shadowOf(webView);
        String js = shadow.getLastEvaluatedJavascript();
        assertNotNull("INIT was not posted", js);
        String prefix = "window.postMessage(";
        String suffix = ", '*');";
        assertTrue(js.startsWith(prefix) && js.endsWith(suffix));
        JSONObject init = new JSONObject(js.substring(prefix.length(), js.length() - suffix.length()));
        assertEquals("INIT", init.getString("type"));
        return init;
    }
}
