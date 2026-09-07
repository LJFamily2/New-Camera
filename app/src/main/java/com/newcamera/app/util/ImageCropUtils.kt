package com.newcamera.app.util

import android.content.ContentResolver
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import java.io.FileNotFoundException

/**
 * CameraX has no native square-capture mode, so 1:1 aspect ratio is achieved by capturing at
 * 4:3 and cropping the saved JPEG to a centered square afterwards.
 */
object ImageCropUtils {

    fun cropToSquare(contentResolver: ContentResolver, uri: Uri) {
        val original = contentResolver.openInputStream(uri)?.use { input ->
            BitmapFactory.decodeStream(input)
        } ?: throw FileNotFoundException("Could not open $uri for reading")

        val side = minOf(original.width, original.height)
        val left = (original.width - side) / 2
        val top = (original.height - side) / 2
        val cropped = Bitmap.createBitmap(original, left, top, side, side)

        val output = contentResolver.openOutputStream(uri, "wt")
            ?: throw FileNotFoundException("Could not open $uri for writing")
        output.use { cropped.compress(Bitmap.CompressFormat.JPEG, 95, it) }

        if (cropped !== original) {
            original.recycle()
        }
        cropped.recycle()
    }
}
