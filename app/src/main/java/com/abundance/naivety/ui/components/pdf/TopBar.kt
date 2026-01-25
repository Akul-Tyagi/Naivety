// Modified TopBar.kt
package com.abundance.naivety.ui.components.pdf

import androidx.compose.animation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.abundance.naivety.R
import com.abundance.naivety.ui.theme.NaivetyPurple

@Composable
fun TopBar(
    pdfName: String,
    isVisible: Boolean,
    onEditClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    AnimatedVisibility(
        visible = isVisible,
        enter = slideInVertically(initialOffsetY = { -it }) + fadeIn(),
        exit = slideOutVertically(targetOffsetY = { -it }) + fadeOut(),
        modifier = modifier
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.88f)
                .padding(16.dp)
                .padding(top = 16.dp),
            color = MaterialTheme.colorScheme.surface.copy(alpha = 0.91f),
            shape = RoundedCornerShape(28.dp),
            shadowElevation = 8.dp
        ) {
            // Use Box to properly center the text independent of the icon
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 10.dp, horizontal = 14.dp),
                contentAlignment = Alignment.Center
            ) {
                // Centered text - takes full width but text is centered
                Text(
                    text = pdfName,
                    color = NaivetyPurple,
                    fontFamily = FontFamily(Font(R.font.guyongazebor)),
                    fontSize = 25.sp,
                    textAlign = TextAlign.Center,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 28.dp) // Padding on both sides to keep text away from icon area
                )

                // Edit icon positioned at the end
                IconButton(
                    onClick = onEditClick,
                    modifier = Modifier
                        .align(Alignment.CenterEnd)
                        .size(18.dp) // Smaller icon button
                ) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "Edit name",
                        tint = NaivetyPurple,
                        modifier = Modifier.size(14.dp) // Smaller icon
                    )
                }
            }
        }
    }
}