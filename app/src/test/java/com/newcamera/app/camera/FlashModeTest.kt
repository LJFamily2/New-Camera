package com.newcamera.app.camera

import androidx.camera.core.ImageCapture
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Test

class FlashModeTest {

    @Test
    fun `next cycles OFF to ON to AUTO and back to OFF`() {
        assertEquals(FlashMode.ON, FlashMode.OFF.next())
        assertEquals(FlashMode.AUTO, FlashMode.ON.next())
        assertEquals(FlashMode.OFF, FlashMode.AUTO.next())
    }

    @Test
    fun `each mode maps to a distinct ImageCapture flash constant`() {
        val mapped = FlashMode.values().map { it.toImageCaptureFlashMode() }
        assertEquals(mapped.size, mapped.toSet().size)
    }

    @Test
    fun `flash modes map to the matching ImageCapture constants`() {
        assertEquals(ImageCapture.FLASH_MODE_OFF, FlashMode.OFF.toImageCaptureFlashMode())
        assertEquals(ImageCapture.FLASH_MODE_ON, FlashMode.ON.toImageCaptureFlashMode())
        assertEquals(ImageCapture.FLASH_MODE_AUTO, FlashMode.AUTO.toImageCaptureFlashMode())
    }

    @Test
    fun `cycling three times returns to the start`() {
        var mode = FlashMode.OFF
        repeat(3) { mode = mode.next() }
        assertEquals(FlashMode.OFF, mode)
    }

    @Test
    fun `off and on are not the same constant`() {
        assertNotEquals(FlashMode.OFF.toImageCaptureFlashMode(), FlashMode.ON.toImageCaptureFlashMode())
    }
}
