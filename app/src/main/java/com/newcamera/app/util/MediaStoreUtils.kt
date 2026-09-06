package com.newcamera.app.util

import android.content.ContentValues
import android.content.Context
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import java.io.File

/**
 * Builds the MediaStore entries needed to save a JPEG into DCIM/Camera, bridging the
 * scoped-storage API (Q+, via RELATIVE_PATH) and the legacy API (26-28, via the DATA column)
 * since this app's minSdk is 26.
 */
object MediaStoreUtils {

    fun createImageContentValues(displayName: String): ContentValues = ContentValues().apply {
        put(MediaStore.MediaColumns.DISPLAY_NAME, displayName)
        put(MediaStore.MediaColumns.MIME_TYPE, "image/jpeg")
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            put(MediaStore.MediaColumns.RELATIVE_PATH, "${Environment.DIRECTORY_DCIM}/Camera")
            put(MediaStore.MediaColumns.IS_PENDING, 1)
        } else {
            val dcimCameraDir = File(
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM),
                "Camera"
            )
            if (!dcimCameraDir.exists()) {
                dcimCameraDir.mkdirs()
            }
            put(MediaStore.MediaColumns.DATA, File(dcimCameraDir, displayName).absolutePath)
        }
    }

    fun markImageComplete(context: Context, uri: Uri) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val values = ContentValues().apply { put(MediaStore.MediaColumns.IS_PENDING, 0) }
            context.contentResolver.update(uri, values, null, null)
        }
    }
}
