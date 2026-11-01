package com.myhand.app

import android.app.Activity
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Rect
import android.graphics.pdf.PdfDocument
import android.os.Bundle
import android.text.Editable
import android.text.InputType
import android.text.TextWatcher
import android.view.Gravity
import android.view.View
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import java.io.File

class EditorActivity : Activity() {
    private val opts = Renderer.Opts(text = "Hello! This is my own handwriting.", paper = 1)
    private lateinit var preview: HandPreview
    private lateinit var warn: TextView
    private lateinit var paperChips: ChipRow
    private var cleanExports = 0

    private val templates = listOf(
        Triple("Diwali", "Happy Diwali!\nWishing you a bright and joyful Diwali.\nMay your home be filled with light,\nlove and laughter.", 3),
        Triple("Holi", "Happy Holi!\nMay your life be full of colours\nand your heart full of joy.", 0),
        Triple("Rakhi", "Happy Raksha Bandhan!\nThank you for always being there for me.\nLove you always.", 0),
        Triple("Eid", "Eid Mubarak!\nWishing you peace, happiness\nand blessings today and always.", 3),
        Triple("Birthday", "Happy Birthday!\nWishing you a day full of smiles,\ncake and happy memories.", 0),
        Triple("Thank you", "Thank you so much!\nYour kindness means a lot to me.\nI am really grateful.", 0),
        Triple("Thanksgiving", "Happy Thanksgiving!\nGrateful for family, friends\nand all the good things in life.", 3),
        Triple("Christmas", "Merry Christmas!\nWishing you warmth, peace\nand joy this holiday season.", 0),
        Triple("New Year", "Happy New Year!\nMay this year bring you health,\nhappiness and success.", 0),
        Triple("Love note", "To my favourite person,\nYou make every day better.\nI love you.", 3),
        Triple("Get well", "Get well soon!\nThinking of you and sending you\nlots of love and strength.", 0),
        Triple("Diary", "Dear diary,\nToday was a good day.\nI want to remember this moment.", 1)
    )

