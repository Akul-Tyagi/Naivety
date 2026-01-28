// app/src/main/java/com/abundance/naivety/navigation/NavGraph.kt

package com.abundance.naivety.navigation

import BookDetailScreen
import android.app.Activity
import com.abundance.naivety.models.OpenLibraryBook
import android.content.Intent
import android.net.Uri
import androidx.activity.compose.LocalActivity
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.abundance.naivety.*
import com.abundance.naivety.ui.screens.*
import com.abundance.naivety.viewmodels.BookViewModel
import kotlin.toString

@Composable
fun NavGraph(
    navController: NavHostController,
    startDestination: String
) {

    val context = LocalContext.current
    val mainViewModel: BookViewModel = hiltViewModel()

    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        composable(Destinations.Walkthrough.route) {
            WalkthroughScreen(
                onFinish = {
                    navController.navigate(Destinations.Auth.route) {
                        popUpTo(Destinations.Walkthrough.route) { inclusive = true }
                    }
                }
            )
        }
        composable(Destinations.Auth.route) {
            AuthMainScreen(
                viewModel = hiltViewModel(),
                activity = LocalActivity.current as Activity
            )
        }

        // Main Screen
        composable(Destinations.Main.route) { backStackEntry ->
            val fromBookDetail = backStackEntry.savedStateHandle.get<Boolean>("fromBookDetail") ?: false
            val defaultSection = if (fromBookDetail) "Browse" else "Home"

            backStackEntry.savedStateHandle["fromBookDetail"] = null

            MainScreen(
                viewModel = mainViewModel,
                onPdfSelect = {
                    (context as? MainScreenActivity)?.launchPdfSelection()
                },
                onNavigateToRead = { uri ->
                    // Navigate to PDF viewer with the URI
                    mainViewModel.books.value.find { it.filePath == uri.toString() }?.let { book ->
                        val intent = Intent(context, PdfViewerActivity::class.java).apply {
                            data = Uri.parse(uri)
                            putExtra("BOOK_ID", book.id.toString())
                            flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
                        }
                        context.startActivity(intent)
                    }
                },
                onSortBooks = { sortOrder ->
                    mainViewModel.sortBooks(sortOrder)
                },
                navController = navController,
                defaultSection = defaultSection
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
                    // Simply pop the back stack - let navigation handle where to go
                    navController.popBackStack()
                },
                navController = navController
            )
        }

        composable(
            route = Destinations.PdfViewer.route,
            arguments = listOf(
                navArgument("encodedUri") {
                    type = NavType.StringType
                }
            )
        ) { backStackEntry ->
            val context = LocalContext.current
            LaunchedEffect(Unit) {
                val encodedUri = backStackEntry.arguments?.getString("encodedUri")
                encodedUri?.let {
                    val uri = Uri.parse(Uri.decode(it))
                    val intent = Intent(context, PdfViewerActivity::class.java).apply {
                        data = uri
                        // Get the book ID if available - use Long extra
                        mainViewModel.books.value.find { it.filePath == uri.toString() }?.let { book ->
                            putExtra("BOOK_ID", book.id.toString()) // Convert to String
                        }
                        flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
                    }
                    context.startActivity(intent)
                }
                navController.popBackStack()
            }
        }
    }
}