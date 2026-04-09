package com.optimove.android.gamifywidgetsdk

import org.junit.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.verify

class AndroidBridgeTest {

    @Test
    fun `closeWidget invokes onClose callback`() {
        val onClose = mock<() -> Unit>()
        val bridge = AndroidBridge(onClose = onClose, onReady = {})
        bridge.closeWidget()
        verify(onClose).invoke()
    }

    @Test
    fun `receiveMessage invokes onReady for READY type`() {
        val onReady = mock<() -> Unit>()
        val bridge = AndroidBridge(onClose = {}, onReady = onReady)
        bridge.receiveMessage("""{"type":"READY"}""")
        verify(onReady).invoke()
    }

    @Test
    fun `receiveMessage does not invoke onReady for other types`() {
        val onReady = mock<() -> Unit>()
        val bridge = AndroidBridge(onClose = {}, onReady = onReady)
        bridge.receiveMessage("""{"type":"TEST"}""")
        verify(onReady, never()).invoke()
    }

}
