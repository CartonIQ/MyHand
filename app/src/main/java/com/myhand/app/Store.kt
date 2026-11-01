package com.myhand.app

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import java.io.File

class Glyph(val bmp: Bitmap, val refH: Float, val baseOff: Float)

object Store {
    private lateinit var dir: File
    private val cache = HashMap<String, Glyph>()
    private val meta = HashMap<String, FloatArray>()

    fun init(ctx: Context) {
        dir = File(ctx.filesDir, "glyphs").apply { mkdirs() }
        loadMeta()
    }

    private fun metaFile() = File(dir, "meta.txt")

    @Synchronized
    private fun loadMeta() {
        meta.clear()
        val f = metaFile()
        if (f.exists()) {
            f.forEachLine { l ->
                val p = l.trim().split(" ")
                if (p.size == 3) {
                    try { meta[p[0]] = floatArrayOf(p[1].toFloat(), p[2].toFloat()) } catch (e: Exception) { }
                }
            }
        }
    }

    private fun saveMeta() {
        metaFile().writeText(meta.entries.joinToString("\n") { "${it.key} ${it.value[0]} ${it.value[1]}" })
    }

    fun dirFile(): File = dir

    @Synchronized
    fun reload() {
        cache.clear()
        loadMeta()
    }

    fun key(c: Char, v: Int) = "${c.code}_$v"

    @Synchronized
    fun save(c: Char, v: Int, bmp: Bitmap, refH: Float, baseOff: Float) {
        val k = key(c, v)
        File(dir, "$k.png").outputStream().use { bmp.compress(Bitmap.CompressFormat.PNG, 100, it) }
        meta[k] = floatArrayOf(refH, baseOff)
        saveMeta()
        cache.remove(k)
    }

    @Synchronized
    fun get(c: Char, v: Int): Glyph? {
        val k = key(c, v)
        cache[k]?.let { return it }
        val m = meta[k] ?: return null
        val f = File(dir, "$k.png")
        if (!f.exists()) return null
        val b = BitmapFactory.decodeFile(f.absolutePath) ?: return null
        val g = Glyph(b, m[0], m[1])
        cache[k] = g
        return g
    }

    fun variants(c: Char): List<Glyph> = (1..3).mapNotNull { get(c, it) }

    @Synchronized
    fun hasChar(c: Char): Boolean = (1..3).any { meta.containsKey(key(c, it)) }

    fun count(): Int = Chars.ALL.count { hasChar(it) }

    @Synchronized
    fun clear() {
        dir.listFiles()?.forEach { it.delete() }
        meta.clear()
        cache.clear()
    }
}
