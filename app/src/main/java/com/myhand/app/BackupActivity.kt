package com.myhand.app

import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView

class BackupActivity : Activity() {
    private val REQ = 77
    private lateinit var info: TextView

    override fun onCreate(b: Bundle?) {
        super.onCreate(b)
        val scroll = ScrollView(this)
        setupWindow(scroll)
        val col = LinearLayout(this)
        col.orientation = LinearLayout.VERTICAL
        col.setPadding(dp(18), dp(30), dp(18), dp(20))
        scroll.addView(col)

        col.addView(script("Backup and restore", 32f, Ui.ROSE))
        col.addView(label("Keep your handwriting safe, even if you change phones \uD83D\uDC9C", 14f, Ui.GREY))

        val c1 = cardBox(18)
        c1.addView(label("Save a backup", 17f, Ui.PLUM, true))
        c1.addView(label("Creates one file with your handwriting and letters. Save it to Google Drive, Files or send it to yourself.", 14f, Ui.GREY), lp(-1, -2, 6))
        c1.addView(pillButton("Create backup file") { create() }, lp(-1, -2, 14))
        col.addView(c1, lp(-1, -2, 16))

        val c2 = cardBox(18)
        c2.addView(label("Restore", 17f, Ui.PLUM, true))
        c2.addView(label("Pick a MyHand backup file. This replaces the handwriting and letters currently in the app.", 14f, Ui.GREY), lp(-1, -2, 6))
        c2.addView(pillButton("Restore from backup", false) {
            val i = Intent(Intent.ACTION_GET_CONTENT).apply { type = "*/*" }
            startActivityForResult(Intent.createChooser(i, "Choose backup file"), REQ)
        }, lp(-1, -2, 14))
        col.addView(c2, lp(-1, -2, 14))

        info = label("", 15f, Ui.PLUM)
        col.addView(info, lp(-1, -2, 14))
        col.addView(label("Note: uninstalling the app deletes your handwriting unless you made a backup first.", 12f, Ui.GREY), lp(-1, -2, 10))
        setContentView(scroll)
    }

    private fun create() {
        try {
            val f = BackupUtil.create(this)
            ShareUtil.shareFile(this, f, "application/zip", "Save your backup")
        } catch (e: Exception) {
            toast("Backup failed: ${e.message}")
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode != REQ || resultCode != RESULT_OK) return
        val uri: Uri = data?.data ?: return
        AlertDialog.Builder(this).setTitle("Replace current data?")
            .setMessage("Your current handwriting and letters will be replaced by the backup.")
            .setPositiveButton("Restore") { _, _ -> doRestore(uri) }
            .setNegativeButton("Cancel", null).show()
    }

    private fun doRestore(uri: Uri) {
        info.text = "Restoring..."
        Thread {
            val msg = try {
                BackupUtil.restore(this, uri)
            } catch (e: Throwable) {
                "Restore failed: ${e.message}"
            }
            runOnUiThread { info.text = msg }
        }.start()
    }
}
