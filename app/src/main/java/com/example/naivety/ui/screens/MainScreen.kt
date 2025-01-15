package com.example.naivety.ui.screens

import android.net.Uri
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
import androidx.navigation.NavHostController
import com.example.naivety.R
import com.example.naivety.models.SortOrder
import com.example.naivety.navigation.Destinations
import com.example.naivety.ui.components.SearchBar
import com.example.naivety.ui.components.SortDropdownMenu
import com.example.naivety.ui.components.BookGrid
import com.example.naivety.viewmodels.BookViewModel

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
    var showSearch by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.Black)
            ) {
                // App Title
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

                // Section Header
                if (selectedSection == "Home") {
                    HomeTopBar(
                        alinsaFont = alinsaFont,
                        showSortMenu = showSortMenu,
                        onSortMenuChange = { showSortMenu = it },
                        onSortBooks = onSortBooks,
                        showSearch = showSearch,
                        onShowSearchChange = { showSearch = it },
                        searchQuery = searchQuery,
                        onSearchQueryChange = { searchQuery = it }
                    )
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
                    onClick = onPdfSelect,
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
            when (selectedSection) {
                "Home" -> {
                    HomeSection(
                        viewModel = viewModel,
                        searchQuery = searchQuery,
                        onNavigateToRead = onNavigateToRead
                    )
                }
                "Browse" -> {
                    BrowseScreen(
                        onBookClick = { book ->
                            navController.navigate(
                                Destinations.BookDetail.createRoute(
                                    book.key,
                                    book.title,
                                    book.author,
                                    book.publishedYear,
                                    book.coverUrl
                                )
                            )
                        }
                    )
                }
                else -> {
                    // Placeholder for Lists and More sections
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Coming Soon",
                            color = Color.White,
                            fontFamily = alinsaFont,
                            fontSize = 20.sp
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun HomeSection(
    viewModel: BookViewModel,
    searchQuery: String,
    onNavigateToRead: (Uri) -> Unit
) {
    val books by viewModel.books.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val fsFont = FontFamily(Font(R.font.fsb))

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

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
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
}

@Composable
private fun HomeTopBar(
    alinsaFont: FontFamily,
    showSortMenu: Boolean,
    onSortMenuChange: (Boolean) -> Unit,
    onSortBooks: (SortOrder) -> Unit,
    showSearch: Boolean,
    onShowSearchChange: (Boolean) -> Unit,
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .padding(bottom = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "Library",
            fontFamily = alinsaFont,
            color = Color(0xFF8E42FF),
            fontSize = 19.sp
        )

        Row(verticalAlignment = Alignment.CenterVertically) {
            Box {
                IconButton(onClick = { onSortMenuChange(true) }) {
                    Icon(
                        imageVector = Icons.Default.Sort,
                        contentDescription = "Sort",
                        tint = Color.White
                    )
                }

                SortDropdownMenu(
                    expanded = showSortMenu,
                    onDismiss = { onSortMenuChange(false) },
                    onSortBooks = onSortBooks,
                    alinsaFont = alinsaFont
                )
            }

            Box {
                if (!showSearch) {
                    IconButton(onClick = { onShowSearchChange(true) }) {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = "Search",
                            tint = Color.White
                        )
                    }
                } else {
                    SearchBar(
                        searchQuery = searchQuery,
                        onSearchQueryChange = onSearchQueryChange,
                        onCloseSearch = {
                            onShowSearchChange(false)
                            onSearchQueryChange("")
                        },
                        alinsaFont = alinsaFont
                    )
                }
            }
        }
    }
}