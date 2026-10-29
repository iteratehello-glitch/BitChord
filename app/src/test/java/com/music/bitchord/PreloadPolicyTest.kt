package com.music.bitchord

import com.music.bitchord.data.settings.SongPreloadMode
import com.music.bitchord.playback.preloadQueueIndices
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class PreloadPolicyTest {
    @Test
    fun `off does not even ask for the next track`() {
        val result = preloadQueueIndices(0, 5, SongPreloadMode.AFTER_CURRENT.songCount, false) {
            error("Disabled preloading must not inspect upcoming items")
        }
        assertTrue(result.isEmpty())
    }

    @Test
    fun `one and three select exactly that many upcoming slots`() {
        assertEquals(listOf(1), preloadQueueIndices(0, 5, SongPreloadMode.ONE.songCount, false) { it + 1 })
        assertEquals(listOf(1, 2, 3), preloadQueueIndices(0, 5, SongPreloadMode.THREE.songCount, false) { it + 1 })
    }

    @Test
    fun `short queue ends without inventing tracks`() {
        assertEquals(listOf(2), preloadQueueIndices(1, 3, 3, false) { it + 1 })
        assertTrue(preloadQueueIndices(2, 3, 3, false) { -1 }.isEmpty())
    }

    @Test
    fun `repeat all wraps without preloading the playing item`() {
        assertEquals(listOf(0, 1, 2), preloadQueueIndices(3, 4, 3, false) { (it + 1) % 4 })
        assertEquals(listOf(0), preloadQueueIndices(1, 2, 3, false) { (it + 1) % 2 })
    }

    @Test
    fun `repeat one and a single song have nothing to preload`() {
        assertTrue(preloadQueueIndices(1, 5, 3, true) { error("repeat one") }.isEmpty())
        assertTrue(preloadQueueIndices(0, 1, 3, false) { 0 }.isEmpty())
    }

    @Test
    fun `shuffle follows the player order rather than incrementing indices`() {
        val next = mapOf(0 to 4, 4 to 1, 1 to 3, 3 to 2)
        assertEquals(listOf(4, 1, 3), preloadQueueIndices(0, 5, 3, false) { next[it] ?: -1 })
    }

    @Test
    fun `cycle and invalid playhead terminate safely`() {
        assertEquals(listOf(2), preloadQueueIndices(0, 5, 3, false) { 2 })
        assertTrue(preloadQueueIndices(-1, 0, 3, false) { error("empty queue") }.isEmpty())
    }

    @Test
    fun `saved settings round trip and missing or unknown values keep the default`() {
        SongPreloadMode.entries.forEach { assertEquals(it, SongPreloadMode.fromStoredName(it.name)) }
        assertEquals(SongPreloadMode.ONE, SongPreloadMode.fromStoredName(null))
        assertEquals(SongPreloadMode.ONE, SongPreloadMode.fromStoredName("invalid"))
    }
}
