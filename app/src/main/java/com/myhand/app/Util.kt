package com.myhand.app

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.media.ExifInterface
import android.net.Uri
import android.widget.Toast
import kotlin.math.max

fun Context.dp(v: Int): Int = (v * resources.displayMetrics.density).toInt()

fun Context.toast(msg: String) {
    Toast.makeText(this, msg, Toast.LENGTH_LONG).show()
}

fun loadBitmap(ctx: Context, uri: Uri, maxW: Int): Bitmap? {
    val cr = ctx.contentResolver
    val o = BitmapFactory.Options().apply { inJustDecodeBounds = true }
    cr.openInputStream(uri)?.use { BitmapFactory.decodeStream(it, null, o) }
    if (o.outWidth <= 0 || o.outHeight <= 0) return null
    val side = max(o.outWidth, o.outHeight)
    var ss = 1
    while (side / (ss * 2) >= 2000) ss *= 2
    val o2 = BitmapFactory.Options().apply { inSampleSize = ss }
    var bmp = cr.openInputStream(uri)?.use { BitmapFactory.decodeStream(it, null, o2) } ?: return null
    val rot = try {
        cr.openInputStream(uri)?.use { s ->
            when (ExifInterface(s).getAttributeInt(ExifInterface.TAG_ORIENTATION, 1)) {
                6 -> 90
                3 -> 180
                8 -> 270
                else -> 0
            }
        } ?: 0
    } catch (e: Exception) { 0 }
    if (rot != 0) {
        val m = Matrix().apply { postRotate(rot.toFloat()) }
        bmp = Bitmap.createBitmap(bmp, 0, 0, bmp.width, bmp.height, m, true)
    }
    if (bmp.width > maxW) {
        bmp = Bitmap.createScaledBitmap(bmp, maxW, bmp.height * maxW / bmp.width, true)
    }
    return bmp
}
