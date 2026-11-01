package com.myhand.app

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.PorterDuff
import android.graphics.PorterDuffColorFilter
import android.graphics.RectF
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.sin

object StickerMaker {
    const val SIZE = 512
    val styleNames = listOf("Clear", "White outline", "Pink bubble", "Lavender bubble", "Yellow bubble")
    private val bubble = intArrayOf(0, 0, 0xFFFFD6E3.toInt(), 0xFFE7DBFF.toInt(), 0xFFFFF3B0.toInt())

    fun make(text: String, ink: Int, style: Int): Bitmap? {
        if (text.isBlank()) return null
        val o = Renderer.Opts(text = text.trim(), paper = -1, ink = ink, size = 60f,
            spacing = 0.5f, slant = 0f, realism = 0.35f, watermark = false)
        val page = Bitmap.createBitmap(Renderer.PW, Renderer.PH, Bitmap.Config.ARGB_8888)
        Renderer.draw(Canvas(page), o)
        val t = GlyphUtil.trim(page, 0f, 20)
        page.recycle()
        if (t == null) return null
        val src = t.first
        val pad = 44
        val box = (SIZE - 2 * pad).toFloat()
        val sc = min(min(box / src.width, box / src.height), 3f)
        val w = src.width * sc
        val h = src.height * sc
        val left = (SIZE - w) / 2f
        val top = (SIZE - h) / 2f
        val dst = RectF(left, top, left + w, top + h)
        val out = Bitmap.createBitmap(SIZE, SIZE, Bitmap.Config.ARGB_8888)
        val c = Canvas(out)
        val paint = Paint(Paint.ANTI_ALIAS_FLAG or Paint.FILTER_BITMAP_FLAG)
        if (style >= 2) {
            val bp = Paint(Paint.ANTI_ALIAS_FLAG)
            bp.color = bubble[style]
            val m = 26f
            c.drawRoundRect(RectF(left - m, top - m, left + w + m, top + h + m), 56f, 56f, bp)
        } else if (style == 1) {
            val wp = Paint(Paint.ANTI_ALIAS_FLAG or Paint.FILTER_BITMAP_FLAG)
            wp.colorFilter = PorterDuffColorFilter(0xFFFFFFFF.toInt(), PorterDuff.Mode.SRC_IN)
            for (ring in 1..2) {
                val r = ring * 6f
                for (k in 0 until 16) {
                    val a = k * (Math.PI * 2 / 16)
                    val dx = (cos(a) * r).toFloat()
                    val dy = (sin(a) * r).toFloat()
                    c.drawBitmap(src, null, RectF(dst.left + dx, dst.top + dy, dst.right + dx, dst.bottom + dy), wp)
                }
            }
        }
        c.drawBitmap(src, null, dst, paint)
        return out
    }
}
