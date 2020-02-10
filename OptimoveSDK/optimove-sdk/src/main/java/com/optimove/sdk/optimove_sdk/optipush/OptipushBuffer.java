package com.optimove.sdk.optimove_sdk.optipush;

import com.google.firebase.messaging.RemoteMessage;
import com.optimove.sdk.optimove_sdk.optipush.registration.RegistrationDao;

public class OptipushBuffer extends OptipushHandler {


    private RegistrationDao registrationDao;

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
        if (optipushMessageCommandWasCalled) {
            next.optipushMessageCommand(optipushRemoteMessage, executionTimeLimitInMs);
        }
        clearBuffer();
    }

    private void clearBuffer() {
        optipushMessageCommandWasCalled = false;
        optipushRemoteMessage = null;
        executionTimeLimitInMs = -1;
    }

    @Override
    public void userIdChanged() {
        if (next != null) {
            next.userIdChanged();
        } else {
            registrationDao.editFlags()
                    .markSetInstallationAsFailed()
                    .save();
        }
    }

    @Override
    public void tokenWasChanged() {
        if (next != null) {
            next.tokenWasChanged();
        } else {
            registrationDao.editFlags()
                    .markSetInstallationAsFailed()
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
