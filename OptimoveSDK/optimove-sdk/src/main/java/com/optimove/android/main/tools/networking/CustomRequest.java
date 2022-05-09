package com.optimove.android.main.tools.networking;

import com.android.volley.NetworkResponse;
import com.android.volley.ParseError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.toolbox.HttpHeaderParser;
import com.google.gson.Gson;
import com.optimove.android.main.tools.opti_logger.OptiLoggerStreamsContainer;

import java.io.UnsupportedEncodingException;

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
    @Override
    protected Response<T> parseNetworkResponse(NetworkResponse response) {
        try {
            String jsonString = new String(response.data, HttpHeaderParser.parseCharset(response.headers, "utf-8"));
            return Response.success(gson.fromJson(jsonString, responseClass), HttpHeaderParser.parseCacheHeaders(response));
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            return Response.error(new ParseError(e));
        } catch (Throwable e) {
            OptiLoggerStreamsContainer.error("Failed to parse network response to %s", responseClass.getName());
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