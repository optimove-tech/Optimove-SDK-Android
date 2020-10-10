package com.optimove.sdk.optimove_sdk.main.common;

import android.app.Activity;
import android.app.Application;
import android.os.Bundle;

import androidx.annotation.NonNull;

import java.lang.ref.WeakReference;
import java.util.ArrayList;

public class LifecycleObserver implements Application.ActivityLifecycleCallbacks {

    private  ArrayList<WeakReference<ActivityStopped>> activityStoppedListeners;
    private  ArrayList<WeakReference<ActivityStarted>> activityStartedListeners;

    public LifecycleObserver() {
        this.activityStoppedListeners = new ArrayList<>();
        this.activityStartedListeners = new ArrayList<>();
    }

    public interface ActivityStopped {
        void activityStopped();
    }
    public interface ActivityStarted {
        void activityStarted();
    }

    public void addActivityStoppedListener(ActivityStopped activityStopped){
        this.activityStoppedListeners.add(new WeakReference<>(activityStopped));
    }
    public void addActivityStartedListener(ActivityStarted activityStarted){
        this.activityStartedListeners.add(new WeakReference<>(activityStarted));
    }
    @Override
    public void onActivityCreated(@NonNull Activity activity, Bundle savedInstanceState) {

    }

    @Override
    public void onActivityStarted(@NonNull Activity activity) {
        for (WeakReference<ActivityStarted> activityStartedWeakReference: activityStartedListeners) {
            if (activityStartedWeakReference.get() != null) {
                activityStartedWeakReference.get().activityStarted();
            }
        }
    }

    @Override
    public void onActivityResumed(@NonNull Activity activity) {

    }

    @Override
    public void onActivityPaused(@NonNull Activity activity) {

    }

    @Override
    public void onActivityStopped(@NonNull Activity activity) {
        for (WeakReference<ActivityStopped> activityStoppedWeakReference: activityStoppedListeners) {
            if (activityStoppedWeakReference.get() != null) {
                activityStoppedWeakReference.get().activityStopped();
            }
        }
    }

    @Override
    public void onActivitySaveInstanceState(@NonNull Activity activity,@NonNull Bundle outState) {

    }

    @Override
    public void onActivityDestroyed(@NonNull Activity activity) {

    }
}
