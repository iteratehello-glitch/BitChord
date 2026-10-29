package com.music.bitchord.data.settings

/** Background audio caching; independent of the stream's quality ceiling. */
enum class SongPreloadMode(val songCount: Int) {
    AFTER_CURRENT(0),
    ONE(1),
    THREE(3);

    companion object {
        fun fromStoredName(name: String?): SongPreloadMode =
            entries.firstOrNull { it.name == name } ?: ONE
    }
}
