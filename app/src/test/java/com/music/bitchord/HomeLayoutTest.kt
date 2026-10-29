package com.music.bitchord

import com.music.bitchord.data.model.*
import org.junit.Assert.*
import org.junit.Test

class HomeLayoutTest {
    private fun track(id: String) = ShelfItem(id, "Artist", null, id, null)
    private fun shelf(name: String, vararg ids: String) = HomeShelf(name, ids.map(::track))

    @Test fun `speed dial prioritizes history and fills missing slots without repeating songs`() {
        val feed = listOf(shelf("Quick picks", "a", "b", "c"), shelf("Recents", "b", "d"))
        assertEquals(listOf("b", "d", "a", "c"), homeSpeedDial(feed, 6).map { it.videoId })
    }

    @Test fun `collections and malformed song ids cannot be randomly played`() {
        val collection = ShelfItem("Album", "Artist", null, null, "album:123")
        val blank = track("")
        val feed = listOf(HomeShelf("Mixed", listOf(collection, blank, track("song"))))
        assertEquals(listOf("song"), homeSpeedDial(feed, 6).map { it.videoId })
        assertEquals(emptyList<ShelfItem>(), homeRandomCandidates(listOf(collection, blank), null))
    }

    @Test fun `random avoids current song but still works for a one song dial`() {
        assertEquals(listOf(track("b")), homeRandomCandidates(listOf(track("a"), track("b")), "a"))
        assertEquals(listOf(track("a")), homeRandomCandidates(listOf(track("a")), "a"))
    }

    @Test fun `custom order survives refresh and new shelves remain visible`() {
        val layout = HomeLayout(shelfOrder = listOf("Listen again", "Quick picks"), hiddenShelves = setOf("Hidden"))
        val feed = listOf(shelf("New"), shelf("Quick picks"), shelf("Recents"), shelf("Hidden"), shelf("Listen again"))
        assertEquals(listOf("Listen again", "Quick picks", "New"), layout.shelves(feed).map { it.title })
        assertEquals(listOf("Listen again", "Quick picks", "New", "Hidden"), layout.shelves(feed, true).map { it.title })
    }

    @Test fun `hiding all feed sections leaves history available and reset restores feed`() {
        val feed = listOf(shelf("Recents", "a"), shelf("Quick picks", "b"))
        val hidden = HomeLayout(hiddenShelves = setOf("Quick picks"))
        assertTrue(hidden.shelves(feed).isEmpty())
        assertEquals(listOf("a", "b"), homeSpeedDial(feed, 6).map { it.videoId })
        assertEquals(listOf("Quick picks"), HomeLayout().shelves(feed).map { it.title })
    }

    @Test fun `imported grid settings cannot create zero column or oversized layouts`() {
        assertEquals(2, HomeLayout(columns = 0).normalized().columns)
        assertEquals(3, HomeLayout(columns = 100, rows = 100).normalized().rows)
        assertEquals(1, HomeLayout(rows = -1).normalized().rows)
        assertTrue(homeSpeedDial(listOf(shelf("Recents", "a")), -1).isEmpty())
    }
}
