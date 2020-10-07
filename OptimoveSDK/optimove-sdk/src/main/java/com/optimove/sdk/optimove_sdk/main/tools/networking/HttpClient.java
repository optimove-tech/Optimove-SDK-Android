package com.optimove.sdk.optimove_sdk.main.tools.networking;

import android.content.Context;
import androidx.annotation.Nullable;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.NetworkResponse;
import com.android.volley.ParseError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.toolbox.HttpHeaderParser;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.JsonRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
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
    public RequestBuilder<JSONObject> postJsonArray(String baseUrl, JSONArray data) {
        return new JsonArrayRequestBuilder(baseUrl, data, Request.Method.POST);
    }

    public RequestBuilder<JSONObject> postJsonWithoutJsonResponse(String baseUrl, JSONObject data) {
        return new JsonRequestBuilderWOJsonResponse(baseUrl, data, Request.Method.POST);
    }
    public <T> RequestBuilder<T> postObject(String baseUrl, Class<T> objectType) {
        return new CustomRequestBuilder<>(baseUrl, objectType, Request.Method.POST);
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
    public class JsonArrayRequestBuilder extends RequestBuilder<JSONObject> {

        protected JSONArray data;

        protected JsonArrayRequestBuilder(String baseUrl, JSONArray data, int method) {
            super(baseUrl, method);
            this.data = data;
        }

        @Override
        public void send() {
            if (url == null) {
                url = baseUrl;
            }
            CustomJsonArrayRequest request = new CustomJsonArrayRequest(method, url, data, successListener, errorListener);
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
    public class CustomJsonArrayRequest extends JsonRequest<JSONObject> {

        /**
         * Creates a new request.
         * @param method the HTTP method to use
         * @param url URL to fetch the JSON from
         * @param jsonRequest A {@link JSONObject} to post with the request. Null is allowed and
         *   indicates no parameters will be posted along with request.
         * @param listener Listener to receive the JSON response
         * @param errorListener Error listener, or null to ignore errors.
         */
        public CustomJsonArrayRequest(int method, String url, JSONArray jsonRequest,
                                      Response.Listener<JSONObject> listener, Response.ErrorListener errorListener) {
            super(method, url, (jsonRequest == null) ? null : jsonRequest.toString(), listener,
                    errorListener);
        }

        @Override
        protected Response<JSONObject> parseNetworkResponse(NetworkResponse response) {
            try {
                String jsonString = new String(response.data,
                        HttpHeaderParser.parseCharset(response.headers, PROTOCOL_CHARSET));
                return Response.success(new JSONObject(jsonString),
                        HttpHeaderParser.parseCacheHeaders(response));
            } catch (UnsupportedEncodingException e) {
                return Response.error(new ParseError(e));
            } catch (JSONException je) {
                return Response.error(new ParseError(je));
            }
        }
    }
}
