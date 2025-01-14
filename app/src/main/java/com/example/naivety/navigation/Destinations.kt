package com.example.naivety.navigation

import android.net.Uri
import androidx.navigation.NavType
import androidx.navigation.navArgument

sealed class Destinations(val route: String) {
    object Walkthrough : Destinations("walkthrough")
    object Auth : Destinations("auth")
    object Main : Destinations("main")
    object Browse : Destinations("browse")

    object BookDetail : Destinations("bookDetail/{bookKey}/{title}/{author}/{year}/{coverUrl}") {
        fun createRoute(bookKey: String, title: String, author: String, year: Int, coverUrl: String): String {
            return "bookDetail/$bookKey/$title/$author/$year/$coverUrl"
        }
    }

    object PdfViewer : Destinations("pdf_viewer/{encodedUri}") { // Changed from uri to encodedUri
        fun createRoute(uri: Uri): String {
            return "pdf_viewer/${Uri.encode(uri.toString())}"
        }
    }
}