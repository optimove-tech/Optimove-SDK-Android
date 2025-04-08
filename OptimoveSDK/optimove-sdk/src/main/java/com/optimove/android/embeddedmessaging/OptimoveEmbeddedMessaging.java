package com.optimove.android.embeddedmessaging;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.optimove.android.Optimove;
import com.optimove.android.main.common.UserInfo;
import com.optimove.android.main.tools.networking.HttpClient;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ExecutorService;

import okhttp3.Response;
import okhttp3.ResponseBody;

public class OptimoveEmbeddedMessaging {
    private static String TAG = "Optimove EM";
    private static OptimoveEmbeddedMessaging shared;
    static ExecutorService executorService;
    static final Handler handler = new Handler(Looper.getMainLooper());

    public static OptimoveEmbeddedMessaging getInstance() {
       if (shared == null) {
           throw new IllegalStateException("OptimoveEmbeddedMessagesModule is not initialized");
       }
       return shared;
    }

    public static void initialize() {
        shared = new OptimoveEmbeddedMessaging();
    }

    public enum ResultType {
        SUCCESS,
        ERROR_USER_NOT_SET,
        ERROR_CONFIG_NOT_SET,
        ERROR
    }
    public interface EmbeddedMessagesGetHandler {
        void run(ResultType result, @Nullable Container[] containers);
    }
    public void getEmbeddedMessagesAsync(@NonNull EmbeddedMessagesGetHandler embeddedMessagesGetHandler, ContainerMessageRequest[] requestBody) {
        EmbeddedMessagingConfig config = Optimove.getConfig().getEmbeddedMessagingConfig();
        if (config == null) {
            Log.e(TAG, "Embedded messaging config is not set");
            handler.post(() -> embeddedMessagesGetHandler.run(ResultType.ERROR_CONFIG_NOT_SET, null));
            return;
        }

        UserInfo userInfo = Optimove.getInstance().getUserInfo();
        String userId = userInfo.getUserId();

        if (userId == null || Objects.equals(userId, userInfo.getVisitorId())) {
            Log.w(TAG, "Customer ID is not set");
            handler.post(() -> embeddedMessagesGetHandler.run(ResultType.ERROR_USER_NOT_SET, null));
            return;
        }
        Runnable task = new GetEmbeddedMessagesRunnable(config, userId, embeddedMessagesGetHandler, requestBody);
        executorService.submit(task);
    }


   static class GetEmbeddedMessagesRunnable implements Runnable {
        private final EmbeddedMessagesGetHandler callback;
        private final EmbeddedMessagingConfig config;
        private final String customerId;

        private final ContainerMessageRequest[] requestBody;
        GetEmbeddedMessagesRunnable(
                EmbeddedMessagingConfig config, String customerId, EmbeddedMessagesGetHandler callback, ContainerMessageRequest[] requestBody) {
            this.config = config;
            this.customerId = customerId;
            this.callback = callback;
            this.requestBody = requestBody;
        }

        @Override
       public void run() {
            Container[] containers = null;
            ResultType resultType  = ResultType.ERROR;

            HttpClient httpClient = HttpClient.getInstance();
            String region = config.getRegion();

            try {
                String encodedCustomerId = URLEncoder.encode(this.customerId, "UTF-8");
                String url = String.format(
                        "https://optimobile-inbox-srv-%s.optimove.net/embeddedMessages/getEmbeddedMessages/?customerId=%s&tenantId=%s&brandId%s",
                        region, encodedCustomerId, config.getTenantId(), config.getBrandId());
                JSONArray postBody = new JSONArray();
                for(ContainerMessageRequest cm : requestBody) {
                    postBody.put(cm.toJSONObject());
                }
                try (Response response = httpClient.postSync(url, postBody, config.getTenantId())) {
                    if (!response.isSuccessful()) {
                        logFailedResponse(response);
                    } else {
                        containers = mapResponseToContainers(response);
                        resultType = ResultType.SUCCESS;
                    }
                }
            } catch(Exception e) {
                Log.e(TAG, e.getMessage());
                e.printStackTrace();
            }

            this.fireCallback(resultType, containers);
        }
       private static Container[] mapResponseToContainers(Response response) {
           try {
               List<Container> containers = new ArrayList<>();
               JSONObject data = new JSONObject(response.body().string());

               Iterator<String> containerKeys = data.keys();
               while(containerKeys.hasNext()) {
                   String containerId = containerKeys.next();
                   JSONArray containerMessages = data.getJSONArray(containerId);
                   EmbeddedMessage[] embeddedMessages = new EmbeddedMessage[containerMessages.length()];
                   for (int i = 0; i < containerMessages.length(); i++) {
                       JSONObject message = containerMessages.getJSONObject(i);
                       embeddedMessages[i] =  new EmbeddedMessage(message);
                   }
                   containers.add(new Container(containerId, "", embeddedMessages));
               }
               return (Container[])containers.toArray();
           } catch (NullPointerException | JSONException | IOException | URISyntaxException e) {
               Log.e(TAG, e.getMessage());
               return null;
           }
       }

        private void fireCallback(ResultType result, @Nullable Container[] containers) {
            handler.post(() -> GetEmbeddedMessagesRunnable.this.callback.run(result, containers));
        }
   }

    private static void logFailedResponse(Response response) {
        int code = response.code();
        String msg = "Request failed with code " + code + ". ";
        String responseBodyAsStr = "";
        try {
            ResponseBody body = response.body();
            if (body != null) {
                responseBodyAsStr = body.string();
            }
        } catch (IOException e) {/**/}


        switch (code) {
            case 400: {
                Log.e(TAG, msg + "Check embedded messaging configuration : " + responseBodyAsStr);
                break;
            }
            default:
                Log.e(TAG, msg + responseBodyAsStr);
                break;
        }
    }
}
