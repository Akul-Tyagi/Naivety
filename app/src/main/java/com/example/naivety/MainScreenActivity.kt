package com.example.naivety

import BookDetailScreen
import SearchResultsScreen
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.OpenableColumns
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.core.view.WindowCompat
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavType
import androidx.navigation.compose.rememberNavController
import com.example.naivety.navigation.NavGraph
import com.example.naivety.ui.screens.MainScreen
import com.example.naivety.ui.theme.NaivetyTheme
import com.example.naivety.ui.theme.TransparentSystemBars
import com.example.naivety.viewmodels.BookViewModel
import kotlinx.coroutines.launch
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.naivety.models.OpenLibraryBook
import com.example.naivety.navigation.Destinations
import com.example.naivety.ui.screens.BrowseScreen
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainScreenActivity : ComponentActivity() {
    private val viewModel: BookViewModel by viewModels()

    private val pdfLauncher = registerForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        uri?.let { handlePdfSelection(it) }
    }

    fun launchPdfSelection() {
        pdfLauncher.launch(arrayOf("application/pdf"))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)

        setContent {
            NaivetyTheme {
                TransparentSystemBars()

                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(MaterialTheme.colorScheme.background)
                ) {
                    val navController = rememberNavController()

                    NavHost(
                        navController = navController,
                        startDestination = "main" ,
                        modifier = Modifier.fillMaxSize()
                    ) {
                        composable("main") { backStackEntry ->
                            // Get the default section from saved state handle or use "Browse" if coming from BookDetail
                            val defaultSection = if (backStackEntry.savedStateHandle.get<Boolean>("fromBookDetail") == true) {
                                "Browse"
                            } else {
                                "Home"
                            }
                            MainScreen(
                                viewModel = viewModel,
                                onPdfSelect = { launchPdfSelection() },
                                onNavigateToRead = { uri ->  // Change this line to accept String
                                    viewModel.books.value.find { it.filePath == uri }?.let { book ->
                                        val intent = Intent(this@MainScreenActivity, PdfViewerActivity::class.java).apply {
                                            data = Uri.parse(uri)  // Convert String to Uri here
                                            putExtra("BOOK_ID", book.id)
                                        }
                                        startActivity(intent)
                                    }
                                },
                                onSortBooks = { sortOrder ->
                                    viewModel.sortBooks(sortOrder)
                                },
                                navController = navController,
                                defaultSection = defaultSection
                            )
                        }
                        composable(Destinations.Browse.route) {
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
                        // Add BookDetail destination here
                        composable(
                            route = Destinations.BookDetail.route,
                            arguments = listOf(
                                navArgument("bookKey") { type = NavType.StringType },
                                navArgument("title") { type = NavType.StringType },
                                navArgument("author") { type = NavType.StringType },
                                navArgument("year") { type = NavType.IntType },
                                navArgument("coverUrl") { type = NavType.StringType }
                            )
                        ) {
                            BookDetailScreen(
                                book = OpenLibraryBook(
                                    key = it.arguments?.getString("bookKey") ?: "",
                                    title = it.arguments?.getString("title") ?: "",
                                    author = it.arguments?.getString("author") ?: "",
                                    publishedYear = it.arguments?.getInt("year") ?: 0,
                                    coverUrl = it.arguments?.getString("coverUrl") ?: "",
                                    description = ""
                                ),
                                onBackPressed = {
                                    navController.previousBackStackEntry?.savedStateHandle?.set("fromBookDetail", true)
                                    navController.navigateUp()
                                },
                                navController = navController
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

                        viewModel.addBook(uri, displayName.removeSuffix(".pdf"))
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