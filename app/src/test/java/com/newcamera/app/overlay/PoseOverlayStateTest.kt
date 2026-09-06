package com.newcamera.app.overlay

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class PoseOverlayStateTest {

    @Test
    fun `default state has neutral scale and no offset`() {
        val state = PoseOverlayState()
        assertEquals(1f, state.scale, 0f)
        assertEquals(0f, state.offsetX, 0f)
        assertEquals(0f, state.offsetY, 0f)
    }

    @Test
    fun `withTransform accumulates pan and multiplies scale`() {
        val state = PoseOverlayState()
            .withTransform(scaleDelta = 1.5f, panDeltaX = 10f, panDeltaY = -5f)
            .withTransform(scaleDelta = 2f, panDeltaX = 5f, panDeltaY = 5f)

        assertEquals(3f, state.scale, 1e-4f)
        assertEquals(15f, state.offsetX, 1e-4f)
        assertEquals(0f, state.offsetY, 1e-4f)
    }

    @Test
    fun `withTransform clamps scale to the allowed range`() {
        val zoomedOut = PoseOverlayState().withTransform(0.01f, 0f, 0f)
        assertEquals(PoseOverlayState.MIN_SCALE, zoomedOut.scale, 0f)

        val zoomedIn = PoseOverlayState().withTransform(100f, 0f, 0f)
        assertEquals(PoseOverlayState.MAX_SCALE, zoomedIn.scale, 0f)
    }

    @Test
    fun `withOpacity clamps to the allowed range`() {
        assertEquals(PoseOverlayState.MIN_OPACITY, PoseOverlayState().withOpacity(-5f).opacity, 0f)
        assertEquals(PoseOverlayState.MAX_OPACITY, PoseOverlayState().withOpacity(5f).opacity, 0f)
        assertEquals(0.5f, PoseOverlayState().withOpacity(0.5f).opacity, 0f)
    }

    @Test
    fun `reset clears scale and offset but keeps opacity`() {
        val transformed = PoseOverlayState(opacity = 0.9f)
            .withTransform(2f, 30f, 40f)
            .reset()

        assertEquals(1f, transformed.scale, 0f)
        assertEquals(0f, transformed.offsetX, 0f)
        assertEquals(0f, transformed.offsetY, 0f)
        assertEquals(0.9f, transformed.opacity, 0f)
    }

    @Test
    fun `min scale is below max scale`() {
        assertTrue(PoseOverlayState.MIN_SCALE < PoseOverlayState.MAX_SCALE)
    }
}
