package com.newcamera.app.util

import android.os.Build
import android.provider.MediaStore
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class MediaStoreUtilsInstrumentedTest {

    @Test
    fun createImageContentValues_setsDisplayNameAndMimeType() {
        val values = MediaStoreUtils.createImageContentValues("IMG_20240101_120000.jpg")

        assertEquals(
            "IMG_20240101_120000.jpg",
            values.getAsString(MediaStore.MediaColumns.DISPLAY_NAME)
        )
        assertEquals("image/jpeg", values.getAsString(MediaStore.MediaColumns.MIME_TYPE))
    }

    @Test
    fun createImageContentValues_targetsDcimCameraOnModernAndroid() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) return

        val values = MediaStoreUtils.createImageContentValues("IMG_test.jpg")

        val relativePath = values.getAsString(MediaStore.MediaColumns.RELATIVE_PATH)
        assertTrue(relativePath != null && relativePath.contains("DCIM"))
        assertEquals(1, values.getAsInteger(MediaStore.MediaColumns.IS_PENDING))
    }
}
