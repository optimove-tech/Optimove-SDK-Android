package com.optimove.sdk.optimove_sdk.main;

import android.content.Context;

import com.optimove.sdk.optimove_sdk.main.event_handlers.EventDecorator;
import com.optimove.sdk.optimove_sdk.main.event_handlers.EventMemoryBuffer;
import com.optimove.sdk.optimove_sdk.main.event_handlers.EventNormalizer;
import com.optimove.sdk.optimove_sdk.main.event_handlers.EventSynchronizer;
import com.optimove.sdk.optimove_sdk.main.event_handlers.EventValidator;
import com.optimove.sdk.optimove_sdk.main.sdk_configs.configs.OptitrackConfigs;
import com.optimove.sdk.optimove_sdk.main.sdk_configs.configs.RealtimeConfigs;
import com.optimove.sdk.optimove_sdk.main.sdk_configs.reused_configs.EventConfigs;
import com.optimove.sdk.optimove_sdk.main.tools.networking.HttpClient;
import com.optimove.sdk.optimove_sdk.optitrack.OptitrackAdapter;
import com.optimove.sdk.optimove_sdk.optitrack.OptitrackManager;
import com.optimove.sdk.optimove_sdk.realtime.RealtimeManager;

import java.util.Map;
import java.util.concurrent.ExecutorService;

//creates specific steps or chain of steps
public class EventHandlerFactory {


    private HttpClient httpClient;
    private OptitrackAdapter optitrackAdapter;
    private UserInfo userInfo;
    private int maximumBufferSize;
    private Context context;

    private EventHandlerFactory(HttpClient httpClient,
                                OptitrackAdapter optitrackAdapter, UserInfo userInfo,
                                int maximumBufferSize,
                                Context context) {
        this.httpClient = httpClient;
        this.optitrackAdapter = optitrackAdapter;
        this.userInfo = userInfo;
        this.maximumBufferSize = maximumBufferSize;
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

    public RealtimeManager getRealtimeManager(RealtimeConfigs realtimeConfigs, Map<String, EventConfigs> eventConfigs) {
        return new RealtimeManager(httpClient, realtimeConfigs,
                eventConfigs, userInfo, context);
    }

    public OptitrackManager getOptitrackManager(OptitrackConfigs optitrackConfigs,
                                                Map<String, EventConfigs> eventConfigs, LifecycleObserver lifecycleObserver) {
        return new OptitrackManager(optitrackAdapter, optitrackConfigs, userInfo,
                eventConfigs, lifecycleObserver, context);
    }


    public static OptitrackAdapterStep builder() {
        return new Builder();
    }

    public interface OptitrackAdapterStep {
        UserInfoStep optitrackAdapter(OptitrackAdapter optitrackAdapter);
    }

    public interface UserInfoStep {
        HttpClientStep userInfo(UserInfo userInfo);
    }


    public interface HttpClientStep {
        MaximumBufferSizeStep httpClient(HttpClient httpClient);
    }
    public interface MaximumBufferSizeStep {
        ContextStep maximumBufferSize(int maximumBufferSize);
    }

    public interface ContextStep {
        Build context(Context context);
    }

    public interface Build {
        EventHandlerFactory build();
    }

    public static class Builder implements ContextStep, MaximumBufferSizeStep, HttpClientStep, UserInfoStep, OptitrackAdapterStep, Build {

        private HttpClient httpClient;
        private OptitrackAdapter optitrackAdapter;
        private UserInfo userInfo;
        private String fullPackageName;
        private int maximumBufferSize;
        private Context context;

        @Override
        public UserInfoStep optitrackAdapter(OptitrackAdapter optitrackAdapter) {
            this.optitrackAdapter = optitrackAdapter;
            return this;
        }

        @Override
        public HttpClientStep userInfo(UserInfo userInfo) {
            this.userInfo = userInfo;
            return this;
        }

        @Override
        public ContextStep maximumBufferSize(int maximumBufferSize) {
            this.maximumBufferSize = maximumBufferSize;
            return this;
        }

        @Override
        public MaximumBufferSizeStep httpClient(HttpClient httpClient) {
            this.httpClient = httpClient;
            return this;
        }

        @Override
        public Build context(Context context) {
            this.context = context;
            return this;
        }

        @Override
        public EventHandlerFactory build() {
            return new EventHandlerFactory(httpClient, optitrackAdapter, userInfo, maximumBufferSize,
                    context);
        }
    }


}
