// DeleteConfirmationDialog.kt
package com.abundance.naivety.ui.components

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import com.abundance.naivety.R
import com.abundance.naivety.models.Book
import com.abundance.naivety.ui.theme.NaivetyPurple

@Composable
fun DeleteConfirmationDialog(
    book: Book,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    val aFont = FontFamily(Font(R.font.nektar))
    val bFont = FontFamily(Font(R.font.montserratextrabold))

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Delete Book",
                fontFamily = aFont,
                color = MaterialTheme.colorScheme.onSurface
            )
        },
        text = {
            Text(
                text = "Are you sure you want to delete \"${book.title}\"? This action cannot be undone.",
                fontFamily = bFont,
                color = MaterialTheme.colorScheme.onSurface
            )
        },
        confirmButton = {
            Button(
                onClick = onConfirm,
                colors = ButtonDefaults.buttonColors(containerColor = NaivetyPurple)
            ) {
                Text(
                    text = "Delete",
                    fontFamily = aFont,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        },
        dismissButton = {
            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Text(
                    text = "Cancel",
                    fontFamily = aFont,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        },
        containerColor = MaterialTheme.colorScheme.surface
    )
}