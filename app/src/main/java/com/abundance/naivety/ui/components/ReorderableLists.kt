package com.abundance.naivety.ui.components

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import com.abundance.naivety.data.List

@Composable
fun ReorderableLists(
    lists: kotlin.collections.List<List>,
    selectedListId: String?,
    onListSelect: (String) -> Unit,
    onListReorder: (kotlin.collections.List<List>) -> Unit,
    onEditList: (List) -> Unit,
    modifier: Modifier = Modifier
) {
    var draggingItemIndex by remember { mutableStateOf<Int?>(null) }
    var dropTargetIndex by remember { mutableStateOf<Int?>(null) }
    var dragOffset by remember { mutableStateOf(Offset.Zero) }
    var currentLists by remember(lists) { mutableStateOf(lists) }

    LazyRow(
        modifier = modifier,
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        itemsIndexed(
            items = currentLists,
            key = { _, item -> item.id }
        ) { index, list ->
            val elevation by animateDpAsState(
                if (draggingItemIndex == index) 8.dp else 1.dp,
                label = "elevation"
            )

            ListChip(
                list = list,
                isSelected = list.id == selectedListId,
                onSelect = { onListSelect(list.id) },
                onEdit = { onEditList(list) },
                modifier = Modifier
                    .graphicsLayer {
                        if (draggingItemIndex == index) {
                            translationX = dragOffset.x
                            shadowElevation = 8f
                            scaleX = 1.05f
                            scaleY = 1.05f
                        }
                    }
                    .zIndex(if (draggingItemIndex == index) 1f else 0f)
                    .shadow(elevation)
                    .pointerInput(Unit) {
                        detectDragGesturesAfterLongPress(
                            onDragStart = { offset ->
                                draggingItemIndex = index
                            },
                            onDrag = { change, offset ->
                                change.consume()
                                dragOffset += offset

                                // Calculate potential drop target
                                val dragX = dragOffset.x
                                val itemWidth = size.width + 8.dp.toPx() // Include spacing
                                val newIndex = (index + (dragX / itemWidth).toInt())
                                    .coerceIn(0, currentLists.size - 1)

                                if (newIndex != dropTargetIndex) {
                                    dropTargetIndex = newIndex
                                    // Reorder the list while dragging
                                    currentLists = currentLists.toMutableList().apply {
                                        val item = removeAt(index)
                                        add(newIndex.coerceIn(0, size), item)
                                    }
                                }
                            },
                            onDragEnd = {
                                // Commit the reordering
                                if (draggingItemIndex != null && dropTargetIndex != null) {
                                    onListReorder(currentLists)
                                }
                                draggingItemIndex = null
                                dropTargetIndex = null
                                dragOffset = Offset.Zero
                            },
                            onDragCancel = {
                                draggingItemIndex = null
                                dropTargetIndex = null
                                dragOffset = Offset.Zero
                                // Reset to original order
                                currentLists = lists
                            }
                        )
                    }
            )
        }
    }
}

@Composable
private fun ListChip(
    list: List,
    isSelected: Boolean,
    onSelect: () -> Unit,
    onEdit: () -> Unit,
    modifier: Modifier = Modifier
) {
    ElevatedCard(
        modifier = modifier
            .height(40.dp),
        colors = CardDefaults.elevatedCardColors(
            containerColor = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface
        )
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = list.name,
                color = MaterialTheme.colorScheme.onSurface,
                style = MaterialTheme.typography.bodyMedium
            )
            IconButton(
                onClick = onEdit,
                modifier = Modifier.size(24.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Edit,
                    contentDescription = "Edit list",
                    tint = MaterialTheme.colorScheme.onBackground
                )
            }
        }
    }
}