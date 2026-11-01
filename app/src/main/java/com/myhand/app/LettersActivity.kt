package com.myhand.app

import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.LinearLayout
import android.widget.ScrollView
import java.text.DateFormat
import java.util.Date

class LettersActivity : Activity() {
    private lateinit var list: LinearLayout

    override fun onCreate(b: Bundle?) {
        super.onCreate(b)
        val scroll = ScrollView(this)
        setupWindow(scroll)
        val col = LinearLayout(this)
        col.orientation = LinearLayout.VERTICAL
        col.setPadding(dp(18), dp(30), dp(18), dp(20))
        scroll.addView(col)

        col.addView(script("Letters to Future You", 30f, Ui.ROSE))
        col.addView(label("Write now. It opens on the date you choose \uD83D\uDC8C", 14f, Ui.GREY))
        col.addView(pillButton("Write a new letter") {
            startActivity(Intent(this, LetterComposeActivity::class.java))
        }, lp(-1, -2, 16))
        list = LinearLayout(this)
        list.orientation = LinearLayout.VERTICAL
        col.addView(list, lp(-1, -2, 4))
        setContentView(scroll)
    }

    override fun onResume() {
        super.onResume()
        render()
    }

    private fun fmt(t: Long): String = DateFormat.getDateInstance(DateFormat.MEDIUM).format(Date(t))

    private fun render() {
        list.removeAllViews()
        val ls = LetterStore.all(this).sortedBy { it.unlockAt }
        if (ls.isEmpty()) {
            list.addView(label("No letters yet. Write one to your future self \u2728", 15f, Ui.GREY), lp(-1, -2, 20))
            return
        }
        for (l in ls) list.addView(card(l), lp(-1, -2, 12))
    }

    private fun card(l: Letter): View {
        val open = System.currentTimeMillis() >= l.unlockAt
        val c = cardBox(18)
        c.addView(label(if (open) "\uD83D\uDC96 Ready to read" else "\uD83D\uDD12 Locked", 17f, Ui.PLUM, true))
        c.addView(label("Opens: ${fmt(l.unlockAt)}", 13f, Ui.GREY), lp(-2, -2, 4))
        c.addView(label("Written: ${fmt(l.createdAt)}", 13f, Ui.GREY), lp(-2, -2, 2))
        if (open) c.addView(label("Tap to read it in your handwriting", 13f, Ui.ROSE, true), lp(-2, -2, 8))
        c.isClickable = true
        c.setOnClickListener {
            if (open) {
                startActivity(Intent(this, EditorActivity::class.java).putExtra("text", l.body).putExtra("paper", 1))
            } else {
                toast("Still locked. It opens on ${fmt(l.unlockAt)} \uD83D\uDC8C")
            }
        }
        c.setOnLongClickListener {
            AlertDialog.Builder(this).setTitle("Delete this letter?")
                .setPositiveButton("Delete") { _, _ -> LetterStore.delete(this, l.id); render() }
                .setNegativeButton("Cancel", null).show()
            true
        }
        return c
    }
}
