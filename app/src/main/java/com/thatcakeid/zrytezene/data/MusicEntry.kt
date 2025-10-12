package com.thatcakeid.zrytezene.data

import com.google.firebase.Timestamp

data class MusicEntry(
    val authorUserId: String?,
    val url: String,
    val playCount: Number,
    val thumb: String?,
    val time: Timestamp,
    val title: String,
) {
    companion object {
        /**
         * Converts a map of values to a [MusicEntry] object.
         *
         * @param map A map containing the values for the [MusicEntry] object.
         * @return A [MusicEntry] object constructed from the given map.
         */
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
