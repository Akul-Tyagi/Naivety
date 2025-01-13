// BookGrid.kt
package com.example.naivety.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.naivety.models.Book
import com.example.naivety.viewmodels.BookViewModel

@Composable
fun BookGrid(
    books: List<Book>,
    onBookClick: (Book) -> Unit,
    viewModel: BookViewModel,
    isLoading: Boolean = false,
    modifier: Modifier = Modifier
) {
    Box(modifier = modifier.fillMaxSize()) {
        when {
            isLoading -> {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center),
                    color = Color(0xFF8E42FF)
                )
            }
            books.isEmpty() -> {
                Text(
                    text = "A library without books is just a room. Time to build your collection.",
                    color = Color.Gray,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .align(Alignment.Center)
                        .padding(32.dp)
                )
            }
            else -> {
                val chunkedBooks = books.chunked(2) // Split books into pairs for 2 columns

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(24.dp)
                ) {
                    chunkedBooks.forEach { rowBooks ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            rowBooks.forEach { book ->
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                ) {
                                    BookItem(
                                        book = book,
                                        onClick = { onBookClick(book) },
                                        viewModel = viewModel
                                    )
                                }
                            }

                            // If odd number of books, add empty space to maintain grid
                            if (rowBooks.size == 1) {
                                Spacer(modifier = Modifier.weight(1f))
                            }
                        }
                    }
                }
            }
        }
    }
}