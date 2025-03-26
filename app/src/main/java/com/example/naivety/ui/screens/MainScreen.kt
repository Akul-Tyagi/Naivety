package com.example.naivety.ui.screens

import android.net.Uri
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.compose.animation.Crossfade
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.tween
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
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.naivety.R
import com.example.naivety.models.Book
import com.example.naivety.models.SortOrder
import com.example.naivety.navigation.Destinations
import com.example.naivety.ui.components.SearchBar
import com.example.naivety.ui.components.SortDropdownMenu
import com.example.naivety.ui.components.BookGrid
import com.example.naivety.ui.components.DeleteConfirmationDialog
import com.example.naivety.viewmodels.BookViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    viewModel: BookViewModel,
    onPdfSelect: () -> Unit,
    onNavigateToRead: (String) -> Unit,
    onSortBooks: (SortOrder) -> Unit,
    navController: NavHostController,
    defaultSection: String = "Home"
) {
    val sonderFont = FontFamily(Font(R.font.sonder))
    val alinsaFont = FontFamily(Font(R.font.alinsa))
    val fsFont = FontFamily(Font(R.font.fsb))
    var selectedSection by remember { mutableStateOf(defaultSection) }
    var showSortMenu by remember { mutableStateOf(false) }
    var showSearch by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }

            Scaffold(
                modifier = Modifier.fillMaxSize(),
                topBar = {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(MaterialTheme.colorScheme.background)
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
                                color = MaterialTheme.colorScheme.primary,
                                textAlign = TextAlign.Center
                            )
                        }

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
                    NavigationBarWithAnimation(
                        selectedSection = selectedSection,
                        onSectionSelected = { selectedSection = it },
                        alinsaFont = alinsaFont,
                        onPdfSelect = onPdfSelect
                    )
                }
            ) { paddingValues ->
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                        .background(MaterialTheme.colorScheme.background)
                ) {
                    // Crossfade animation between sections
                    Crossfade(
                        targetState = selectedSection,
                        animationSpec = tween(200)
                    ) { section ->
                        when (section) {
                            "Home" -> {
                                HomeSection(
                                    viewModel = viewModel,
                                    searchQuery = searchQuery,
                                    onNavigateToRead = { uri -> onNavigateToRead(uri.toString()) }
                                )
                            }
                            "Lists" -> {
                                ListsScreen(
                                    onNavigateToRead = onNavigateToRead,
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

                            "More" -> {
                                MoreScreen(
                                    viewModel = hiltViewModel(),
                                    navController = navController,
                                    onNavigateToAchievements = {
                                        navController.navigate("achievements")
                                    },
                                    onNavigateToThemeSettings = {
                                        navController.navigate(Destinations.ThemeSettings.route)
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }

@Composable
private fun NavigationBarWithAnimation(
    selectedSection: String,
    onSectionSelected: (String) -> Unit,
    alinsaFont: FontFamily,
    onPdfSelect: () -> Unit
) {
    NavigationBar(
        modifier = Modifier
            .background(MaterialTheme.colorScheme.background)
            .animateContentSize(),
        containerColor = MaterialTheme.colorScheme.surface
    ) {
        val items = listOf(
            Triple(Icons.Default.Home, "Home", "Home"),
            Triple(Icons.Default.Coffee, "Lists", "Lists"),
            Triple(Icons.Default.Add, "Add", ""),
            Triple(Icons.Default.Explore, "Browse", "Browse"),
            Triple(Icons.Default.MoreVert, "More", "More")
        )

        items.forEach { (icon, label, section) ->
            NavigationBarItem(
                icon = {
                    Icon(
                        imageVector = icon,
                        contentDescription = label,
                        modifier = if (label == "Add") Modifier.size(32.dp) else Modifier
                    )
                },
                label = if (label != "Add") {
                    {
                        Text(
                            text = label,
                            fontFamily = alinsaFont,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                } else null,
                selected = selectedSection == section,
                onClick = if (label == "Add") onPdfSelect else { -> onSectionSelected(section) },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = MaterialTheme.colorScheme.primary,
                    unselectedIconColor = MaterialTheme.colorScheme.onSurface,
                    indicatorColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    }
}

@Composable
private fun ComingSoonSection(alinsaFont: FontFamily) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "Coming Soon",
                color = MaterialTheme.colorScheme.onBackground,
                fontFamily = alinsaFont,
                fontSize = 20.sp
            )
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
    var bookToDelete by remember { mutableStateOf<Book?>(null) }

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
                    color = MaterialTheme.colorScheme.primary
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
                    onLongPress = { book ->
                        bookToDelete = book
                    },
                    viewModel = viewModel,
                    modifier = Modifier.fillMaxSize()
                )
            }
        }
    }

    bookToDelete?.let { book ->
        DeleteConfirmationDialog(
            book = book,
            onDismiss = { bookToDelete = null },
            onConfirm = {
                viewModel.deleteBook(book)
                bookToDelete = null
            }
        )
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
            color = MaterialTheme.colorScheme.primary,
            fontSize = 19.sp
        )

        Row(verticalAlignment = Alignment.CenterVertically) {
            Box {
                IconButton(onClick = { onSortMenuChange(true) }) {
                    Icon(
                        imageVector = Icons.Default.Sort,
                        contentDescription = "Sort",
                        tint = MaterialTheme.colorScheme.onBackground
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
                            tint = MaterialTheme.colorScheme.onBackground
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