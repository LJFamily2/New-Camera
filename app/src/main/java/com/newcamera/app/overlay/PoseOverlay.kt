package com.newcamera.app.overlay

import androidx.compose.foundation.Image
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.painterResource

/**
 * Transparent gesture-driven guide rendered on top of the viewfinder. This composable only
 * reads [pose] and [state]; it never touches camera/ViewModel state, so camera preview
 * recompositions never cascade into overlay gesture handling and vice versa.
 *
 * IMPORTANT: this overlay draws only on the Compose canvas above [androidx.camera.view.PreviewView].
 * It is never part of the CameraX ImageCapture pipeline, so it can never end up baked into a
 * saved photo.
 */
@Composable
fun PoseOverlay(
    pose: PoseGuide?,
    state: PoseOverlayState,
    onStateChange: (PoseOverlayState) -> Unit,
    modifier: Modifier = Modifier
) {
    if (pose == null) return

    // rememberUpdatedState keeps the long-lived gesture-detection coroutine (keyed on pose.id,
    // not on every state change) reading the latest state/callback without needing to restart -
    // restarting on every drag/zoom tick would drop in-flight gesture events and feel laggy.
    val currentState = rememberUpdatedState(state)
    val currentOnStateChange = rememberUpdatedState(onStateChange)

    Box(
        modifier = modifier
            .fillMaxSize()
            .pointerInput(pose.id) {
                detectTransformGestures { _, pan, zoom, _ ->
                    val latest = currentState.value
                    currentOnStateChange.value(latest.withTransform(zoom, pan.x, pan.y))
                }
            }
    ) {
        Image(
            painter = painterResource(id = pose.iconRes),
            contentDescription = pose.displayName,
            modifier = Modifier
                .align(Alignment.Center)
                .fillMaxSize(fraction = 0.85f)
                .graphicsLayer {
                    scaleX = state.scale
                    scaleY = state.scale
                    translationX = state.offsetX
                    translationY = state.offsetY
                    alpha = state.opacity
                }
        )
    }
}
