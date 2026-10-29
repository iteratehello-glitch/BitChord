package com.music.bitchord

import android.graphics.Bitmap
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import androidx.test.platform.app.InstrumentationRegistry
import com.music.bitchord.data.model.*
import com.music.bitchord.ui.components.LocalLiquidGlassEnabled
import com.music.bitchord.ui.components.lightweightLiquidGlass
import com.music.bitchord.ui.screens.HomeCustomizationSheet
import com.music.bitchord.ui.screens.HomeScreen
import com.music.bitchord.ui.theme.BitChordTheme
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test
import java.io.File

class HomeCustomizationUiTest {
    @get:Rule val compose = createComposeRule()
    private val tracks = (1..9).map { ShelfItem("Song $it", "Artist $it", null, "song-$it", null) }
    private val shelves = listOf(
        HomeShelf("Recents", tracks.take(6)),
        HomeShelf("Quick picks", tracks),
        HomeShelf("Listen again", tracks.reversed()),
        HomeShelf("Albums for you", tracks),
    )
    private var played: String? = null

    @OptIn(ExperimentalMaterial3Api::class)
    private fun showHome(fontScale: Float = 1f) {
        compose.setContent {
            BitChordTheme(darkTheme = true) {
                val density = LocalDensity.current
                var layout by remember { mutableStateOf(HomeLayout()) }
                var customize by remember { mutableStateOf(false) }
                CompositionLocalProvider(
                    LocalDensity provides Density(density.density, fontScale),
                    LocalLiquidGlassEnabled provides true,
                ) {
                    Box(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
                        HomeScreen(
                            state = UiState.Success(shelves), listState = rememberLazyListState(),
                            onItemClick = { item, _ -> played = item.videoId },
                            onRetry = {}, refreshing = false, onRefresh = {},
                            pullState = rememberPullToRefreshState(), contentPadding = PaddingValues(top = 36.dp, bottom = 24.dp),
                            title = "Home", homeLayout = layout, hasMore = false,
                            onCustomizeHome = { customize = true },
                            controlSurface = { it.lightweightLiquidGlass(CircleShape, MaterialTheme.colorScheme.surface) },
                        )
                    }
                    if (customize) HomeCustomizationSheet(layout, shelves, { layout = it }, { customize = false })
                }
            }
        }
    }

    private fun capture(name: String) {
        compose.waitForIdle()
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val file = File(context.getExternalFilesDir(null), "home-ui/$name.png")
        file.parentFile!!.mkdirs()
        file.outputStream().use { compose.onRoot().captureToImage().asAndroidBitmap().compress(Bitmap.CompressFormat.PNG, 100, it) }
    }

    @Test fun tabsRandomAndCustomizationFooterWork() {
        showHome()
        compose.onNodeWithText("Home").assertIsDisplayed()
        compose.onNodeWithText("Speed Dial").assertIsDisplayed()
        capture("home-top")
        compose.onNodeWithText("Random").performClick()
        compose.runOnIdle { assertTrue(played in tracks.take(6).map { it.videoId }) }
        compose.onNodeWithText("Recents").performClick()
        compose.onNodeWithText("Random").assertDoesNotExist()
        compose.onNodeWithText("Song 1").assertIsDisplayed()
        compose.onNodeWithText("For you").performClick()
        compose.onNodeWithTag("home_feed").performScrollToNode(hasText("Customize home"))
        capture("home-footer")
        compose.onNodeWithText("Customize home").performClick()
        compose.onNodeWithTag("home_customization").performScrollToNode(hasText("Reset home layout"))
        compose.onNodeWithText("Reset home layout").assertIsDisplayed()
        compose.onNodeWithText("Reset home layout").performClick()
        compose.onNodeWithText("Done").performClick()
    }

    @Test fun largeTextKeepsTabsRandomAndFooterUsable() {
        showHome(fontScale = 1.3f)
        compose.onNodeWithText("Random").assertIsDisplayed().performClick()
        capture("home-large-text")
        compose.onNodeWithTag("home_feed").performScrollToNode(hasText("Customize home"))
        compose.onNodeWithText("Customize home").assertIsDisplayed().performClick()
        // Hiding Speed Dial must not strand the customization controls.
        compose.onNode(hasText("Speed Dial") and SemanticsMatcher.expectValue(SemanticsProperties.Role, Role.Switch)).performClick()
        compose.onNodeWithText("Done").performClick()
        compose.onNodeWithText("Customize home").assertIsDisplayed()
    }
}
