// app/src/main/java/com/example/naivety/ui/components/pdf/QuickNavigation.kt
package com.example.naivety.ui.components.pdf

import androidx.compose.animation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.naivety.ui.theme.NaivetyPurple

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuickNavigation(
    currentPage: Int,
    totalPages: Int,
    onPageSelect: (Int) -> Unit,
    onDismiss: () -> Unit
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = Color.Black
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                "Quick Navigation",
                style = MaterialTheme.typography.titleLarge,
                color = NaivetyPurple
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Page number input
            OutlinedTextField(
                value = (currentPage + 1).toString(),
                onValueChange = { value ->
                    value.toIntOrNull()?.let { page ->
                        if (page in 1..totalPages) {
                            onPageSelect(page - 1)
                        }
                    }
                },
                label = { Text("Go to page") },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = NaivetyPurple,
                    unfocusedBorderColor = Color.Gray,
                    focusedLabelColor = NaivetyPurple,
                    unfocusedLabelColor = Color.Gray,
                    cursorColor = NaivetyPurple
                ),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Quick access buttons
            LazyVerticalGrid(
                columns = GridCells.Fixed(5),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(10) { index ->
                    val pageNumber = ((index + 1) * (totalPages / 10)).coerceAtMost(totalPages)
                    QuickAccessButton(
                        page = pageNumber,
                        selected = currentPage == pageNumber - 1,
                        onClick = { onPageSelect(pageNumber - 1) }
                    )
                }
            }
        }
    }
}

@Composable
private fun QuickAccessButton(
    page: Int,
    selected: Boolean,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        colors = ButtonDefaults.buttonColors(
            containerColor = if (selected) NaivetyPurple else Color(0xFF1A1A1A)
        ),
        modifier = Modifier.size(48.dp)
    ) {
        Text(
            text = page.toString(),
            color = if (selected) Color.White else Color.Gray
        )
    }
}