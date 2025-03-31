// app/src/main/java/com/abundance/naivety/ui/components/SortDropdownMenu.kt
package com.abundance.naivety.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import com.abundance.naivety.models.SortOrder

@Composable
fun SortDropdownMenu(
    expanded: Boolean,
    onDismiss: () -> Unit,
    onSortBooks: (SortOrder) -> Unit,
    alinsaFont: FontFamily
) {
    DropdownMenu(
        expanded = expanded,
        onDismissRequest = onDismiss,
        modifier = Modifier
            .background(MaterialTheme.colorScheme.background)
            .width(180.dp),
        offset = DpOffset(x = (-120).dp, y = 8.dp),
        shape = RoundedCornerShape(10.dp)
    ) {
        DropdownMenuItem(
            text = {
                Text(
                    "Recently Added",
                    color = MaterialTheme.colorScheme.onSurface,
                    fontFamily = alinsaFont
                )
            },
            onClick = {
                onSortBooks(SortOrder.RECENT)
                onDismiss()
            }
        )
        DropdownMenuItem(
            text = {
                Text(
                    "Title: A to Z",
                    color = MaterialTheme.colorScheme.onSurface,
                    fontFamily = alinsaFont
                )
            },
            onClick = {
                onSortBooks(SortOrder.TITLE)
                onDismiss()
            }
        )
        DropdownMenuItem(
            text = {
                Text(
                    "Progress",
                    color = MaterialTheme.colorScheme.onSurface,
                    fontFamily = alinsaFont
                )
            },
            onClick = {
                onSortBooks(SortOrder.PROGRESS)
                onDismiss()
            }
        )
        DropdownMenuItem(
            text = {
                Text(
                    "Author",
                    color = MaterialTheme.colorScheme.onSurface,
                    fontFamily = alinsaFont
                )
            },
            onClick = {
                onSortBooks(SortOrder.AUTHOR)
                onDismiss()
            }
        )
    }
}