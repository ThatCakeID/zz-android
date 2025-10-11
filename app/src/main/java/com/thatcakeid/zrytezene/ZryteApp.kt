package com.thatcakeid.zrytezene

import android.app.Application
import android.icu.text.CompactDecimalFormat
import android.icu.util.ULocale
import com.google.firebase.Timestamp
import java.text.DecimalFormat
import java.text.SimpleDateFormat
import java.util.Locale

class ZryteApp : Application() {
    companion object {
        fun parseDuration(duration: Long): String {
            return DecimalFormat("0").format(duration / 60000) +
                    ":" + DecimalFormat("00").format(duration / 1000 % 60)
        }

        // TODO: Use proper pretty date format :piola:
        //     10/11/2025; borfei was here :)
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