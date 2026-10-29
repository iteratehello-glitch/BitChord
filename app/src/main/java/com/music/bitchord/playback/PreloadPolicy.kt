package com.music.bitchord.playback

/** Follow the player's actual order, stopping at the end or before revisiting an item. */
internal fun preloadQueueIndices(
    currentIndex: Int,
    itemCount: Int,
    songCount: Int,
    repeatOne: Boolean,
    nextIndex: (Int) -> Int,
): List<Int> {
    if (currentIndex !in 0 until itemCount || songCount <= 0 || repeatOne) return emptyList()
    val visited = mutableSetOf(currentIndex)
    val result = mutableListOf<Int>()
    var index = currentIndex
    repeat(minOf(songCount, itemCount - 1)) {
        index = nextIndex(index)
        if (index !in 0 until itemCount || !visited.add(index)) return result
        result += index
    }
    return result
}
