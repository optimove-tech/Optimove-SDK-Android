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

    public static HttpClient getInstance() {
        if (instance != null) {
            return instance;
        }
        synchronized (lock) {
            if (instance == null) {
                instance = new HttpClient();
            }
        }
        return instance;
    }

    public RequestBuilder<JSONObject> postJson(String baseUrl, JSONObject data) {
        return new JsonRequestBuilder(baseUrl, data, 3);
    }
    public RequestBuilder<JSONObject> postJsonArray(String baseUrl, JSONArray data) {
        return new JsonArrayRequestBuilder(baseUrl, data, 5);
    }
//
//    public RequestBuilder<JSONObject> postJsonWithoutJsonResponse(String baseUrl, JSONObject data) {
//        return new JsonRequestBuilderWOJsonResponse(baseUrl, data, Request.Method.POST);
//    }
//    public <T> RequestBuilder<T> postObject(String baseUrl, Class<T> objectType) {
//        return new CustomRequestBuilder<>(baseUrl, objectType, Request.Method.POST);
//    }

    public <T> RequestBuilder<T> getObject(String baseUrl, Class<T> objectType) {
        return new CustomRequestBuilder<>(baseUrl, objectType, 3);
    }

    public abstract class RequestBuilder<T> {

        protected String baseUrl;
        protected int method;
        protected String url;
        @Nullable
        protected SuccessListener<T> successListener;
        @Nullable
        protected ErrorListener errorListener;

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

        protected JsonRequestBuilder(String baseUrl, JSONObject data, int method) {
            super(baseUrl, method);
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

            new OkHttpClient().newCall(request).enqueue(new Callback() {
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

        protected JsonArrayRequestBuilder(String baseUrl, JSONArray data, int method) {
            super(baseUrl, method);
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

            new OkHttpClient().newCall(request).enqueue(new Callback() {
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
//    public class JsonRequestBuilderWOJsonResponse extends RequestBuilder<JSONObject> {
//
//        protected JSONObject data;
//
//        protected JsonRequestBuilderWOJsonResponse(String baseUrl, JSONObject data, int method) {
//            super(baseUrl, method);
//            this.data = data;
//        }
//        @Override
//        public void send() {
//            if (url == null) {
//                url = baseUrl;
//            }
//            JsonObjectRequest request = new JsonObjectRequest(method, url, data, successListener, errorListener) {
//                @Override
//                protected Response<JSONObject> parseNetworkResponse(NetworkResponse response) {
//
//                    String json = new String(response.data, StandardCharsets.UTF_8);
//
//                    if (json.length() == 0) {
//                        return Response.success(null, HttpHeaderParser.parseCacheHeaders(response));
//                    } else {
//                        return super.parseNetworkResponse(response);
//                    }
//                }
//            };
//            request.setRetryPolicy(new DefaultRetryPolicy(60000, 2, 2));
//            mainRequestQueue.add(request);
//        }
//    }
//
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

            Request request = new Request.Builder().url(url).get().build();

            new OkHttpClient().newCall(request).enqueue(new Callback() {
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

//    public class CustomJsonArrayRequest extends JsonRequest<JSONObject> {
//
//        /**
//         * Creates a new request.
//         * @param method the HTTP method to use
//         * @param url URL to fetch the JSON from
//         * @param jsonRequest A {@link JSONObject} to post with the request. Null is allowed and
//         *   indicates no parameters will be posted along with request.
//         * @param listener Listener to receive the JSON response
//         * @param errorListener Error listener, or null to ignore errors.
//         */
//        public CustomJsonArrayRequest(int method, String url, JSONArray jsonRequest,
//                                      Response.Listener<JSONObject> listener, Response.ErrorListener errorListener) {
//            super(method, url, (jsonRequest == null) ? null : jsonRequest.toString(), listener,
//                    errorListener);
//        }
//
//        @Override
//        protected Response<JSONObject> parseNetworkResponse(NetworkResponse response) {
//            try {
//                String jsonString = new String(response.data,
//                        HttpHeaderParser.parseCharset(response.headers, PROTOCOL_CHARSET));
//                return Response.success(new JSONObject(jsonString),
//                        HttpHeaderParser.parseCacheHeaders(response));
//            } catch (UnsupportedEncodingException e) {
//                return Response.error(new ParseError(e));
//            } catch (JSONException je) {
//                return Response.error(new ParseError(je));
//            }
//        }
//    }
}
