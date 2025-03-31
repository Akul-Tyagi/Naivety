// app/src/main/java/com/abundance/naivety/ui/components/SearchBar.kt
package com.abundance.naivety.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun SearchBar(
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    onCloseSearch: () -> Unit,
    alinsaFont: FontFamily
) {
    Row(
        modifier = Modifier
            .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(20.dp))
            .width(200.dp)
            .height(40.dp)
            .padding(horizontal = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        BasicTextField(
            value = searchQuery,
            onValueChange = onSearchQueryChange,
            singleLine = true,
            cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
            textStyle = TextStyle(
                color = MaterialTheme.colorScheme.onSurface,
                fontSize = 14.sp,
                fontFamily = alinsaFont
            ),
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 8.dp)
        )

        IconButton(
            onClick = onCloseSearch,
            modifier = Modifier.size(24.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Close,
                contentDescription = "Close search",
                tint = MaterialTheme.colorScheme.onBackground
            )
        }
    }
}