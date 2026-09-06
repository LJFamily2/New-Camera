package com.newcamera.app.util

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.content.ContextCompat

object PermissionUtils {

    /**
     * WRITE_EXTERNAL_STORAGE is only required up to API 28: from API 29 (Q) onward, scoped
     * storage lets apps insert into MediaStore's DCIM/Camera collection without it.
     */
    fun requiredPermissions(sdkInt: Int = Build.VERSION.SDK_INT): List<String> = buildList {
        add(Manifest.permission.CAMERA)
        if (sdkInt <= Build.VERSION_CODES.P) {
            add(Manifest.permission.WRITE_EXTERNAL_STORAGE)
        }
    }

    fun hasRequiredPermissions(context: Context): Boolean =
        requiredPermissions().all {
            ContextCompat.checkSelfPermission(context, it) == PackageManager.PERMISSION_GRANTED
        }
}
