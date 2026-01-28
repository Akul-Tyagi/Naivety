package com.abundance.naivety

import BookDetailScreen
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.OpenableColumns
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.core.view.WindowCompat
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavType
import androidx.navigation.compose.rememberNavController
import com.abundance.naivety.ui.screens.MainScreen
import com.abundance.naivety.ui.theme.NaivetyTheme
import com.abundance.naivety.ui.theme.TransparentSystemBars
import com.abundance.naivety.viewmodels.BookViewModel
import kotlinx.coroutines.launch
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.abundance.naivety.models.OpenLibraryBook
import com.abundance.naivety.navigation.Destinations
import com.abundance.naivety.ui.screens.AchievementsScreen
import com.abundance.naivety.ui.screens.BrowseScreen
import com.abundance.naivety.ui.screens.ReadingHeatmapScreen
import com.abundance.naivety.ui.screens.SearchResultsScreen
import com.abundance.naivety.ui.screens.ThemeSettingsScreen
import dagger.hilt.android.AndroidEntryPoint
import com.abundance.naivety.utils.PreferencesManager
import androidx.hilt.navigation.compose.hiltViewModel
import com.abundance.naivety.viewmodels.ListsViewModel
import com.abundance.naivety.viewmodels.SearchViewModel

@AndroidEntryPoint
class MainScreenActivity : ComponentActivity() {
    private val viewModel: BookViewModel by viewModels()

