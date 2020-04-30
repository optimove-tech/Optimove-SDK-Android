package com.optimove.sdk.optimove_sdk.main.tools.networking;

import android.content.Context;
import android.support.annotation.Nullable;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.toolbox.HttpHeaderParser;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONObject;

import java.nio.charset.StandardCharsets;

public class HttpClient {

    private static final Object lock = new Object();
    private static HttpClient instance;

    public static HttpClient getInstance(Context context) {
        if (instance != null) {
            return instance;
        }
        synchronized (lock) {
            if (instance == null) {
                instance = new HttpClient(context);
            }
        }
        return instance;
    }

    private RequestQueue mainRequestQueue;

    private HttpClient(Context context) {
        mainRequestQueue = Volley.newRequestQueue(context.getApplicationContext());
    }


    public RequestBuilder<JSONObject> postJson(String baseUrl, JSONObject data) {
        return new JsonRequestBuilder(baseUrl, data, Request.Method.POST);
    }

    public RequestBuilder<JSONObject> postJsonWithoutJsonResponse(String baseUrl, JSONObject data) {
        return new JsonRequestBuilderWOJsonResponse(baseUrl, data, Request.Method.POST);
    }
    public RequestBuilder<JSONObject> putJsonWithoutJsonResponse(String baseUrl, JSONObject data) {
        return new JsonRequestBuilderWOJsonResponse(baseUrl, data, Request.Method.PUT);
    }


    public <T> RequestBuilder<T> getObject(String baseUrl, Class<T> objectType) {
        return new CustomRequestBuilder<>(baseUrl, objectType, Request.Method.GET);
    }

    public abstract class RequestBuilder<T> {

        protected String baseUrl;
        protected int method;
        protected String url;
        @Nullable
        protected Response.Listener<T> successListener;
        @Nullable
        protected Response.ErrorListener errorListener;

        protected RequestBuilder(String baseUrl, int method) {
            this.baseUrl = baseUrl;
            this.method = method;
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

        public RequestBuilder<T> successListener(Response.Listener<T> successListener) {
            this.successListener = successListener;
            return this;
        }

        public RequestBuilder<T> errorListener(Response.ErrorListener errorListener) {
            this.errorListener = errorListener;
            return this;
        }

        public abstract void send();
    }

    public class JsonRequestBuilder extends RequestBuilder<JSONObject> {

        protected JSONObject data;

        protected JsonRequestBuilder(String baseUrl, JSONObject data, int method) {
            super(baseUrl, method);
            this.data = data;
        }

        @Override
        public void send() {
            if (url == null) {
                url = baseUrl;
            }
            JsonObjectRequest request = new JsonObjectRequest(method, url, data, successListener, errorListener);
            request.setRetryPolicy(new DefaultRetryPolicy(60000, 2, 2));
            mainRequestQueue.add(request);
        }
    }
    public class JsonRequestBuilderWOJsonResponse extends RequestBuilder<JSONObject> {

        protected JSONObject data;

        protected JsonRequestBuilderWOJsonResponse(String baseUrl, JSONObject data, int method) {
            super(baseUrl, method);
            this.data = data;
        }
        @Override
        public void send() {
            if (url == null) {
                url = baseUrl;
            }
            JsonObjectRequest request = new JsonObjectRequest(method, url, data, successListener, errorListener) {
                @Override
                protected Response<JSONObject> parseNetworkResponse(NetworkResponse response) {

                    String json = new String(response.data, StandardCharsets.UTF_8);

                    if (json.length() == 0) {
                        return Response.success(null, HttpHeaderParser.parseCacheHeaders(response));
                    } else {
                        return super.parseNetworkResponse(response);
                    }
                }
            };
            request.setRetryPolicy(new DefaultRetryPolicy(60000, 2, 2));
            mainRequestQueue.add(request);
        }
    }

    public class CustomRequestBuilder<T> extends RequestBuilder<T> {

        Class<T> typeToParse;

        protected CustomRequestBuilder(String baseUrl, Class<T> typeToParse, int method) {
            super(baseUrl, method);
            this.typeToParse = typeToParse;
        }

        @Override
        public void send() {
            if (url == null) {
                url = baseUrl;
            }

            CustomRequest<T> customRequest = new CustomRequest<>(method, url, typeToParse, successListener,
                    errorListener);
            customRequest.setRetryPolicy(new DefaultRetryPolicy(60000, 2, 2));
            mainRequestQueue.add(customRequest);
        }

    }
}
