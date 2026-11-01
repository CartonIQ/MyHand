package com.myhand.app

import android.app.Activity
import android.graphics.Bitmap
import android.graphics.Canvas
import android.os.Bundle
import android.widget.LinearLayout
import android.widget.ScrollView

class SignatureActivity : Activity() {
    private lateinit var draw: DrawView
    private val colors = intArrayOf(0xFF111111.toInt(), 0xFF1A3FA0.toInt(), 0xFF4A2545.toInt(), 0xFFE85D8A.toInt())

    override fun onCreate(b: Bundle?) {
        super.onCreate(b)
        val scroll = ScrollView(this)
        setupWindow(scroll)
        val col = LinearLayout(this)
        col.orientation = LinearLayout.VERTICAL
        col.setPadding(dp(18), dp(30), dp(18), dp(20))
        scroll.addView(col)

        col.addView(script("Signature maker", 32f, Ui.ROSE))
        col.addView(label("Sign with your finger and save a transparent PNG.", 14f, Ui.GREY))

        val card = cardBox(14)
        draw = DrawView(this)
        draw.guides = false
        draw.penRatio = 0.022f
        val w = resources.displayMetrics.widthPixels - dp(36) - dp(28)
        card.addView(draw, lp(w, (w * 0.55f).toInt()))
        card.addView(label("Ink colour", 14f, Ui.GREY, true), lp(-2, -2, 14))
        val chips = ChipRow(this, listOf("Black", "Blue", "Plum", "Rose"), 0) { draw.setPenColor(colors[it]) }
        card.addView(chips.view, lp(-1, -2, 8))
        card.addView(label("Thickness", 14f, Ui.GREY, true), lp(-2, -2, 14))
        card.addView(styledSeek(100, 40) {
            draw.penRatio = 0.01f + it / 100f * 0.03f
            draw.applyPen()
        }, lp(-1, -2))
        col.addView(card, lp(-1, -2, 14))

        val row = LinearLayout(this)
        row.orientation = LinearLayout.HORIZONTAL
        val pa = LinearLayout.LayoutParams(0, -2, 1f)
        pa.rightMargin = dp(5)
        val pb = LinearLayout.LayoutParams(0, -2, 1.4f)
        pb.leftMargin = dp(5)
        row.addView(pillButton("Clear", false) { draw.clear() }, pa)
        row.addView(pillButton("Save PNG") { save() }, pb)
        col.addView(row, lp(-1, -2, 16))

        col.addView(label("Please use your signature only on your own genuine documents.", 12f, Ui.GREY), lp(-1, -2, 14))
        setContentView(scroll)
    }

    private fun save() {
        val m = draw.mask()
        val t = if (m == null) null else GlyphUtil.trim(m, 0f, 30)
        if (t == null) {
            toast("Please sign first.")
            return
        }
        val src = t.first
        val pad = 24
        val out = Bitmap.createBitmap(src.width + 2 * pad, src.height + 2 * pad, Bitmap.Config.ARGB_8888)
        Canvas(out).drawBitmap(src, pad.toFloat(), pad.toFloat(), null)
        val f = ShareUtil.savePng(this, out, "MyHand_signature_${System.currentTimeMillis()}.png")
        ShareUtil.shareFile(this, f, "image/png", "Save or share signature")
    }
}
