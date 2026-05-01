package com.optimove.android.main.tools.networking;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.gson.Gson;
import com.optimove.android.OptimoveAuthHeaders;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;

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
        @Nullable
        protected String userJwt;

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

        public RequestBuilder<T> userJwt(@Nullable String jwt) {
            this.userJwt = jwt;
            return this;
        }

        public abstract void send();
    }

    public class JsonRequestBuilder extends RequestBuilder<String> {

        protected String json;

        protected JsonRequestBuilder(String baseUrl, String json) {
            super(baseUrl);
            this.json = json;
        }

        @Override
        public void send() {
            if (url == null) {
                url = baseUrl;
            }

            RequestBody body = RequestBody.create(MediaType.parse("application/json; charset=utf-8"),
                    json);

            Request.Builder rb = new Request.Builder().url(url).post(body)
                    .addHeader(OptimoveAuthHeaders.AUTH_CAPABLE, OptimoveAuthHeaders.AUTH_CAPABLE_VALUE);
            if (userJwt != null && !userJwt.isEmpty()) {
                rb.addHeader(OptimoveAuthHeaders.USER_JWT, userJwt);
            }
            Request request = rb.build();

            okHttpClient.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(@NonNull Call call, @NonNull IOException e) {
                    if (errorListener != null) {
                        errorListener.sendError(e);
                    }
                }

                @Override
                public void onResponse(@NonNull Call call, @NonNull Response response) {
                    if (!response.isSuccessful()) {
                        if (errorListener != null) {
                            errorListener.sendError(new HttpStatusException(response.code(), response.message()));
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

    public static final class HttpStatusException extends Exception {
        private final int code;

        public HttpStatusException(int code, String message) {
            super("Response wasn't successful - " + code + " " + message);
            this.code = code;
        }

        public int getCode() {
            return code;
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

            Request.Builder rb = new Request.Builder().url(url).get()
                    .addHeader(OptimoveAuthHeaders.AUTH_CAPABLE, OptimoveAuthHeaders.AUTH_CAPABLE_VALUE);
            if (userJwt != null && !userJwt.isEmpty()) {
                rb.addHeader(OptimoveAuthHeaders.USER_JWT, userJwt);
            }
            Request request = rb.build();

            okHttpClient.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(@NonNull Call call, @NonNull IOException e) {
                    if (errorListener != null) {
                        errorListener.sendError(e);
                    }
                }

                @Override
                public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                    if (!response.isSuccessful() || response.body() == null) {
                        if (errorListener != null) {
                            errorListener.sendError(new HttpStatusException(response.code(), response.message()));
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

    public Response getSync(String url, int tenantId, @Nullable String userJwt) throws IOException {
        Request.Builder builder = new Request.Builder().get();
        Request request = this.buildRequest(builder, url, tenantId, userJwt);
        return this.doSyncRequest(request);
    }

    public Response putSync(String url, JSONArray data, int tenantId) throws IOException {
        return putSync(url, data, tenantId, null);
    }

    public Response putSync(String url, JSONArray data, int tenantId, @Nullable String userJwt) throws IOException {
        String dataStr = data.toString();
        RequestBody body = RequestBody.create(dataStr, MediaType.parse("application/json; charset=utf-8"));
        Request.Builder builder = new Request.Builder().put(body);
        Request request = this.buildRequest(builder, url, tenantId, userJwt);
        return this.doSyncRequest(request);
    }

    public Response putSingleSync(String url, JSONObject data, int tenantId) throws IOException {
        return putSingleSync(url, data, tenantId, null);
    }

    public Response putSingleSync(String url, JSONObject data, int tenantId, @Nullable String userJwt) throws IOException {
        String dataStr = data.toString();
        RequestBody body = RequestBody.create(dataStr, MediaType.parse("application/json; charset=utf-8"));
        Request.Builder builder = new Request.Builder().put(body);
        Request request = this.buildRequest(builder, url, tenantId, userJwt);
        return this.doSyncRequest(request);
    }

    public Response postSync(String url, JSONArray data, int tenantId) throws IOException {
        return postSync(url, data, tenantId, null);
    }

    public Response postSync(String url, JSONArray data, int tenantId, @Nullable String userJwt) throws IOException {
        String dataStr = data.toString();
        RequestBody body = RequestBody.create(dataStr, MediaType.parse("application/json; charset=utf-8"));
        Request.Builder builder = new Request.Builder().post(body);
        Request request = this.buildRequest(builder, url, tenantId, userJwt);
        return this.doSyncRequest(request);
    }

    public Response postSingleSync(String url, JSONObject data, int tenantId) throws IOException {
        return postSingleSync(url, data, tenantId, null);
    }

    public Response postSingleSync(String url, JSONObject data, int tenantId, @Nullable String userJwt) throws IOException {
        String dataStr = data.toString();
        RequestBody body = RequestBody.create(dataStr, MediaType.parse("application/json; charset=utf-8"));
        Request.Builder builder = new Request.Builder().post(body);
        Request request = this.buildRequest(builder, url, tenantId, userJwt);
        return this.doSyncRequest(request);
    }

    public Response deleteSync(String url, int tenantId) throws IOException {
        return deleteSync(url, tenantId, null);
    }

    public Response deleteSync(String url, int tenantId, @Nullable String userJwt) throws IOException {
        Request.Builder builder = new Request.Builder().delete();
        Request request = this.buildRequest(builder, url, tenantId, userJwt);
        return this.doSyncRequest(request);
    }

    private Request buildRequest(Request.Builder builder, String url, int tenantId, @Nullable String userJwt) {
        builder.url(url)
                .addHeader("Accept", "application/json")
                .addHeader("Content-Type", "application/json")
                .addHeader("X-Tenant-Id", String.valueOf(tenantId))
                .addHeader(OptimoveAuthHeaders.AUTH_CAPABLE, OptimoveAuthHeaders.AUTH_CAPABLE_VALUE);
        if (userJwt != null && !userJwt.isEmpty()) {
            builder.addHeader(OptimoveAuthHeaders.USER_JWT, userJwt);
        }
        return builder.build();
    }

    private Response doSyncRequest(Request request) throws IOException {
        return this.okHttpClient.newCall(request).execute();
    }
}
