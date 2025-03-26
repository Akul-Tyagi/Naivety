package com.example.naivety.navigation

import android.net.Uri

sealed class Destinations(val route: String) {
    object Walkthrough : Destinations("walkthrough")
    object Auth : Destinations("auth")
    object Main : Destinations("main")
    object Browse : Destinations("browse")
    object Lists : Destinations("lists")
    object ThemeSettings : Destinations("theme_settings")
    object Achievements : Destinations("achievements")

    object BookDetail : Destinations(
        "bookDetail/{bookKey}/{title}/{author}/{year}/{coverUrl}"
    ) {
        fun createRoute(
            bookKey: String,
            title: String,
            author: String,
            year: Int,
            coverUrl: String
        ): String {
            return "bookDetail/${Uri.encode(bookKey)}/${Uri.encode(title)}/${
                Uri.encode(author)
            }/$year/${Uri.encode(coverUrl)}"
        }
    }

    object PdfViewer : Destinations("pdf_viewer/{encodedUri}") {
        fun createRoute(uri: Uri): String {
            return "pdf_viewer/${Uri.encode(uri.toString())}"
        }
    }

    object ListDetail : Destinations("list/{listId}") {
        fun createRoute(listId: String): String = "list/$listId"
    }
}