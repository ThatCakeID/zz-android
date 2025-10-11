package com.thatcakeid.zrytezene

import android.app.AlarmManager
import android.app.Application
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.icu.text.CompactDecimalFormat
import android.icu.util.ULocale
import android.os.Process
import com.google.firebase.Timestamp
import com.thatcakeid.zrytezene.ui.DebugActivity
import java.text.DecimalFormat
import java.text.SimpleDateFormat
import java.util.Locale
import kotlin.system.exitProcess

class HelperClass : Application() {
    override fun onCreate() {
        super.onCreate()

        Thread.setDefaultUncaughtExceptionHandler { _, ex ->
            val intent = Intent(applicationContext, DebugActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_CLEAR_TASK
                putExtra("error", ex.stackTraceToString())
            }

            val pendingIntent = PendingIntent.getActivity(
                applicationContext,
                1111,
                intent,
                PendingIntent.FLAG_ONE_SHOT or PendingIntent.FLAG_IMMUTABLE
            )

            val am = getSystemService(Context.ALARM_SERVICE) as AlarmManager
            am[AlarmManager.ELAPSED_REALTIME_WAKEUP, 1000] = pendingIntent

            Process.killProcess(Process.myPid())
            exitProcess(1)
        }
    }

    companion object {
        fun parseDuration(duration: Long): String {
            return DecimalFormat("0").format(duration / 60000) +
                    ":" + DecimalFormat("00").format(duration / 1000 % 60)
        }

        // TODO: Stub
        fun getPrettyDateFormat(timestamp: Timestamp): String {
            return SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                .format(timestamp.toDate())
        }

        fun getPrettyPlaysCount(plays: Number?): String {
            return CompactDecimalFormat.getInstance(ULocale.getDefault(),
                CompactDecimalFormat.CompactStyle.SHORT).format(plays) + " x played"
        }
    }
}