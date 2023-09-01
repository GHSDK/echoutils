package com.echo.utils

import android.app.Activity
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.ImageDecoder
import android.graphics.ImageFormat
import android.graphics.Rect
import android.graphics.YuvImage
import android.media.Image
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import java.io.ByteArrayOutputStream

/**
 *
 * 相机Image转化为Bitmap
 * */
fun Image.toBitmap(): Bitmap {
    if (planes.size == 1) {
        val buffer = planes[0].buffer
        val bytes = ByteArray(buffer.capacity())
        buffer[bytes]
        return BitmapFactory.decodeByteArray(bytes, 0, bytes.size, null)
    }
    val yBuffer = planes[0].buffer // Y
    val vuBuffer = planes[2].buffer // VU
    val ySize = yBuffer.remaining()
    val vuSize = vuBuffer.remaining()
    val nv21 = ByteArray(ySize + vuSize)
    yBuffer.get(nv21, 0, ySize)
    vuBuffer.get(nv21, ySize, vuSize)
    val yuvImage = YuvImage(nv21, ImageFormat.NV21, this.width, this.height, null)
    val out = ByteArrayOutputStream()
    yuvImage.compressToJpeg(Rect(0, 0, yuvImage.width, yuvImage.height), 50, out)
    val imageBytes = out.toByteArray()
    return BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
}

fun Uri.toBitmap(): Bitmap {
    return if (Build.VERSION.SDK_INT < 28) {
        MediaStore.Images.Media.getBitmap(
            EchoUtils.getApplicationContext().contentResolver,
            this
        )
    } else {
        val source =
            ImageDecoder.createSource(EchoUtils.getApplicationContext().contentResolver, this)
        ImageDecoder.decodeBitmap(source)
    }
}

suspend fun Bitmap.save(
    activity: Activity,
    noGranted: ((Boolean) -> Unit)? = null,
    name: String? = null,
): String? {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
        return SystemUtils.saveImageToGallery(activity, this, name)
    }
    val ans = FileUtils.askForWriteExternalStorage(activity)
    if (ans.first) {
        return SystemUtils.saveImageToGallery(activity, this, name)
    } else {
        noGranted?.invoke(ans.second)
    }
    return null
}