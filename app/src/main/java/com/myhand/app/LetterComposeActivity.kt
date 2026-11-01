package com.myhand.app

import android.app.Activity
import android.app.DatePickerDialog
import android.os.Build
import android.os.Bundle
import android.text.InputType
import android.view.Gravity
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import java.text.DateFormat
import java.util.Calendar
import java.util.Date

class LetterComposeActivity : Activity() {
    private var unlockAt = 0L
    private lateinit var whenTv: TextView

    override fun onCreate(b: Bundle?) {
        super.onCreate(b)
        unlockAt = at9(Calendar.getInstance().apply { add(Calendar.YEAR, 1) })
        val scroll = ScrollView(this)
        setupWindow(scroll)
        val col = LinearLayout(this)
        col.orientation = LinearLayout.VERTICAL
        col.setPadding(dp(18), dp(30), dp(18), dp(20))
        scroll.addView(col)

        col.addView(script("Dear future me...", 32f, Ui.ROSE))

        val card = cardBox(16)
        val et = EditText(this)
        et.hint = "Write to your future self. What do you hope for? What do you want to remember?"
        et.minLines = 8
        et.gravity = Gravity.TOP
        et.setTextColor(Ui.PLUM)
        et.background = rounded(Ui.LAV_LIGHT, 14)
        et.setPadding(dp(12), dp(10), dp(12), dp(10))
        et.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_FLAG_MULTI_LINE or
            InputType.TYPE_TEXT_FLAG_CAP_SENTENCES
        card.addView(et, lp(-1, -2))

        card.addView(label("When should it open?", 14f, Ui.GREY, true), lp(-2, -2, 16))
        val chips = ChipRow(this, listOf("1 month", "6 months", "1 year", "5 years", "Pick a date"), 2) { i ->
            when (i) {
                0 -> setIn(Calendar.MONTH, 1)
                1 -> setIn(Calendar.MONTH, 6)
                2 -> setIn(Calendar.YEAR, 1)
                3 -> setIn(Calendar.YEAR, 5)
                else -> pickDate()
            }
        }
        card.addView(chips.view, lp(-1, -2, 8))
        whenTv = label("", 14f, Ui.ROSE, true)
        card.addView(whenTv, lp(-2, -2, 10))
        col.addView(card, lp(-1, -2, 14))

        col.addView(pillButton("Seal this letter \uD83D\uDC8C") { save(et.text.toString()) }, lp(-1, -2, 16))
        col.addView(label("Your letter stays on this phone. It cannot be opened early. You will get a notification when it is ready (the time can be a few minutes off).", 12f, Ui.GREY), lp(-1, -2, 12))
        setContentView(scroll)
        showWhen()
    }

    private fun at9(c: Calendar): Long {
        c.set(Calendar.HOUR_OF_DAY, 9)
        c.set(Calendar.MINUTE, 0)
        c.set(Calendar.SECOND, 0)
        return c.timeInMillis
    }

    private fun setIn(field: Int, amount: Int) {
        val c = Calendar.getInstance()
        c.add(field, amount)
        unlockAt = at9(c)
        showWhen()
    }

    private fun pickDate() {
        val now = Calendar.getInstance()
        val d = DatePickerDialog(this, { _, y, m, day ->
            val c = Calendar.getInstance()
            c.set(y, m, day)
            unlockAt = at9(c)
            showWhen()
        }, now.get(Calendar.YEAR), now.get(Calendar.MONTH), now.get(Calendar.DAY_OF_MONTH))
        d.datePicker.minDate = System.currentTimeMillis() + 24L * 60 * 60 * 1000
        d.show()
    }

    private fun showWhen() {
        whenTv.text = "Opens on " + DateFormat.getDateInstance(DateFormat.LONG).format(Date(unlockAt))
    }

    private fun save(body: String) {
        if (body.isBlank()) {
            toast("Please write something first.")
            return
        }
        if (unlockAt <= System.currentTimeMillis()) {
            toast("Please choose a future date.")
            return
        }
        val now = System.currentTimeMillis()
        LetterStore.add(this, Letter(now, now, unlockAt, body.trim()))
        if (Build.VERSION.SDK_INT >= 33) {
            requestPermissions(arrayOf("android.permission.POST_NOTIFICATIONS"), 7)
        }
        toast("Sealed! See you then \uD83D\uDC8C")
        finish()
    }
}
