package com.optimove.sdk.optimove_sdk.optipush_tests;

import com.google.firebase.messaging.RemoteMessage;
import com.optimove.sdk.optimove_sdk.main.SdkOperationListener;
import com.optimove.sdk.optimove_sdk.optipush.OptipushBuffer;
import com.optimove.sdk.optimove_sdk.optipush.OptipushHandler;
import com.optimove.sdk.optimove_sdk.optipush.registration.RegistrationDao;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import static com.optimove.sdk.optimove_sdk.optitrack.OptitrackConstants.EVENT_PLATFORM_PARAM_KEY;
import static info.solidsoft.mockito.java8.AssertionMatcher.assertArg;
import static org.mockito.ArgumentMatchers.anySet;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class OptipushBufferTests {

    @Mock
    private RegistrationDao registrationDao;
    @Mock
    private RegistrationDao.FlagsEditor flagsEditor;
    @Mock
    private OptipushHandler nextOptipushHandler;

    private OptipushBuffer optipushBuffer;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        optipushBuffer = new OptipushBuffer(registrationDao);
        when(registrationDao.editFlags()).thenReturn(flagsEditor);
        when(flagsEditor.markSetUserAsFailed()).thenReturn(flagsEditor);
        when(flagsEditor.markAddUserAliasesAsFailed(anySet())).thenReturn(flagsEditor);
    }

    @Test
    public void addRegisteredUserOnDeviceWillBeCalledWhenNextSet() {
        String visitorId = "some_visitor_id";
        String userId = "some_user_id";
        optipushBuffer.addRegisteredUserOnDevice(visitorId,userId);
        optipushBuffer.setNext(nextOptipushHandler);
        InOrder inOrder = Mockito.inOrder(flagsEditor);
        inOrder.verify(flagsEditor).markAddUserAliasesAsFailed((assertArg(arg -> Assert.assertTrue(arg.contains(userId)))));
        inOrder.verify(flagsEditor).save();
    }

    @Test
    public void setUserWillBeMarkedAsFailedIfNoNext() {
        optipushBuffer.tokenWasChanged();
        InOrder inOrder = Mockito.inOrder(flagsEditor);
        inOrder.verify(flagsEditor).markSetUserAsFailed();
        inOrder.verify(flagsEditor).save();
    }
    @Test
    public void optipushMessageCommandWillBeCalledWhenNextSet() {
        int executionTime = 44543;
        RemoteMessage remoteMessage = mock(RemoteMessage.class);

        optipushBuffer.optipushMessageCommand(remoteMessage, executionTime);
        optipushBuffer.setNext(nextOptipushHandler);
        verify(nextOptipushHandler).optipushMessageCommand(remoteMessage, executionTime);
    }

}
