// DeleteConfirmationDialog.kt
package com.example.naivety.ui.components

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import com.example.naivety.R
import com.example.naivety.models.Book
import com.example.naivety.ui.theme.NaivetyPurple

@Composable
fun DeleteConfirmationDialog(
    book: Book,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    val aFont = FontFamily(Font(R.font.alinsa))
    val bFont = FontFamily(Font(R.font.montserratextrabold))

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Delete Book",
                fontFamily = aFont,
                color = Color.White
            )
        },
        text = {
            Text(
                text = "Are you sure you want to delete \"${book.title}\"? This action cannot be undone.",
                fontFamily = bFont,
                color = Color.White
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
                    color = Color.White
                )
            }
        },
        dismissButton = {
            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(containerColor = Color.Gray)
            ) {
                Text(
                    text = "Cancel",
                    fontFamily = aFont,
                    color = Color.White
                )
            }
        },
        containerColor = Color(0xFF222222)
    )
}