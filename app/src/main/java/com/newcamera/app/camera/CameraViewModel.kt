package com.newcamera.app.camera

import android.content.Context
import android.net.Uri
import android.view.OrientationEventListener
import android.view.Surface
import androidx.camera.core.Camera
import androidx.camera.core.CameraSelector
import androidx.camera.core.FocusMeteringAction
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.lifecycle.awaitInstance
import androidx.camera.view.PreviewView
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ViewModel
import com.newcamera.app.util.FileNaming
import com.newcamera.app.util.MediaStoreUtils
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import java.util.concurrent.TimeUnit

sealed interface CaptureResult {
    data class Success(val uri: Uri) : CaptureResult
    data class Error(val message: String) : CaptureResult
}

data class CameraUiState(
    val lensFacing: Int = CameraSelector.LENS_FACING_BACK,
    val flashMode: FlashMode = FlashMode.OFF,
    val isCapturing: Boolean = false,
    val hasFrontCamera: Boolean = true,
    val hasBackCamera: Boolean = true,
    val hasFlashUnit: Boolean = true,
    val lastCaptureResult: CaptureResult? = null
)

/**
 * Owns the CameraX use cases (Preview + ImageCapture) and all capture/flash/lens-facing state.
 * Deliberately holds no reference to the pose overlay's transform state - the overlay is purely
 * a Compose-canvas layer above the preview and is isolated from this ViewModel so gesture
 * updates never trigger a camera rebind or vice versa.
 */
class CameraViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(CameraUiState())
    val uiState: StateFlow<CameraUiState> = _uiState.asStateFlow()

    private var cameraProvider: ProcessCameraProvider? = null
    private var imageCapture: ImageCapture? = null
    private var camera: Camera? = null
    private var orientationEventListener: OrientationEventListener? = null

    suspend fun bindToLifecycle(
        context: Context,
        lifecycleOwner: LifecycleOwner,
        previewView: PreviewView
    ) {
        val provider = cameraProvider ?: ProcessCameraProvider.awaitInstance(context).also {
            cameraProvider = it
        }

        _uiState.update {
            it.copy(
                hasBackCamera = provider.hasCamera(CameraSelector.DEFAULT_BACK_CAMERA),
                hasFrontCamera = provider.hasCamera(CameraSelector.DEFAULT_FRONT_CAMERA)
            )
        }

        val preview = Preview.Builder().build().also {
            it.surfaceProvider = previewView.surfaceProvider
        }

        val capture = ImageCapture.Builder()
            .setFlashMode(_uiState.value.flashMode.toImageCaptureFlashMode())
            .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
            .build()
        imageCapture = capture

        val selector = CameraSelector.Builder()
            .requireLensFacing(_uiState.value.lensFacing)
            .build()

        provider.unbindAll()
        camera = provider.bindToLifecycle(lifecycleOwner, selector, preview, capture)

        _uiState.update { it.copy(hasFlashUnit = camera?.cameraInfo?.hasFlashUnit() ?: false) }

        startOrientationListener(context)
    }

    fun toggleLensFacing() {
        _uiState.update {
            val newFacing = if (it.lensFacing == CameraSelector.LENS_FACING_BACK) {
                CameraSelector.LENS_FACING_FRONT
            } else {
                CameraSelector.LENS_FACING_BACK
            }
            it.copy(lensFacing = newFacing)
        }
    }

    fun cycleFlashMode() {
        _uiState.update { current ->
            val next = current.flashMode.next()
            imageCapture?.flashMode = next.toImageCaptureFlashMode()
            current.copy(flashMode = next)
        }
    }

    fun focusAndMeterAt(previewView: PreviewView, x: Float, y: Float) {
        val activeCamera = camera ?: return
        val point = previewView.meteringPointFactory.createPoint(x, y)
        val action = FocusMeteringAction.Builder(point, FocusMeteringAction.FLAG_AF or FocusMeteringAction.FLAG_AE)
            .setAutoCancelDuration(3, TimeUnit.SECONDS)
            .build()
        activeCamera.cameraControl.startFocusAndMetering(action)
    }

    fun capturePhoto(context: Context) {
        val capture = imageCapture ?: return
        if (_uiState.value.isCapturing) return
        _uiState.update { it.copy(isCapturing = true) }

        val displayName = FileNaming.buildJpegFileName(System.currentTimeMillis())
        val contentValues = MediaStoreUtils.createImageContentValues(displayName)

        val outputOptions = ImageCapture.OutputFileOptions.Builder(
            context.contentResolver,
            android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
            contentValues
        ).build()

        capture.takePicture(
            outputOptions,
            ContextCompat.getMainExecutor(context),
            object : ImageCapture.OnImageSavedCallback {
                override fun onImageSaved(output: ImageCapture.OutputFileResults) {
                    val savedUri = output.savedUri
                    if (savedUri != null) {
                        MediaStoreUtils.markImageComplete(context, savedUri)
                    }
                    _uiState.update {
                        it.copy(
                            isCapturing = false,
                            lastCaptureResult = CaptureResult.Success(savedUri ?: Uri.EMPTY)
                        )
                    }
                }

                override fun onError(exception: ImageCaptureException) {
                    _uiState.update {
                        it.copy(
                            isCapturing = false,
                            lastCaptureResult = CaptureResult.Error(exception.message ?: "Capture failed")
                        )
                    }
                }
            }
        )
    }

    fun consumeCaptureResult() {
        _uiState.update { it.copy(lastCaptureResult = null) }
    }

    private fun startOrientationListener(context: Context) {
        if (orientationEventListener != null) return
        orientationEventListener = object : OrientationEventListener(context) {
            override fun onOrientationChanged(orientation: Int) {
                if (orientation == ORIENTATION_UNKNOWN) return
                val rotation = when {
                    orientation >= 315 || orientation < 45 -> Surface.ROTATION_0
                    orientation in 45 until 135 -> Surface.ROTATION_270
                    orientation in 135 until 225 -> Surface.ROTATION_180
                    else -> Surface.ROTATION_90
                }
                imageCapture?.targetRotation = rotation
            }
        }.also { it.enable() }
    }

    private fun stopOrientationListener() {
        orientationEventListener?.disable()
        orientationEventListener = null
    }

    fun releaseCamera() {
        stopOrientationListener()
        cameraProvider?.unbindAll()
        camera = null
        imageCapture = null
    }

    override fun onCleared() {
        super.onCleared()
        releaseCamera()
        cameraProvider = null
    }
}
