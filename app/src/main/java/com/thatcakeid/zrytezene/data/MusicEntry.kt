package com.thatcakeid.zrytezene.data

import com.google.firebase.Timestamp

data class MusicEntry(
    val authorUserId: String,
    val musicUrl: String,
    val plays: Number,
    val thumb: String?,
    val time: Timestamp,
    val title: String,
) {
    companion object {
        fun from(map: Map<String, Any>): MusicEntry {
            val thumb = map["thumb"] as String

            return MusicEntry(
                map["author"] as String,
                map["music_url"] as String,
                map["plays"] as Number,
                if (thumb == "") null else thumb,
                map["time"] as Timestamp,
                map["title"] as String,
            )
        }
    }
}
