package com.myhand.app

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.PorterDuff
import android.view.MotionEvent
import android.view.View

class DrawView(ctx: Context) : View(ctx) {
    private var bmp: Bitmap? = null
    private var bc: Canvas? = null
    private val pen = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.BLACK
        style = Paint.Style.STROKE
        strokeCap = Paint.Cap.ROUND
        strokeJoin = Paint.Join.ROUND
    }
    private val guide = Paint().apply { color = 0xFFD9B8C8.toInt(); strokeWidth = 3f }
    private val faint = Paint().apply { color = 0xFFF1E4EA.toInt(); strokeWidth = 2f }
    private val frame = Paint().apply { color = 0xFFE7D7DF.toInt(); style = Paint.Style.STROKE; strokeWidth = 4f }
    private var lx = 0f
    private var ly = 0f

    var guides = true
    var penRatio = 0.04f

    fun baselineY(): Float = height * Chars.BASELINE

    fun mask(): Bitmap? = bmp

    fun applyPen() { pen.strokeWidth = height * penRatio }

    fun setPenColor(c: Int) { pen.color = c }

    fun clear() {
        bc?.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR)
        invalidate()
    }

    override fun onSizeChanged(w: Int, h: Int, ow: Int, oh: Int) {
        pen.strokeWidth = h * penRatio
        val nb = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
        bmp?.let { Canvas(nb).drawBitmap(it, 0f, 0f, null) }
        bmp = nb
        bc = Canvas(nb)
    }

    override fun onDraw(canvas: Canvas) {
        canvas.drawColor(Color.WHITE)
        val wf = width.toFloat()
        if (guides) {
            canvas.drawLine(0f, height * 0.12f, wf, height * 0.12f, faint)
            canvas.drawLine(0f, height * 0.45f, wf, height * 0.45f, faint)
            canvas.drawLine(0f, baselineY(), wf, baselineY(), guide)
        }
        bmp?.let { canvas.drawBitmap(it, 0f, 0f, null) }
        canvas.drawRect(2f, 2f, wf - 2f, height - 2f, frame)
    }

    override fun onTouchEvent(e: MotionEvent): Boolean {
        val x = e.x
        val y = e.y
        when (e.actionMasked) {
            MotionEvent.ACTION_DOWN -> {
                parent?.requestDisallowInterceptTouchEvent(true)
                lx = x; ly = y
                bc?.drawPoint(x, y, pen)
            }
            MotionEvent.ACTION_MOVE -> {
                for (i in 0 until e.historySize) {
                    val hx = e.getHistoricalX(i)
                    val hy = e.getHistoricalY(i)
                    bc?.drawLine(lx, ly, hx, hy, pen)
                    lx = hx; ly = hy
                }
                bc?.drawLine(lx, ly, x, y, pen)
                lx = x; ly = y
            }
        }
        invalidate()
        return true
    }
}
