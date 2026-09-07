package com.newcamera.app.camera

import androidx.camera.core.AspectRatio

enum class CaptureAspectRatio(val displayLabel: String, val widthToHeightRatio: Float?) {
    FULL("Full", null),
    RATIO_4_3("4:3", 3f / 4f),
    RATIO_1_1("1:1", 1f);

    fun next(): CaptureAspectRatio = when (this) {
        FULL -> RATIO_4_3
        RATIO_4_3 -> RATIO_1_1
        RATIO_1_1 -> FULL
    }

    /** CameraX has no native square capture mode, so 1:1 captures at 4:3 and is center-cropped after saving. */
    fun toCameraXAspectRatio(): Int? = when (this) {
        FULL -> null
        RATIO_4_3, RATIO_1_1 -> AspectRatio.RATIO_4_3
    }

    val requiresSquareCrop: Boolean get() = this == RATIO_1_1
}
