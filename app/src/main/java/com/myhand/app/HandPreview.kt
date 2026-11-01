package com.myhand.app

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Rect
import android.view.View

class HandPreview(ctx: Context, private val o: Renderer.Opts) : View(ctx) {
    private var cache: Bitmap? = null
    private val p = Paint(Paint.FILTER_BITMAP_FLAG)
    var overflow = false

    fun refresh() {
        val s = 0.6f
        val bw = (Renderer.PW * s).toInt()
        val bh = (Renderer.PH * s).toInt()
        var b = cache
        if (b == null) {
            b = Bitmap.createBitmap(bw, bh, Bitmap.Config.ARGB_8888)
            cache = b
        }
        val c = Canvas(b)
        c.save()
        c.scale(s, s)
        overflow = Renderer.draw(c, o)
        c.restore()
        invalidate()
    }

    override fun onMeasure(wSpec: Int, hSpec: Int) {
        val w = MeasureSpec.getSize(wSpec)
        setMeasuredDimension(w, (w * Renderer.PH / Renderer.PW.toFloat()).toInt())
    }

    override fun onDraw(canvas: Canvas) {
        cache?.let { canvas.drawBitmap(it, null, Rect(0, 0, width, height), p) }
    }
}
