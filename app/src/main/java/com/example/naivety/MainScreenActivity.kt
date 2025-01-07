package com.example.naivety

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.core.view.WindowCompat
import androidx.lifecycle.lifecycleScope
import com.example.naivety.ui.theme.NaivetyTheme
import com.example.naivety.ui.theme.TransparentSystemBars
import com.example.naivety.viewmodels.BookViewModel
import kotlinx.coroutines.launch
import com.example.naivety.ui.screens.MainScreen
import com.example.naivety.ui.screens.SortOrder
class MainScreenActivity : ComponentActivity() {
    private val viewModel: BookViewModel by viewModels()

    private val pdfLauncher = registerForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        uri?.let { handlePdfSelection(it) }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)

        setContent {
            NaivetyTheme {
                TransparentSystemBars()
                val books by viewModel.books.collectAsState()
                val isLoading by viewModel.isLoading.collectAsState()

                MainScreen(
                    viewModel = viewModel,
                    onPdfSelect = {
                        pdfLauncher.launch(arrayOf("application/pdf"))
                    },
                    onNavigateToRead = { uri ->
                        viewModel.books.value.find { it.filePath == uri.toString() }?.let { book ->
                            openPdfViewer(uri, book.id)
                        }
                    },
                    onSortBooks = { sortOrder ->
                        viewModel.sortBooks(sortOrder)
                    }
                )
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
                        val displayName = cursor.getString(
                            cursor.getColumnIndex(android.provider.OpenableColumns.DISPLAY_NAME)
                        ) ?: "Untitled"

                        viewModel.addBook(uri, displayName.removeSuffix(".pdf"))
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
    private fun openPdfViewer(uri: Uri, bookId: String) {
        val intent = Intent(this, PdfViewerActivity::class.java).apply {
            data = uri
            putExtra("BOOK_ID", bookId)
        }
        startActivity(intent)
    }
}