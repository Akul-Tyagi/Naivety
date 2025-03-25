// BookItem.kt
package com.example.naivety.ui.components

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.naivety.models.Book
import androidx.compose.foundation.background
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.sp
import com.example.naivety.R
import com.example.naivety.ui.theme.NaivetyPurple
import com.example.naivety.viewmodels.BookViewModel

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun BookItem(
    book: Book,
    onClick: () -> Unit,
    onLongPress: () -> Unit = {},
    viewModel: BookViewModel,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .aspectRatio(0.7f)
            .clip(RoundedCornerShape(12.dp))
            .combinedClickable(
                onClick = onClick,
                onLongClick = onLongPress
            )
    ) {
        // Book thumbnail
        AsyncImage(
            model = book.thumbnailPath,
            contentDescription = "Cover of ${book.title}",
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )

        // Pages remaining indicator
        if (book.lastReadPage > 0 && book.totalPages > 0) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(7.dp)
                    .background(
                        color = NaivetyPurple,
                        shape = RoundedCornerShape(9.dp)
                    )
                    .padding(horizontal = 6.dp)
            ) {
                Text(
                    text = "${viewModel.getPagesRemaining(book)}",
                    color = Color.White,
                    fontSize = 10.sp,
                    fontFamily = FontFamily(Font(R.font.alinsa)),
                    maxLines = 1
                )
            }
        }

        // Progress indicator
        if (book.lastReadPage > 0) {
            LinearProgressIndicator(
                progress = viewModel.getReadingProgress(book),
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .height(4.dp),
                color = NaivetyPurple,
                trackColor = Color.Black.copy(alpha = 0.5f)
            )
        }
    }
}