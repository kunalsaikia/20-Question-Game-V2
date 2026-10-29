package com.example.twentyquestions.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Help
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.RemoveCircle
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Undo
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.twentyquestions.model.AnswerChoice
import com.example.twentyquestions.model.GamePhase
import com.example.twentyquestions.ui.GameUiState
import com.example.twentyquestions.ui.theme.ButtonNoRedBottom
import com.example.twentyquestions.ui.theme.ButtonNoRedTop
import com.example.twentyquestions.ui.theme.ButtonSometimesYellowBottom
import com.example.twentyquestions.ui.theme.ButtonSometimesYellowTop
import com.example.twentyquestions.ui.theme.ButtonUnknownBlueBottom
import com.example.twentyquestions.ui.theme.ButtonUnknownBlueTop
import com.example.twentyquestions.ui.theme.ButtonYesGreenBottom
import com.example.twentyquestions.ui.theme.ButtonYesGreenTop

@Composable
fun TactileButtonsView(
    state: GameUiState,
    onAnswer: (AnswerChoice) -> Unit,
    onConfirmGuess: (Boolean) -> Unit,
    onUndo: () -> Unit,
    onForceGuess: () -> Unit,
    onStartGame: () -> Unit,
    onOpenLearning: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isGuessing = state.phase == GamePhase.GUESSING
    val isPlaying = state.phase == GamePhase.PLAYING
    val isFinished = state.phase == GamePhase.VICTORY ||
            state.phase == GamePhase.LEARNING ||
            state.phase == GamePhase.LEARNED_SUCCESS

    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        if (isFinished) {
            // FINISHED STATE: Prominent Tactile Play Again Button
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (state.phase == GamePhase.LEARNING) {
                    TactileBigButton(
                        text = "TEACH [T]",
                        subText = "ADD OBJECT",
                        icon = Icons.Default.School,
                        topColor = Color(0xFF6366F1),
                        bottomColor = Color(0xFF3730A3),
                        borderColor = Color(0xFFA5B4FC),
                        modifier = Modifier.weight(1f).testTag("teach_device_button"),
                        onClick = onOpenLearning
                    )
                }

                TactileBigButton(
                    text = "PLAY AGAIN [R]",
                    subText = "RESET 20Q CORE",
                    icon = Icons.Default.Refresh,
                    topColor = Color(0xFF10B981),
                    bottomColor = Color(0xFF065F46),
                    borderColor = Color(0xFF6EE7B7),
                    modifier = Modifier.weight(1f).testTag("play_again_button"),
                    onClick = onStartGame
                )
            }
        } else {
            // PRIMARY 4 TACTILE BUTTONS
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                // YES Button
                TactileHardwareButton(
                    text = "YES",
                    subText = if (isGuessing) "CORRECT" else "[Y]",
                    icon = Icons.Default.Check,
                    topColor = ButtonYesGreenTop,
                    bottomColor = ButtonYesGreenBottom,
                    borderColor = Color(0xFF34D399),
                    enabled = (isPlaying && !state.isLoading) || isGuessing,
                    modifier = Modifier
                        .weight(1f)
                        .testTag("button_yes"),
                    onClick = {
                        if (isGuessing) onConfirmGuess(true) else onAnswer(AnswerChoice.YES)
                    }
                )

                // NO Button
                TactileHardwareButton(
                    text = "NO",
                    subText = if (isGuessing) "WRONG" else "[N]",
                    icon = Icons.Default.Close,
                    topColor = ButtonNoRedTop,
                    bottomColor = ButtonNoRedBottom,
                    borderColor = Color(0xFFF87171),
                    enabled = (isPlaying && !state.isLoading) || isGuessing,
                    modifier = Modifier
                        .weight(1f)
                        .testTag("button_no"),
                    onClick = {
                        if (isGuessing) onConfirmGuess(false) else onAnswer(AnswerChoice.NO)
                    }
                )

                // SOMETIMES Button
                TactileHardwareButton(
                    text = "SOMETIMES",
                    subText = "[S]",
                    icon = Icons.Default.RemoveCircle,
                    topColor = ButtonSometimesYellowTop,
                    bottomColor = ButtonSometimesYellowBottom,
                    borderColor = Color(0xFFFBBF24),
                    enabled = isPlaying && !state.isLoading,
                    modifier = Modifier
                        .weight(1f)
                        .testTag("button_sometimes"),
                    onClick = { onAnswer(AnswerChoice.SOMETIMES) }
                )

                // UNKNOWN Button
                TactileHardwareButton(
                    text = "UNKNOWN",
                    subText = "[U]",
                    icon = Icons.Default.Help,
                    topColor = ButtonUnknownBlueTop,
                    bottomColor = ButtonUnknownBlueBottom,
                    borderColor = Color(0xFF60A5FA),
                    enabled = isPlaying && !state.isLoading,
                    modifier = Modifier
                        .weight(1f)
                        .testTag("button_unknown"),
                    onClick = { onAnswer(AnswerChoice.UNKNOWN) }
                )
            }
        }

        // AUXILIARY CONTROLS (Undo, Guess Now)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Undo button
            AuxiliaryPillButton(
                icon = Icons.Default.Undo,
                label = "UNDO [Z]",
                enabled = state.canUndo && isPlaying && !state.isLoading,
                modifier = Modifier.testTag("button_undo"),
                onClick = onUndo
            )

            // Force Guess button
            AuxiliaryPillButton(
                icon = Icons.Default.Lightbulb,
                label = "GUESS NOW",
                enabled = state.canGuessNow && isPlaying && !state.isLoading,
                modifier = Modifier.testTag("button_guess_now"),
                onClick = onForceGuess
            )
        }
    }
}

