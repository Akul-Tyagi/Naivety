// app/src/main/java/com/abundance/naivety/ui/components/BookItem.kt
package com.abundance.naivety.ui.components

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
import com.abundance.naivety.models.Book
import androidx.compose.foundation.background
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.Alignment
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.sp
import com.abundance.naivety.R
import com.abundance.naivety.ui.theme.NaivetyPurple
import com.abundance.naivety.viewmodels.BookViewModel

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun BookItem(
    book: Book,
    onClick: () -> Unit,
    onLongPress: () -> Unit = {},
    viewModel: BookViewModel,
    modifier: Modifier = Modifier
) {
    // Calculate progress and pages remaining locally
    val readingProgress = if (book.totalPages > 0) {
        (book.lastReadPage.toFloat() / book.totalPages.toFloat()).coerceIn(0f, 1f)
    } else {
        0f
    }

    val pagesRemaining = maxOf(0, book.totalPages - book.lastReadPage)

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

        // Pages remaining indicator - show only if book has been started (lastReadPage > 0)
        if (book.totalPages > 0 && book.lastReadPage > 0) {
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
                    text = "$pagesRemaining",
                    color = MaterialTheme.colorScheme.onBackground,
                    fontSize = 10.sp,
                    fontFamily = FontFamily(Font(R.font.nektar)),
                    maxLines = 1
                )
            }
        }

        // Progress indicator - show only if book has been started (lastReadPage > 0)
        if (book.totalPages > 0 && book.lastReadPage > 0) {
            LinearProgressIndicator(
                progress = { readingProgress },
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .height(4.dp),
                color = NaivetyPurple,
                trackColor = MaterialTheme.colorScheme.background.copy(alpha = 0.5f)
            )
        }
    }
}