    override fun onCreate(b: Bundle?) {
        super.onCreate(b)
        File(cacheDir, "exports").listFiles()?.forEach { it.delete() }
        intent.getStringExtra("text")?.let { opts.text = it }
        opts.paper = intent.getIntExtra("paper", opts.paper)

        val scroll = ScrollView(this)
        scroll.isVerticalScrollBarEnabled = false
        setupWindow(scroll)
        val col = LinearLayout(this)
        col.orientation = LinearLayout.VERTICAL
        col.setPadding(dp(16), dp(28), dp(16), dp(28))
        scroll.addView(col)

        col.addView(script("Write in my handwriting", 30f, Ui.ROSE))
        if (Store.count() == 0) {
            col.addView(label("No handwriting saved yet, so a plain font is used. Go back and draw or scan first \uD83D\uDC97", 13f, 0xFFB3261E.toInt()), lp(-1, -2, 6))
        }

        // Text card
        val textCard = cardBox()
        textCard.addView(label("Your text", 14f, Ui.GREY, true))
        val et = EditText(this)
        et.setText(opts.text)
        et.hint = "Type your text"
        et.minLines = 3
        et.gravity = Gravity.TOP
        et.setTextColor(Ui.PLUM)
        et.background = rounded(Ui.LAV_LIGHT, 14)
        et.setPadding(dp(12), dp(10), dp(12), dp(10))
        et.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_FLAG_MULTI_LINE or
            InputType.TYPE_TEXT_FLAG_CAP_SENTENCES
        textCard.addView(et, lp(-1, -2, 6))
        col.addView(textCard, lp(-1, -2, 14))

        // Preview card
        val pCard = cardBox(8)
        preview = HandPreview(this, opts)
        pCard.addView(preview, lp(-1, -2))
        warn = label("Text is too long for one page. Shorten it or lower the size.", 13f, 0xFFB3261E.toInt())
        warn.visibility = View.GONE
        pCard.addView(warn, lp(-1, -2, 6))
        col.addView(pCard, lp(-1, -2, 14))

        et.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, st: Int, c: Int, a: Int) {}
            override fun onTextChanged(s: CharSequence?, st: Int, b: Int, c: Int) {}
            override fun afterTextChanged(s: Editable?) {
                opts.text = s?.toString() ?: ""
                refresh()
            }
        })

        // Style card
        val style = cardBox()
        style.addView(label("Paper", 14f, Ui.GREY, true))
        paperChips = ChipRow(this, Renderer.paperNames, opts.paper) { opts.paper = it; refresh() }
        style.addView(paperChips.view, lp(-1, -2, 8))
        style.addView(label("Ink", 14f, Ui.GREY, true), lp(-2, -2, 14))
        val inkChips = ChipRow(this, Renderer.inkNames, opts.ink) { opts.ink = it; refresh() }
        style.addView(inkChips.view, lp(-1, -2, 8))
        style.addView(label("Ideas \u2728", 14f, Ui.GREY, true), lp(-2, -2, 14))
        val tplChips = ChipRow(this, templates.map { it.first }, -1) { i ->
            opts.paper = templates[i].third
            paperChips.selected = templates[i].third
            paperChips.build()
            et.setText(templates[i].second)
        }
        style.addView(tplChips.view, lp(-1, -2, 8))
        col.addView(style, lp(-1, -2, 14))

        // Sliders card
        val sl = cardBox()
        sl.addView(label("Size", 14f, Ui.GREY, true))
        sl.addView(styledSeek(36, 12) { opts.size = 24f + it; refresh() }, lp(-1, -2))
        sl.addView(label("Letter spacing", 14f, Ui.GREY, true), lp(-2, -2, 8))
        sl.addView(styledSeek(100, 50) { opts.spacing = it / 100f; refresh() }, lp(-1, -2))
        sl.addView(label("Slant", 14f, Ui.GREY, true), lp(-2, -2, 8))
        sl.addView(styledSeek(60, 30) { opts.slant = (it - 30) / 100f; refresh() }, lp(-1, -2))
        sl.addView(label("Natural look  (left = neat, right = casual)", 14f, Ui.GREY, true), lp(-2, -2, 8))
        sl.addView(styledSeek(100, 50) { opts.realism = it / 100f; refresh() }, lp(-1, -2))
        col.addView(sl, lp(-1, -2, 14))

        // Buttons
        val row = LinearLayout(this)
        row.orientation = LinearLayout.HORIZONTAL
        val pa = LinearLayout.LayoutParams(0, -2, 1f)
        pa.rightMargin = dp(5)
        val pb = LinearLayout.LayoutParams(0, -2, 1f)
        pb.leftMargin = dp(5)
        row.addView(pillButton("Export PNG") { export(false) }, pa)
        row.addView(pillButton("Export PDF", false) { export(true) }, pb)
        col.addView(row, lp(-1, -2, 18))
        col.addView(pillButton("\uD83C\uDF81 Remove watermark (watch a short ad)", false) { rewarded() }, lp(-1, -2, 10))

        setContentView(scroll)
        refresh()
    }

    private fun refresh() {
        opts.watermark = cleanExports == 0
        preview.refresh()
        warn.visibility = if (preview.overflow) View.VISIBLE else View.GONE
    }

    private fun rewarded() {
        AdsHelper.showRewarded(this, {
            cleanExports = 1
            refresh()
            toast("Done! Your next export has no watermark \uD83D\uDC96")
        }, {
            toast("No ad is ready yet. Please try again in a moment.")
        })
    }

    private fun export(pdf: Boolean) {
        val clean = cleanExports > 0
        val o = opts.copyWith(watermark = !clean)
        toast("Creating file...")
        Thread {
            try {
                val sc = 1.5f
                val bmp = Bitmap.createBitmap((Renderer.PW * sc).toInt(), (Renderer.PH * sc).toInt(), Bitmap.Config.ARGB_8888)
                val cv = Canvas(bmp)
                cv.scale(sc, sc)
                Renderer.draw(cv, o)
                val dir = ShareUtil.exportsDir(this)
                val f = File(dir, "MyHand_${System.currentTimeMillis()}." + (if (pdf) "pdf" else "png"))
                if (pdf) {
                    val d = PdfDocument()
                    val pg = d.startPage(PdfDocument.PageInfo.Builder(595, 842, 1).create())
                    pg.canvas.drawBitmap(bmp, null, Rect(0, 0, 595, 842), null)
                    d.finishPage(pg)
                    f.outputStream().use { d.writeTo(it) }
                    d.close()
                } else {
                    f.outputStream().use { bmp.compress(Bitmap.CompressFormat.PNG, 100, it) }
                }
                bmp.recycle()
                runOnUiThread { afterExport(f, pdf, clean) }
            } catch (e: Throwable) {
                runOnUiThread { toast("Export failed: ${e.message}") }
            }
        }.start()
    }

    private fun afterExport(f: File, pdf: Boolean, clean: Boolean) {
        val go: () -> Unit = { ShareUtil.shareFile(this, f, if (pdf) "application/pdf" else "image/png", "Save or share") }
        if (clean) {
            cleanExports = 0
            refresh()
            go()
        } else {
            AdsHelper.showInterstitial(this, go)
        }
    }
}
