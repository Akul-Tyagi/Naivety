// app/src/main/java/com/example/naivety/ui/components/pdf/PdfViewerLayout.kt
package com.example.naivety.ui.components.pdf

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun PdfViewerLayout(
    modifier: Modifier = Modifier,
    pageIndicatorVisible: Boolean = true,
    currentPage: Int,
    totalPages: Int,
    controlsVisible: Boolean,
    onPageIndicatorVisibilityChanged: (Boolean) -> Unit,
    pdfContent: @Composable () -> Unit,
    controls: @Composable () -> Unit
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        // PDF Content
        Box(modifier = Modifier.fillMaxSize()) {
            pdfContent()
        }

        // Page Indicator
        PageIndicator(
            currentPage = currentPage,
            totalPages = totalPages,
            isVisible = pageIndicatorVisible && !controlsVisible,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 16.dp)
        )

        // Controls
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
        ) {
            controls()
        }
    }
}