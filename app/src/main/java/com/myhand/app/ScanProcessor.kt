package com.myhand.app

import android.graphics.Bitmap
import android.graphics.Matrix
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min

object ScanProcessor {
    class Result(val saved: Int, val missing: List<Char>, val error: String?)

    fun process(src: Bitmap, variant: Int): Result {
        val cands = ArrayList<Bitmap>()
        cands.add(src)
        if (src.width > src.height) {
            cands.add(rotate(src, 90f))
            cands.add(rotate(src, 270f))
        }
        var lastErr = "Could not find the page border."
        for (b in cands) {
            val r = tryProcess(b, variant)
            if (r.error == null) return r
            lastErr = r.error
        }
        return Result(0, emptyList(), lastErr)
    }

    private fun rotate(b: Bitmap, deg: Float): Bitmap {
        val m = Matrix().apply { postRotate(deg) }
        return Bitmap.createBitmap(b, 0, 0, b.width, b.height, m, true)
    }

    private fun fail(msg: String) = Result(0, emptyList(), msg)

    private fun tryProcess(srcIn: Bitmap, variant: Int): Result {
        val maxW = 1600
        val bmp = if (srcIn.width > maxW)
            Bitmap.createScaledBitmap(srcIn, maxW, srcIn.height * maxW / srcIn.width, true) else srcIn
        val w = bmp.width
        val h = bmp.height
        val px = IntArray(w * h)
        bmp.getPixels(px, 0, w, 0, 0, w, h)
        val lum = IntArray(w * h)
        for (i in px.indices) {
            val p = px[i]
            lum[i] = (((p shr 16) and 255) * 299 + ((p shr 8) and 255) * 587 + (p and 255) * 114) / 1000
        }
        val iw = w + 1
        val integ = IntArray(iw * (h + 1))
        for (y in 0 until h) {
            var row = 0
            for (x in 0 until w) {
                row += lum[y * w + x]
                integ[(y + 1) * iw + x + 1] = integ[y * iw + x + 1] + row
            }
        }
        val r = max(8, w / 22)
        fun mean(x: Int, y: Int): Float {
            val x0 = max(0, x - r)
            val x1 = min(w, x + r + 1)
            val y0 = max(0, y - r)
            val y1 = min(h, y + r + 1)
            val s = integ[y1 * iw + x1] - integ[y0 * iw + x1] - integ[y1 * iw + x0] + integ[y0 * iw + x0]
            return s.toFloat() / ((x1 - x0) * (y1 - y0))
        }

        val rowC = IntArray(h)
        val colC = IntArray(w)
        for (y in 0 until h) {
            for (x in 0 until w) {
                val m = mean(x, y)
                if (lum[y * w + x] < 0.6f * m) {
                    rowC[y]++; colC[x]++
                }
            }
        }
        val maxR = rowC.max()
        val maxC = colC.max()
        if (maxR < w * 0.3f || maxC < h * 0.3f) return fail("Could not find the thick black border. Photograph the whole page, flat and in good light.")
        var top = -1; var bottom = -1
        for (y in 0 until h) if (rowC[y] > 0.6f * maxR) { if (top < 0) top = y; bottom = y }
        var left = -1; var right = -1
        for (x in 0 until w) if (colC[x] > 0.6f * maxC) { if (left < 0) left = x; right = x }
        val gw = (right - left).toFloat()
        val gh = (bottom - top).toFloat()
        if (gw < w * 0.4f || gh < h * 0.4f) return fail("The page border looks too small. Move closer so the page fills the photo.")
        val expected = TemplateMaker.GRID_W.toFloat() / TemplateMaker.GRID_H
        if (abs((gw / gh) / expected - 1f) > 0.22f) return fail("The page shape looks wrong. Hold the phone straight above the page and keep the whole border in view.")

        val cw = gw / Chars.COLS
        val ch = gh / Chars.ROWS
        var saved = 0
        val missing = ArrayList<Char>()
        for ((idx, c) in Chars.ALL.withIndex()) {
            val row = idx / Chars.COLS
            val col = idx % Chars.COLS
            val cx0 = left + col * cw
            val cy0 = top + row * ch
            val ix0 = (cx0 + cw * 0.07f).toInt()
            val ix1 = (cx0 + cw * 0.93f).toInt()
            val iy0 = (cy0 + ch * 0.06f).toInt()
            val iy1 = (cy0 + ch * 0.97f).toInt()
            val cwid = ix1 - ix0
            val chei = iy1 - iy0
            if (cwid <= 4 || chei <= 4) { missing.add(c); continue }
            val out = IntArray(cwid * chei)
            for (yy in 0 until chei) {
                for (xx in 0 until cwid) {
                    val x = ix0 + xx
                    val y = iy0 + yy
                    if (x < 0 || y < 0 || x >= w || y >= h) continue
                    val m = mean(x, y)
                    if (m < 1f) continue
                    val a = (((0.78f * m - lum[y * w + x]) / (0.5f * m)) * 255f).toInt().coerceIn(0, 255)
                    out[yy * cwid + xx] = a shl 24
                }
            }
            val mask = Bitmap.createBitmap(out, cwid, chei, Bitmap.Config.ARGB_8888)
            val baseline = (cy0 + Chars.BASELINE * ch) - iy0
            val t = GlyphUtil.trim(mask, baseline, 25)
            if (t == null) {
                missing.add(c)
            } else {
                Store.save(c, variant, t.first, ch, t.second)
                saved++
            }
        }
        return Result(saved, missing, null)
    }
}
