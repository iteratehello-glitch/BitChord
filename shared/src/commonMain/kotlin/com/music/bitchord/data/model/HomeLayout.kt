package com.music.bitchord.data.model

/** Device-local home preferences. Unfamiliar/new recommendation shelves remain visible. */
data class HomeLayout(
    val showSpeedDial: Boolean = true,
    val columns: Int = 3,
    val rows: Int = 2,
    val shelfOrder: List<String> = emptyList(),
    val hiddenShelves: Set<String> = emptySet(),
) {
    fun normalized() = copy(columns = columns.coerceIn(2, 3), rows = rows.coerceIn(1, 3), shelfOrder = shelfOrder.distinct())

    fun shelves(source: List<HomeShelf>, includeHidden: Boolean = false): List<HomeShelf> {
        val rank = shelfOrder.withIndex().associate { it.value to it.index }
        return source.filter { !it.isHomeRecents() && (includeHidden || it.title !in hiddenShelves) }
            .sortedBy { rank[it.title] ?: Int.MAX_VALUE }
    }
}

fun HomeShelf.isHomeRecents(): Boolean = title.equals("Recents", ignoreCase = true)

/** Recent tracks first; recommendations fill gaps. Collections never become random-play targets. */
fun homeSpeedDial(shelves: List<HomeShelf>, count: Int): List<ShelfItem> =
    (shelves.filter { it.isHomeRecents() } + shelves.filterNot { it.isHomeRecents() })
        .flatMap { it.items }.filter { !it.videoId.isNullOrBlank() }
        .distinctBy { it.videoId }.take(count.coerceAtLeast(0))

fun homeRandomCandidates(items: List<ShelfItem>, currentVideoId: String?): List<ShelfItem> {
    val playable = items.filter { !it.videoId.isNullOrBlank() }.distinctBy { it.videoId }
    return playable.filter { it.videoId != currentVideoId }.ifEmpty { playable }
}
