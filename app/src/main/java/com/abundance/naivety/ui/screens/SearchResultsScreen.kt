// app/src/main/java/com/abundance/naivety/ui/screens/SearchResultsScreen.kt
package com.abundance.naivety.ui.screens

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.abundance.naivety.R
import com.abundance.naivety.models.OpenLibraryBook
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import com.abundance.naivety.ui.components.BookCard
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun SearchResultsScreen(
    books: List<OpenLibraryBook>,
    onBookClick: (OpenLibraryBook) -> Unit,
    onBackPress: () -> Unit,
    isLoading: Boolean = false,
    isBookInAnyList: ((String) -> Flow<Boolean>)? = null,
    modifier: Modifier = Modifier
) {
    val sonderFont = FontFamily(Font(R.font.sonder))

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Column {
            // App Title - "Naivety" header (same as MainScreen)
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Naivety",
                    modifier = Modifier
                        .padding(7.dp)
                        .padding(top = 28.dp),
                    fontFamily = sonderFont,
                    fontSize = 24.sp,
                    color = MaterialTheme.colorScheme.primary,
                    textAlign = TextAlign.Center
                )
            }

            // Header with back button
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBackPress) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = MaterialTheme.colorScheme.onBackground
                    )
                }
                Text(
                    text = "Search Results",
                    color = MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier.padding(start = 16.dp),
                    fontFamily = FontFamily(Font(R.font.nektar))
                )
            }

            // Results
            if (isLoading) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                }
            } else if (books.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No results found",
                        color = MaterialTheme.colorScheme.onBackground,
                        fontFamily = FontFamily(Font(R.font.montserratbold))
                    )
                }
            } else {
                LazyVerticalStaggeredGrid(
                    columns = StaggeredGridCells.Fixed(2),
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(10.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalItemSpacing = 10.dp
                ) {
                    items(
                        items = books,
                        key = { it.key }
                    ) { book ->
                        // Check if book is in any list for heart icon
                        val isInAnyList by (isBookInAnyList?.invoke(book.key) ?: flowOf(false))
                            .collectAsState(initial = false)

                        BookCard(
                            book = book,
                            onClick = {
                                onBookClick(book)
                            },
                            isLiked = isInAnyList,
                            modifier = Modifier.animateItem()
                        )
                    }
                }
            }
        }
    }
}