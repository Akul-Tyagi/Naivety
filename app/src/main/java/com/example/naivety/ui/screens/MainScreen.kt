package com.example.naivety.ui.screens

import android.net.Uri
import android.os.Bundle
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.naivety.R
import com.example.naivety.models.Book
import com.example.naivety.ui.components.BookGrid
import com.example.naivety.ui.theme.NaivetyPurple
import com.example.naivety.viewmodels.BookViewModel

@OptIn(ExperimentalMaterial3Api::class)
    @Composable
    fun MainScreen(
        viewModel: BookViewModel,
        onPdfSelect: () -> Unit,
        onNavigateToRead: (Uri) -> Unit,
        onSortBooks: (SortOrder) -> Unit
    ) {
    val sonderFont = FontFamily(Font(R.font.sonder))
    val alinsaFont = FontFamily(Font(R.font.alinsa))
    val fsFont = FontFamily(Font(R.font.fsb))
    var selectedSection by remember { mutableStateOf("Home") }
    var showSortMenu by remember { mutableStateOf(false) }

    val books by viewModel.books.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.Black),
            ) {
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
                        color = Color(0xFF8E42FF),
                        textAlign = TextAlign.Center
                    )
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                        .padding(bottom = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "Library",
                            fontFamily = alinsaFont,
                            color = NaivetyPurple,
                            fontSize = 19.sp
                        )
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(onClick = { showSortMenu = true }) {
                            Icon(
                                imageVector = Icons.Default.Sort,
                                contentDescription = "Sort",
                                tint = Color.White
                            )
                        }
                        IconButton(onClick = { /* Search functionality */ }) {
                            Icon(
                                imageVector = Icons.Default.Search,
                                contentDescription = "Search",
                                tint = Color.White
                            )
                        }
                    }
                }
            }
        },
        bottomBar = {
            NavigationBar(
                modifier = Modifier.background(Color.Black),
                containerColor = Color.Black
            ) {
                NavigationBarItem(
                    icon = { Icon(Icons.Default.Home, "Home") },
                    label = { Text("Home", fontFamily = alinsaFont, color = Color(0xFF8E42FF)) },
                    selected = selectedSection == "Home",
                    onClick = { selectedSection = "Home" },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Color(0xFF8E42FF),
                        unselectedIconColor = Color.White,
                        indicatorColor = Color(0xFF222222) // Light gray background for selected item
                    )
                )
                NavigationBarItem(
                    icon = { Icon(Icons.Default.Coffee, "Lists") },
                    label = { Text("Lists", fontFamily = alinsaFont, color = Color(0xFF8E42FF)) },
                    selected = selectedSection == "Lists",
                    onClick = { selectedSection = "Lists" },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Color(0xFF8E42FF),
                        unselectedIconColor = Color.White,
                        indicatorColor = Color(0xFF222222) // Light gray background for selected item
                    )
                )
                NavigationBarItem(
                    icon = {
                        Icon(
                            Icons.Default.Add,
                            contentDescription = "Add",
                            modifier = Modifier.size(32.dp)
                        )
                    },
                    selected = false,
                    onClick = { onPdfSelect() },
                    label = null
                )
                NavigationBarItem(
                    icon = { Icon(Icons.Default.Explore, "Browse") },
                    label = { Text("Browse", fontFamily = alinsaFont, color = Color(0xFF8E42FF)) },
                    selected = selectedSection == "Browse",
                    onClick = { selectedSection = "Browse" },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Color(0xFF8E42FF),
                        unselectedIconColor = Color.White,
                        indicatorColor = Color(0xFF222222) // Light gray background for selected item
                    )
                )
                NavigationBarItem(
                    icon = { Icon(Icons.Default.MoreVert, "More") },
                    label = { Text("More", fontFamily = alinsaFont, color = Color(0xFF8E42FF)) },
                    selected = selectedSection == "More",
                    onClick = { selectedSection = "More" },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Color(0xFF8E42FF),
                        unselectedIconColor = Color.White,
                        indicatorColor = Color(0xFF222222) // Light gray background for selected item
                    )
                )
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(Color.Black)
        ) {
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
                        color = Color.LightGray,
                        textAlign = TextAlign.Center,
                        fontFamily = fsFont,
                        lineHeight = 20.sp,
                        modifier = Modifier
                            .align(Alignment.Center)
                            .padding(32.dp)
                    )
                }
                else -> {
                    BookGrid(
                        books = books,
                        onBookClick = { book ->
                            onNavigateToRead(Uri.parse(book.filePath))
                        },
                        viewModel = viewModel,  // Pass the viewModel here
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }
        }

        if (showSortMenu) {
            DropdownMenu(
                expanded = showSortMenu,
                onDismissRequest = { showSortMenu = false },
                modifier = Modifier.background(Color.Black)
            ) {
                DropdownMenuItem(
                    text = { Text("Recently Added", color = Color.White) },
                    onClick = {
                        onSortBooks(SortOrder.RECENT)
                        showSortMenu = false
                    }
                )
                DropdownMenuItem(
                    text = { Text("Title: A to Z", color = Color.White) },
                    onClick = {
                        onSortBooks(SortOrder.TITLE)
                        showSortMenu = false
                    }
                )
                DropdownMenuItem(
                    text = { Text("Author", color = Color.White) },
                    onClick = {
                        onSortBooks(SortOrder.AUTHOR)
                        showSortMenu = false
                    }
                )
            }
        }
    }
}


enum class SortOrder {
    RECENT,
    TITLE,
    AUTHOR,
    PROGRESS
}