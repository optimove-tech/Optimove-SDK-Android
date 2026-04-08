package com.optimove.android.gamifywidgetsdk

import org.junit.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify

class AndroidBridgeTest {

    @Test
    fun `closeWidget invokes onClose callback`() {
        val onClose = mock<() -> Unit>()
        val bridge = AndroidBridge(onClose)
        bridge.closeWidget()
        verify(onClose).invoke()
    }

    @Test
    fun `receiveMessage does not throw`() {
        val bridge = AndroidBridge(onClose = {})
        bridge.receiveMessage("""{"type":"TEST"}""")
    }
}
