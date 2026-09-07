package com.newcamera.app.ui

import android.view.MotionEvent
import android.view.ScaleGestureDetector
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
import com.newcamera.app.overlay.PoseCategory
import com.newcamera.app.overlay.PoseGuide
import com.newcamera.app.overlay.PoseOverlay
import com.newcamera.app.overlay.PoseOverlayState
import com.newcamera.app.overlay.PoseRepository
import kotlinx.coroutines.delay

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

    // Aspect ratio changes require rebuilding ImageCapture with a new target ratio, so a rebind
    // is keyed on it too, alongside lens facing.
    LaunchedEffect(uiState.lensFacing, uiState.captureAspectRatio) {
        viewModel.bindToLifecycle(context, lifecycleOwner, previewView)
    }

    DisposableEffect(Unit) {
        onDispose { viewModel.releaseCamera() }
    }

    // Pose overlay transform state lives here, isolated from CameraUiState, so dragging or
    // zooming the guide never triggers a camera rebind and switching cameras never resets it.
    var selectedPose by remember { mutableStateOf<PoseGuide?>(null) }
    var overlayState by remember { mutableStateOf(PoseOverlayState()) }
    var selectedCategory by remember { mutableStateOf<PoseCategory?>(null) }

    // Self-timer countdown is transient UI state, not camera state - it doesn't need to
    // survive process death and never touches the ViewModel until it fires the capture.
    var countdownSeconds by remember { mutableStateOf<Int?>(null) }
    LaunchedEffect(countdownSeconds) {
        val remaining = countdownSeconds ?: return@LaunchedEffect
        if (remaining <= 0) {
            countdownSeconds = null
            viewModel.capturePhoto(context)
        } else {
            delay(1000)
            countdownSeconds = remaining - 1
        }
    }

    val scaleGestureDetector = remember {
        ScaleGestureDetector(
            context,
            object : ScaleGestureDetector.SimpleOnScaleGestureListener() {
                override fun onScale(detector: ScaleGestureDetector): Boolean {
                    val current = viewModel.uiState.value
                    viewModel.setZoomRatio(current.zoomRatio * detector.scaleFactor)
                    return true
                }
            }
        )
    }

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
                        scaleGestureDetector.onTouchEvent(event)
                        if (event.action == MotionEvent.ACTION_UP && !scaleGestureDetector.isInProgress) {
                            viewModel.focusAndMeterAt(this, event.x, event.y)
                            view.performClick()
                        }
                        true
                    }
                }
            },
            modifier = Modifier.fillMaxSize()
        )

        if (uiState.isGridEnabled) {
            GridOverlay(modifier = Modifier.fillMaxSize())
        }

        AspectRatioMask(
            widthToHeightRatio = uiState.captureAspectRatio.widthToHeightRatio,
            modifier = Modifier.fillMaxSize()
        )

        PoseOverlay(
            pose = selectedPose,
            state = overlayState,
            onStateChange = { overlayState = it },
            modifier = Modifier.fillMaxSize()
        )

        TimerCountdownOverlay(secondsRemaining = countdownSeconds, modifier = Modifier.fillMaxSize())

        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 16.dp)
        )

        TopOptionsBar(
            isGridEnabled = uiState.isGridEnabled,
            onToggleGrid = viewModel::toggleGrid,
            timer = uiState.timer,
            onCycleTimer = viewModel::cycleTimer,
            aspectRatio = uiState.captureAspectRatio,
            onCycleAspectRatio = viewModel::cycleAspectRatio,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(top = 16.dp, end = 16.dp)
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
                    PoseCategoryChipRow(
                        selectedCategory = selectedCategory,
                        onCategorySelected = { selectedCategory = it }
                    )

                    Spacer(Modifier.height(8.dp))

                    PoseSelectorRow(
                        poses = PoseRepository.byCategory(selectedCategory),
                        selectedPoseId = selectedPose?.id,
                        onPoseSelected = { pose ->
                            selectedPose = pose
                            overlayState = overlayState.reset()
                        }
                    )

                    Spacer(Modifier.height(16.dp))

                    Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                        ZoomPillRow(
                            currentZoomRatio = uiState.zoomRatio,
                            maxZoomRatio = uiState.maxZoomRatio,
                            onZoomSelected = viewModel::setZoomRatio
                        )
                    }

                    Spacer(Modifier.height(12.dp))

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
                            isCapturing = uiState.isCapturing || countdownSeconds != null,
                            onClick = {
                                if (uiState.timer.seconds == 0) {
                                    viewModel.capturePhoto(context)
                                } else {
                                    countdownSeconds = uiState.timer.seconds
                                }
                            },
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
