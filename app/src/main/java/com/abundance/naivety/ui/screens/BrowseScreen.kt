// app/src/main/java/com/abundance/naivety/ui/screens/BrowseScreen.kt

package com.abundance.naivety.ui.screens

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridItemSpan
import androidx.compose.foundation.lazy.staggeredgrid.rememberLazyStaggeredGridState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.paging.compose.collectAsLazyPagingItems
import com.abundance.naivety.R
import com.abundance.naivety.viewmodels.BrowseViewModel
import kotlinx.coroutines.flow.distinctUntilChanged
import android.app.Activity
import com.abundance.naivety.ads.AdManager
import androidx.compose.ui.platform.LocalContext
import com.abundance.naivety.models.OpenLibraryBook
import com.abundance.naivety.models.SearchState
import com.abundance.naivety.ui.components.BookCard
import com.abundance.naivety.ui.components.CustomSearchBar
import com.abundance.naivety.ui.screens.SearchResultsScreen

// Shimmer effect extension - moved outside to fix the reference issue
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

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun BrowseScreen(
    viewModel: BrowseViewModel = hiltViewModel(),
    onBookClick: (OpenLibraryBook) -> Unit
) {
    val context = LocalContext.current
    var searchQuery by remember { mutableStateOf("") }
    val books by viewModel.booksState.collectAsState()
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
            val layoutInfo = gridState.layoutInfo
            val totalItems = layoutInfo.totalItemsCount
            val lastVisibleItemIndex = layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0

            // Trigger load when reaching end of list
            lastVisibleItemIndex >= totalItems - 5
        }
            .distinctUntilChanged()
            .collect { shouldLoadMore ->
                if (shouldLoadMore) {
                    viewModel.loadMoreBooks()
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
                            items(
                                count = books.size,
                                key = { index -> books[index].key }
                            ) { index ->
                                val book = books[index]
                                key(book.key) {
                                    val isInAnyList by viewModel.isBookInAnyList(book.key).collectAsState(initial = false)

                                        BookCard(
                                            book = book,
                                            onClick = {
                                                    onBookClick(book)
                                            },
                                            isLiked = isInAnyList,
                                            onLikeToggle = {
                                                // Show list selection dialog
                                                val showListsDialog = true
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

// Removed 'private' modifiers from local @Composable functions
@Composable
fun PlaceholderBookCard() {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(2f / 3f),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .shimmerBackground() // Now the shimmer effect is properly referenced
        )
    }
}

@Composable
fun LoadingIndicator() {
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
fun ErrorItem(onRetry: () -> Unit) {
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