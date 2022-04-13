package com.optimove.sdk.optimove_sdk.kumulos;

import android.app.Activity;
import android.app.Application;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.UiThread;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

class AppStateWatcher implements Application.ActivityLifecycleCallbacks {

    private static final String TAG = AppStateWatcher.class.getName();

    interface AppStateChangedListener {
        @UiThread
        void appEnteredForeground();

        @UiThread
        void activityAvailable(@NonNull Activity activity);

        @UiThread
        void activityUnavailable(@NonNull Activity activity);

        @UiThread
        void appEnteredBackground();
    }

    private int runningActivities;
    private final List<AppStateChangedListener> listeners;
    private boolean appInForeground;
    private WeakReference<Activity> currentActivityRef;

    AppStateWatcher() {
        listeners = new ArrayList<>(2);
        currentActivityRef = new WeakReference<>(null);
        appInForeground = false;
    }

    @UiThread
    void registerListener(AppStateChangedListener listener) {
        listeners.add(listener);

        if (appInForeground) {
            listener.appEnteredForeground();
        }

        Activity current = currentActivityRef.get();
        if (null != current) {
            listener.activityAvailable(current);
        }
    }

    @Override
    public void onActivityCreated(@NonNull Activity activity, @Nullable Bundle savedInstanceState) { /* noop */ }

    @Override
    public void onActivityStarted(@NonNull Activity activity) { /* noop */ }

    @Override
    public void onActivityResumed(@NonNull Activity activity) {
        ++runningActivities;

        if (1 == runningActivities && !appInForeground) {
            Kumulos.log(TAG, "appEnteredForeground");
            appInForeground = true;

            for (AppStateChangedListener listener : listeners) {
                listener.appEnteredForeground();
            }
        }

        Activity current = currentActivityRef.get();
        if (current != activity) {
            Kumulos.log(TAG, "activityAvailable");
            currentActivityRef = new WeakReference<>(activity);
            for (AppStateChangedListener listener : listeners) {
                listener.activityAvailable(activity);
            }
        }
    }

    @Override
    public void onActivityPaused(@NonNull Activity activity) {
        --runningActivities;

        Kumulos.handler.postDelayed(() -> {
            if (0 == runningActivities) {
                appInForeground = false;
                for (AppStateChangedListener listener : listeners) {
                    Kumulos.log(TAG, "appEnteredBackground");
                    listener.appEnteredBackground();
                }
            }
        }, 700);
    }

    @Override
    public void onActivityStopped(@NonNull Activity activity) { /* noop */ }

    @Override
    public void onActivitySaveInstanceState(@NonNull Activity activity, @NonNull Bundle outState) { /* noop */ }

    @Override
    public void onActivityDestroyed(@NonNull Activity activity) {
        if (this.isKumulosInvisibleActivity(activity)) {
            return;
        }

        Kumulos.log(TAG, "activityUnavailable");
        for (AppStateChangedListener listener : listeners) {
            listener.activityUnavailable(activity);
        }
    }

    private boolean isKumulosInvisibleActivity(@NonNull Activity activity){
        return activity.getComponentName().getClassName().equals(PushOpenInvisibleActivity.class.getName());
    }
}