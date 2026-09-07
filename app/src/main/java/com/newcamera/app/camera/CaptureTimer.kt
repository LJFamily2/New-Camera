package com.newcamera.app.camera

enum class CaptureTimer(val seconds: Int) {
    OFF(0), THREE(3), TEN(10);

    fun next(): CaptureTimer = when (this) {
        OFF -> THREE
        THREE -> TEN
        TEN -> OFF
    }
}