    private val pdfLauncher = registerForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        uri?.let {
            android.util.Log.d("MainScreenActivity", "File selected: $it")
            try {
                contentResolver.takePersistableUriPermission(
                    it,
                    Intent.FLAG_GRANT_READ_URI_PERMISSION
                )
                android.util.Log.d("MainScreenActivity", "Permission granted, adding book...")
                viewModel.addBook(it)  // This line was missing!
            } catch (e: Exception) {
                android.util.Log.e("MainScreenActivity", "Error adding book: ${e.message}", e)
            }
        } ?: run {
            android.util.Log.d("MainScreenActivity", "No file selected (uri is null)")
        }
    }

    fun launchPdfSelection() {
        pdfLauncher.launch(arrayOf("application/pdf", "application/epub+zip"))
    }

    private val userPreferencesRepository by lazy {
        (application as NaivetyApplication).userPreferencesRepository
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)

        // Track app opens for rating prompt
        com.abundance.naivety.ui.components.RatingPromptManager.onAppOpened(this)

        setContent {

            val isDarkTheme = userPreferencesRepository.isDarkTheme.collectAsState().value

            NaivetyTheme(darkTheme = isDarkTheme) {
                TransparentSystemBars(darkTheme = isDarkTheme)

                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(MaterialTheme.colorScheme.background)
                ) {
                    val navController = rememberNavController()

                    val isGuestMode = PreferencesManager.isGuestMode(this@MainScreenActivity)

                    NavHost(
                        navController = navController,
                        startDestination = Destinations.Main.route,
                        modifier = Modifier.fillMaxSize()
                    ) {
                        composable(Destinations.Main.route) { backStackEntry ->
                            // Get the section to show from savedStateHandle (set when navigating back)
                            val savedSection = backStackEntry.savedStateHandle.get<String>("selectedSection")

                            // Wrap MainScreen and RatingPromptHandler in a Box to properly overlay
                            Box(modifier = Modifier.fillMaxSize()) {
                                MainScreen(
                                    viewModel = viewModel,
                                    onPdfSelect = { launchPdfSelection() },
                                    onNavigateToRead = { uri ->
                                        viewModel.books.value.find { it.filePath == uri }?.let { book ->
                                            val intent = Intent(this@MainScreenActivity, PdfViewerActivity::class.java).apply {
                                                data = Uri.parse(uri)
                                                putExtra("BOOK_ID", book.id.toString())
                                                flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
                                            }
                                            startActivity(intent)
                                        }
                                    },
                                    onSortBooks = { sortOrder ->
                                        viewModel.sortBooks(sortOrder)
                                    },
                                    navController = navController,
                                    defaultSection = savedSection ?: "Home",
                                    isGuestMode = isGuestMode
                                )

                                // Show rating prompt on Main screen (overlaid on top)
                                com.abundance.naivety.ui.components.RatingPromptHandler()
                            }
                        }

                        // Search Results Screen - proper navigation destination
                        composable(
                            route = Destinations.SearchResults.route,
                            arguments = listOf(
                                navArgument("query") { type = NavType.StringType }
                            )
                        ) { backStackEntry ->
                            val query = backStackEntry.arguments?.getString("query")?.let { Uri.decode(it) } ?: ""
                            val searchViewModel: SearchViewModel = hiltViewModel()
                            val listsViewModel: ListsViewModel = hiltViewModel()

                            // Trigger search when screen is displayed
                            LaunchedEffect(query) {
                                if (query.isNotBlank()) {
                                    searchViewModel.searchBooks(query)
                                }
                            }

                            val searchResults by searchViewModel.searchResults.collectAsState()
                            val searchState by searchViewModel.searchState.collectAsState()

                            SearchResultsScreen(
                                books = searchResults,
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
                                },
                                onBackPress = {
                                    navController.popBackStack()
                                },
                                isLoading = searchState is com.abundance.naivety.models.SearchState.Searching,
                                isBookInAnyList = { bookKey -> listsViewModel.isBookInAnyList(bookKey) }
                            )
                        }

                        // Book Detail Screen
                        composable(
                            route = Destinations.BookDetail.route,
                            arguments = listOf(
                                navArgument("bookKey") { type = NavType.StringType },
                                navArgument("title") { type = NavType.StringType },
                                navArgument("author") { type = NavType.StringType },
                                navArgument("year") { type = NavType.IntType },
                                navArgument("coverUrl") { type = NavType.StringType }
                            )
                        ) { backStackEntry ->
                            val bookKey = backStackEntry.arguments?.getString("bookKey")?.let { Uri.decode(it) } ?: ""
                            val title = backStackEntry.arguments?.getString("title")?.let { Uri.decode(it) } ?: ""
                            val author = backStackEntry.arguments?.getString("author")?.let { Uri.decode(it) } ?: ""
                            val year = backStackEntry.arguments?.getInt("year") ?: 0
                            val coverUrl = backStackEntry.arguments?.getString("coverUrl")?.let { Uri.decode(it) } ?: ""

                            BookDetailScreen(
                                book = OpenLibraryBook(
                                    key = bookKey,
                                    title = title,
                                    author = author,
                                    publishedYear = year,
                                    coverUrl = coverUrl,
                                    description = ""
                                ),
                                onBackPressed = {
                                    // Simply pop back - let the navigation stack handle where to go
                                    navController.popBackStack()
                                },
                                navController = navController
                            )
                        }

                        // Achievements Screen
                        composable(Destinations.Achievements.route) {
                            val fsFont = FontFamily(Font(R.font.montserratblack))
                            AchievementsScreen(
                                customFont = fsFont,
                                onBackPressed = {
                                    navController.popBackStack()
                                }
                            )
                        }

                        // Reading Heatmap Screen
                        composable(Destinations.ReadingHeatmap.route) {
                            val fsFont = FontFamily(Font(R.font.montserratblack))
                            ReadingHeatmapScreen(
                                customFont = fsFont,
                                onBackPressed = {
                                    navController.popBackStack()
                                }
                            )
                        }

                        // Theme Settings Screen
                        composable(Destinations.ThemeSettings.route) {
                            val fsFont = FontFamily(Font(R.font.montserratblack))
                            ThemeSettingsScreen(
                                isDarkTheme = userPreferencesRepository.isDarkTheme.collectAsState().value,
                                onThemeChange = { isDark ->
                                    lifecycleScope.launch {
                                        userPreferencesRepository.setDarkTheme(this@MainScreenActivity, isDark)
                                    }
                                },
                                customFont = fsFont,
                                onBackPressed = {
                                    navController.popBackStack()
                                }
                            )
                        }
                    }
                }
            }
        }
    }

    private fun handlePdfSelection(uri: Uri) {
        lifecycleScope.launch {
            try {
                contentResolver.takePersistableUriPermission(
                    uri,
                    Intent.FLAG_GRANT_READ_URI_PERMISSION
                )

                contentResolver.query(uri, null, null, null, null)?.use { cursor ->
                    if (cursor.moveToFirst()) {
                        val displayNameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                        val displayName = if (displayNameIndex != -1) {
                            cursor.getString(displayNameIndex)
                        } else "Untitled"

                        viewModel.addBook(uri)
                    }
                }
            } catch (e: Exception) {
                Toast.makeText(
                    this@MainScreenActivity,
                    "Error adding PDF: ${e.message}",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }
}