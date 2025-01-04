package com.example.naivety
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.github.barteksc.pdfviewer.PDFView
import android.widget.Toast
import androidx.core.view.WindowCompat


class PdfViewerActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)
        setContentView(R.layout.activity_pdf_viewer)

        window.statusBarColor = android.graphics.Color.TRANSPARENT
        window.navigationBarColor = android.graphics.Color.TRANSPARENT

        val pdfView: PDFView = findViewById(R.id.pdfView)
        val uri: Uri? = intent.data

        uri?.let {
            // Take persistent URI permission
            contentResolver.takePersistableUriPermission(
                it,
                Intent.FLAG_GRANT_READ_URI_PERMISSION
            )

            try {
                pdfView.fromUri(it)
                    .enableSwipe(true)
                    .swipeHorizontal(false)
                    .enableDoubletap(true)
                    .defaultPage(0)
                    .onError { t ->
                        t.printStackTrace()
                        Toast.makeText(this, "Error loading PDF: ${t.message}", Toast.LENGTH_LONG).show()
                    }
                    .load()
            } catch (e: Exception) {
                e.printStackTrace()
                Toast.makeText(this, "Error loading PDF: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }
}