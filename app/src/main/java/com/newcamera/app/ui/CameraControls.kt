package com.newcamera.app.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FlashAuto
import androidx.compose.material.icons.filled.FlashOff
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.FlipCameraAndroid
import androidx.compose.material.icons.filled.Opacity
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.newcamera.app.R
import com.newcamera.app.camera.FlashMode
import com.newcamera.app.overlay.PoseGuide
import com.newcamera.app.overlay.PoseOverlayState

@Composable
fun ShutterButton(isCapturing: Boolean, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val shutterDescription = stringResource(id = R.string.content_desc_shutter)
    Box(
        modifier = modifier
            .size(72.dp)
            .clip(CircleShape)
            .background(Color.White.copy(alpha = 0.25f))
            .border(3.dp, Color.White, CircleShape)
            .semantics { contentDescription = shutterDescription }
            .clickable(enabled = !isCapturing, onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .size(58.dp)
                .clip(CircleShape)
                .background(if (isCapturing) Color.Gray else Color.White)
        )
    }
}

@Composable
fun FlashToggleButton(flashMode: FlashMode, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val icon = when (flashMode) {
        FlashMode.OFF -> Icons.Filled.FlashOff
        FlashMode.ON -> Icons.Filled.FlashOn
        FlashMode.AUTO -> Icons.Filled.FlashAuto
    }
    IconButton(onClick = onClick, modifier = modifier.size(48.dp)) {
        Icon(
            imageVector = icon,
            contentDescription = stringResource(id = R.string.content_desc_flash, flashMode.name),
            tint = Color.White
        )
    }
}

@Composable
fun CameraSwitchButton(onClick: () -> Unit, enabled: Boolean, modifier: Modifier = Modifier) {
    IconButton(onClick = onClick, enabled = enabled, modifier = modifier.size(48.dp)) {
        Icon(
            imageVector = Icons.Filled.FlipCameraAndroid,
            contentDescription = stringResource(id = R.string.content_desc_switch_camera),
            tint = if (enabled) Color.White else Color.White.copy(alpha = 0.3f)
        )
    }
}

@Composable
fun OpacitySlider(opacity: Float, onOpacityChange: (Float) -> Unit, modifier: Modifier = Modifier) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp)
    ) {
        Icon(
            imageVector = Icons.Filled.Opacity,
            contentDescription = stringResource(id = R.string.content_desc_opacity),
            tint = Color.White,
            modifier = Modifier.size(18.dp)
        )
        Slider(
            value = opacity,
            onValueChange = onOpacityChange,
            valueRange = PoseOverlayState.MIN_OPACITY..PoseOverlayState.MAX_OPACITY,
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 8.dp),
            colors = SliderDefaults.colors(
                thumbColor = Color.White,
                activeTrackColor = Color.White,
                inactiveTrackColor = Color.White.copy(alpha = 0.3f)
            )
        )
    }
}

@Composable
fun PoseSelectorRow(
    poses: List<PoseGuide>,
    selectedPoseId: String?,
    onPoseSelected: (PoseGuide?) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyRow(
        modifier = modifier.fillMaxWidth(),
        contentPadding = PaddingValues(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item(key = "none") {
            PoseThumbnail(
                label = stringResource(id = R.string.pose_none),
                iconRes = null,
                selected = selectedPoseId == null,
                onClick = { onPoseSelected(null) }
            )
        }
        items(poses, key = { it.id }) { pose ->
            PoseThumbnail(
                label = pose.displayName,
                iconRes = pose.iconRes,
                selected = selectedPoseId == pose.id,
                onClick = { onPoseSelected(pose) }
            )
        }
    }
}

@Composable
private fun PoseThumbnail(
    label: String,
    iconRes: Int?,
    selected: Boolean,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .width(56.dp)
            .clickable(onClick = onClick)
    ) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
                .background(Color.White.copy(alpha = if (selected) 0.35f else 0.15f))
                .border(
                    width = if (selected) 2.dp else 0.dp,
                    color = if (selected) Color.White else Color.Transparent,
                    shape = CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            if (iconRes != null) {
                Icon(
                    painter = painterResource(id = iconRes),
                    contentDescription = label,
                    tint = Color.White,
                    modifier = Modifier.size(28.dp)
                )
            } else {
                Icon(
                    imageVector = Icons.Filled.Close,
                    contentDescription = label,
                    tint = Color.White,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
        Spacer(Modifier.height(4.dp))
        Text(text = label, color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Medium, maxLines = 1)
    }
}
