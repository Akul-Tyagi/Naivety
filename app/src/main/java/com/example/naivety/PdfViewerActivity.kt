package com.example.naivety

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.github.barteksc.pdfviewer.PDFView
import android.widget.Toast
import androidx.core.view.WindowCompat
import com.example.naivety.viewmodels.BookViewModel
import kotlinx.coroutines.launch

class PdfViewerActivity : AppCompatActivity() {
    private val viewModel: BookViewModel by viewModels()
    private var bookId: String? = null
    private lateinit var pdfView: PDFView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)
        setContentView(R.layout.activity_pdf_viewer)

        window.statusBarColor = android.graphics.Color.TRANSPARENT
        window.navigationBarColor = android.graphics.Color.TRANSPARENT

        bookId = intent.getStringExtra("BOOK_ID")
        val lastPage = intent.getIntExtra("LAST_PAGE", 0)
        pdfView = findViewById(R.id.pdfView)

        val uri: Uri? = intent.data

        uri?.let {
            try {
                pdfView.fromUri(it)
                    .defaultPage(lastPage)
                    .onPageChange { page, _ ->
                        // Save reading progress
                        saveReadingProgress(page)
                    }
                    .enableSwipe(true)
                    .swipeHorizontal(false)
                    .enableDoubletap(true)
                    .spacing(10) // Add spacing between pages
                    .onError { t ->
                        t.printStackTrace()
                        Toast.makeText(
                            this,
                            "Error loading PDF: ${t.message}",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                    .load()
            } catch (e: Exception) {
                e.printStackTrace()
                Toast.makeText(
                    this,
                    "Error loading PDF: ${e.message}",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }

    private fun saveReadingProgress(page: Int) {
        bookId?.let { id ->
            lifecycleScope.launch {
                viewModel.updateBookProgress(
                    bookId = id,
                    page = page,
                    position = pdfView.positionOffset
                )
            }
        }
    }

    override fun onPause() {
        super.onPause()
        // Save progress when app is paused
        saveReadingProgress(pdfView.currentPage)
    }
}