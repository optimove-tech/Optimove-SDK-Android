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
import java.net.URLEncoder;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

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
        executorService = Executors.newSingleThreadExecutor();
    }

    public enum ResultType {
        SUCCESS,
        ERROR_USER_NOT_SET,
        ERROR_CONFIG_NOT_SET,
        ERROR
    }

    public interface EmbeddedMessagesGetHandler {
        void run(ResultType result, @Nullable EmbeddedMessagesResponse response);
    }

    public interface EmbeddedMessagesSetHandler {
        void run(ResultType result);
    }

    public class EmbeddedMessagingResult {
        private ResultType result;
        private EmbeddedMessagesResponse response;

        public EmbeddedMessagingResult(ResultType result, EmbeddedMessagesResponse response) {
            this.result = result;
            this.response = response;
        }

        public ResultType getResult() {
            return this.result;
        }

        public EmbeddedMessagesResponse getResponse() {
            return this.response;
        }
    }

    /**
     * Gets the embedded messages from the server.
     *
     * @param containerRequestOptions    The options for the container request.
     * @param embeddedMessagesGetHandler handler that returns the status of the process and the embedded messages response.
     */
    public void getMessagesAsync(ContainerRequestOptions[] containerRequestOptions, @NonNull EmbeddedMessagesGetHandler embeddedMessagesGetHandler) {
        EmbeddedMessagingConfig config = Optimove.getConfig().getEmbeddedMessagingConfig();
        if (config == null) {
            Log.e(TAG, "Embedded messaging config is not set");
            handler.post(() -> embeddedMessagesGetHandler.run(ResultType.ERROR_CONFIG_NOT_SET, null));
            return;
        }

        UserInfo userInfo = Optimove.getInstance().getUserInfo();
        String userId = userInfo.getUserId() == null ? userInfo.getVisitorId() : userInfo.getUserId();

        if (userId == null) {
            Log.w(TAG, "Customer/Visitor ID is not set");
            handler.post(() -> embeddedMessagesGetHandler.run(ResultType.ERROR_USER_NOT_SET, null));
            return;
        }
        Runnable task = new GetEmbeddedMessagesRunnable(config, userId, embeddedMessagesGetHandler, containerRequestOptions);
        executorService.submit(task);
    }

    /**
     * Deletes the given message from the server.
     *
     * @param message                       the message to delete.
     * @param embeddedMessagesDeleteHandler handler that returns the status of the process.
     */
    public void deleteMessageAsync(EmbeddedMessage message, @NonNull EmbeddedMessagesSetHandler embeddedMessagesDeleteHandler) {
        EmbeddedMessagingConfig config = handleConfigForAsyncSetCall(embeddedMessagesDeleteHandler);
        if (config == null) return;

        UserInfo userInfo = Optimove.getInstance().getUserInfo();
        String userId = userInfo.getUserId() == null ? userInfo.getVisitorId() : userInfo.getUserId();

        if (userId == null) {
            Log.w(TAG, "Customer/Visitor ID is not set");
            handler.post(() -> embeddedMessagesDeleteHandler.run(ResultType.ERROR_USER_NOT_SET));
            return;
        }
        EmbeddedMessageMetricEventContext context = new EmbeddedMessageMetricEventContext(
                message.getId(), message.getContainerId());
        EmbeddedMessageEventRequest request = new EmbeddedMessageEventRequest(
                new Date(), UUID.randomUUID().toString(), EventType.DELETED, context, userId,
                userInfo.getVisitorId());

        Runnable task = new EventReportEmbeddedMessagesRunnable(config, request, embeddedMessagesDeleteHandler);
        executorService.submit(task);
    }

    /**
     * Reports a click metric for the given embedded message
     *
     * @param message                        The message to report the click metric for.
     * @param embeddedMessagesMetricsHandler handler that returns the status of the process.
     */
    public void reportClickMetricAsync(
            EmbeddedMessage message,
            @NonNull EmbeddedMessagesSetHandler embeddedMessagesMetricsHandler) {
        EmbeddedMessagingConfig config = handleConfigForAsyncSetCall(embeddedMessagesMetricsHandler);
        if (config == null) return;

        UserInfo userInfo = Optimove.getInstance().getUserInfo();
        String userId = userInfo.getUserId() == null ? userInfo.getVisitorId() : userInfo.getUserId();

        if (userId == null) {
            Log.w(TAG, "Customer/Visitor ID is not set");
            handler.post(() -> embeddedMessagesMetricsHandler.run(ResultType.ERROR_USER_NOT_SET));
            return;
        }
        EmbeddedMessageMetricEventContext context = new EmbeddedMessageMetricEventContext(
                message.getId(), message.getContainerId());
        EmbeddedMessageEventRequest request = new EmbeddedMessageEventRequest(
                new Date(), UUID.randomUUID().toString(), EventType.CLICKED, context, userId,
                userInfo.getVisitorId());
        Runnable task = new EventReportEmbeddedMessagesRunnable(config, request, embeddedMessagesMetricsHandler);
        executorService.submit(task);
    }

    /**
     * Updates the read status of the given embedded message on the server
     *
     * @param message                       The message to update.
     * @param isRead                        The new read status of the message boolean.
     * @param embeddedMessagesStatusHandler handler that returns the status of the process.
     */
    public void setAsReadASync(EmbeddedMessage message, boolean isRead,
                               @NonNull EmbeddedMessagesSetHandler embeddedMessagesStatusHandler) {
        EmbeddedMessagingConfig config = handleConfigForAsyncSetCall(embeddedMessagesStatusHandler);
        if (config == null) return;

        UserInfo userInfo = Optimove.getInstance().getUserInfo();
        String userId = userInfo.getUserId() == null ? userInfo.getVisitorId() : userInfo.getUserId();

        if (userId == null) {
            Log.w(TAG, "Customer/Visitor ID is not set");
            handler.post(() -> embeddedMessagesStatusHandler.run(ResultType.ERROR_USER_NOT_SET));
            return;
        }
        EmbeddedMessageMetricEventContext context = new EmbeddedMessageMetricEventContext(
                message.getId(), message.getContainerId());
        EmbeddedMessageEventRequest request = new EmbeddedMessageEventRequest(
                new Date(), UUID.randomUUID().toString(), isRead ? EventType.READ : EventType.UNREAD,
                context, userId, userInfo.getVisitorId());
        Runnable task = new EventReportEmbeddedMessagesRunnable(
                config, request, embeddedMessagesStatusHandler);
        executorService.submit(task);
    }

    private EmbeddedMessagingConfig handleConfigForAsyncSetCall(EmbeddedMessagesSetHandler setHandler) {
        EmbeddedMessagingConfig config = Optimove.getConfig().getEmbeddedMessagingConfig();
        if (config == null) {
            Log.e(TAG, "Embedded messaging config is not set");
            handler.post(() -> setHandler.run(ResultType.ERROR_CONFIG_NOT_SET));
            return null;
        }

        return config;
    }

    class GetEmbeddedMessagesRunnable extends EmbeddedMessagesRunnableBase implements Runnable {
        private final EmbeddedMessagesGetHandler callback;
        private final String customerId;
        private final ContainerRequestOptions[] requestBody;

        GetEmbeddedMessagesRunnable(
                EmbeddedMessagingConfig config, String customerId, EmbeddedMessagesGetHandler callback, ContainerRequestOptions[] requestBody) {
            super(config);
            this.customerId = customerId;
            this.callback = callback;
            this.requestBody = requestBody;
        }

        @Override
        public void run() {
            EmbeddedMessagingResult result = new EmbeddedMessagingResult(ResultType.ERROR, null);
            try {
                String encodedCustomerId = URLEncoder.encode(this.customerId, "UTF-8");
                String url = String.format(
                        "%s&customerId=%s",
                        super.getBaseUrl("embedded-messages/get"),
                        encodedCustomerId);
                JSONArray postBody = new JSONArray();
                for (ContainerRequestOptions cm : requestBody) {
                    postBody.put(cm.toJSONObject());
                }
                result = super.postSync(url, postBody, true);
            } catch (Exception e) {
                Log.e(TAG, e.getMessage());
                e.printStackTrace();
            }

            this.fireCallback(result.getResult(), result.getResponse());
        }

        private void fireCallback(ResultType result, @Nullable EmbeddedMessagesResponse response) {
            handler.post(() -> GetEmbeddedMessagesRunnable.this.callback.run(result, response));
        }
    }

    class EventReportEmbeddedMessagesRunnable extends EmbeddedMessagesRunnableBase implements Runnable {
        private final EmbeddedMessagesSetHandler callback;

        private final EmbeddedMessageEventRequest request;

        EventReportEmbeddedMessagesRunnable(
                EmbeddedMessagingConfig config,
                EmbeddedMessageEventRequest request,
                EmbeddedMessagesSetHandler callback) {
            super(config);
            this.callback = callback;
            this.request = request;
        }

        @Override
        public void run() {
            EmbeddedMessagingResult result = new EmbeddedMessagingResult(ResultType.ERROR, null);

            try {
                JSONArray postData = new JSONArray();
                postData.put(request.toJSONObject());
                String url = super.getBaseUrl("events/report");
                result = super.postSync(url, postData, false);
            } catch (Exception e) {
                Log.e(TAG, e.getMessage());
                e.printStackTrace();
            }

            this.fireCallback(result.getResult());
        }

        private void fireCallback(ResultType result) {
            handler.post(() -> EventReportEmbeddedMessagesRunnable.this.callback.run(result));
        }
    }

    class EmbeddedMessagesRunnableBase {
        protected EmbeddedMessagingConfig config;

        public EmbeddedMessagesRunnableBase(EmbeddedMessagingConfig config) {
            this.config = config;
        }

        public EmbeddedMessagingResult postSync(String url, JSONArray postData, boolean expectResponse) {
            HttpClient httpClient = HttpClient.getInstance();

            try (Response response = httpClient.postSync(url, postData, config.getTenantId())) {
                return handleResponse(response, expectResponse);
            } catch (Exception e) {
                Log.e(TAG, e.getMessage());
                e.printStackTrace();
                return new EmbeddedMessagingResult(ResultType.ERROR, null);
            }
        }
        protected String getBaseUrl(String endpoint) {
            String region = config.getRegion();

            return String.format(
                    "https://optimobile-inbox-srv-%s.optimove.net/api/v2/%s?tenantId=%s&brandId=%s",
                    region, endpoint, config.getTenantId(), config.getBrandId());
        }

        private EmbeddedMessagingResult handleResponse(Response response, boolean expectResponse) {
            EmbeddedMessagesResponse containers = null;
            ResultType resultType = ResultType.ERROR;
            if (!response.isSuccessful()) {
                logFailedResponse(response);
            } else {
                containers = expectResponse ? mapResponse(response) : null;
                resultType = ResultType.SUCCESS;
            }
            return new EmbeddedMessagingResult(resultType, containers);
        }

        private EmbeddedMessagesResponse mapResponse(Response response) {
            try {
                Map<String, Container> containerMap = new HashMap<>();
                JSONObject data = new JSONObject(response.body().string());

                JSONObject containerObject = data.getJSONObject("containers");
                Iterator<String> containerKeys = containerObject.keys();
                while (containerKeys.hasNext()) {
                    String containerId = containerKeys.next();
                    JSONArray containerMessages = containerObject.getJSONArray(containerId);
                    EmbeddedMessage[] embeddedMessages = new EmbeddedMessage[containerMessages.length()];
                    for (int i = 0; i < containerMessages.length(); i++) {
                        JSONObject message = containerMessages.getJSONObject(i);
                        embeddedMessages[i] = new EmbeddedMessage(message);
                    }
                    containerMap.put(containerId, new Container(containerId, embeddedMessages));
                }
                return new EmbeddedMessagesResponse(containerMap);
            } catch (NullPointerException | JSONException | IOException e) {
                Log.e(TAG, e.getMessage());
                return null;
            }
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
