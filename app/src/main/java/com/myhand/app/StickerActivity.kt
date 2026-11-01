package com.myhand.app

import android.app.Activity
import android.graphics.Bitmap
import android.os.Bundle
import android.text.Editable
import android.text.InputType
import android.text.TextWatcher
import android.view.Gravity
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ScrollView
import java.io.File

class StickerActivity : Activity() {
    private var text = "Hello"
    private var style = 1
    private var ink = 0
    private var current: Bitmap? = null
    private lateinit var img: ImageView

    override fun onCreate(b: Bundle?) {
        super.onCreate(b)
        val scroll = ScrollView(this)
        setupWindow(scroll)
        val col = LinearLayout(this)
        col.orientation = LinearLayout.VERTICAL
        col.setPadding(dp(18), dp(30), dp(18), dp(20))
        scroll.addView(col)

        col.addView(script("Sticker maker", 32f, Ui.ROSE))
        col.addView(label("Turn a word into a sticker in your handwriting \uD83C\uDF38", 14f, Ui.GREY))

        val card = cardBox(16)
        val et = EditText(this)
        et.setText(text)
        et.hint = "Type a word, like Hello or Love"
        et.setTextColor(Ui.PLUM)
        et.gravity = Gravity.CENTER
        et.background = rounded(Ui.LAV_LIGHT, 14)
        et.setPadding(dp(12), dp(10), dp(12), dp(10))
        et.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_FLAG_MULTI_LINE
        et.maxLines = 3
        card.addView(et, lp(-1, -2))

        card.addView(label("Style", 14f, Ui.GREY, true), lp(-2, -2, 14))
        card.addView(ChipRow(this, StickerMaker.styleNames, style) { style = it; refresh() }.view, lp(-1, -2, 8))
        card.addView(label("Ink", 14f, Ui.GREY, true), lp(-2, -2, 14))
        card.addView(ChipRow(this, Renderer.inkNames, ink) { ink = it; refresh() }.view, lp(-1, -2, 8))

        img = ImageView(this)
        img.scaleType = ImageView.ScaleType.FIT_CENTER
        img.background = rounded(Ui.LAV_LIGHT, 22)
        img.setPadding(dp(8), dp(8), dp(8), dp(8))
        card.addView(img, lp(-1, dp(280), 16))
        col.addView(card, lp(-1, -2, 14))

        et.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, st: Int, c: Int, a: Int) {}
            override fun onTextChanged(s: CharSequence?, st: Int, b: Int, c: Int) {}
            override fun afterTextChanged(s: Editable?) {
                text = s?.toString() ?: ""
                refresh()
            }
        })

        val row = LinearLayout(this)
        row.orientation = LinearLayout.HORIZONTAL
        val pa = LinearLayout.LayoutParams(0, -2, 1f)
        pa.rightMargin = dp(5)
        val pb = LinearLayout.LayoutParams(0, -2, 1f)
        pb.leftMargin = dp(5)
        row.addView(pillButton("Share PNG") { share(false) }, pa)
        row.addView(pillButton("Share WebP", false) { share(true) }, pb)
        col.addView(row, lp(-1, -2, 16))

        col.addView(label("To use these as real WhatsApp stickers, import the images into a sticker-pack app. One-tap pack export is planned for a later version.", 12f, Ui.GREY), lp(-1, -2, 14))
        setContentView(scroll)
        refresh()
    }

    private fun refresh() {
        current = StickerMaker.make(text, ink, style)
        img.setImageBitmap(current)
    }

    private fun share(webp: Boolean) {
        val b = current
        if (b == null) {
            toast("Type a word first.")
            return
        }
        val stamp = System.currentTimeMillis()
        if (webp) {
            val f = File(ShareUtil.exportsDir(this), "MyHand_sticker_$stamp.webp")
            f.outputStream().use { b.compress(Bitmap.CompressFormat.WEBP, 90, it) }
            ShareUtil.shareFile(this, f, "image/webp", "Share sticker")
        } else {
            val f = ShareUtil.savePng(this, b, "MyHand_sticker_$stamp.png")
            ShareUtil.shareFile(this, f, "image/png", "Share sticker")
        }
    }
}
