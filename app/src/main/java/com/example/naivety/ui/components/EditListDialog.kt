package com.example.naivety.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.selection.TextSelectionColors
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.material3.TextFieldDefaults

@Composable
fun EditListDialog(
    initialName: String,
    onConfirm: (String) -> Unit,
    onDismiss: () -> Unit
) {
    var name by remember { mutableStateOf(initialName) }
    val focusRequester = remember { FocusRequester() }

    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
    }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color(0xFF1A1A1A)
            )
        ) {
            Column(
                modifier = Modifier
                    .padding(16.dp)
            ) {
                Text(
                    text = "Edit List Name",
                    style = MaterialTheme.typography.titleLarge,
                    color = Color.White
                )

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .focusRequester(focusRequester),
                    colors = TextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        disabledTextColor = Color.Gray,
                        errorTextColor = Color.Red,
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent,
                        disabledContainerColor = Color.Transparent,
                        errorContainerColor = Color.Transparent,
                        cursorColor = Color.White,
                        errorCursorColor = Color.Red,
                        selectionColors = TextSelectionColors(
                            handleColor = Color.White,
                            backgroundColor = Color.Gray.copy(alpha = 0.4f)
                        ),
                        focusedIndicatorColor = Color.White,
                        unfocusedIndicatorColor = Color.DarkGray,
                        disabledIndicatorColor = Color.Gray,
                        errorIndicatorColor = Color.Red,
                        focusedLeadingIconColor = Color.White,
                        unfocusedLeadingIconColor = Color.DarkGray,
                        disabledLeadingIconColor = Color.Gray,
                        errorLeadingIconColor = Color.Red,
                        focusedTrailingIconColor = Color.White,
                        unfocusedTrailingIconColor = Color.DarkGray,
                        disabledTrailingIconColor = Color.Gray,
                        errorTrailingIconColor = Color.Red,
                        focusedLabelColor = Color.White,
                        unfocusedLabelColor = Color.DarkGray,
                        disabledLabelColor = Color.Gray,
                        errorLabelColor = Color.Red,
                        focusedPlaceholderColor = Color.White,
                        unfocusedPlaceholderColor = Color.DarkGray,
                        disabledPlaceholderColor = Color.Gray,
                        errorPlaceholderColor = Color.Red,
                        focusedSupportingTextColor = Color.White,
                        unfocusedSupportingTextColor = Color.DarkGray,
                        disabledSupportingTextColor = Color.Gray,
                        errorSupportingTextColor = Color.Red,
                        focusedPrefixColor = Color.White,
                        unfocusedPrefixColor = Color.DarkGray,
                        disabledPrefixColor = Color.Gray,
                        errorPrefixColor = Color.Red,
                        focusedSuffixColor = Color.White,
                        unfocusedSuffixColor = Color.DarkGray,
                        disabledSuffixColor = Color.Gray,
                        errorSuffixColor = Color.Red
                    ),
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                    keyboardActions = KeyboardActions(onDone = {
                        if (name.isNotBlank()) {
                            onConfirm(name)
                        }
                    })
                )

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Cancel", color = Color.Gray)
                    }
                    TextButton(
                        onClick = {
                            if (name.isNotBlank()) {
                                onConfirm(name)
                            }
                        }
                    ) {
                        Text("Save", color = Color(0xFF8E42FF))
                    }
                }
            }
        }
    }
}