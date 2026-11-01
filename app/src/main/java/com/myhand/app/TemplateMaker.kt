package com.myhand.app

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint

object TemplateMaker {
    const val W = 1240
    const val H = 1754
    const val M = 50
    const val HEAD = 90
    const val GRID_W = W - 2 * M              // 1140
    const val GRID_H = H - M - (M + HEAD)     // 1564

    fun make(): Bitmap {
        val b = Bitmap.createBitmap(W, H, Bitmap.Config.ARGB_8888)
        val c = Canvas(b)
        c.drawColor(Color.WHITE)
        val top = (M + HEAD).toFloat()
        val left = M.toFloat()
        val right = (W - M).toFloat()
        val bottom = (H - M).toFloat()
        val cw = GRID_W / Chars.COLS.toFloat()
        val ch = GRID_H / Chars.ROWS.toFloat()

        val title = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = 0xFF1F3A5F.toInt(); textSize = 34f; isFakeBoldText = true }
        c.drawText("MyHand sheet", left, 62f, title)
        val sub = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = 0xFF555555.toInt(); textSize = 25f }
        c.drawText("Write each character in its box, sitting on the light line. Use a dark pen.", left, 100f, sub)

        val grid = Paint().apply { color = 0xFFB4B4B4.toInt(); strokeWidth = 3f }
        for (i in 1 until Chars.COLS) c.drawLine(left + i * cw, top, left + i * cw, bottom, grid)
        for (i in 1 until Chars.ROWS) c.drawLine(left, top + i * ch, right, top + i * ch, grid)

        val base = Paint().apply { color = 0xFFD2D2D2.toInt(); strokeWidth = 2f }
        val lab = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = 0xFFC8C8C8.toInt(); textSize = 22f }
        for ((idx, chr) in Chars.ALL.withIndex()) {
            val r = idx / Chars.COLS
            val col = idx % Chars.COLS
            val x0 = left + col * cw
            val y0 = top + r * ch
            c.drawLine(x0 + 8f, y0 + Chars.BASELINE * ch, x0 + cw - 8f, y0 + Chars.BASELINE * ch, base)
            c.drawText(chr.toString(), x0 + 8f, y0 + 24f, lab)
        }

        val border = Paint().apply { color = Color.BLACK; style = Paint.Style.STROKE; strokeWidth = 8f }
        c.drawRect(left, top, right, bottom, border)
        return b
    }
}
