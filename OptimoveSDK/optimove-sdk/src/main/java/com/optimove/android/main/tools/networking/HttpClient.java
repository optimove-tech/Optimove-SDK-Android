package com.optimove.android.main.tools.networking;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.gson.Gson;

import org.json.JSONArray;
import org.json.JSONException;
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
    private OkHttpClient okHttpClient;

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

    public HttpClient(OkHttpClient okHttpClient) {
        this.okHttpClient = okHttpClient;
    }

    public RequestBuilder<JSONObject> postJson(String baseUrl, JSONObject data) {
        return new JsonRequestBuilder(baseUrl, data);
    }
    public RequestBuilder<JSONObject> postJsonArray(String baseUrl, JSONArray data) {
        return new JsonArrayRequestBuilder(baseUrl, data);
    }

    public <T> RequestBuilder<T> getObject(String baseUrl, Class<T> objectType) {
        return new CustomRequestBuilder<>(baseUrl, objectType);
    }

    public abstract class RequestBuilder<T> {

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

    public class JsonRequestBuilder extends RequestBuilder<JSONObject> {

        protected JSONObject data;

        protected JsonRequestBuilder(String baseUrl, JSONObject data) {
            super(baseUrl);
            this.data = data;
        }

        @Override
        public void send() {
            if (url == null) {
                url = baseUrl;
            }

            RequestBody body = RequestBody.create(MediaType.parse("application/json; charset=utf-8"),
                    data.toString());

            Request request = new Request.Builder().url(url).post(body).build();

            okHttpClient.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(@NonNull Call call, @NonNull IOException e) {
                    errorListener.sendError(new Exception());
                }

                @Override
                public void onResponse(@NonNull Call call, @NonNull Response response) {
                    if (!response.isSuccessful()) {
                        errorListener.sendError(new Exception());
                        return;
                    }
                    try {
                        successListener.sendResponse(new JSONObject(response.body()
                                .toString()));
                    } catch (JSONException jsonException) {
                        errorListener.sendError(new Exception());
                    }
                }
            });
        }
    }

    public class JsonArrayRequestBuilder extends RequestBuilder<JSONObject> {

        protected JSONArray data;

        protected JsonArrayRequestBuilder(String baseUrl, JSONArray data) {
            super(baseUrl);
            this.data = data;
        }

        @Override
        public void send() {
            if (url == null) {
                url = baseUrl;
            }

            RequestBody body = RequestBody.create(MediaType.parse("application/json; charset=utf-8"),
                    data.toString());

            Request request = new Request.Builder().url(url).post(body).build();

            okHttpClient.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(@NonNull Call call, @NonNull IOException e) {
                    errorListener.sendError(new Exception());
                }

                @Override
                public void onResponse(@NonNull Call call, @NonNull Response response) {
                    if (!response.isSuccessful()) {
                        errorListener.sendError(new Exception());
                        return;
                    }
                    try {
                        successListener.sendResponse(new JSONObject(response.body()
                                .toString()));
                    } catch (JSONException jsonException) {
                        errorListener.sendError(new Exception());
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
                   //trigger failure
                }

                @Override
                public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                    // trigger success listener
                    if (!response.isSuccessful() || response.body() == null) {
                        //fail
                        return;
                    }
                    //success with
                    successListener.sendResponse(new Gson().fromJson(response.body().string(), typeToParse));
                }
            });
        }

    }

    public interface SuccessListener<T> {
        void sendResponse(T response);
    }

    public interface ErrorListener {
        void sendError(Exception e);
    }
}
