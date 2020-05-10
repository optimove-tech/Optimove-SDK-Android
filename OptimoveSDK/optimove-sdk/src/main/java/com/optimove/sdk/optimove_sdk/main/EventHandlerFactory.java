package com.optimove.sdk.optimove_sdk.main;

import android.content.Context;

import com.optimove.sdk.optimove_sdk.main.event_handlers.DestinationDecider;
import com.optimove.sdk.optimove_sdk.main.event_handlers.EventDecorator;
import com.optimove.sdk.optimove_sdk.main.event_handlers.EventMemoryBuffer;
import com.optimove.sdk.optimove_sdk.main.event_handlers.EventNormalizer;
import com.optimove.sdk.optimove_sdk.main.event_handlers.EventSynchronizer;
import com.optimove.sdk.optimove_sdk.main.event_handlers.EventValidator;
import com.optimove.sdk.optimove_sdk.main.event_handlers.OptistreamHandler;
import com.optimove.sdk.optimove_sdk.main.sdk_configs.configs.OptitrackConfigs;
import com.optimove.sdk.optimove_sdk.main.sdk_configs.configs.RealtimeConfigs;
import com.optimove.sdk.optimove_sdk.main.sdk_configs.reused_configs.EventConfigs;
import com.optimove.sdk.optimove_sdk.main.tools.networking.HttpClient;
import com.optimove.sdk.optimove_sdk.optitrack.OptistreamQueue;
import com.optimove.sdk.optimove_sdk.realtime.RealtimeManager;

import java.util.Map;
import java.util.concurrent.ExecutorService;

//creates specific steps or chain of steps
public class EventHandlerFactory {


    private HttpClient httpClient;
    private UserInfo userInfo;
    private int maximumBufferSize;
    private OptistreamQueue optistreamQueue;
    private LifecycleObserver lifecycleObserver;
    private Context context;

    private EventHandlerFactory(HttpClient httpClient, UserInfo userInfo,
                                int maximumBufferSize, OptistreamQueue optistreamQueue,
                                LifecycleObserver lifecycleObserver, Context context) {
        this.httpClient = httpClient;
        this.userInfo = userInfo;
        this.maximumBufferSize = maximumBufferSize;
        this.optistreamQueue = optistreamQueue;
        this.lifecycleObserver = lifecycleObserver;
        this.context = context;

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

    public RealtimeManager getRealtimeMananger(RealtimeConfigs realtimeConfigs) {
        return new RealtimeManager(httpClient, realtimeConfigs, context);
    }

    public OptistreamHandler getOptistreamHandler(OptitrackConfigs optitrackConfigs) {
        return new OptistreamHandler(httpClient, lifecycleObserver, optistreamQueue, optitrackConfigs);
    }

    public DestinationDecider getDestinationDecider(Map<String, EventConfigs> eventConfigs,
                                                    OptistreamHandler optistreamHandler,
                                                    RealtimeManager realtimeManager,
                                                    OptistreamEventBuilder optistreamEventBuilder,
                                                    boolean realtimeEnabled, boolean realtimeEnabledThroughOptistream) {
        return new DestinationDecider(eventConfigs, optistreamHandler, realtimeManager, optistreamEventBuilder,
                realtimeEnabled, realtimeEnabledThroughOptistream);
    }

    public OptistreamEventBuilder getOptistreamEventBuilder(int tenantId) {
        return new OptistreamEventBuilder(tenantId, userInfo);
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
        LifecycleObserverStep optistreamQueue(OptistreamQueue optistreamQueue);
    }

    public interface LifecycleObserverStep {
        ContextStep lifecycleObserver(LifecycleObserver lifecycleObserver);
    }

    public interface ContextStep {
        Build context(Context context);
    }

    public interface Build {
        EventHandlerFactory build();
    }

    public static class Builder implements OptistreamQueueStep, MaximumBufferSizeStep, HttpClientStep, UserInfoStep,
            LifecycleObserverStep, ContextStep, Build {

        private HttpClient httpClient;
        private UserInfo userInfo;
        private int maximumBufferSize;
        private OptistreamQueue optistreamQueue;
        private LifecycleObserver lifecycleObserver;
        private Context context;

        @Override
        public HttpClientStep userInfo(UserInfo userInfo) {
            this.userInfo = userInfo;
            return this;
        }

        @Override
        public MaximumBufferSizeStep httpClient(HttpClient httpClient) {
            this.httpClient = httpClient;
            return this;
        }

        @Override
        public OptistreamQueueStep maximumBufferSize(int maximumBufferSize) {
            this.maximumBufferSize = maximumBufferSize;
            return this;
        }

        @Override
        public LifecycleObserverStep optistreamQueue(OptistreamQueue optistreamQueue) {
            this.optistreamQueue = optistreamQueue;
            return this;
        }

        @Override
        public ContextStep lifecycleObserver(LifecycleObserver lifecycleObserver) {
            this.lifecycleObserver = lifecycleObserver;
            return this;
        }

        @Override
        public Build context(Context context) {
            this.context = context;
            return this;
        }

        @Override
        public EventHandlerFactory build() {
            return new EventHandlerFactory(httpClient, userInfo, maximumBufferSize, optistreamQueue,
                    lifecycleObserver, context);
        }
    }


}
