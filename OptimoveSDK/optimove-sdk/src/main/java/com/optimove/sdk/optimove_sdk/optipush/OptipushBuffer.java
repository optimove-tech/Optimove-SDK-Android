package com.optimove.sdk.optimove_sdk.optipush;

import android.support.annotation.Nullable;

import com.google.firebase.messaging.RemoteMessage;
import com.optimove.sdk.optimove_sdk.main.SdkOperationListener;
import com.optimove.sdk.optimove_sdk.optipush.registration.RegistrationDao;

import java.util.HashSet;

public class OptipushBuffer extends OptipushHandler {


    private RegistrationDao registrationDao;

    //testmode
    private boolean startTestModeWasCalled;
    private SdkOperationListener startTestModeOperationListener;
    private boolean stopTestModeWasCalled;
    private SdkOperationListener stopTestModeOperationListener;

    //messaging
    private boolean optipushMessageCommandWasCalled;
    private int executionTimeLimitInMs = -1;
    private RemoteMessage optipushRemoteMessage;


    public OptipushBuffer(RegistrationDao registrationDao)  {
        this.registrationDao = registrationDao;
    }


    @Override
    public void setNext(OptipushHandler next) {
        super.setNext(next);
        processBufferedOperations();
    }

    private void processBufferedOperations() {
        if (startTestModeWasCalled) {
            next.startTestMode(startTestModeOperationListener);
        }
        if (stopTestModeWasCalled) {
            next.stopTestMode(stopTestModeOperationListener);
        }
        if (optipushMessageCommandWasCalled) {
            next.optipushMessageCommand(optipushRemoteMessage, executionTimeLimitInMs);
        }
        clearBuffer();
    }

    private void clearBuffer() {
        startTestModeWasCalled = false;
        stopTestModeWasCalled = false;
        stopTestModeOperationListener = null;
        startTestModeOperationListener = null;
        optipushMessageCommandWasCalled = false;
        optipushRemoteMessage = null;
        executionTimeLimitInMs = -1;
    }

    @Override
    public void addRegisteredUserOnDevice(String visitorId, String userId) {
        if (next != null) {
            next.addRegisteredUserOnDevice(visitorId, userId);
        } else {
            registrationDao.editFlags()
                    .markAddUserAliasesAsFailed(new HashSet<String>() {{
                        add(userId);
                    }})
                    .save();
        }
    }

    @Override
    public void startTestMode(@Nullable SdkOperationListener operationListener) {
        if (next != null) {
            next.startTestMode(operationListener);
        } else {
            startTestModeWasCalled = true;
            startTestModeOperationListener = operationListener;
        }
    }

    @Override
    public void stopTestMode(@Nullable SdkOperationListener operationListener) {
        if (next != null) {
            next.stopTestMode(operationListener);
        } else {
            stopTestModeWasCalled = true;
            stopTestModeOperationListener = operationListener;
        }
    }

    @Override
    public void tokenWasChanged() {
        if (next != null) {
            next.tokenWasChanged();
        } else {
            registrationDao.editFlags()
                    .markSetUserAsFailed()
                    .save();
        }
    }

    //messaging

    @Override
    public void optipushMessageCommand(RemoteMessage remoteMessage, int executionTimeLimitInMs) {
        if (next != null) {
            next.optipushMessageCommand(remoteMessage, executionTimeLimitInMs);
        } else {
            optipushMessageCommandWasCalled = true;
            this.executionTimeLimitInMs = executionTimeLimitInMs;
            this.optipushRemoteMessage = remoteMessage;
        }
    }
}
