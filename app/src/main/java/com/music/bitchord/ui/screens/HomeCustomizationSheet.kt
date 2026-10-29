package com.music.bitchord.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.selection.toggleable
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowDownward
import androidx.compose.material.icons.rounded.ArrowUpward
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.unit.dp
import com.music.bitchord.R
import com.music.bitchord.data.model.HomeLayout
import com.music.bitchord.data.model.HomeShelf

/** Uses the same sheet, typography and controls as Settings; changes affect Home only. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeCustomizationSheet(
    layout: HomeLayout,
    shelves: List<HomeShelf>,
    onChange: (HomeLayout) -> Unit,
    onDismiss: () -> Unit,
) {
    val sections = layout.shelves(shelves, includeHidden = true).distinctBy { it.title }
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = MaterialTheme.colorScheme.background,
    ) {
        Column(Modifier.fillMaxWidth().heightIn(max = LocalConfiguration.current.screenHeightDp.dp * 0.85f)) {
            Row(Modifier.padding(start = 22.dp, end = 12.dp), verticalAlignment = Alignment.CenterVertically) {
                Text(stringResource(R.string.home_customize), style = MaterialTheme.typography.titleLarge, modifier = Modifier.weight(1f))
                TextButton(onClick = onDismiss) { Text(stringResource(R.string.done)) }
            }
            LazyColumn(modifier = Modifier.testTag("home_customization"), contentPadding = PaddingValues(start = 22.dp, end = 22.dp, bottom = 24.dp)) {
                item {
                    Text(
                        stringResource(R.string.home_customize_description),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(top = 8.dp, bottom = 20.dp),
                    )
                    Row(
                        Modifier.fillMaxWidth().toggleable(
                            value = layout.showSpeedDial, role = Role.Switch,
                            onValueChange = { onChange(layout.copy(showSpeedDial = it)) },
                        ).padding(vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Column(Modifier.weight(1f).padding(end = 12.dp)) {
                            Text(stringResource(R.string.home_speed_dial), style = MaterialTheme.typography.titleMedium)
                            Text(stringResource(R.string.home_speed_dial_detail), style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Switch(checked = layout.showSpeedDial, onCheckedChange = null)
                    }
                    HomeGridChoice(stringResource(R.string.home_grid_columns), listOf(2, 3), layout.columns, layout.showSpeedDial) {
                        onChange(layout.copy(columns = it))
                    }
                    HomeGridChoice(stringResource(R.string.home_grid_rows), listOf(1, 2, 3), layout.rows, layout.showSpeedDial) {
                        onChange(layout.copy(rows = it))
                    }
                    HorizontalDivider(Modifier.padding(vertical = 20.dp))
                    Text(stringResource(R.string.home_sections), style = MaterialTheme.typography.titleMedium)
                    Text(
                        stringResource(R.string.home_sections_detail), style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(top = 6.dp, bottom = 12.dp),
                    )
                }
                itemsIndexed(sections, key = { _, shelf -> shelf.title }) { index, shelf ->
                    fun move(offset: Int) {
                        val order = sections.map { it.title }.toMutableList()
                        val target = index + offset
                        if (target !in order.indices) return
                        order.add(target, order.removeAt(index))
                        // Retain preferences for shelves temporarily absent from this recommendation page.
                        onChange(layout.copy(shelfOrder = order + layout.shelfOrder.filterNot { it in order }))
                    }
                    val sectionLabel = stringResource(R.string.home_show_section, shelf.title)
                    Row(Modifier.fillMaxWidth().padding(vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
                        Text(shelf.title, style = MaterialTheme.typography.bodyLarge, modifier = Modifier.weight(1f).padding(end = 4.dp))
                        IconButton(onClick = { move(-1) }, enabled = index > 0) {
                            Icon(Icons.Rounded.ArrowUpward, contentDescription = stringResource(R.string.home_move_up, shelf.title))
                        }
                        IconButton(onClick = { move(1) }, enabled = index < sections.lastIndex) {
                            Icon(Icons.Rounded.ArrowDownward, contentDescription = stringResource(R.string.home_move_down, shelf.title))
                        }
                        Switch(
                            checked = shelf.title !in layout.hiddenShelves,
                            onCheckedChange = { visible ->
                                onChange(layout.copy(hiddenShelves = if (visible) layout.hiddenShelves - shelf.title else layout.hiddenShelves + shelf.title))
                            },
                            modifier = Modifier.semantics { contentDescription = sectionLabel },
                        )
                    }
                }
                item {
                    TextButton(onClick = { onChange(HomeLayout()) }, modifier = Modifier.fillMaxWidth().padding(top = 16.dp)) {
                        Text(stringResource(R.string.home_reset))
                    }
                }
            }
        }
    }
}

@Composable
private fun HomeGridChoice(label: String, choices: List<Int>, selected: Int, enabled: Boolean, onSelect: (Int) -> Unit) {
    Row(Modifier.fillMaxWidth().padding(top = 8.dp), verticalAlignment = Alignment.CenterVertically) {
        Text(label, modifier = Modifier.weight(1f), style = MaterialTheme.typography.bodyLarge)
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            choices.forEach { value ->
                FilterChip(
                    selected = selected == value, onClick = { onSelect(value) }, enabled = enabled,
                    label = { Text(stringResource(R.string.home_grid_number, value)) },
                )
            }
        }
    }
}
