package com.optimove.sdk.optimove_sdk.main;

import com.optimove.sdk.optimove_sdk.main.event_handlers.EventDecorator;
import com.optimove.sdk.optimove_sdk.main.event_handlers.EventMemoryBuffer;
import com.optimove.sdk.optimove_sdk.main.event_handlers.EventNormalizer;
import com.optimove.sdk.optimove_sdk.main.event_handlers.EventSynchronizer;
import com.optimove.sdk.optimove_sdk.main.event_handlers.EventValidator;
import com.optimove.sdk.optimove_sdk.main.event_handlers.OptistreamHandler;
import com.optimove.sdk.optimove_sdk.main.sdk_configs.configs.OptitrackConfigs;
import com.optimove.sdk.optimove_sdk.main.sdk_configs.reused_configs.EventConfigs;
import com.optimove.sdk.optimove_sdk.main.tools.networking.HttpClient;
import com.optimove.sdk.optimove_sdk.optitrack.OptistreamQueue;

import java.util.Map;
import java.util.concurrent.ExecutorService;

//creates specific steps or chain of steps
public class EventHandlerFactory {


    private HttpClient httpClient;
    private UserInfo userInfo;
    private int maximumBufferSize;
    private OptistreamQueue optistreamQueue;

    private EventHandlerFactory(HttpClient httpClient, UserInfo userInfo,
                                int maximumBufferSize, OptistreamQueue optistreamQueue) {
        this.httpClient = httpClient;
        this.userInfo = userInfo;
        this.maximumBufferSize = maximumBufferSize;
        this.optistreamQueue = optistreamQueue;
    }

    public EventMemoryBuffer getEventBuffer() {
        return new EventMemoryBuffer(maximumBufferSize);
    }

    public EventSynchronizer getEventSynchronizer(ExecutorService singleThreadExecutor) {
        return new EventSynchronizer(singleThreadExecutor);
    }

    public EventValidator getEventValidator(Map<String, EventConfigs> eventConfigs) {
        return new EventValidator(eventConfigs);
    }

    public EventNormalizer getEventNormalizer() {
        return new EventNormalizer();
    }

    public EventDecorator getEventDecorator(Map<String, EventConfigs> eventConfigs) {
        return new EventDecorator(eventConfigs);
    }


    public OptistreamHandler getOptistreamHandler(OptitrackConfigs optitrackConfigs,
                                           Map<String, EventConfigs> eventConfigs,
                                           LifecycleObserver lifecycleObserver) {
        return new OptistreamHandler(httpClient, userInfo, eventConfigs, lifecycleObserver, optistreamQueue, optitrackConfigs);
    }


    public static UserInfoStep builder() {
        return new Builder();
    }


    public interface UserInfoStep {
        HttpClientStep userInfo(UserInfo userInfo);
    }


    public interface HttpClientStep {
        MaximumBufferSizeStep httpClient(HttpClient httpClient);
    }

    public interface MaximumBufferSizeStep {
        OptistreamQueueStep maximumBufferSize(int maximumBufferSize);
    }

    public interface OptistreamQueueStep {
        Build optistreamQueue(OptistreamQueue optistreamQueue);
    }


    public interface Build {
        EventHandlerFactory build();
    }

    public static class Builder implements OptistreamQueueStep, MaximumBufferSizeStep, HttpClientStep, UserInfoStep, Build {

        private HttpClient httpClient;
        private UserInfo userInfo;
        private int maximumBufferSize;
        private OptistreamQueue optistreamQueue;


        @Override
        public HttpClientStep userInfo(UserInfo userInfo) {
            this.userInfo = userInfo;
            return this;
        }

        @Override
        public OptistreamQueueStep maximumBufferSize(int maximumBufferSize) {
            this.maximumBufferSize = maximumBufferSize;
            return this;
        }

        @Override
        public Build optistreamQueue(OptistreamQueue optistreamQueue) {
            this.optistreamQueue = optistreamQueue;
            return this;
        }

        @Override
        public MaximumBufferSizeStep httpClient(HttpClient httpClient) {
            this.httpClient = httpClient;
            return this;
        }

        @Override
        public EventHandlerFactory build() {
            return new EventHandlerFactory(httpClient, userInfo, maximumBufferSize, optistreamQueue);
        }
    }


}
