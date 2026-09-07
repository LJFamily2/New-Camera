package com.newcamera.app.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/** Rule-of-thirds composition guide drawn directly on the preview; purely visual, never captured. */
@Composable
fun GridOverlay(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier.fillMaxSize()) {
        val lineColor = Color.White.copy(alpha = 0.45f)
        val strokeWidth = 1.dp.toPx()
        val thirdWidth = size.width / 3f
        val thirdHeight = size.height / 3f

        drawLine(lineColor, Offset(thirdWidth, 0f), Offset(thirdWidth, size.height), strokeWidth)
        drawLine(lineColor, Offset(thirdWidth * 2, 0f), Offset(thirdWidth * 2, size.height), strokeWidth)
        drawLine(lineColor, Offset(0f, thirdHeight), Offset(size.width, thirdHeight), strokeWidth)
        drawLine(lineColor, Offset(0f, thirdHeight * 2), Offset(size.width, thirdHeight * 2), strokeWidth)
    }
}

/**
 * Dims the parts of the viewfinder that fall outside [widthToHeightRatio], previewing what the
 * saved photo will actually keep. `null` means "Full" - no cropping, so nothing is drawn.
 */
@Composable
fun AspectRatioMask(widthToHeightRatio: Float?, modifier: Modifier = Modifier) {
    if (widthToHeightRatio == null) return

    Canvas(modifier = modifier.fillMaxSize()) {
        val maskColor = Color.Black.copy(alpha = 0.55f)
        val containerRatio = size.width / size.height

        if (containerRatio > widthToHeightRatio) {
            val targetWidth = size.height * widthToHeightRatio
            val barWidth = (size.width - targetWidth) / 2f
            drawRect(color = maskColor, topLeft = Offset.Zero, size = Size(barWidth, size.height))
            drawRect(
                color = maskColor,
                topLeft = Offset(size.width - barWidth, 0f),
                size = Size(barWidth, size.height)
            )
        } else {
            val targetHeight = size.width / widthToHeightRatio
            val barHeight = (size.height - targetHeight) / 2f
            drawRect(color = maskColor, topLeft = Offset.Zero, size = Size(size.width, barHeight))
            drawRect(
                color = maskColor,
                topLeft = Offset(0f, size.height - barHeight),
                size = Size(size.width, barHeight)
            )
        }
    }
}

@Composable
fun TimerCountdownOverlay(secondsRemaining: Int?, modifier: Modifier = Modifier) {
    if (secondsRemaining == null || secondsRemaining <= 0) return
    Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text(
            text = secondsRemaining.toString(),
            color = Color.White,
            fontSize = 96.sp,
            fontWeight = FontWeight.Bold
        )
    }
}
