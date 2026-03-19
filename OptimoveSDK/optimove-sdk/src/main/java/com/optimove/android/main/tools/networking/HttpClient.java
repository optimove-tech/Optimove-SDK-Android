package com.optimove.android.main.tools.networking;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.gson.Gson;
import com.optimove.android.Optimove;
import com.optimove.android.OptimoveConfig;
import com.optimove.android.optimobile.Optimobile;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class HttpClient {

    private static final Object lock = new Object();
    private static HttpClient instance;
    private final OkHttpClient okHttpClient;

    public static HttpClient getInstance() {
        if (instance != null) {
            return instance;
        }
        synchronized (lock) {
            if (instance == null) {
                instance = new HttpClient(new OkHttpClient());
            }
        }
        return instance;
    }

    private HttpClient(OkHttpClient okHttpClient) {
        this.okHttpClient = okHttpClient;
    }

    public RequestBuilder<String> postJson(String baseUrl, String json) {
        return new JsonRequestBuilder(baseUrl, json);
    }

    public <T> RequestBuilder<T> getObject(String baseUrl, Class<T> objectType) {
        return new CustomRequestBuilder<>(baseUrl, objectType);
    }

    public abstract static class RequestBuilder<T> {

        protected String baseUrl;
        protected String url;
        @Nullable
        protected SuccessListener<T> successListener;
        @Nullable
        protected ErrorListener errorListener;

        protected RequestBuilder(String baseUrl) {
            this.baseUrl = baseUrl;
            this.url = null;
            this.successListener = null;
            this.errorListener = null;
        }

        public RequestBuilder<T> destination(String urlComponentsPattern, Object... urlComponents) {
            if (baseUrl.endsWith("/")) {
                url = baseUrl + String.format(urlComponentsPattern, urlComponents);
            } else {
                url = baseUrl + "/" + String.format(urlComponentsPattern, urlComponents);
            }
            return this;
        }

        public RequestBuilder<T> successListener(SuccessListener<T> successListener) {
            this.successListener = successListener;
            return this;
        }

        public RequestBuilder<T> errorListener(ErrorListener errorListener) {
            this.errorListener = errorListener;
            return this;
        }

        public abstract void send();
    }

    public class JsonRequestBuilder extends RequestBuilder<String> {

        protected String json;
        private final Map<String, String> extraHeaders = new HashMap<>();

        protected JsonRequestBuilder(String baseUrl, String json) {
            super(baseUrl);
            this.json = json;
        }

        public JsonRequestBuilder addHeader(String name, String value) {
            extraHeaders.put(name, value);
            return this;
        }

        @Override
        public void send() {
            if (url == null) {
                url = baseUrl;
            }

            RequestBody body = RequestBody.create(MediaType.parse("application/json; charset=utf-8"),
                    json);

            Request.Builder reqBuilder = new Request.Builder().url(url).post(body)
                    .addHeader("X-Optimove-Auth-Capable", "1");

            for (Map.Entry<String, String> entry : extraHeaders.entrySet()) {
                reqBuilder.addHeader(entry.getKey(), entry.getValue());
            }

            Request request = reqBuilder.build();

            okHttpClient.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(@NonNull Call call, @NonNull IOException e) {
                    if (errorListener !=null) {
                        errorListener.sendError(e);
                    }
                }

                @Override
                public void onResponse(@NonNull Call call, @NonNull Response response) {
                    if (!response.isSuccessful()) {
                        if (errorListener !=null) {
                            errorListener.sendError(new Exception("Response wasn't successful - " + response.message()));
                        }
                        return;
                    }
                    if (successListener == null) {
                        return;
                    }

                    try {
                        successListener.sendResponse(response.body() != null ? response.body()
                                .string() : null);
                    } catch (IOException e) {
                        successListener.sendResponse(null);
                    }
                }
            });
        }
    }

    public class CustomRequestBuilder<T> extends RequestBuilder<T> {

        Class<T> typeToParse;

        protected CustomRequestBuilder(String baseUrl, Class<T> typeToParse) {
            super(baseUrl);
            this.typeToParse = typeToParse;
        }

        @Override
        public void send() {
            if (url == null) {
                url = baseUrl;
            }

            Request request = new Request.Builder().url(url).get().build();

            okHttpClient.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(@NonNull Call call, @NonNull IOException e) {
                    if (errorListener !=null) {
                        errorListener.sendError(e);
                    }
                }

                @Override
                public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                    if (!response.isSuccessful() || response.body() == null) {
                        if (errorListener !=null) {
                            errorListener.sendError(new Exception("Response wasn't successful - " + response.message()));
                        }
                        return;
                    }
                    if (successListener != null) {
                        successListener.sendResponse(new Gson().fromJson(response.body().string(), typeToParse));
                    }
                }
            });
        }

    }

    public interface SuccessListener<T> {
        void sendResponse(@Nullable T response);
    }

    public interface ErrorListener {
        void sendError(Exception e);
    }

    public Response getSync(String url, int tenantId) throws IOException {
        return getSync(url, tenantId, null);
    }

    public Response getSync(String url, int tenantId, @Nullable Map<String, String> extraHeaders) throws IOException {
        Request.Builder builder = new Request.Builder().get();
        Request request = this.buildRequest(builder, url, tenantId, extraHeaders);
        return this.doSyncRequest(request);
    }

    public Response putSync(String url, JSONArray data, int tenantId) throws IOException {
        return putSync(url, data, tenantId, null);
    }

    public Response putSync(String url, JSONArray data, int tenantId, @Nullable Map<String, String> extraHeaders) throws IOException {
        String dataStr = data.toString();
        RequestBody body = RequestBody.create(dataStr, MediaType.parse("application/json; charset=utf-8"));
        Request.Builder builder = new Request.Builder().put(body);
        Request request = this.buildRequest(builder, url, tenantId, extraHeaders);
        return this.doSyncRequest(request);
    }

    public Response putSingleSync(String url, JSONObject data, int tenantId) throws IOException {
        return putSingleSync(url, data, tenantId, null);
    }

    public Response putSingleSync(String url, JSONObject data, int tenantId, @Nullable Map<String, String> extraHeaders) throws IOException {
        String dataStr = data.toString();
        RequestBody body = RequestBody.create(dataStr, MediaType.parse("application/json; charset=utf-8"));
        Request.Builder builder = new Request.Builder().put(body);
        Request request = this.buildRequest(builder, url, tenantId, extraHeaders);
        return this.doSyncRequest(request);
    }

    public Response postSync(String url, JSONArray data, int tenantId) throws IOException {
        return postSync(url, data, tenantId, null);
    }

    public Response postSync(String url, JSONArray data, int tenantId, @Nullable Map<String, String> extraHeaders) throws IOException {
        String dataStr = data.toString();
        RequestBody body = RequestBody.create(dataStr, MediaType.parse("application/json; charset=utf-8"));
        Request.Builder builder = new Request.Builder().post(body);
        Request request = this.buildRequest(builder, url, tenantId, extraHeaders);
        return this.doSyncRequest(request);
    }

    public Response postSingleSync(String url, JSONObject data, int tenantId) throws IOException {
        return postSingleSync(url, data, tenantId, null);
    }

    public Response postSingleSync(String url, JSONObject data, int tenantId, @Nullable Map<String, String> extraHeaders) throws IOException {
        String dataStr = data.toString();
        RequestBody body = RequestBody.create(dataStr, MediaType.parse("application/json; charset=utf-8"));
        Request.Builder builder = new Request.Builder().post(body);
        Request request = this.buildRequest(builder, url, tenantId, extraHeaders);
        return this.doSyncRequest(request);
    }

    public Response deleteSync(String url, int tenantId) throws IOException {
        Request.Builder builder = new Request.Builder().delete();
        Request request = this.buildRequest(builder, url, tenantId, null);
        return this.doSyncRequest(request);
    }

    private Request buildRequest(Request.Builder builder, String url, int tenantId,
                                 @Nullable Map<String, String> extraHeaders) {
        builder.url(url)
                .addHeader("Accept", "application/json")
                .addHeader("Content-Type", "application/json")
                .addHeader("X-Tenant-Id", String.valueOf(tenantId))
                .addHeader("X-Optimove-Auth-Capable", "1");

        if (extraHeaders != null) {
            for (Map.Entry<String, String> entry : extraHeaders.entrySet()) {
                builder.addHeader(entry.getKey(), entry.getValue());
            }
        }

        return builder.build();
    }

    private Response doSyncRequest(Request request) throws IOException {
        return this.okHttpClient.newCall(request).execute();
    }
}
