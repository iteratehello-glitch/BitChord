package com.music.bitchord.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Shuffle
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.music.bitchord.data.model.*
import com.music.bitchord.sharedui.resources.*
import com.music.bitchord.ui.components.PAGE_GUTTER
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun HomeSpeedDial(
    items: List<ShelfItem>, columns: Int,
    onItemClick: (ShelfItem, String) -> Unit,
    onItemLongPress: ((ShelfItem) -> Unit)?,
    currentSong: Song?, isPlaying: Boolean,
    controlSurface: @Composable (Modifier) -> Modifier,
) {
    val title = stringResource(Res.string.home_speed_dial)
    Column(Modifier.padding(horizontal = PAGE_GUTTER).padding(top = 8.dp, bottom = 26.dp)) {
        Row(Modifier.fillMaxWidth().padding(bottom = 14.dp), verticalAlignment = Alignment.CenterVertically) {
            Text(title, style = MaterialTheme.typography.titleMedium, modifier = Modifier.weight(1f).semantics { heading() })
            Row(
                controlSurface(Modifier).clickable(role = Role.Button) {
                    homeRandomCandidates(items, currentSong?.videoId).randomOrNull()?.let { onItemClick(it, title) }
                }.padding(horizontal = 16.dp, vertical = 14.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(Icons.Rounded.Shuffle, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(6.dp))
                Text(stringResource(Res.string.home_random), style = MaterialTheme.typography.labelLarge)
            }
        }
        Column(verticalArrangement = Arrangement.spacedBy(18.dp)) {
            items.chunked(columns).forEach { row ->
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                    row.forEach { item ->
                        ShelfCard(
                            item = item, onClick = { onItemClick(item, title) },
                            onLongPress = onItemLongPress?.let { { it(item) } },
                            modifier = Modifier.weight(1f),
                            isCurrent = item.videoId == currentSong?.videoId,
                            isPlaying = item.videoId == currentSong?.videoId && isPlaying,
                        )
                    }
                    repeat(columns - row.size) { Spacer(Modifier.weight(1f)) }
                }
            }
        }
    }
}
