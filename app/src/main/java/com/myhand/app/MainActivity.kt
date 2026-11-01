package com.myhand.app

import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView

class MainActivity : Activity() {
    private lateinit var countTv: TextView
    private lateinit var barHolder: FrameLayout
    private lateinit var adBox: FrameLayout
    private lateinit var privacyTv: TextView

    override fun onCreate(b: Bundle?) {
        super.onCreate(b)
        val outer = LinearLayout(this)
        outer.orientation = LinearLayout.VERTICAL
        setupWindow(outer)

        val scroll = ScrollView(this)
        scroll.isVerticalScrollBarEnabled = false
        val col = LinearLayout(this)
        col.orientation = LinearLayout.VERTICAL
        col.setPadding(dp(18), dp(30), dp(18), dp(12))
        scroll.addView(col)

        val head = LinearLayout(this)
        head.orientation = LinearLayout.VERTICAL
        head.background = gradient(Ui.ROSE, Ui.LAV, 28)
        head.setPadding(dp(22), dp(22), dp(22), dp(22))
        head.elevation = dp(6).toFloat()
        head.addView(script("MyHand", 46f, 0xFFFFFFFF.toInt()))
        head.addView(label("Your handwriting, everywhere \u2728", 15f, 0xFFFFFFFF.toInt()))
        countTv = label("", 15f, 0xFFFFFFFF.toInt(), true)
        head.addView(countTv, lp(-2, -2, 18))
        barHolder = FrameLayout(this)
        head.addView(barHolder, lp(-1, dp(8), 8))
        col.addView(head, lp(-1, -2))

        col.addView(label("Create", 18f, Ui.PLUM, true), lp(-2, -2, 22))

        fun row(a: View, c: View) {
            val r = LinearLayout(this)
            r.orientation = LinearLayout.HORIZONTAL
            val la = LinearLayout.LayoutParams(0, -2, 1f)
            la.rightMargin = dp(6)
            val lc = LinearLayout.LayoutParams(0, -2, 1f)
            lc.leftMargin = dp(6)
            r.addView(a, la)
            r.addView(c, lc)
            col.addView(r, lp(-1, -2, 12))
        }

        row(
            featureCard("\u270D\uFE0F", "Draw", "Write your letters") {
                pickSet { v -> startActivity(Intent(this, DrawActivity::class.java).putExtra("variant", v)) }
            },
            featureCard("\uD83D\uDCF7", "Scan (beta)", "From a printed sheet") {
                pickSet { v -> startActivity(Intent(this, ScanActivity::class.java).putExtra("variant", v)) }
            }
        )
        row(
            featureCard("\u2728", "Write", "Type in your handwriting") {
                startActivity(Intent(this, EditorActivity::class.java))
            },
            featureCard("\uD83D\uDC8C", "Letters", "To your future self") {
                startActivity(Intent(this, LettersActivity::class.java))
            }
        )
        col.addView(label("Extras", 18f, Ui.PLUM, true), lp(-2, -2, 22))
        row(
            featureCard("\uD83D\uDD8B\uFE0F", "Signature", "Transparent PNG") {
                startActivity(Intent(this, SignatureActivity::class.java))
            },
            featureCard("\uD83C\uDF38", "Stickers", "Cute word stickers") {
                startActivity(Intent(this, StickerActivity::class.java))
            }
        )
        row(
            featureCard("\uD83D\uDCBE", "Backup", "Save and restore") {
                startActivity(Intent(this, BackupActivity::class.java))
            },
            featureCard("\uD83D\uDDD1\uFE0F", "Reset", "Delete my handwriting") {
                AlertDialog.Builder(this).setTitle("Delete all saved handwriting?")
                    .setMessage("This cannot be undone. Your letters are kept.")
                    .setPositiveButton("Delete") { _, _ -> Store.clear(); updateStatus() }
                    .setNegativeButton("Cancel", null).show()
            }
        )

        col.addView(label("Tip: finish Set 1 first. Sets 2 and 3 are optional extra versions of each letter that make your text look more natural.", 13f, Ui.GREY), lp(-1, -2, 20))

        privacyTv = label("\uD83D\uDD12 Privacy and ad settings", 14f, Ui.ROSE, true)
        privacyTv.visibility = View.GONE
        privacyTv.setOnClickListener { AdsHelper.showPrivacy(this) }
        col.addView(privacyTv, lp(-2, -2, 14))

        val policyTv = label("\uD83D\uDCDC Privacy Policy", 14f, Ui.ROSE, true)
        policyTv.setOnClickListener {
            startActivity(Intent(Intent.ACTION_VIEW, android.net.Uri.parse("https://cartoniq.github.io/privacy/")))
        }
        col.addView(policyTv, lp(-2, -2, 14))

        val aboutTv = label("\u2764\uFE0F About CartoniQ", 14f, Ui.ROSE, true)
        aboutTv.setOnClickListener { showAboutDialog() }
        col.addView(aboutTv, lp(-2, -2, 14))

        adBox = FrameLayout(this)
        outer.addView(scroll, LinearLayout.LayoutParams(-1, 0, 1f))
        outer.addView(adBox, LinearLayout.LayoutParams(-1, -2))
        setContentView(outer)

        AdsHelper.start(this) {
            AdsHelper.loadBanner(this, adBox)
            privacyTv.visibility = if (AdsHelper.privacyRequired(this)) View.VISIBLE else View.GONE
        }
    }

    override fun onResume() {
        super.onResume()
        updateStatus()
    }

    private fun updateStatus() {
        val n = Store.count()
        val total = Chars.ALL.size
        countTv.text = if (n == 0) "Start by drawing your first letters" else "$n of $total letters saved"
        barHolder.removeAllViews()
        barHolder.addView(progressBar(n * 100 / total), FrameLayout.LayoutParams(-1, -1))
    }

    private fun showAboutDialog() {
        AlertDialog.Builder(this)
            .setTitle("About CartoniQ")
            .setMessage("Mohd Arsh — Founder · CartoniQ\n\n" +
                        "I build Android apps from a phone. No laptop. No team. Just a terminal, an idea, and the stubborn belief that great software doesn't need a big office — it needs a clear mind.\n\n" +
                        "Every CartoniQ app is designed, coded, and signed by hand, on the same device it runs on. Offline-first. Privacy-respecting. Quietly premium.\n\n" +
                        "This is what one person with grit can build. \uD83D\uDE80\n\n" +
                        "\uD83C\uDF10 cartoniq.runs-at.dev\n" +
                        "\u2709\uFE0F cartoniq.business@gmail.com\n\n" +
                        "MyHand v1.1.0\nYour handwriting, everywhere.")
            .setPositiveButton("Close", null)
            .show()
    }

    private fun pickSet(next: (Int) -> Unit) {
        AlertDialog.Builder(this).setTitle("Which set?")
            .setItems(arrayOf("Set 1 (main - do this first)", "Set 2 (adds variety)", "Set 3 (adds more variety)")) { _, w ->
                next(w + 1)
            }.show()
    }
}
