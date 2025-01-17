// app/src/main/java/com/example/naivety/ui/screens/BrowseScreen.kt

package com.example.naivety.ui.screens

import BookCard
import BookPreviewModal
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import com.example.naivety.models.OpenLibraryBook
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridItemSpan
import androidx.compose.foundation.lazy.staggeredgrid.rememberLazyStaggeredGridState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.blur
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.paging.LoadState
import androidx.paging.compose.collectAsLazyPagingItems
import com.example.naivety.R
import com.example.naivety.viewmodels.BrowseViewModel
import kotlin.coroutines.ContinuationInterceptor
import kotlin.coroutines.CoroutineContext

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun BrowseScreen(
    viewModel: BrowseViewModel = hiltViewModel(),
    onBookClick: (OpenLibraryBook) -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    val books = viewModel.books.collectAsLazyPagingItems()
    val selectedBook by viewModel.selectedBook.collectAsState()
    val fsFont = FontFamily(Font(R.font.fsultralit))

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        // Search Bar
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(34.dp) // Fixed height for consistent alignment
                    .background(Color.Transparent, RoundedCornerShape(28.dp))
                    .padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically,

                ) {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = "Search",
                    tint = Color.Gray,
                    modifier = Modifier.size(20.dp)
                )

                Spacer(modifier = Modifier.width(12.dp))

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .background(Color.Transparent)
                        .height(34.dp), // Match parent height
                    contentAlignment = Alignment.CenterStart // Center the content vertically
                ) {
                    BasicTextField(
                        value = searchQuery,
                        onValueChange = { newQuery ->
                            searchQuery = newQuery
                            // Don't trigger search immediately on each keystroke
                        },
                        textStyle = TextStyle(
                            color = Color.White,
                            fontSize = 20.sp,
                            fontFamily = fsFont
                        ),
                        cursorBrush = SolidColor(Color(0xFF8E42FF)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .align(Alignment.CenterStart)
                            .onKeyEvent { keyEvent ->
                                if (keyEvent.key == Key.Enter) {
                                    viewModel.searchBooks(searchQuery)
                                    true
                                } else false
                            },
                        decorationBox = { innerTextField ->
                            Box(
                                contentAlignment = Alignment.CenterStart // Center placeholder text
                            ) {
                                if (searchQuery.isEmpty()) {
                                    Text(
                                        text = "Find a story worth staying up for.",
                                        color = Color.Gray,
                                        fontSize = 20.sp,
                                        fontFamily = fsFont
                                    )
                                }
                                innerTextField()
                            }
                        }
                    )
                }
            }
        }
        // Book Grid
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black)
        ) {
            LazyVerticalStaggeredGrid(
                columns = StaggeredGridCells.Fixed(2),
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(10.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalItemSpacing = 10.dp,
                state = rememberLazyStaggeredGridState()
            ) {
                items(
                    count = books.itemCount,
                    key = { index ->
                        // Create a truly unique key combining index and book key
                        val book = books[index]
                        "${index}_${book?.key ?: System.nanoTime()}"
                    }
                ) { index ->
                    val book = books[index]
                    if (book != null) {
                        BookCard(
                            book = book,
                            onLongPress = { viewModel.onBookLongPressed(book) },
                            onClick = { onBookClick(book) },
                            modifier = Modifier
                                .animateItemPlacement()
                                .blur(if (selectedBook?.key == book.key) 0.dp else if (selectedBook != null) 8.dp else 0.dp)
                        )
                    } else {
                        // Placeholder while loading
                        PlaceholderBookCard()
                    }
                }

                // Add loading state at the bottom with correct span type
                when (books.loadState.append) {
                    is LoadState.Loading -> {
                        item(span = StaggeredGridItemSpan.FullLine) {
                            LoadingIndicator()
                        }
                    }
                    is LoadState.Error -> {
                        item(span = StaggeredGridItemSpan.FullLine) {
                            ErrorItem { books.retry() }
                        }
                    }
                    else -> {}
                }
            }
        }


        selectedBook?.let { book ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.5f))
                    .clickable { viewModel.clearSelectedBook() }
            ) {
                Card(
                    modifier = Modifier
                        .padding(16.dp)
                        .align(Alignment.Center)
                        .width(280.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF1A1A1A))
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Text(
                            text = book.title,
                            style = MaterialTheme.typography.titleLarge,
                            color = Color.White
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = book.author,
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.Gray
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = book.description,
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.Gray
                        )
                    }
                }
            }
        }
    }
}
@Composable
private fun PlaceholderBookCard() {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(2f/3f),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .shimmerBackground() // Add a shimmer effect
        )
    }
}

@Composable
private fun LoadingIndicator() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator(
            color = Color(0xFF8E42FF),
            modifier = Modifier.size(32.dp)
        )
    }
}

@Composable
private fun ErrorItem(onRetry: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Error loading books",
            color = Color.White,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        Button(
            onClick = onRetry,
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF8E42FF))
        ) {
            Text("Retry")
        }
    }
}

// Shimmer effect extension
fun Modifier.shimmerBackground(): Modifier = composed {
    val transition = rememberInfiniteTransition()
    val alpha by transition.animateFloat(
        initialValue = 0.2f,
        targetValue = 0.4f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000),
            repeatMode = RepeatMode.Reverse
        )
    )
    background(Color.Gray.copy(alpha = alpha))
}