@Composable
fun TactileHardwareButton(
    text: String,
    subText: String,
    icon: ImageVector,
    topColor: Color,
    bottomColor: Color,
    borderColor: Color,
    enabled: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(if (isPressed) 0.94f else 1f, label = "press_scale")

    val bgBrush = if (enabled) {
        Brush.verticalGradient(listOf(topColor, bottomColor))
    } else {
        Brush.verticalGradient(listOf(Color(0xFF27272A), Color(0xFF18181B)))
    }

    Box(
        modifier = modifier
            .scale(scale)
            .clip(RoundedCornerShape(16.dp))
            .background(bgBrush)
            .border(
                width = 2.dp,
                color = if (enabled) borderColor.copy(alpha = 0.8f) else Color(0xFF3F3F46),
                shape = RoundedCornerShape(16.dp)
            )
            .clickable(
                enabled = enabled,
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            )
            .padding(vertical = 10.dp, horizontal = 4.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Glowing LED indicator pip
            Box(
                modifier = Modifier
                    .size(6.dp)
                    .clip(CircleShape)
                    .background(if (enabled) borderColor else Color.DarkGray)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Icon(
                imageVector = icon,
                contentDescription = text,
                tint = if (enabled) Color.White else Color.Gray,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = text,
                color = if (enabled) Color.White else Color.Gray,
                fontSize = 11.sp,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Black
            )
            Text(
                text = subText,
                color = if (enabled) Color.White.copy(alpha = 0.7f) else Color.DarkGray,
                fontSize = 8.sp,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

@Composable
fun TactileBigButton(
    text: String,
    subText: String,
    icon: ImageVector,
    topColor: Color,
    bottomColor: Color,
    borderColor: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(if (isPressed) 0.96f else 1f, label = "press_scale_big")

    Box(
        modifier = modifier
            .scale(scale)
            .clip(RoundedCornerShape(18.dp))
            .background(Brush.verticalGradient(listOf(topColor, bottomColor)))
            .border(2.dp, borderColor, RoundedCornerShape(18.dp))
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            )
            .padding(vertical = 12.dp, horizontal = 12.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = text,
                tint = Color.White,
                modifier = Modifier.size(20.dp)
            )
            Column {
                Text(
                    text = text,
                    color = Color.White,
                    fontSize = 12.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Black
                )
                Text(
                    text = subText,
                    color = Color.White.copy(alpha = 0.8f),
                    fontSize = 8.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}

@Composable
fun AuxiliaryPillButton(
    icon: ImageVector,
    label: String,
    enabled: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(if (enabled) Color(0xFF1E2433) else Color(0xFF121622))
            .border(
                1.dp,
                if (enabled) Color(0xFF3B4760) else Color(0xFF1F2432),
                RoundedCornerShape(12.dp)
            )
            .clickable(enabled = enabled, onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 6.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = if (enabled) Color(0xFF93C5FD) else Color(0xFF4B5563),
                modifier = Modifier.size(14.dp)
            )
            Text(
                text = label,
                color = if (enabled) Color(0xFFE2E8F0) else Color(0xFF6B7280),
                fontSize = 10.sp,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold
            )
        }
    }
}
