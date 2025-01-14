package com.example.naivety.ui.screens

import android.net.Uri
import android.os.Bundle
import androidx.collection.emptyLongSet
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import com.example.naivety.R
import com.example.naivety.models.Book
import com.example.naivety.navigation.Destinations
import com.example.naivety.ui.components.BookGrid
import com.example.naivety.ui.theme.NaivetyPurple
import com.example.naivety.viewmodels.BookViewModel
import kotlin.math.round

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    viewModel: BookViewModel,
    onPdfSelect: () -> Unit,
    onNavigateToRead: (Uri) -> Unit,
    onSortBooks: (SortOrder) -> Unit,
    navController: NavHostController
) {
    val sonderFont = FontFamily(Font(R.font.sonder))
    val alinsaFont = FontFamily(Font(R.font.alinsa))
    val fsFont = FontFamily(Font(R.font.fsb))
    var selectedSection by remember { mutableStateOf("Home") }
    var showSortMenu by remember { mutableStateOf(false) }
    val books by viewModel.books.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    var showSearch by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }
    val filteredBooks = remember(books, searchQuery) {
        if (searchQuery.isEmpty()) {
            books
        } else {
            books.filter { book ->
                book.title.contains(searchQuery, ignoreCase = true) ||
                        book.author?.contains(searchQuery, ignoreCase = true) == true
            }
        }
    }

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
                when (selectedSection) {
                    "Home" -> {
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
                                Box {
                                    IconButton(onClick = { showSortMenu = true }) {
                                        Icon(
                                            imageVector = Icons.Default.Sort,
                                            contentDescription = "Sort",
                                            tint = Color.White
                                        )
                                    }

                                    DropdownMenu(
                                        expanded = showSortMenu,
                                        onDismissRequest = { showSortMenu = false },
                                        modifier = Modifier
                                            .background(Color(0xFF121212))
                                            .width(180.dp),
                                        offset = DpOffset(x = (-120).dp, y = 8.dp),
                                        shape = RoundedCornerShape(10.dp)
                                    ) {
                                        DropdownMenuItem(
                                            text = {
                                                Text(
                                                    "Recently Added",
                                                    color = Color.White,
                                                    fontFamily = alinsaFont
                                                )
                                            },
                                            onClick = {
                                                onSortBooks(SortOrder.RECENT)
                                                showSortMenu = false
                                            }
                                        )
                                        DropdownMenuItem(
                                            text = {
                                                Text(
                                                    "Title: A to Z",
                                                    color = Color.White,
                                                    fontFamily = alinsaFont
                                                )
                                            },
                                            onClick = {
                                                onSortBooks(SortOrder.TITLE)
                                                showSortMenu = false
                                            }
                                        )
                                        DropdownMenuItem(
                                            text = {
                                                Text(
                                                    "Progress",
                                                    color = Color.White,
                                                    fontFamily = alinsaFont
                                                )
                                            },
                                            onClick = {
                                                onSortBooks(SortOrder.PROGRESS)
                                                showSortMenu = false
                                            }
                                        )
                                        DropdownMenuItem(
                                            text = {
                                                Text(
                                                    "Author",
                                                    color = Color.White,
                                                    fontFamily = alinsaFont
                                                )
                                            },
                                            onClick = {
                                                onSortBooks(SortOrder.AUTHOR)
                                                showSortMenu = false
                                            }
                                        )
                                    }
                                }
                                Box {
                                    if (!showSearch) {
                                        IconButton(onClick = { showSearch = true }) {
                                            Icon(
                                                imageVector = Icons.Default.Search,
                                                contentDescription = "Search",
                                                tint = Color.White
                                            )
                                        }
                                    } else {
                                        Row(
                                            modifier = Modifier
                                                .background(
                                                    Color(0xFF1A1A1A),
                                                    RoundedCornerShape(20.dp)
                                                )
                                                .width(200.dp)
                                                .height(40.dp)
                                                .padding(horizontal = 8.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            BasicTextField(
                                                value = searchQuery,
                                                onValueChange = { searchQuery = it },
                                                singleLine = true,
                                                cursorBrush = SolidColor(Color(0xFF8E42FF)),
                                                textStyle = TextStyle(
                                                    color = Color.White,
                                                    fontSize = 14.sp,
                                                    fontFamily = alinsaFont
                                                ),
                                                modifier = Modifier
                                                    .weight(1f)
                                                    .padding(horizontal = 8.dp)
                                            )

                                            IconButton(
                                                onClick = {
                                                    showSearch = false
                                                    searchQuery = ""
                                                },
                                                modifier = Modifier.size(24.dp)
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.Close,
                                                    contentDescription = "Close search",
                                                    tint = Color.White
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                    "Browse" -> {
                        // No additional header for Browse section
                    }
                    "Lists" -> {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp)
                                .padding(bottom = 8.dp, top = 12.dp),
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
                        indicatorColor = Color(0xFF222222)
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
                        indicatorColor = Color(0xFF222222)
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
                        indicatorColor = Color(0xFF222222)
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
                        indicatorColor = Color(0xFF222222)
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
            when (selectedSection) {  // Add this when statement to handle different sections
                "Home" -> {
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
                                books = filteredBooks,
                                onBookClick = { book ->
                                    onNavigateToRead(Uri.parse(book.filePath))
                                },
                                viewModel = viewModel,
                                modifier = Modifier.fillMaxSize()
                            )
                        }
                    }
                }

                "Browse" -> {
                    BrowseScreen(
                        onBookClick = { book ->
                            navController.navigate(
                                Destinations.BookDetail.createRoute(
                                    bookKey = book.key,
                                    title = book.title,
                                    author = book.author,
                                    year = book.publishedYear,
                                    coverUrl = book.coverUrl
                                )
                            )
                        }
                    )
                }
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