package com.myhand.app

import android.graphics.Bitmap
import kotlin.math.max
import kotlin.math.min

object GlyphUtil {
    /**
     * src: ARGB bitmap where the alpha channel is the ink strength.
     * Returns the trimmed glyph and baseOff (bottom of glyph minus baseline, in source pixels).
     */
    fun trim(src: Bitmap, baselineY: Float, minInk: Int): Pair<Bitmap, Float>? {
        val w = src.width
        val h = src.height
        val px = IntArray(w * h)
        src.getPixels(px, 0, w, 0, 0, w, h)
        val rows = IntArray(h)
        val cols = IntArray(w)
        var total = 0
        for (y in 0 until h) {
            val o = y * w
            for (x in 0 until w) {
                if ((px[o + x] ushr 24) > 40) {
                    rows[y]++; cols[x]++; total++
                }
            }
        }
        if (total < minInk) return null
        var y0 = 0
        while (y0 < h && rows[y0] < 2) y0++
        var y1 = h - 1
        while (y1 >= 0 && rows[y1] < 2) y1--
        var x0 = 0
        while (x0 < w && cols[x0] < 2) x0++
        var x1 = w - 1
        while (x1 >= 0 && cols[x1] < 2) x1--
        if (y1 <= y0 || x1 <= x0) return null
        val pad = 2
        x0 = max(0, x0 - pad)
        y0 = max(0, y0 - pad)
        x1 = min(w - 1, x1 + pad)
        y1 = min(h - 1, y1 + pad)
        val out = Bitmap.createBitmap(src, x0, y0, x1 - x0 + 1, y1 - y0 + 1)
        return Pair(out, (y1 + 1) - baselineY)
    }
}
