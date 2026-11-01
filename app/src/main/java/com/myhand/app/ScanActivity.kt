package com.myhand.app

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.os.Bundle
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import java.io.File

class ScanActivity : Activity() {
    private var variant = 1
    private lateinit var info: TextView
    private val REQ = 42

    override fun onCreate(b: Bundle?) {
        super.onCreate(b)
        variant = intent.getIntExtra("variant", 1)
        val scroll = ScrollView(this)
        setupWindow(scroll)
        val col = LinearLayout(this)
        col.orientation = LinearLayout.VERTICAL
        col.setPadding(dp(18), dp(30), dp(18), dp(20))
        scroll.addView(col)

        col.addView(script("Scan my handwriting", 32f, Ui.ROSE))
        col.addView(label("Beta  -  Set $variant", 14f, Ui.GREY))

        val steps = cardBox(18)
        steps.addView(label("How it works", 16f, Ui.PLUM, true))
        steps.addView(label("1\uFE0F\u20E3  Get the template and print it (or open it on a tablet).", 14f, Ui.PLUM), lp(-1, -2, 10))
        steps.addView(label("2\uFE0F\u20E3  Write one character per box with a dark pen, on the light line.", 14f, Ui.PLUM), lp(-1, -2, 8))
        steps.addView(label("3\uFE0F\u20E3  Lay the page flat in good light. Photograph the WHOLE page from straight above so the thick black border is fully visible.", 14f, Ui.PLUM), lp(-1, -2, 8))
        steps.addView(label("4\uFE0F\u20E3  Tap Choose photo and pick it.", 14f, Ui.PLUM), lp(-1, -2, 8))
        steps.addView(label("Dark pens work best. Light pencil may be missed.", 12f, Ui.GREY), lp(-1, -2, 10))
        col.addView(steps, lp(-1, -2, 14))

        col.addView(pillButton("Get template (PNG)", false) { shareTemplate() }, lp(-1, -2, 16))
        col.addView(pillButton("Choose photo of my page") {
            val i = Intent(Intent.ACTION_GET_CONTENT).apply { type = "image/*" }
            startActivityForResult(Intent.createChooser(i, "Choose photo"), REQ)
        }, lp(-1, -2, 10))

        info = label("", 15f, Ui.PLUM)
        col.addView(info, lp(-1, -2, 16))
        setContentView(scroll)
    }

    private fun shareTemplate() {
        try {
            val bmp = TemplateMaker.make()
            val f: File = ShareUtil.savePng(this, bmp, "MyHand_template.png")
            bmp.recycle()
            ShareUtil.shareFile(this, f, "image/png", "Print or save template")
        } catch (e: Exception) {
            toast("Could not create template: ${e.message}")
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode != REQ || resultCode != RESULT_OK) return
        val uri = data?.data ?: return
        info.text = "Processing... this can take a few seconds."
        Thread {
            val msg: String = try {
                val bmp: Bitmap? = loadBitmap(this, uri, 1600)
                if (bmp == null) {
                    "Could not open that picture."
                } else {
                    val r = ScanProcessor.process(bmp, variant)
                    if (r.error != null) {
                        r.error
                    } else {
                        val miss = if (r.missing.isEmpty()) "none" else r.missing.joinToString(" ")
                        "Saved ${r.saved} of ${Chars.ALL.size} characters \uD83D\uDC96\nMissing or too faint: $miss\n\n" +
                            "You can scan again, or fix single letters in Draw mode using the same set number."
                    }
                }
            } catch (e: Throwable) {
                "Scan failed: ${e.message}"
            }
            runOnUiThread { info.text = msg }
        }.start()
    }
}
