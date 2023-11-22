package com.optimove.android.optimobile;

import android.os.Build;

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
    private final OkHttpClient okHttpClient;

    OptimobileHttpClient() {
        okHttpClient = this.buildOkHttpClient();
    }

    private OkHttpClient buildOkHttpClient() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            return new OkHttpClient();
        }

        //ciphers available on Android 4.4 have intersections with the approved ones in MODERN_TLS, but the intersections are on bad cipher list, so,
        //perhaps not supported by CloudFlare. On older devices allow all ciphers
        ConnectionSpec spec = new ConnectionSpec.Builder(ConnectionSpec.MODERN_TLS)
                .allEnabledCipherSuites()
                .build();

        return new OkHttpClient.Builder()
                .connectionSpecs(Collections.singletonList(spec))
                .build();
    }

    Response postSync(String url, String data) throws IOException, Optimobile.PartialInitialisationException {
        RequestBody body = RequestBody.create(data, MediaType.parse("application/json; charset=utf-8"));

        Request.Builder builder = new Request.Builder().post(body);
        Request request = this.buildRequest(builder, url);

        return this.doSyncRequest(request);
    }

    Response getSync(String url) throws IOException, Optimobile.PartialInitialisationException {
        Request.Builder builder = new Request.Builder().get();

        Request request = this.buildRequest(builder, url);

        return this.doSyncRequest(request);
    }

    void getAsync(String url, Callback callback) throws Optimobile.PartialInitialisationException {
        Request.Builder builder = new Request.Builder().get();
        Request request = this.buildRequest(builder, url);

        this.doAsyncRequest(request, callback);
    }

    private Request buildRequest(Request.Builder builder, String url) throws Optimobile.PartialInitialisationException {
        if (!Optimobile.hasFinishedHttpInitialisation()) {
            throw new Optimobile.PartialInitialisationException();
        }

        return builder.url(url)
                .addHeader(Optimobile.KEY_AUTH_HEADER, Optimobile.authHeader)
                .addHeader("Accept", "application/json")
                .addHeader("Content-Type", "application/json")
                .build();
    }

    private Response doSyncRequest(Request request) throws IOException {
        return this.okHttpClient.newCall(request).execute();
    }

    private void doAsyncRequest(Request request, Callback callback) {
        this.okHttpClient.newCall(request).enqueue(callback);
    }
}
