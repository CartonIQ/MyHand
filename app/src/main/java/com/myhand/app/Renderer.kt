package com.myhand.app

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.PorterDuff
import android.graphics.PorterDuffColorFilter
import android.graphics.RadialGradient
import android.graphics.RectF
import android.graphics.Shader
import android.graphics.Typeface
import kotlin.math.sin

object Renderer {
    const val PW = 1080
    const val PH = 1527
    val paperNames = listOf("Plain", "Ruled notebook", "Graph paper", "Parchment", "Sticky note")
    val inkNames = listOf("Blue ball pen", "Black gel pen", "Pencil", "Fountain pen")
    private val inkColors = intArrayOf(0xFF1A3FA0.toInt(), 0xFF111111.toInt(), 0xDD4A4A4A.toInt(), 0xFF0B1F4F.toInt())

    class Opts(
        var text: String = "",
        var paper: Int = 1,
        var ink: Int = 0,
        var size: Float = 36f,
        var spacing: Float = 0.5f,
        var slant: Float = 0f,
        var realism: Float = 0.5f,
        var watermark: Boolean = true
    ) {
        fun copyWith(watermark: Boolean) = Opts(text, paper, ink, size, spacing, slant, realism, watermark)
    }

    private class Item(val ch: Char, val i: Int)

    private fun noise(x: Float): Float = (sin(x) + 0.5f * sin(2.3f * x + 1f)) / 1.5f

    private fun pick(i: Int, n: Int): Int {
        if (n <= 1) return 0
        val h = (i * 1103515245 + 12345) and 0x7fffffff
        return (h / 65536) % n
    }

    /** Draws the page in logical coordinates (PW x PH). Returns true if the text did not fit. */
    fun draw(c: Canvas, o: Opts): Boolean {
        val cellPx = o.size * 2.4f
        val lineH = cellPx * 0.85f
        val marginX = 100f
        val top = 120f
        drawPaper(c, o.paper, lineH, top)

        val inkColor = inkColors[o.ink.coerceIn(0, inkColors.size - 1)]
        val paint = Paint(Paint.ANTI_ALIAS_FLAG or Paint.FILTER_BITMAP_FLAG)
        paint.colorFilter = PorterDuffColorFilter(inkColor, PorterDuff.Mode.SRC_IN)
        val fb = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = inkColor
            typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.ITALIC)
            textSize = cellPx * 0.4f
        }

        val gapBase = cellPx * (0.02f + 0.06f * o.spacing)
        val spaceW = cellPx * (0.18f + 0.12f * o.spacing)
        val maxX = PW - marginX

        fun adv(ch: Char, i: Int): Float {
            if (ch == ' ') return spaceW
            val vs = Store.variants(ch)
            if (vs.isEmpty()) return fb.measureText(ch.toString()) + gapBase
            val g = vs[pick(i, vs.size)]
            return g.bmp.width * (cellPx / g.refH) + gapBase
        }

        val lines = ArrayList<ArrayList<Item>>()
        var cur = ArrayList<Item>()
        var x = marginX
        val word = ArrayList<Item>()
        fun flush() {
            if (word.isEmpty()) return
            var ww = 0f
            for (w in word) ww += adv(w.ch, w.i)
            if (x + ww > maxX && cur.isNotEmpty()) {
                lines.add(cur); cur = ArrayList(); x = marginX
            }
            cur.addAll(word)
            x += ww
            word.clear()
        }
        for ((i, ch) in o.text.withIndex()) {
            when (ch) {
                '\n' -> { flush(); lines.add(cur); cur = ArrayList(); x = marginX }
                ' ' -> { flush(); if (cur.isNotEmpty()) { cur.add(Item(' ', i)); x += spaceW } }
                else -> word.add(Item(ch, i))
            }
        }
        flush()
        lines.add(cur)

        var overflow = false
        val rr = o.realism
        for ((li, line) in lines.withIndex()) {
            val baseY0 = top + (li + 1) * lineH
            if (baseY0 > PH - 90f) { overflow = true; break }
            val slope = noise(li * 3.7f) * cellPx * 0.06f * rr
            var px = marginX
            for (item in line) {
                val i = item.i
                val ch = item.ch
                if (ch == ' ') { px += spaceW; continue }
                val r = java.util.Random(i * 7919L + 13L)
                val dy = (noise(i * 0.37f) * 0.6f + (r.nextFloat() - 0.5f) * 0.6f) * cellPx * 0.05f * rr +
                    slope * ((px - marginX) / (maxX - marginX))
                val rot = (r.nextFloat() - 0.5f) * 2f * 3f * rr
                val sc = 1f + (r.nextFloat() - 0.5f) * 0.12f * rr
                val vs = Store.variants(ch)
                if (vs.isEmpty()) {
                    c.drawText(ch.toString(), px, baseY0 + dy, fb)
                    px += fb.measureText(ch.toString()) + gapBase
                    continue
                }
                val g = vs[pick(i, vs.size)]
                val s = cellPx / g.refH * sc
                val gw = g.bmp.width * s
                val gh = g.bmp.height * s
                val bottom = baseY0 + g.baseOff * s + dy
                c.save()
                c.translate(px + gw / 2f, bottom - gh / 2f)
                c.rotate(rot)
                c.skew(-o.slant, 0f)
                c.drawBitmap(g.bmp, null, RectF(-gw / 2f, -gh / 2f, gw / 2f, gh / 2f), paint)
                c.restore()
                px += g.bmp.width * (cellPx / g.refH) + gapBase
            }
        }

        if (o.watermark) {
            val wp = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = 0x88666666.toInt(); textSize = 28f; textAlign = Paint.Align.RIGHT
            }
            c.drawText("Made with MyHand", PW - 40f, PH - 36f, wp)
        }
        return overflow
    }

    private fun drawPaper(c: Canvas, type: Int, lineH: Float, top: Float) {
        val p = Paint(Paint.ANTI_ALIAS_FLAG)
        val w = PW.toFloat()
        val h = PH.toFloat()
        when (type) {
            -1 -> { }
            0 -> c.drawColor(0xFFFBF7EF.toInt())
            1 -> {
                c.drawColor(0xFFFFFEF8.toInt())
                p.color = 0xFFB9C9E3.toInt(); p.strokeWidth = 2f
                var y = top + lineH
                while (y < h - 60f) { c.drawLine(0f, y, w, y, p); y += lineH }
                p.color = 0xFFE9A3A3.toInt()
                c.drawLine(80f, 0f, 80f, h, p)
            }
            2 -> {
                c.drawColor(Color.WHITE)
                p.color = 0xFFCFE0EE.toInt(); p.strokeWidth = 1.5f
                var x = 0f
                while (x <= w) { c.drawLine(x, 0f, x, h, p); x += 40f }
                var y = 0f
                while (y <= h) { c.drawLine(0f, y, w, y, p); y += 40f }
            }
            3 -> {
                c.drawColor(0xFFF0DFB8.toInt())
                p.shader = RadialGradient(w / 2f, h / 2f, h * 0.75f, 0x00C9A66B, 0xAA8B6B3A.toInt(), Shader.TileMode.CLAMP)
                c.drawRect(0f, 0f, w, h, p)
            }
            else -> {
                c.drawColor(0xFFFFF176.toInt())
                p.color = 0xFFFFEB3B.toInt()
                c.drawRect(0f, 0f, w, 70f, p)
            }
        }
    }
}
