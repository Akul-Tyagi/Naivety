// app/src/main/java/com/example/naivety/navigation/NavGraph.kt

package com.example.naivety.navigation

import BookDetailScreen
import com.example.naivety.models.OpenLibraryBook
import android.content.Intent
import android.net.Uri
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.example.naivety.*
import com.example.naivety.ui.screens.*
import com.example.naivety.viewmodels.BookViewModel
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.firebase.auth.FirebaseAuth

@Composable
fun NavGraph(
    navController: NavHostController,
    auth: FirebaseAuth,
    googleSignInClient: GoogleSignInClient,
    startDestination: String = Destinations.Walkthrough.route
) {

    val context = LocalContext.current
    val mainViewModel: BookViewModel = hiltViewModel()

    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        // Walkthrough Screen
        composable(Destinations.Walkthrough.route) {
            WalkthroughScreen(
                onFinish = {
                    navController.navigate(Destinations.Auth.route) {
                        popUpTo(Destinations.Walkthrough.route) { inclusive = true }
                    }
                }
            )
        }

        // Auth Screen
        composable(Destinations.Auth.route) {
            AuthMain(
                auth = auth,
                googleSignInClient = googleSignInClient,
                signInWithGoogle = {
                    (context as? AuthActivity)?.signInWithGoogle()
                },
                navigateToMainScreen = {
                    navController.navigate(Destinations.Main.route) {
                        popUpTo(Destinations.Auth.route) { inclusive = true }
                    }
                }
            )
        }

        // Main Screen
        composable(Destinations.Main.route) {
            MainScreen(
                viewModel = mainViewModel,
                onPdfSelect = {
                    // Use the activity's PDF launcher
                    (context as? MainScreenActivity)?.launchPdfSelection()
                },
                onNavigateToRead = { uri ->
                    // Navigate to PDF viewer with the URI
                    mainViewModel.books.value.find { it.filePath == uri.toString() }?.let { book ->
                        val intent = Intent(context, PdfViewerActivity::class.java).apply {
                            data = uri
                            putExtra("BOOK_ID", book.id)
                        }
                        context.startActivity(intent)
                    }
                },
                onSortBooks = { sortOrder ->
                    mainViewModel.sortBooks(sortOrder)
                },
                navController = navController
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
                onBackClick = { navController.navigateUp() }
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
                        // Get the book ID if available
                        mainViewModel.books.value.find { book ->
                            book.filePath == uri.toString()
                        }?.let { book ->
                            putExtra("BOOK_ID", book.id)
                        }
                    }
                    context.startActivity(intent)
                }
                navController.popBackStack()
            }
        }
    }
}