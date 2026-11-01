package com.myhand.app

import android.app.Activity
import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.Drawable
import android.graphics.drawable.GradientDrawable
import android.graphics.drawable.RippleDrawable
import android.view.Gravity
import android.view.View
import android.widget.Button
import android.widget.HorizontalScrollView
import android.widget.LinearLayout
import android.widget.SeekBar
import android.widget.TextView

object Ui {
    val BLUSH = 0xFFFFF0F5.toInt()
    val CREAM = 0xFFFFF8F0.toInt()
    val ROSE = 0xFFE85D8A.toInt()
    val ROSE2 = 0xFFFF8FAB.toInt()
    val PLUM = 0xFF4A2545.toInt()
    val LAV = 0xFFB79CED.toInt()
    val LAV_LIGHT = 0xFFF3ECFF.toInt()
    val GREY = 0xFF7A6272.toInt()
    val LINE = 0xFFE7D7DF.toInt()
}

fun Context.lp(w: Int, h: Int, topDp: Int = 0, weight: Float = 0f): LinearLayout.LayoutParams =
    LinearLayout.LayoutParams(w, h, weight).apply { topMargin = dp(topDp) }

fun Context.rounded(color: Int, radiusDp: Int, stroke: Int = 0, strokeDp: Int = 0): GradientDrawable {
    val g = GradientDrawable()
    g.setColor(color)
    g.cornerRadius = dp(radiusDp).toFloat()
    if (strokeDp > 0) g.setStroke(dp(strokeDp), stroke)
    return g
}

fun Context.gradient(c1: Int, c2: Int, radiusDp: Int, horizontal: Boolean = true): GradientDrawable {
    val o = if (horizontal) GradientDrawable.Orientation.LEFT_RIGHT else GradientDrawable.Orientation.TOP_BOTTOM
    val g = GradientDrawable(o, intArrayOf(c1, c2))
    g.cornerRadius = dp(radiusDp).toFloat()
    return g
}

fun Context.ripple(content: Drawable, radiusDp: Int, rippleColor: Int = 0x33E85D8A): Drawable =
    RippleDrawable(ColorStateList.valueOf(rippleColor), content, rounded(Color.WHITE, radiusDp))

fun Activity.setupWindow(root: View) {
    root.background = GradientDrawable(GradientDrawable.Orientation.TOP_BOTTOM, intArrayOf(Ui.BLUSH, Ui.CREAM))
    window.statusBarColor = Ui.BLUSH
    window.navigationBarColor = Ui.CREAM
    window.decorView.systemUiVisibility =
        View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR or View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR
}

fun Context.label(text: String, sizeSp: Float = 15f, color: Int = Ui.PLUM, bold: Boolean = false): TextView {
    val t = TextView(this)
    t.text = text
    t.textSize = sizeSp
    t.setTextColor(color)
    if (bold) t.setTypeface(null, Typeface.BOLD)
    return t
}

fun Context.script(text: String, sizeSp: Float, color: Int): TextView {
    val t = TextView(this)
    t.text = text
    t.textSize = sizeSp
    t.setTextColor(color)
    t.typeface = Typeface.create("cursive", Typeface.BOLD)
    return t
}

fun Context.pillButton(text: String, primary: Boolean = true, onClick: () -> Unit): Button {
    val b = Button(this)
    b.text = text
    b.isAllCaps = false
    b.textSize = 16f
    b.setTypeface(null, Typeface.BOLD)
    b.stateListAnimator = null
    b.setPadding(dp(18), dp(12), dp(18), dp(12))
    if (primary) {
        b.setTextColor(Color.WHITE)
        b.background = ripple(gradient(Ui.ROSE, Ui.ROSE2, 28), 28, 0x44FFFFFF)
    } else {
        b.setTextColor(Ui.ROSE)
        b.background = ripple(rounded(Color.WHITE, 28, Ui.ROSE, 2), 28)
    }
    b.setOnClickListener { onClick() }
    return b
}

fun Context.cardBox(padDp: Int = 16): LinearLayout {
    val c = LinearLayout(this)
    c.orientation = LinearLayout.VERTICAL
    c.background = rounded(Color.WHITE, 22)
    c.setPadding(dp(padDp), dp(padDp), dp(padDp), dp(padDp))
    c.elevation = dp(3).toFloat()
    return c
}

fun Context.featureCard(emoji: String, title: String, sub: String, onClick: () -> Unit): LinearLayout {
    val c = LinearLayout(this)
    c.orientation = LinearLayout.VERTICAL
    c.background = ripple(rounded(Color.WHITE, 22), 22)
    c.setPadding(dp(16), dp(16), dp(16), dp(16))
    c.elevation = dp(3).toFloat()
    c.minimumHeight = dp(124)
    val e = TextView(this)
    e.text = emoji
    e.textSize = 30f
    c.addView(e)
    c.addView(label(title, 16f, Ui.PLUM, true), lp(-2, -2, 6))
    c.addView(label(sub, 12f, Ui.GREY), lp(-2, -2, 2))
    c.isClickable = true
    c.setOnClickListener { onClick() }
    return c
}

fun Context.chip(text: String, selected: Boolean, onClick: () -> Unit): TextView {
    val t = TextView(this)
    t.text = text
    t.textSize = 14f
    t.gravity = Gravity.CENTER
    t.setPadding(dp(16), dp(8), dp(16), dp(8))
    if (selected) {
        t.setTextColor(Color.WHITE)
        t.background = gradient(Ui.ROSE, Ui.ROSE2, 20)
    } else {
        t.setTextColor(Ui.PLUM)
        t.background = rounded(Color.WHITE, 20, Ui.LINE, 1)
    }
    t.setOnClickListener { onClick() }
    return t
}

class ChipRow(private val ctx: Context, private val items: List<String>, var selected: Int, private val onSelect: (Int) -> Unit) {
    val view = HorizontalScrollView(ctx)
    private val row = LinearLayout(ctx)

    init {
        view.isHorizontalScrollBarEnabled = false
        row.orientation = LinearLayout.HORIZONTAL
        view.addView(row)
        build()
    }

    fun build() {
        row.removeAllViews()
        for ((i, t) in items.withIndex()) {
            val lp = LinearLayout.LayoutParams(-2, -2)
            lp.rightMargin = ctx.dp(8)
            row.addView(ctx.chip(t, i == selected) {
                selected = i
                build()
                onSelect(i)
            }, lp)
        }
    }
}

fun Context.styledSeek(max: Int, init: Int, onChange: (Int) -> Unit): SeekBar {
    val sb = SeekBar(this)
    sb.max = max
    sb.progress = init
    sb.progressTintList = ColorStateList.valueOf(Ui.ROSE)
    sb.thumbTintList = ColorStateList.valueOf(Ui.ROSE)
    sb.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
        override fun onProgressChanged(s: SeekBar?, p: Int, u: Boolean) { onChange(p) }
        override fun onStartTrackingTouch(s: SeekBar?) {}
        override fun onStopTrackingTouch(s: SeekBar?) {}
    })
    return sb
}

fun Context.progressBar(percent: Int): LinearLayout {
    val track = LinearLayout(this)
    track.orientation = LinearLayout.HORIZONTAL
    track.background = rounded(0x55FFFFFF, 6)
    val p = percent.coerceIn(0, 100)
    if (p > 0) {
        val fill = View(this)
        fill.background = rounded(Color.WHITE, 6)
        track.addView(fill, LinearLayout.LayoutParams(0, -1, p.toFloat()))
    }
    if (p < 100) track.addView(View(this), LinearLayout.LayoutParams(0, -1, (100 - p).toFloat()))
    return track
}
