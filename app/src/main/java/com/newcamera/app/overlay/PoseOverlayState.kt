package com.newcamera.app.overlay

/**
 * Transform state for the pose guide overlay: pan/zoom to align the guide with the subject,
 * plus a user-adjustable opacity. Kept as a plain data class with pure functions (no Android
 * or Compose types) so it is trivially unit-testable and so the overlay's frequent gesture
 * updates never touch camera/ViewModel state.
 */
data class PoseOverlayState(
    val scale: Float = 1f,
    val offsetX: Float = 0f,
    val offsetY: Float = 0f,
    val opacity: Float = 0.6f
) {
    fun withTransform(scaleDelta: Float, panDeltaX: Float, panDeltaY: Float): PoseOverlayState {
        val newScale = (scale * scaleDelta).coerceIn(MIN_SCALE, MAX_SCALE)
        return copy(
            scale = newScale,
            offsetX = offsetX + panDeltaX,
            offsetY = offsetY + panDeltaY
        )
    }

    fun withOpacity(newOpacity: Float): PoseOverlayState =
        copy(opacity = newOpacity.coerceIn(MIN_OPACITY, MAX_OPACITY))

    fun reset(): PoseOverlayState = copy(scale = 1f, offsetX = 0f, offsetY = 0f)

    companion object {
        const val MIN_SCALE = 0.3f
        const val MAX_SCALE = 4f
        const val MIN_OPACITY = 0.1f
        const val MAX_OPACITY = 1f
    }
}
