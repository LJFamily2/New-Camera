package com.newcamera.app.util

import android.Manifest
import android.os.Build
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class PermissionUtilsTest {

    @Test
    fun `pre-Q devices also require WRITE_EXTERNAL_STORAGE`() {
        val permissions = PermissionUtils.requiredPermissions(sdkInt = Build.VERSION_CODES.P)
        assertTrue(permissions.contains(Manifest.permission.CAMERA))
        assertTrue(permissions.contains(Manifest.permission.WRITE_EXTERNAL_STORAGE))
    }

    @Test
    fun `Q and later only require CAMERA`() {
        val permissions = PermissionUtils.requiredPermissions(sdkInt = Build.VERSION_CODES.Q)
        assertTrue(permissions.contains(Manifest.permission.CAMERA))
        assertFalse(permissions.contains(Manifest.permission.WRITE_EXTERNAL_STORAGE))
    }
}
