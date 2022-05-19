package com.freezer.chatapp.utils

import android.content.ContentResolver
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import kotlin.math.roundToInt
import kotlin.math.sqrt


fun Bitmap.reduceBitmapSize(maxSize: Int = 307200): Bitmap? {
    val bitmapHeight: Int = this.height
    val bitmapWidth: Int = this.width
    val ratioSquare = (bitmapHeight * bitmapWidth / maxSize).toDouble()
    if (ratioSquare <= 1) return this
    val ratio = sqrt(ratioSquare)
    val requiredHeight = (bitmapHeight / ratio).roundToInt()
    val requiredWidth = (bitmapWidth / ratio).roundToInt()
    return Bitmap.createScaledBitmap(this, requiredWidth, requiredHeight, true)
}

fun Uri.getBitmap(contentResolver: ContentResolver): Bitmap? {
    return try {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            ImageDecoder.decodeBitmap(ImageDecoder.createSource(contentResolver, this))
        } else {
            MediaStore.Images.Media.getBitmap(contentResolver, this)
        }
    } catch (e: Exception){
        null
    }
}