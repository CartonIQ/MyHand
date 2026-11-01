package com.myhand.app

import android.app.AlarmManager
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import org.json.JSONArray
import org.json.JSONObject
import java.io.File

class Letter(val id: Long, val createdAt: Long, val unlockAt: Long, val body: String)

object LetterStore {
    private fun file(ctx: Context) = File(ctx.filesDir, "letters.json")

    fun all(ctx: Context): MutableList<Letter> {
        val out = ArrayList<Letter>()
        val f = file(ctx)
        if (!f.exists()) return out
        try {
            val arr = JSONArray(f.readText())
            for (i in 0 until arr.length()) {
                val o = arr.getJSONObject(i)
                out.add(Letter(o.getLong("id"), o.getLong("created"), o.getLong("unlock"), o.getString("body")))
            }
        } catch (e: Exception) {
        }
        return out
    }

    fun save(ctx: Context, list: List<Letter>) {
        val arr = JSONArray()
        for (l in list) {
            arr.put(JSONObject().put("id", l.id).put("created", l.createdAt).put("unlock", l.unlockAt).put("body", l.body))
        }
        file(ctx).writeText(arr.toString())
    }

    fun add(ctx: Context, l: Letter) {
        val list = all(ctx)
        list.add(l)
        save(ctx, list)
        Scheduler.schedule(ctx, l)
    }

    fun delete(ctx: Context, id: Long) {
        val list = all(ctx)
        list.removeAll { it.id == id }
        save(ctx, list)
        Scheduler.cancel(ctx, id)
    }
}

object Scheduler {
    const val ACTION = "com.myhand.app.LETTER_UNLOCK"

    private fun pi(ctx: Context, id: Long, flags: Int): PendingIntent? {
        val i = Intent(ctx, LetterReceiver::class.java).setAction(ACTION).putExtra("id", id)
        return PendingIntent.getBroadcast(ctx, (id and 0x7fffffffL).toInt(), i, flags or PendingIntent.FLAG_IMMUTABLE)
    }

    fun schedule(ctx: Context, l: Letter) {
        if (l.unlockAt <= System.currentTimeMillis()) return
        val p = pi(ctx, l.id, PendingIntent.FLAG_UPDATE_CURRENT) ?: return
        val am = ctx.getSystemService(AlarmManager::class.java)
        am.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, l.unlockAt, p)
    }

    fun cancel(ctx: Context, id: Long) {
        val p = pi(ctx, id, PendingIntent.FLAG_NO_CREATE) ?: return
        ctx.getSystemService(AlarmManager::class.java).cancel(p)
    }

    fun rescheduleAll(ctx: Context) {
        for (l in LetterStore.all(ctx)) schedule(ctx, l)
    }
}

class LetterReceiver : BroadcastReceiver() {
    override fun onReceive(ctx: Context, i: Intent) {
        val a = i.action
        if (a == Intent.ACTION_BOOT_COMPLETED) {
            Scheduler.rescheduleAll(ctx)
        } else if (a == Scheduler.ACTION) {
            notifyUnlock(ctx)
        }
    }

    private fun notifyUnlock(ctx: Context) {
        val nm = ctx.getSystemService(NotificationManager::class.java)
        val ch = "letters"
        nm.createNotificationChannel(NotificationChannel(ch, "Letters to Future You", NotificationManager.IMPORTANCE_DEFAULT))
        val open = PendingIntent.getActivity(
            ctx, 1,
            Intent(ctx, LettersActivity::class.java).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK),
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )
        val n = Notification.Builder(ctx, ch)
            .setSmallIcon(android.R.drawable.ic_dialog_email)
            .setContentTitle("A letter from past you is ready")
            .setContentText("Tap to open it")
            .setContentIntent(open)
            .setAutoCancel(true)
            .build()
        nm.notify((System.currentTimeMillis() and 0x7fffffffL).toInt(), n)
    }
}
