package com.example.twentyquestions.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog

@Composable
fun LearningDialog(
    wrongGuess: String,
    onCommit: (correctName: String, newQuestion: String) -> Unit,
    onCancel: () -> Unit
) {
    var correctName by remember { mutableStateOf("") }
    var newQuestion by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    Dialog(onDismissRequest = onCancel) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = Color(0xFF131722),
            modifier = Modifier
                .fillMaxWidth()
                .border(2.dp, Color(0xFF10B981).copy(alpha = 0.6f), RoundedCornerShape(24.dp))
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .fillMaxWidth(),
                horizontalAlignment = Alignment.Start
            ) {
                Text(
                    text = "TEACH 64-BIT NEURAL CORE",
                    color = Color(0xFF6EE7B7),
                    fontSize = 15.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
                Text(
                    text = "FAILED ON: $wrongGuess",
                    color = Color(0xFFF87171),
                    fontSize = 11.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.SemiBold
                )

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "Teach the algorithm by adding your item and a distinguishing question that separates it from $wrongGuess.",
                    color = Color(0xFF94A3B8),
                    fontSize = 12.sp,
                    lineHeight = 16.sp,
                    fontFamily = FontFamily.Monospace
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Intended Object Field
                OutlinedTextField(
                    value = correctName,
                    onValueChange = {
                        correctName = it
                        errorMessage = null
                    },
                    label = { Text("What object were you thinking of?", fontFamily = FontFamily.Monospace, fontSize = 11.sp) },
                    placeholder = { Text("e.g. Platypus, Laptop, Sunglasses", color = Color(0xFF64748B), fontSize = 11.sp) },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.LightGray,
                        focusedBorderColor = Color(0xFF10B981),
                        unfocusedBorderColor = Color(0xFF334155)
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("learning_correct_name_input")
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Distinguishing Question Field
                OutlinedTextField(
                    value = newQuestion,
                    onValueChange = {
                        newQuestion = it
                        errorMessage = null
                    },
                    label = { Text("Distinguishing Question (YES for your item, NO for $wrongGuess)?", fontFamily = FontFamily.Monospace, fontSize = 11.sp) },
                    placeholder = { Text("e.g. Does it lay eggs?", color = Color(0xFF64748B), fontSize = 11.sp) },
                    singleLine = false,
                    maxLines = 3,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.LightGray,
                        focusedBorderColor = Color(0xFF10B981),
                        unfocusedBorderColor = Color(0xFF334155)
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("learning_question_input")
                )

                if (errorMessage != null) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = errorMessage ?: "",
                        color = Color(0xFFF87171),
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    OutlinedButton(
                        onClick = onCancel,
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFF94A3B8)),
                        modifier = Modifier.testTag("cancel_learning_button")
                    ) {
                        Text("CANCEL", fontFamily = FontFamily.Monospace, fontSize = 11.sp)
                    }

                    Spacer(modifier = Modifier.padding(horizontal = 4.dp))

                    Button(
                        onClick = {
                            if (correctName.isBlank()) {
                                errorMessage = "Please enter what object you were thinking of."
                                return@Button
                            }
                            if (newQuestion.isBlank()) {
                                errorMessage = "Please enter a distinguishing question."
                                return@Button
                            }
                            onCommit(correctName.trim(), newQuestion.trim())
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF059669),
                            contentColor = Color.White
                        ),
                        modifier = Modifier.testTag("commit_learning_button")
                    ) {
                        Text("COMMIT TO FLASH", fontFamily = FontFamily.Monospace, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
