package com.optimove.android.optimobile;

import android.os.Build;
import android.util.Base64;

import androidx.annotation.Nullable;

import com.optimove.android.Optimove;
import com.optimove.android.OptimoveConfig;
import com.optimove.android.auth.AuthManager;

import org.json.JSONArray;

import java.io.IOException;
import java.util.Collections;

import okhttp3.Callback;
import okhttp3.ConnectionSpec;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

class OptimobileHttpClient {

    private static final Object lock = new Object();
    private static OptimobileHttpClient instance;

    private final OkHttpClient okHttpClient;
    private @Nullable String authHeader;
    private @Nullable AuthManager authManager;

    static OptimobileHttpClient getInstance() {
        if (instance != null) {
            return instance;
        }
        synchronized (lock) {
            if (instance == null) {
                instance = new OptimobileHttpClient();
            }
        }
        return instance;
    }

    private OptimobileHttpClient() {
        okHttpClient = this.buildOkHttpClient();
    }

    void setAuthManager(@Nullable AuthManager authManager) {
        this.authManager = authManager;
    }

    private OkHttpClient buildOkHttpClient() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            return new OkHttpClient();
        }

        ConnectionSpec spec = new ConnectionSpec.Builder(ConnectionSpec.MODERN_TLS)
                .allEnabledCipherSuites()
                .build();

        return new OkHttpClient.Builder()
                .connectionSpecs(Collections.singletonList(spec))
                .build();
    }

    Response postSync(String url, JSONArray data) throws IOException, Optimobile.PartialInitialisationException {
        return postSync(url, data, (String) null);
    }

    Response postSync(String url, JSONArray data, @Nullable String authUserId) throws IOException, Optimobile.PartialInitialisationException {
        String dataStr = data.toString();
        RequestBody body = RequestBody.create(dataStr, MediaType.parse("application/json; charset=utf-8"));
        Request.Builder builder = new Request.Builder().post(body);
        return this.buildAndSend(builder, url, authUserId);
    }

    Response getSync(String url) throws IOException, Optimobile.PartialInitialisationException {
        return getSync(url, (String) null);
    }

    Response getSync(String url, @Nullable String authUserId) throws IOException, Optimobile.PartialInitialisationException {
        Request.Builder builder = new Request.Builder().get();
        return this.buildAndSend(builder, url, authUserId);
    }

    void getAsync(String url, Callback callback) throws Optimobile.PartialInitialisationException {
        Request.Builder builder = new Request.Builder().get();
        Request request = this.buildRequest(builder, url);
        this.doAsyncRequest(request, callback);
    }

    private Response buildAndSend(Request.Builder builder, String url,
                                  @Nullable String authUserId)
            throws IOException, Optimobile.PartialInitialisationException {
        Request request = this.buildRequest(builder, url);

        if (authManager != null && authUserId != null && !authUserId.isEmpty()) {
            String jwt = AuthManager.getTokenBlocking(authManager, authUserId);
            if (jwt == null) {
                throw new IOException("Auth token fetch failed for userId: " + authUserId);
            }
            request = request.newBuilder()
                    .addHeader("X-User-JWT", jwt)
                    .build();
        }

        return this.doSyncRequest(request);
    }

    private Request buildRequest(Request.Builder builder, String url) throws Optimobile.PartialInitialisationException {
        if (this.authHeader == null) {
            OptimoveConfig config = Optimove.getConfig();

            String apiKey = config.getApiKey();
            String secretKey = config.getSecretKey();
            if (apiKey == null && secretKey == null) {
                throw new Optimobile.PartialInitialisationException();
            }

            this.authHeader = buildBasicAuthHeader(apiKey, secretKey);
        }

        return builder.url(url)
                .addHeader(Optimobile.KEY_AUTH_HEADER, this.authHeader)
                .addHeader("Accept", "application/json")
                .addHeader("Content-Type", "application/json")
                .addHeader("X-Optimove-Auth-Capable", "1")
                .build();
    }

    private Response doSyncRequest(Request request) throws IOException {
        return this.okHttpClient.newCall(request).execute();
    }

    private void doAsyncRequest(Request request, Callback callback) {
        this.okHttpClient.newCall(request).enqueue(callback);
    }

    private static String buildBasicAuthHeader(String apiKey, String secretKey) {
        return "Basic "
                + Base64.encodeToString((apiKey + ":" + secretKey).getBytes(), Base64.NO_WRAP);
    }
}
