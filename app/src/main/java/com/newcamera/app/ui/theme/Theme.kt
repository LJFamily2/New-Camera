package com.newcamera.app.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val CameraColorScheme = darkColorScheme(
    primary = CameraAccent,
    onPrimary = CameraBlack,
    background = CameraBlack,
    onBackground = CameraWhite,
    surface = CameraSurface,
    onSurface = CameraWhite
)

@Composable
fun NewCameraTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = CameraColorScheme,
        content = content
    )
}
