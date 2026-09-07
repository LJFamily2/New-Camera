package com.newcamera.app.util

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import java.io.File
import java.io.FileOutputStream

@RunWith(AndroidJUnit4::class)
class ImageCropUtilsInstrumentedTest {

    @Test
    fun cropToSquare_centerCropsARectangularJpegToASquare() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val file = File(context.cacheDir, "crop_test_${System.currentTimeMillis()}.jpg")

        val rectangular = Bitmap.createBitmap(400, 300, Bitmap.Config.ARGB_8888)
        FileOutputStream(file).use { rectangular.compress(Bitmap.CompressFormat.JPEG, 100, it) }
        rectangular.recycle()

        val uri = Uri.fromFile(file)
        ImageCropUtils.cropToSquare(context.contentResolver, uri)

        val cropped = BitmapFactory.decodeFile(file.absolutePath)
        assertEquals(300, cropped.width)
        assertEquals(300, cropped.height)
        cropped.recycle()

        file.delete()
    }
}
