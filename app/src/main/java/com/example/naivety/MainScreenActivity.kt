package com.example.naivety

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.WindowCompat
import com.example.naivety.ui.theme.NaivetyTheme
import com.example.naivety.ui.theme.TransparentSystemBars

class MainScreenActivity : ComponentActivity() {
    private val REQUEST_CODE = 1
    private val PERMISSION_REQUEST_CODE = 2

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        WindowCompat.setDecorFitsSystemWindows(window, false)
        window.statusBarColor = android.graphics.Color.TRANSPARENT
        window.navigationBarColor = android.graphics.Color.TRANSPARENT

        setContent {
            NaivetyTheme {
                TransparentSystemBars()
                MainScreenContent { checkPermissionAndOpenFilePicker() }
            }
        }
    }

    private fun checkPermissionAndOpenFilePicker() {
        when {
            // For Android 10 and above
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q -> {
                openFilePicker()
            }
            // For Android 9 and below
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.READ_EXTERNAL_STORAGE
            ) == PackageManager.PERMISSION_GRANTED -> {
                openFilePicker()
            }
            else -> {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
                    PERMISSION_REQUEST_CODE
                )
            }
        }
    }

    private fun openFilePicker() {
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type = "application/pdf"
        }
        startActivityForResult(intent, REQUEST_CODE)
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == PERMISSION_REQUEST_CODE && grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            openFilePicker()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_CODE && resultCode == RESULT_OK) {
            data?.data?.let { uri ->
                val intent = Intent(this, PdfViewerActivity::class.java)
                intent.data = uri
                startActivity(intent)
            }
        }
    }
}

@Composable
fun MainScreenContent(onOpenPdf: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Welcome to the Main Screen!",
            style = MaterialTheme.typography.headlineMedium.copy(
                color = Color(0xFF8E42FF)
            )
        )
        Spacer(modifier = Modifier.height(16.dp))
        Button(onClick = onOpenPdf) {
            Text(text = "Open PDF")
        }
    }
}