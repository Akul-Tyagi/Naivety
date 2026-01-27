package com.abundance.naivety.ui.screens

import android.R.attr.data
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
import com.abundance.naivety.R
import com.abundance.naivety.models.Book
import com.abundance.naivety.models.SortOrder
import com.abundance.naivety.navigation.Destinations
import com.abundance.naivety.ui.components.SearchBar
import com.abundance.naivety.ui.components.SortDropdownMenu
import com.abundance.naivety.ui.components.BookGrid
import com.abundance.naivety.ui.components.DeleteConfirmationDialog
import com.abundance.naivety.viewmodels.BookViewModel
import androidx.compose.ui.platform.LocalContext
import android.app.Activity
import android.content.Intent
import com.abundance.naivety.EpubReaderActivity
import com.abundance.naivety.utils.BookFileType
import kotlin.jvm.java
import kotlin.toString

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    viewModel: BookViewModel,
    onPdfSelect: () -> Unit,
    onNavigateToRead: (String) -> Unit,
    onSortBooks: (SortOrder) -> Unit,
    navController: NavHostController,
    defaultSection: String = "Home",
    isGuestMode: Boolean = false
) {
    val sonderFont = FontFamily(Font(R.font.sonder))
    val alinsaFont = FontFamily(Font(R.font.nektar))
    val fsFont = FontFamily(Font(R.font.fsb))
    var selectedSection by remember { mutableStateOf(defaultSection) }
    var showSortMenu by remember { mutableStateOf(false) }
    var showSearch by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }

    // Update selectedSection when defaultSection changes (e.g., when navigating back)
    LaunchedEffect(defaultSection) {
        selectedSection = defaultSection
    }

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
                                        // Save current section before navigating
                                        navController.currentBackStackEntry?.savedStateHandle?.set("selectedSection", "Lists")
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
                                        // Save current section before navigating
                                        navController.currentBackStackEntry?.savedStateHandle?.set("selectedSection", "Browse")
                                        navController.navigate(
                                            Destinations.BookDetail.createRoute(
                                                book.key,
                                                book.title,
                                                book.author,
                                                book.publishedYear,
                                                book.coverUrl
                                            )
                                        )
                                    },
                                    onSearchSubmit = { query ->
                                        // Save current section before navigating to search
                                        navController.currentBackStackEntry?.savedStateHandle?.set("selectedSection", "Browse")
                                        navController.navigate(
                                            Destinations.SearchResults.createRoute(query)
                                        )
                                    }
                                )
                            }

                            "More" -> {
                                MoreScreen(
                                    viewModel = hiltViewModel(),
                                    navController = navController,
                                    onNavigateToAchievements = {
                                        // Save current section before navigating
                                        navController.currentBackStackEntry?.savedStateHandle?.set("selectedSection", "More")
                                        navController.navigate(Destinations.Achievements.route)
                                    },
                                    onNavigateToThemeSettings = {
                                        // Save current section before navigating
                                        navController.currentBackStackEntry?.savedStateHandle?.set("selectedSection", "More")
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
                fontSize = 24.sp
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
    val context = LocalContext.current
    val books by viewModel.books.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val fsFont = FontFamily(Font(R.font.fsb))
    val fsFontt = FontFamily(Font(R.font.montserratblack))
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
                    text = "A library without books is just a room.\n\n Click on + to start building your collection." ,
                    color = Color.LightGray,
                    textAlign = TextAlign.Center,
                    fontFamily = fsFontt,
                    lineHeight = 16.sp,
                    fontSize = 14.sp,
                    modifier = Modifier
                        .align(Alignment.Center)
                        .padding(25.dp)
                )
            }
            else -> {
                BookGrid(
                    books = filteredBooks,
                    onBookClick = { book ->
                                    // Check file type and route appropriately
                                    if (book.getBookFileType() == BookFileType.EPUB) {
                                        // Navigate to EPUB reader (you need to create this)
                                        val intent = Intent(
                                            context,
                                            EpubReaderActivity::class.java
                                        ).apply {
                                            data = Uri.parse(book.filePath)
                                            putExtra("BOOK_ID", book.id.toString())
                                        }
                                        context.startActivity(intent)
                                    } else {
                                        onNavigateToRead(Uri.parse(book.filePath))
                                    }
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
            fontSize = 21.sp
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