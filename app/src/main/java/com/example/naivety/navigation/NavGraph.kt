// app/src/main/java/com/example/naivety/navigation/NavGraph.kt

package com.example.naivety.navigation

import BookDetailScreen
import OpenLibraryBook
import android.content.Intent
import android.net.Uri
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.example.naivety.*
import com.example.naivety.ui.screens.*
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.firebase.auth.FirebaseAuth

@Composable
fun NavGraph(
    navController: NavHostController,
    auth: FirebaseAuth,
    googleSignInClient: GoogleSignInClient,
    startDestination: String = Destinations.Walkthrough.route
) {
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
                signInWithGoogle = { /* Implement in AuthActivity */ },
                navigateToMainScreen = {
                    navController.navigate(Destinations.Main.route) {
                        popUpTo(Destinations.Auth.route) { inclusive = true }
                    }
                }
            )
        }

        // Main Screen
        composable(Destinations.Main.route) { backStackEntry ->
            MainScreen(
                viewModel = viewModel(),
                onPdfSelect = { /* Handle in MainScreenActivity */ },
                onNavigateToRead = { uri ->
                    navController.navigate(Destinations.PdfViewer.createRoute(uri))
                },
                onSortBooks = { sortOrder ->
                    /* Handle in MainScreenActivity */
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

        // PDF Viewer Screen
        // PDF Viewer Screen
        composable(
            route = Destinations.PdfViewer.route,
            arguments = listOf(
                navArgument("encodedUri") { // Changed from uri to encodedUri to match Destinations
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
                    }
                    context.startActivity(intent)
                }
                // Pop back to avoid the blank screen when returning
                navController.popBackStack()
            }
        }
    }
}