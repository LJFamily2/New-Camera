package com.newcamera.app.camera

import androidx.camera.core.AspectRatio
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class CaptureAspectRatioTest {

    @Test
    fun `next cycles Full to 4x3 to 1x1 and back to Full`() {
        assertEquals(CaptureAspectRatio.RATIO_4_3, CaptureAspectRatio.FULL.next())
        assertEquals(CaptureAspectRatio.RATIO_1_1, CaptureAspectRatio.RATIO_4_3.next())
        assertEquals(CaptureAspectRatio.FULL, CaptureAspectRatio.RATIO_1_1.next())
    }

    @Test
    fun `Full has no CameraX aspect ratio constraint`() {
        assertNull(CaptureAspectRatio.FULL.toCameraXAspectRatio())
    }

    @Test
    fun `4x3 and 1x1 both capture at CameraX RATIO_4_3 since square has no native support`() {
        assertEquals(AspectRatio.RATIO_4_3, CaptureAspectRatio.RATIO_4_3.toCameraXAspectRatio())
        assertEquals(AspectRatio.RATIO_4_3, CaptureAspectRatio.RATIO_1_1.toCameraXAspectRatio())
    }

    @Test
    fun `only 1x1 requires a post-capture square crop`() {
        assertTrue(CaptureAspectRatio.RATIO_1_1.requiresSquareCrop)
        assertTrue(!CaptureAspectRatio.FULL.requiresSquareCrop)
        assertTrue(!CaptureAspectRatio.RATIO_4_3.requiresSquareCrop)
    }

    @Test
    fun `width-to-height ratios are portrait-oriented`() {
        assertNull(CaptureAspectRatio.FULL.widthToHeightRatio)
        assertEquals(3f / 4f, CaptureAspectRatio.RATIO_4_3.widthToHeightRatio!!, 1e-4f)
        assertEquals(1f, CaptureAspectRatio.RATIO_1_1.widthToHeightRatio!!, 1e-4f)
    }
}
