package com.newcamera.app.camera

import org.junit.Assert.assertEquals
import org.junit.Test

class CaptureTimerTest {

    @Test
    fun `next cycles Off to 3s to 10s and back to Off`() {
        assertEquals(CaptureTimer.THREE, CaptureTimer.OFF.next())
        assertEquals(CaptureTimer.TEN, CaptureTimer.THREE.next())
        assertEquals(CaptureTimer.OFF, CaptureTimer.TEN.next())
    }

    @Test
    fun `seconds match the enum intent`() {
        assertEquals(0, CaptureTimer.OFF.seconds)
        assertEquals(3, CaptureTimer.THREE.seconds)
        assertEquals(10, CaptureTimer.TEN.seconds)
    }

    @Test
    fun `cycling three times returns to the start`() {
        var timer = CaptureTimer.OFF
        repeat(3) { timer = timer.next() }
        assertEquals(CaptureTimer.OFF, timer)
    }
}
