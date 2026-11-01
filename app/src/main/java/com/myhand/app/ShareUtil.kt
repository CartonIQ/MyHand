package com.myhand.app

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import androidx.core.content.FileProvider
import java.io.File

object ShareUtil {
    fun exportsDir(ctx: Context): File = File(ctx.cacheDir, "exports").apply { mkdirs() }

    fun savePng(ctx: Context, bmp: Bitmap, name: String): File {
        val f = File(exportsDir(ctx), name)
        f.outputStream().use { bmp.compress(Bitmap.CompressFormat.PNG, 100, it) }
        return f
    }

    fun shareFile(a: Activity, f: File, mime: String, chooserTitle: String) {
        try {
            val uri = FileProvider.getUriForFile(a, "${a.packageName}.fileprovider", f)
            val i = Intent(Intent.ACTION_SEND).apply {
                type = mime
                putExtra(Intent.EXTRA_STREAM, uri)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            a.startActivity(Intent.createChooser(i, chooserTitle))
        } catch (e: Exception) {
            a.toast("Could not share: ${e.message}")
        }
    }
}
