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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AspectRatio
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FlashAuto
import androidx.compose.material.icons.filled.FlashOff
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.FlipCameraAndroid
import androidx.compose.material.icons.filled.GridOff
import androidx.compose.material.icons.filled.GridOn
import androidx.compose.material.icons.filled.Opacity
import androidx.compose.material.icons.filled.Timer10
import androidx.compose.material.icons.filled.Timer3
import androidx.compose.material.icons.filled.TimerOff
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.newcamera.app.R
import com.newcamera.app.camera.CaptureAspectRatio
import com.newcamera.app.camera.CaptureTimer
import com.newcamera.app.camera.FlashMode
import com.newcamera.app.overlay.PoseCategory
import com.newcamera.app.overlay.PoseGuide
import com.newcamera.app.overlay.PoseOverlayState
import kotlin.math.abs

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
fun PoseCategoryChipRow(
    selectedCategory: PoseCategory?,
    onCategorySelected: (PoseCategory?) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyRow(
        modifier = modifier.fillMaxWidth(),
        contentPadding = PaddingValues(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        item(key = "all") {
            CategoryChip(
                label = stringResource(id = R.string.pose_category_all),
                selected = selectedCategory == null,
                onClick = { onCategorySelected(null) }
            )
        }
        items(PoseCategory.entries, key = { it.name }) { category ->
            CategoryChip(
                label = stringResource(id = category.labelRes),
                selected = selectedCategory == category,
                onClick = { onCategorySelected(category) }
            )
        }
    }
}

@Composable
private fun CategoryChip(label: String, selected: Boolean, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(16.dp))
            .background(if (selected) Color.White.copy(alpha = 0.9f) else Color.White.copy(alpha = 0.15f))
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 6.dp)
    ) {
        Text(
            text = label,
            color = if (selected) Color.Black else Color.White,
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium
        )
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

@Composable
fun TopOptionsBar(
    isGridEnabled: Boolean,
    onToggleGrid: () -> Unit,
    timer: CaptureTimer,
    onCycleTimer: () -> Unit,
    aspectRatio: CaptureAspectRatio,
    onCycleAspectRatio: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(24.dp))
            .background(Color.Black.copy(alpha = 0.35f))
            .padding(horizontal = 8.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        IconButton(onClick = onToggleGrid, modifier = Modifier.size(40.dp)) {
            Icon(
                imageVector = if (isGridEnabled) Icons.Filled.GridOn else Icons.Filled.GridOff,
                contentDescription = stringResource(id = R.string.content_desc_grid),
                tint = Color.White
            )
        }
        TopOptionChip(
            icon = when (timer) {
                CaptureTimer.OFF -> Icons.Filled.TimerOff
                CaptureTimer.THREE -> Icons.Filled.Timer3
                CaptureTimer.TEN -> Icons.Filled.Timer10
            },
            label = if (timer == CaptureTimer.OFF) null else "${timer.seconds}s",
            description = stringResource(id = R.string.content_desc_timer, timer.seconds),
            onClick = onCycleTimer
        )
        TopOptionChip(
            icon = Icons.Filled.AspectRatio,
            label = aspectRatio.displayLabel,
            description = stringResource(id = R.string.content_desc_aspect_ratio, aspectRatio.displayLabel),
            onClick = onCycleAspectRatio
        )
    }
}

@Composable
private fun TopOptionChip(
    icon: ImageVector,
    label: String?,
    description: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(16.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 8.dp, vertical = 6.dp)
            .semantics(mergeDescendants = true) { contentDescription = description },
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(imageVector = icon, contentDescription = null, tint = Color.White, modifier = Modifier.size(20.dp))
        if (label != null) {
            Spacer(Modifier.width(4.dp))
            Text(text = label, color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Medium)
        }
    }
}

@Composable
fun ZoomPillRow(
    currentZoomRatio: Float,
    maxZoomRatio: Float,
    onZoomSelected: (Float) -> Unit,
    modifier: Modifier = Modifier
) {
    val levels = remember(maxZoomRatio) {
        buildList {
            add(1f)
            if (maxZoomRatio >= 2f) add(2f)
            if (maxZoomRatio >= 3f) add(3f)
        }
    }
    if (levels.size <= 1) return

    Row(
        modifier = modifier
            .clip(RoundedCornerShape(20.dp))
            .background(Color.Black.copy(alpha = 0.35f))
            .padding(horizontal = 6.dp, vertical = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        levels.forEach { level ->
            val isSelected = abs(currentZoomRatio - level) < 0.15f
            Box(
                modifier = Modifier
                    .clip(CircleShape)
                    .background(if (isSelected) Color.White else Color.Transparent)
                    .clickable { onZoomSelected(level) }
                    .padding(horizontal = 10.dp, vertical = 6.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "${level.toInt()}x",
                    color = if (isSelected) Color.Black else Color.White,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}
