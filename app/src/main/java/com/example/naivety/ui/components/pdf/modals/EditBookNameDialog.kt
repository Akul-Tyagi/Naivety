// Create this file: app/src/main/java/com/example/naivety/ui/components/pdf/modals/EditBookNameDialog.kt
package com.example.naivety.ui.components.pdf.modals

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.example.naivety.R
import com.example.naivety.ui.theme.NaivetyPurple
import androidx.compose.material3.TextFieldDefaults

@Composable
fun EditBookNameDialog(
    currentName: String,
    onNameChange: (String) -> Unit,
    onDismiss: () -> Unit
) {
    var bookName by remember { mutableStateOf(currentName) }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = MaterialTheme.shapes.medium,
            color = Color.Black
        ) {
            Column(
                modifier = Modifier
                    .padding(24.dp)
                    .fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Edit Book Name",
                    fontFamily = FontFamily(Font(R.font.alinsa)),
                    style = MaterialTheme.typography.titleLarge,
                    color = NaivetyPurple,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                OutlinedTextField(
                    value = bookName,
                    onValueChange = { bookName = it },
                    label = {
                        Text(
                            "Book Name",
                            fontFamily = FontFamily(Font(R.font.fsb)),
                            color = Color.White.copy(alpha = 0.7f)
                        )
                    },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    colors = TextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        focusedIndicatorColor = NaivetyPurple,
                        focusedContainerColor = NaivetyPurple,
                        unfocusedContainerColor  = Color.White.copy(alpha = 0.5f)
                    ),
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                    keyboardActions = KeyboardActions(onDone = {
                        if (bookName.isNotBlank()) {
                            onNameChange(bookName)
                            onDismiss()
                        }
                    })
                )

                Spacer(modifier = Modifier.height(24.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(
                        onClick = onDismiss,
                        colors = ButtonDefaults.textButtonColors(
                            contentColor = Color.White
                        )
                    ) {
                        Text(
                            "Cancel",
                            fontFamily = FontFamily(Font(R.font.fsb))
                        )
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    Button(
                        onClick = {
                            if (bookName.isNotBlank()) {
                                onNameChange(bookName)
                                onDismiss()
                            }
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = NaivetyPurple
                        ),
                        enabled = bookName.isNotBlank()
                    ) {
                        Text(
                            "Save",
                            fontFamily = FontFamily(Font(R.font.fsb))
                        )
                    }
                }
            }
        }
    }
}