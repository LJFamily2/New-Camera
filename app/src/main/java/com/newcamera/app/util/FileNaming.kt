package com.newcamera.app.util

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/** Pure filename-formatting logic, kept free of Android framework types so it is JVM-testable. */
object FileNaming {
    private const val PATTERN = "yyyyMMdd_HHmmss"

    fun buildJpegFileName(timestampMillis: Long): String {
        val formatter = SimpleDateFormat(PATTERN, Locale.US)
        return "IMG_${formatter.format(Date(timestampMillis))}.jpg"
    }
}
