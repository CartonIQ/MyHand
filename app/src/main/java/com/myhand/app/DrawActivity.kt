package com.myhand.app

import android.app.Activity
import android.graphics.Typeface
import android.os.Bundle
import android.view.Gravity
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView

class DrawActivity : Activity() {
    private var idx = 0
    private var variant = 1
    private lateinit var big: TextView
    private lateinit var info: TextView
    private lateinit var draw: DrawView

    override fun onCreate(b: Bundle?) {
        super.onCreate(b)
        variant = intent.getIntExtra("variant", 1)
        val scroll = ScrollView(this)
        setupWindow(scroll)
        val root = LinearLayout(this)
        root.orientation = LinearLayout.VERTICAL
        root.setPadding(dp(18), dp(30), dp(18), dp(18))
        scroll.addView(root)

        root.addView(script("Draw your letters", 32f, Ui.ROSE))
        root.addView(label("Set $variant  -  take your time \u2728", 14f, Ui.GREY))

        val card = cardBox(14)
        big = TextView(this)
        big.textSize = 58f
        big.gravity = Gravity.CENTER
        big.setTextColor(Ui.PLUM)
        big.typeface = Typeface.create("cursive", Typeface.BOLD)
        info = label("", 14f, Ui.GREY)
        info.gravity = Gravity.CENTER
        draw = DrawView(this)
        val w = resources.displayMetrics.widthPixels - dp(36) - dp(28)
        card.addView(big, lp(-1, -2))
        card.addView(info, lp(-1, -2))
        card.addView(draw, lp(w, w * 3 / 4, 8))
        root.addView(card, lp(-1, -2, 14))

        val row = LinearLayout(this)
        row.orientation = LinearLayout.HORIZONTAL
        val m = dp(4)
        fun add(v: android.view.View, weight: Float) {
            val p = LinearLayout.LayoutParams(0, -2, weight)
            p.leftMargin = m; p.rightMargin = m
            row.addView(v, p)
        }
        add(pillButton("Clear", false) { draw.clear() }, 1f)
        add(pillButton("Skip", false) { next() }, 1f)
        add(pillButton("Save \u2192") { save() }, 1.3f)
        root.addView(row, lp(-1, -2, 14))

        root.addView(label("Write on the pink line. Tails of letters like g, y and j can go below it.", 13f, Ui.GREY), lp(-1, -2, 12))
        setContentView(scroll)
        update()
    }

    private fun update() {
        if (idx >= Chars.ALL.size) {
            toast("All done! Your handwriting is saved \uD83D\uDC96")
            finish()
            return
        }
        big.text = Chars.ALL[idx].toString()
        info.text = "${idx + 1} of ${Chars.ALL.size}"
        draw.clear()
    }

    private fun next() {
        idx++
        update()
    }

    private fun save() {
        val m = draw.mask()
        val t = if (m == null) null else GlyphUtil.trim(m, draw.baselineY(), 30)
        if (t == null) {
            toast("Please draw the character first.")
            return
        }
        Store.save(Chars.ALL[idx], variant, t.first, draw.height.toFloat(), t.second)
        next()
    }
}
