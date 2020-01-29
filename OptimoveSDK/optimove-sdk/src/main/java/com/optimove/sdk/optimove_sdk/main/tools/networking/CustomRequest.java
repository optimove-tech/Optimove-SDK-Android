package com.optimove.sdk.optimove_sdk.main.tools.networking;

import com.android.volley.NetworkResponse;
import com.android.volley.ParseError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.toolbox.HttpHeaderParser;
import com.google.gson.Gson;

import java.io.UnsupportedEncodingException;

/**
 Create a class which extend the Volley Request<T> class
 **/
class CustomRequest<T> extends Request<T> {

    private Response.Listener<T> listener;

    private Gson gson;

    private Class<T> responseClass;

    public CustomRequest(int method, String url, Class<T> responseClass, Response.Listener<T> listener,
                         Response.ErrorListener errorListener) {
        super(method, url, errorListener);
        gson = new Gson();
        this.listener = listener;
        this.responseClass = responseClass;
    }
    /**
     This method needs to be implemented to parse the raw network response
     and return an appropriate response type.This method will be called
     from a worker thread. The response
     will not be delivered if you return null.
     @param NetworkResponse - Response payload as byte[],headers and status code
     **/
    @Override
    protected Response<T> parseNetworkResponse(NetworkResponse response) {
        try {
            String jsonString = new String(response.data, HttpHeaderParser.parseCharset(response.headers, "utf-8"));
            return Response.success(gson.fromJson(jsonString, responseClass), HttpHeaderParser.parseCacheHeaders(response));
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            return Response.error(new ParseError(e));
        }
    }
    /**
     This is called on the main thread with the object you returned in
     parseNetworkResponse(). You should be invoking your callback interface
     from here
     **/
    @Override
    protected void deliverResponse(T response) {
        listener.onResponse(response);
    }
}