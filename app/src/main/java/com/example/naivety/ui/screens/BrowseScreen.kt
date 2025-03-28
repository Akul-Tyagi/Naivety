// app/src/main/java/com/example/naivety/ui/screens/BrowseScreen.kt

package com.example.naivety.ui.screens

import BookCard
import BookPreviewModal
import CustomSearchBar
import SearchResultsScreen
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
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
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
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.paging.LoadState
import androidx.paging.compose.collectAsLazyPagingItems
import com.example.naivety.R
import com.example.naivety.viewmodels.BrowseViewModel
import kotlin.coroutines.ContinuationInterceptor
import kotlin.coroutines.CoroutineContext
import kotlinx.coroutines.flow.distinctUntilChanged
import android.app.Activity
import com.example.naivety.ads.AdManager
import androidx.compose.ui.platform.LocalContext

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun BrowseScreen(
    viewModel: BrowseViewModel = hiltViewModel(),
    onBookClick: (OpenLibraryBook) -> Unit
) {
    val context = LocalContext.current
    var searchQuery by remember { mutableStateOf("") }
    val books = viewModel.books.collectAsLazyPagingItems()
    val selectedBook by viewModel.selectedBook.collectAsState()
    val searchState by viewModel.searchState.collectAsState()
    val fsFont = FontFamily(Font(R.font.fsultralit))
    var showSearchResults by remember { mutableStateOf(false) }
    val searchResults by viewModel.searchResults.collectAsState()
    val isInitialLoading by viewModel.isInitialLoading.collectAsState()
    val isLoadingMore by viewModel.isLoadingMore.collectAsState()
    val gridState = rememberLazyStaggeredGridState()

    // Detect when we're near the end of the list and load more books
    LaunchedEffect(gridState) {
        snapshotFlow {
            val lastIndex = gridState.layoutInfo.visibleItemsInfo.lastOrNull()?.index
            if (lastIndex != null) lastIndex else -1
        }
            .distinctUntilChanged()
            .collect { lastVisibleIndex ->
                if (lastVisibleIndex > 0 && books.itemCount > 0) {
                    val threshold = books.itemCount - 5
                    if (lastVisibleIndex >= threshold) {
                        // We're near the end, load more
                        viewModel.loadMoreBooks()
                    }
                }
            }
    }

    if (showSearchResults) {
        SearchResultsScreen(
            books = searchResults,
            onBookClick = onBookClick,
            onBackPress = {
                showSearchResults = false
                viewModel.clearSearch()
            },
            isLoading = searchState is SearchState.Searching
        )
    } else {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
        ) {
            CustomSearchBar(
                searchQuery = searchQuery,
                onSearchQueryChange = { searchQuery = it },
                onSearchSubmit = { query ->
                    viewModel.searchBooks(query)
                    showSearchResults = true
                },
                fsFont = FontFamily(Font(R.font.fsultralit))
            )

            // Book Grid
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.background)
            ) {
                when (searchState) {
                    is SearchState.Searching -> {
                        CircularProgressIndicator(
                            modifier = Modifier.align(Alignment.Center),
                            color = MaterialTheme.colorScheme.primary
                        )
                    }

                    is SearchState.NoResults -> {
                        Text(
                            text = "No results found",
                            color = MaterialTheme.colorScheme.onBackground,
                            modifier = Modifier.align(Alignment.Center)
                        )
                    }

                    is SearchState.Error -> {
                        Text(
                            text = "Error searching books",
                            color = Color.Red,
                            modifier = Modifier.align(Alignment.Center)
                        )
                    }

                    else -> {
                        LazyVerticalStaggeredGrid(
                            columns = StaggeredGridCells.Fixed(2),
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(10.dp),
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            verticalItemSpacing = 10.dp,
                            state = gridState
                        ) {
                            items(books.itemCount) { index ->
                                books[index]?.let { book ->
                                    BookCard(
                                        book = book,
                                        onClick = {
                                            // Show ad before navigating to book details
                                            val activity = (context as? Activity)
                                            if (activity != null) {
                                                AdManager.showRewardedAd(
                                                    activity = activity,
                                                    onAdClosed = {
                                                        onBookClick(book)
                                                    },
                                                    onAdFailedToShow = {
                                                        onBookClick(book)
                                                    }
                                                )
                                            } else {
                                                onBookClick(book)
                                            }
                                        }
                                    )
                                }
                            }

                            // Loading indicator at the bottom
                            item(span = StaggeredGridItemSpan.FullLine) {
                                if (isLoadingMore) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(16.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                                    }
                                }
                            }
                        }
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
            color = MaterialTheme.colorScheme.primary,
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
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        Button(
            onClick = onRetry,
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
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

