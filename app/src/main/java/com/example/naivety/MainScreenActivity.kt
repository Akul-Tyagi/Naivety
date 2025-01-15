package com.example.naivety

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

                    MainScreen(
                        viewModel = viewModel,
                        onPdfSelect = { launchPdfSelection() },
                        onNavigateToRead = { uri ->
                            viewModel.books.value.find { it.filePath == uri.toString() }?.let { book ->
                                val intent = Intent(this@MainScreenActivity, PdfViewerActivity::class.java).apply {
                                    data = uri
                                    putExtra("BOOK_ID", book.id)
                                }
                                startActivity(intent)
                            }
                        },
                        onSortBooks = { sortOrder ->
                            viewModel.sortBooks(sortOrder)
                        },
                        navController = navController
                    )
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
                Log.e("MainScreenActivity", "Error handling PDF selection", e)
                Toast.makeText(
                    this@MainScreenActivity,
                    "Error adding PDF: ${e.message}",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }
}