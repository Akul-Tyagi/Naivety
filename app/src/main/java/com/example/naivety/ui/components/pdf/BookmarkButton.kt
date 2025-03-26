// app/src/main/java/com/example/naivety/ui/components/pdf/BookmarkButton.kt
package com.example.naivety.ui.components.pdf

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.outlined.BookmarkBorder
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.graphics.Color
import com.example.naivety.ui.theme.NaivetyPurple

@Composable
fun BookmarkButton(
    isBookmarked: Boolean,
    onBookmarkChange: (Boolean) -> Unit
) {
    val iconTint by animateColorAsState(
        targetValue = if (isBookmarked) NaivetyPurple else MaterialTheme.colorScheme.onBackground,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        )
    )

    IconButton(onClick = { onBookmarkChange(!isBookmarked) }) {
        Icon(
            imageVector = if (isBookmarked) Icons.Filled.Bookmark else Icons.Outlined.BookmarkBorder,
            contentDescription = if (isBookmarked) "Remove bookmark" else "Add bookmark",
            tint = iconTint
        )
    }
}