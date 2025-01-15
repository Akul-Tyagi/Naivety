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
import androidx.navigation.compose.rememberNavController
import com.example.naivety.navigation.NavGraph
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

    // Add this function to be called from NavGraph
    fun launchPdfSelection() {
        pdfLauncher.launch(arrayOf("application/pdf"))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)

        setContent {
            NaivetyTheme {
                TransparentSystemBars()
                val navController = rememberNavController()
                NavGraph(
                    navController = navController,
                    auth = Firebase.auth,
                    googleSignInClient = GoogleSignIn.getClient(
                        this,
                        GoogleSignInOptions.DEFAULT_SIGN_IN
                    )
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