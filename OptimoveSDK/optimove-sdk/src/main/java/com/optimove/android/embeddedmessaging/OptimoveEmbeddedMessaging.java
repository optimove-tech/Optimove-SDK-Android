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
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import kotlin.NotImplementedError;
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

    public void getEmbeddedMessagesAsync(ContainerMessageRequest[] requestBody, @NonNull EmbeddedMessagesGetHandler embeddedMessagesGetHandler) {
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
        Runnable task = new GetEmbeddedMessagesRunnable(config, userId, embeddedMessagesGetHandler, requestBody);
        executorService.submit(task);
    }

    public void getEmbeddedMessagesAsync(@NonNull EmbeddedMessagesGetHandler embeddedMessagesGetHandler) {
        getEmbeddedMessagesAsync(new ContainerMessageRequest[]{}, embeddedMessagesGetHandler);
    }

    public void deleteEmbeddedMessageAsync(String Id, @NonNull EmbeddedMessagesSetHandler embeddedMessagesDeleteHandler) {
        EmbeddedMessagingConfig config = handleConfigForAsyncSetCall(embeddedMessagesDeleteHandler);
        if (config == null) return;

        Runnable task = new DeleteEmbeddedMessagesRunnable(config, Id, embeddedMessagesDeleteHandler);
        executorService.submit(task);
    }

    public void reportClickMetricAsync(
            EmbeddedMessageMetricsRequest metrics,
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
        Runnable task = new PostEmbeddedMesssagesMetricsRunnable(metrics, config, userId, embeddedMessagesMetricsHandler);
        executorService.submit(task);
    }

    public void setAsReadASync(EmbeddedMessageStatusRequest statusMetrics,
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
        Runnable task = new PutEmbeddedMessagesStatusRunnable(
                statusMetrics, config, userId, embeddedMessagesStatusHandler);
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
        private final ContainerMessageRequest[] requestBody;

        GetEmbeddedMessagesRunnable(
                EmbeddedMessagingConfig config, String customerId, EmbeddedMessagesGetHandler callback, ContainerMessageRequest[] requestBody) {
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
                        super.getBaseUrl("embedded-messages/get-embedded-messages"),
                        encodedCustomerId);
                JSONArray postBody = new JSONArray();
                for (ContainerMessageRequest cm : requestBody) {
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

    class PutEmbeddedMessagesStatusRunnable extends EmbeddedMessagesRunnableBase implements Runnable {
        private final EmbeddedMessagesSetHandler callback;
        private final EmbeddedMessagingConfig config;
        private final EmbeddedMessageStatusRequest statusMetrics;
        private final String customerId;

        public PutEmbeddedMessagesStatusRunnable(
                EmbeddedMessageStatusRequest statusMetrics, EmbeddedMessagingConfig config,
                String customerId, EmbeddedMessagesSetHandler callback) {
            super(config);
            this.config = config;
            this.callback = callback;
            this.customerId = customerId;
            this.statusMetrics = statusMetrics;
        }

        @Override
        public void run() {
            EmbeddedMessagingResult result = new EmbeddedMessagingResult(ResultType.ERROR, null);

            try {
                String url = super.getBaseUrl("messages/status");
                statusMetrics.setTenantId(config.getTenantId());
                statusMetrics.setBrandId(config.getBrandId());
                statusMetrics.setCustomerId(customerId);
                result = super.putSync(url, statusMetrics.toJSONObject(), false);
            } catch (Exception e) {
                Log.e(TAG, e.getMessage());
                e.printStackTrace();
            }

            this.fireCallback(result.getResult());
        }

        private void fireCallback(ResultType result) {
            handler.post(() -> PutEmbeddedMessagesStatusRunnable.this.callback.run(result));
        }
    }

    class PostEmbeddedMesssagesMetricsRunnable extends EmbeddedMessagesRunnableBase implements Runnable {
        private final EmbeddedMessagesSetHandler callback;
        private final EmbeddedMessagingConfig config;
        private final EmbeddedMessageMetricsRequest metrics;
        private final String customerId;

        public PostEmbeddedMesssagesMetricsRunnable(
                EmbeddedMessageMetricsRequest metrics, EmbeddedMessagingConfig config,
                String customerId, EmbeddedMessagesSetHandler callback) {
            super(config);
            this.config = config;
            this.callback = callback;
            this.metrics = metrics;
            this.customerId = customerId;
        }

        @Override
        public void run() {
            EmbeddedMessagingResult result = new EmbeddedMessagingResult(ResultType.ERROR, null);

            try {
                String url = super.getBaseUrl("messages/metrics");
                metrics.setTenantId(config.getTenantId());
                metrics.setBrandId(config.getBrandId());
                metrics.setCustomerId(customerId);
                result = super.postSync(url, metrics.toJSONObject(), false);
            } catch (Exception e) {
                Log.e(TAG, e.getMessage());
                e.printStackTrace();
            }

            this.fireCallback(result.getResult());
        }

        private void fireCallback(ResultType result) {
            handler.post(() -> PostEmbeddedMesssagesMetricsRunnable.this.callback.run(result));
        }

    }

    class DeleteEmbeddedMessagesRunnable extends EmbeddedMessagesRunnableBase implements Runnable {
        private final EmbeddedMessagesSetHandler callback;
        private final String id;

        DeleteEmbeddedMessagesRunnable(EmbeddedMessagingConfig config, String id, EmbeddedMessagesSetHandler callback) {
            super(config);
            this.callback = callback;
            this.id = id;
        }

        @Override
        public void run() {
            EmbeddedMessagingResult result = new EmbeddedMessagingResult(ResultType.ERROR, null);

            try {
                String url = super.getBaseUrl(String.format("messages/%s", id));
                result = super.deleteSync(url);
            } catch (Exception e) {
                Log.e(TAG, e.getMessage());
                e.printStackTrace();
            }

            this.fireCallback(result.getResult());
        }

        private void fireCallback(ResultType result) {
            handler.post(() -> DeleteEmbeddedMessagesRunnable.this.callback.run(result));
        }
    }

    class EmbeddedMessagesRunnableBase {
        protected EmbeddedMessagingConfig config;

        public EmbeddedMessagesRunnableBase(EmbeddedMessagingConfig config) {
            this.config = config;
        }

        public EmbeddedMessagingResult deleteSync(String url) {
            HttpClient httpClient = HttpClient.getInstance();

            try (Response response = httpClient.deleteSync(url, config.getTenantId())) {
                return handleResponse(response, false);
            } catch (Exception e) {
                Log.e(TAG, e.getMessage());
                e.printStackTrace();
                return new EmbeddedMessagingResult(ResultType.ERROR, null);
            }
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

        public EmbeddedMessagingResult postSync(String url, JSONObject postData, boolean expectResponse) {
            HttpClient httpClient = HttpClient.getInstance();

            try (Response response = httpClient.postSingleSync(url, postData, config.getTenantId())) {
                return handleResponse(response, expectResponse);
            } catch (Exception e) {
                Log.e(TAG, e.getMessage());
                e.printStackTrace();
                return new EmbeddedMessagingResult(ResultType.ERROR, null);
            }
        }

        public EmbeddedMessagingResult putSync(String url, JSONObject postData, boolean expectResponse) {
            HttpClient httpClient = HttpClient.getInstance();

            try (Response response = httpClient.putSingleSync(url, postData, config.getTenantId())) {
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
                    "https://optimobile-inbox-srv-%s.optimove.net/api/v1/%s?tenantId=%s&brandId=%s",
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
