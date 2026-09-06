package com.newcamera.app.ui

import android.view.MotionEvent
import androidx.camera.view.PreviewView
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.newcamera.app.camera.CameraViewModel
import com.newcamera.app.camera.CaptureResult
import com.newcamera.app.overlay.PoseGuide
import com.newcamera.app.overlay.PoseOverlay
import com.newcamera.app.overlay.PoseOverlayState
import com.newcamera.app.overlay.PoseRepository

@Composable
fun CameraScreen(viewModel: CameraViewModel = viewModel(), modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    val previewView = remember {
        PreviewView(context).apply {
            implementationMode = PreviewView.ImplementationMode.PERFORMANCE
            scaleType = PreviewView.ScaleType.FILL_CENTER
        }
    }

    LaunchedEffect(uiState.lensFacing) {
        viewModel.bindToLifecycle(context, lifecycleOwner, previewView)
    }

    DisposableEffect(Unit) {
        onDispose { viewModel.releaseCamera() }
    }

    // Pose overlay transform state lives here, isolated from CameraUiState, so dragging or
    // zooming the guide never triggers a camera rebind and switching cameras never resets it.
    var selectedPose by remember { mutableStateOf<PoseGuide?>(null) }
    var overlayState by remember { mutableStateOf(PoseOverlayState()) }

    val snackbarHostState = remember { SnackbarHostState() }
    LaunchedEffect(uiState.lastCaptureResult) {
        when (val result = uiState.lastCaptureResult) {
            is CaptureResult.Success -> {
                snackbarHostState.showSnackbar("Photo saved")
                viewModel.consumeCaptureResult()
            }
            is CaptureResult.Error -> {
                snackbarHostState.showSnackbar("Capture failed: ${result.message}")
                viewModel.consumeCaptureResult()
            }
            null -> Unit
        }
    }

    Box(modifier = modifier.fillMaxSize().background(Color.Black)) {
        AndroidView(
            factory = {
                previewView.apply {
                    setOnTouchListener { view, event ->
                        if (event.action == MotionEvent.ACTION_UP) {
                            viewModel.focusAndMeterAt(this, event.x, event.y)
                            view.performClick()
                        }
                        true
                    }
                }
            },
            modifier = Modifier.fillMaxSize()
        )

        PoseOverlay(
            pose = selectedPose,
            state = overlayState,
            onStateChange = { overlayState = it },
            modifier = Modifier.fillMaxSize()
        )

        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 16.dp)
        )

        Column(modifier = Modifier.align(Alignment.BottomCenter)) {
            if (selectedPose != null) {
                OpacitySlider(
                    opacity = overlayState.opacity,
                    onOpacityChange = { overlayState = overlayState.withOpacity(it) }
                )
                Spacer(Modifier.height(8.dp))
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        Brush.verticalGradient(
                            listOf(Color.Transparent, Color.Black.copy(alpha = 0.65f))
                        )
                    )
                    .padding(vertical = 12.dp)
            ) {
                Column {
                    PoseSelectorRow(
                        poses = PoseRepository.poses,
                        selectedPoseId = selectedPose?.id,
                        onPoseSelected = { pose ->
                            selectedPose = pose
                            overlayState = overlayState.reset()
                        }
                    )

                    Spacer(Modifier.height(20.dp))

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 40.dp)
                    ) {
                        if (uiState.hasFlashUnit) {
                            FlashToggleButton(
                                flashMode = uiState.flashMode,
                                onClick = viewModel::cycleFlashMode,
                                modifier = Modifier.align(Alignment.CenterStart)
                            )
                        }

                        ShutterButton(
                            isCapturing = uiState.isCapturing,
                            onClick = { viewModel.capturePhoto(context) },
                            modifier = Modifier.align(Alignment.Center)
                        )

                        CameraSwitchButton(
                            onClick = viewModel::toggleLensFacing,
                            enabled = uiState.hasFrontCamera && uiState.hasBackCamera,
                            modifier = Modifier.align(Alignment.CenterEnd)
                        )
                    }

                    Spacer(Modifier.height(24.dp))
                }
            }
        }
    }
}
