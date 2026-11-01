package com.myhand.app

import android.content.Context
import android.net.Uri
import java.io.File
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream
import java.util.zip.ZipOutputStream

object BackupUtil {
    private val okName = Regex("^(\\d+_[123]\\.png|meta\\.txt|letters\\.json)$")

    fun create(ctx: Context): File {
        val out = File(ShareUtil.exportsDir(ctx), "MyHand_backup.zip")
        ZipOutputStream(out.outputStream().buffered()).use { z ->
            Store.dirFile().listFiles()?.forEach { f ->
                if (okName.matches(f.name)) {
                    z.putNextEntry(ZipEntry(f.name))
                    f.inputStream().use { it.copyTo(z) }
                    z.closeEntry()
                }
            }
            val lf = File(ctx.filesDir, "letters.json")
            if (lf.exists()) {
                z.putNextEntry(ZipEntry("letters.json"))
                lf.inputStream().use { it.copyTo(z) }
                z.closeEntry()
            }
        }
        return out
    }

    /** Returns a message. Replaces current handwriting and letters. */
    fun restore(ctx: Context, uri: Uri): String {
        val files = HashMap<String, ByteArray>()
        val input = ctx.contentResolver.openInputStream(uri) ?: return "Could not open that file."
        ZipInputStream(input.buffered()).use { z ->
            var e = z.nextEntry
            var n = 0
            while (e != null) {
                val name = File(e.name).name
                if (!e.isDirectory && okName.matches(name) && n < 400) {
                    val bytes = z.readBytes()
                    if (bytes.size <= 5_000_000) { files[name] = bytes; n++ }
                }
                z.closeEntry()
                e = z.nextEntry
            }
        }
        if (!files.containsKey("meta.txt")) return "This does not look like a MyHand backup."
        for (l in LetterStore.all(ctx)) Scheduler.cancel(ctx, l.id)
        Store.clear()
        val dir = Store.dirFile()
        var glyphs = 0
        for ((name, bytes) in files) {
            if (name == "letters.json") {
                File(ctx.filesDir, "letters.json").writeBytes(bytes)
            } else {
                File(dir, name).writeBytes(bytes)
                if (name.endsWith(".png")) glyphs++
            }
        }
        Store.reload()
        Scheduler.rescheduleAll(ctx)
        return "Restored $glyphs handwriting pieces" + (if (files.containsKey("letters.json")) " and your letters." else ".")
    }
}
