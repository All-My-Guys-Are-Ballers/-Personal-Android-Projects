package com.android.chatmeup.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Environment
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.InputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

const val MAX_FILE_SIZE = 500000

fun convertFileToByteArray(context: Context, uri: Uri): ByteArray {
    val inputStream: InputStream? = context.contentResolver.openInputStream(uri)
    val bitmap = BitmapFactory.decodeStream(inputStream)
    val byteArrayOutputStream = ByteArrayOutputStream()
    bitmap.compress(Bitmap.CompressFormat.JPEG, 50, byteArrayOutputStream)
    var quality = 100
    while (byteArrayOutputStream.toByteArray().size > MAX_FILE_SIZE && quality > 0) {
        byteArrayOutputStream.reset()
        quality -= 5
        bitmap.compress(Bitmap.CompressFormat.JPEG, quality, byteArrayOutputStream)
    }

    return byteArrayOutputStream.toByteArray()
}

fun createTempImageFile(context: Context): File {
    val timeStamp: String = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
    val storageDir: File? = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES)
    return File.createTempFile("JPEG_${timeStamp}_", ".jpg", storageDir)
}
