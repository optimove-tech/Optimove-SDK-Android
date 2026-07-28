package com.optimove.android.gamifywidgetsdk;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import org.junit.Test;

public class AndroidBridgeTest {

    @Test
    public void closeWidget_invokesOnCloseCallback() {
        Runnable onClose = mock(Runnable.class);
        AndroidBridge bridge = new AndroidBridge(onClose, mock(Runnable.class));
        bridge.closeWidget();
        verify(onClose).run();
    }

    @Test
    public void receiveMessage_invokesOnReadyForReadyType() {
        Runnable onReady = mock(Runnable.class);
        AndroidBridge bridge = new AndroidBridge(mock(Runnable.class), onReady);
        bridge.receiveMessage("{\"type\":\"READY\"}");
        verify(onReady).run();
    }

    @Test
    public void receiveMessage_doesNotInvokeOnReadyForOtherTypes() {
        Runnable onReady = mock(Runnable.class);
        AndroidBridge bridge = new AndroidBridge(mock(Runnable.class), onReady);
        bridge.receiveMessage("{\"type\":\"TEST\"}");
        verify(onReady, never()).run();
    }
}
