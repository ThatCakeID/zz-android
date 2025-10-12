package com.thatcakeid.zrytezene.data

import com.google.firebase.Timestamp

data class UserEntry(
    val id: String,
    val imgUrl: String?,
    val mail: String?,
    val timeCreation: Timestamp,
    val username: String?,
) {
    companion object {
        /**
         * Converts a map of values to a [UserEntry] object.
         *
         * @param map A map containing the values for the [UserEntry] object.
         * @return A [UserEntry] object constructed from the given map.
         */
        fun from(map: Map<String, Any>): UserEntry {
            val imgUrl = map["img_url"] as String
            val mail = map["mail"] as String

            return UserEntry(
                map["uid"] as String,
                if (imgUrl == "") null else imgUrl,
                if (mail == "") null else mail,
                map["time_creation"] as Timestamp,
                map["username"] as String,
            )
        }
    }
}
