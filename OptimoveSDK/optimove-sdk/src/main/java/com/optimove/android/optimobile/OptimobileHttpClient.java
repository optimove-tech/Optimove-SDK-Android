package com.optimove.android.optimobile;

import java.io.IOException;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

class OptimobileHttpClient {
    private final OkHttpClient okHttpClient;

    OptimobileHttpClient(){
        okHttpClient = new OkHttpClient();
    }

    Response postSync(String url, String data) throws IOException, Optimobile.PartialInitialisationException {
        RequestBody body = RequestBody.create(data, MediaType.parse("application/json; charset=utf-8"));

        Request.Builder builder = new Request.Builder().post(body);

        return this.doSyncRequest(builder, url);
    }

    Response getSync(){
        //TODO:
        return null;
    }

    private Response doSyncRequest(Request.Builder builder, String url) throws IOException, Optimobile.PartialInitialisationException{
        if (!Optimobile.hasFinishedHttpInitialisation()){
            throw new Optimobile.PartialInitialisationException();
        }

        Request request = builder.url(url)
                .addHeader(Optimobile.KEY_AUTH_HEADER, Optimobile.authHeader)
                .addHeader("Accept", "application/json")
                .addHeader("Content-Type", "application/json")
                .build();

        return this.okHttpClient.newCall(request).execute();
    }
}
