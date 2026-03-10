package com.optimove.android.optimobile;

public interface InAppMessageInterceptorCallback {
    void show();
    void suppress();
    void defer();
}
