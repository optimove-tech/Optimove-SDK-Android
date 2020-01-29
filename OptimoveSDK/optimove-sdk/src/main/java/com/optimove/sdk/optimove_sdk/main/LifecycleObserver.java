package com.optimove.sdk.optimove_sdk.main;

import android.app.Activity;
import android.app.Application;
import android.os.Bundle;

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
    public void onActivityCreated(Activity activity, Bundle savedInstanceState) {

    }

    @Override
    public void onActivityStarted(Activity activity) {
        for (WeakReference<ActivityStarted> activityStartedWeakReference: activityStartedListeners) {
            if (activityStartedWeakReference.get() != null) {
                activityStartedWeakReference.get().activityStarted();
            }
        }
    }

    @Override
    public void onActivityResumed(Activity activity) {

    }

    @Override
    public void onActivityPaused(Activity activity) {

    }

    @Override
    public void onActivityStopped(Activity activity) {
        for (WeakReference<ActivityStopped> activityStoppedWeakReference: activityStoppedListeners) {
            if (activityStoppedWeakReference.get() != null) {
                activityStoppedWeakReference.get().activityStopped();
            }
        }
    }

    @Override
    public void onActivitySaveInstanceState(Activity activity, Bundle outState) {

    }

    @Override
    public void onActivityDestroyed(Activity activity) {

    }
}
