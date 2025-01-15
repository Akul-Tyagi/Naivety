// app/src/main/java/com/example/naivety/ui/screens/BrowseScreen.kt

package com.example.naivety.ui.screens

import BookCard
import BookPreviewModal
import com.example.naivety.models.OpenLibraryBook
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.paging.compose.collectAsLazyPagingItems
import com.example.naivety.R
import com.example.naivety.viewmodels.BrowseViewModel

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
    val viewModel: BrowseViewModel = hiltViewModel()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        // Search Bar
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 16.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp) // Fixed height for consistent alignment
                    .background(Color(0xFF1A1A1A), RoundedCornerShape(28.dp))
                    .padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically,

            ) {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = "Search",
                    tint = Color.Gray,
                    modifier = Modifier.size(24.dp)
                )

                Spacer(modifier = Modifier.width(12.dp))

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(56.dp), // Match parent height
                    contentAlignment = Alignment.CenterStart // Center the content vertically
                ) {
                    BasicTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        textStyle = TextStyle(
                            color = Color.White,
                            fontSize = 20.sp,
                            fontFamily = fsFont
                        ),
                        cursorBrush = SolidColor(Color(0xFF8E42FF)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .align(Alignment.CenterStart), // Align to center-start
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
        // Pinterest-style grid
        LazyVerticalStaggeredGrid(
            columns = StaggeredGridCells.Fixed(2),
            modifier = Modifier
                .fillMaxSize()
                .blur(if (selectedBook != null) 8.dp else 0.dp),
            contentPadding = PaddingValues(8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalItemSpacing = 8.dp
        ) {
            items(books.itemCount) { index ->
                books[index]?.let { book ->
                    BookCard(
                        book = book,
                        onLongPress = { viewModel.onBookLongPressed(book) },
                        onClick = { onBookClick(book) },
                        modifier = Modifier.animateItem()
                    )
                }
            }
        }

        selectedBook?.let { book ->
            BookPreviewModal(
                book = book,
                onDismiss = { viewModel.clearSelectedBook() }
            )
        }
    }
}