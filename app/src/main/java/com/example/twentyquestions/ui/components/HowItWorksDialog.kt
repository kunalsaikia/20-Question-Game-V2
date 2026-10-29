package com.example.twentyquestions.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties

@Composable
fun HowItWorksDialog(onClose: () -> Unit) {
    Dialog(
        onDismissRequest = onClose,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = Color(0xFF0F131D),
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .fillMaxHeight(0.85f)
                .border(2.dp, Color(0xFF10B981).copy(alpha = 0.5f), RoundedCornerShape(20.dp))
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .fillMaxHeight()
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "20Q SYSTEM ARCHITECTURE",
                            color = Color(0xFF34D399),
                            fontSize = 15.sp,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        )
                        Text(
                            text = "OPERATING MANUAL // 64-BIT NEURAL CHIP",
                            color = Color(0xFF94A3B8),
                            fontSize = 10.sp,
                            fontFamily = FontFamily.Monospace
                        )
                    }

                    IconButton(onClick = onClose, modifier = Modifier.testTag("close_how_it_works_button")) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.White)
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Scrollable Content
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    ManualSection(
                        title = "1. DIFFERENCE-PENALTY MATHEMATICS",
                        body = "Every candidate object in 64-bit flash memory begins with a penalty score of 0.0.\n\nWhen you answer a question:\n• YES = 1.0\n• NO = 0.0\n• SOMETIMES = 0.5\n• UNKNOWN = null (skipped)\n\nPenalty Formula:\nscores[obj] -= abs(player_answer - obj_attribute)\n\nSurviving candidates with scores nearest to 0.0 remain top contenders."
                    )

                    ManualSection(
                        title = "2. MINIMAX INFORMATION-JUMP",
                        body = "Rather than random traversal, the device calculates the worst-case partition among plausible surviving candidates for every unasked question.\n\nIt picks the question where min(yes_candidates, no_candidates) is maximized, ensuring that whichever answer you give, maximum information is gained."
                    )

                    ManualSection(
                        title = "3. STRICT BRANCH PRUNING",
                        body = "• If Living is answered NO: All 30+ biological, animal, and organism questions are strictly pruned.\n• If Inanimate is answered YES: Manufactured, electrical, and wearable branches are explored.\n• If Electricity is NO: Digital and battery questions are skipped."
                    )

                    ManualSection(
                        title = "4. GATED NEURAL EXPANSION",
                        body = "When the handheld is stumped, you teach it the missing item and a distinguishing question.\n\nTo prevent corruption of other items, new questions are GATED specifically to distinguish between the correct item and the wrong guess—ensuring clean, robust learning!"
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                Button(
                    onClick = onClose,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF059669)),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("CLOSE MANUAL", fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                }
            }
        }
    }
}

@Composable
fun ManualSection(title: String, body: String) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFF1E2433), RoundedCornerShape(10.dp))
            .padding(12.dp)
    ) {
        Text(
            text = title,
            color = Color(0xFF38BDF8),
            fontSize = 12.sp,
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = body,
            color = Color(0xFFCBD5E1),
            fontSize = 11.sp,
            lineHeight = 16.sp,
            fontFamily = FontFamily.Monospace
        )
    }
}
