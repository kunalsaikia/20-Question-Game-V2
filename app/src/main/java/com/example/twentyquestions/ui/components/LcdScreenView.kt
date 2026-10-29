package com.example.twentyquestions.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.twentyquestions.model.BacklightTheme
import com.example.twentyquestions.model.GamePhase
import com.example.twentyquestions.ui.GameUiState
import com.example.twentyquestions.ui.theme.LcdAmberAccent
import com.example.twentyquestions.ui.theme.LcdAmberBg
import com.example.twentyquestions.ui.theme.LcdAmberBorder
import com.example.twentyquestions.ui.theme.LcdAmberMuted
import com.example.twentyquestions.ui.theme.LcdAmberText
import com.example.twentyquestions.ui.theme.LcdCyanAccent
import com.example.twentyquestions.ui.theme.LcdCyanBg
import com.example.twentyquestions.ui.theme.LcdCyanBorder
import com.example.twentyquestions.ui.theme.LcdCyanMuted
import com.example.twentyquestions.ui.theme.LcdCyanText
import com.example.twentyquestions.ui.theme.LcdEmeraldAccent
import com.example.twentyquestions.ui.theme.LcdEmeraldBg
import com.example.twentyquestions.ui.theme.LcdEmeraldBorder
import com.example.twentyquestions.ui.theme.LcdEmeraldMuted
import com.example.twentyquestions.ui.theme.LcdEmeraldText
import com.example.twentyquestions.ui.theme.LcdRubyAccent
import com.example.twentyquestions.ui.theme.LcdRubyBg
import com.example.twentyquestions.ui.theme.LcdRubyBorder
import com.example.twentyquestions.ui.theme.LcdRubyMuted
import com.example.twentyquestions.ui.theme.LcdRubyText

data class ThemeColorTokens(
    val bg: Color,
    val border: Color,
    val textPrimary: Color,
    val textMuted: Color,
    val accent: Color
)

@Composable
fun getThemeTokens(theme: BacklightTheme): ThemeColorTokens = when (theme) {
    BacklightTheme.EMERALD -> ThemeColorTokens(LcdEmeraldBg, LcdEmeraldBorder, LcdEmeraldText, LcdEmeraldMuted, LcdEmeraldAccent)
    BacklightTheme.AMBER -> ThemeColorTokens(LcdAmberBg, LcdAmberBorder, LcdAmberText, LcdAmberMuted, LcdAmberAccent)
    BacklightTheme.CYAN -> ThemeColorTokens(LcdCyanBg, LcdCyanBorder, LcdCyanText, LcdCyanMuted, LcdCyanAccent)
    BacklightTheme.RUBY -> ThemeColorTokens(LcdRubyBg, LcdRubyBorder, LcdRubyText, LcdRubyMuted, LcdRubyAccent)
}

@Composable
fun LcdScreenView(
    state: GameUiState,
    modifier: Modifier = Modifier
) {
    val tokens = getThemeTokens(state.theme)

    val transition = rememberInfiniteTransition(label = "pulse")
    val alphaAnim by transition.animateFloat(
        initialValue = 0.5f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "alpha"
    )

    Box(
        modifier = modifier
            .testTag("lcd_screen_box")
            .clip(RoundedCornerShape(24.dp))
            .background(tokens.bg)
            .border(3.dp, tokens.border.copy(alpha = 0.8f), RoundedCornerShape(24.dp))
            .padding(12.dp)
    ) {
        // CRT Scanline Canvas effect
        Canvas(modifier = Modifier.fillMaxSize()) {
            val step = 4.dp.toPx()
            var y = 0f
            while (y < size.height) {
                drawLine(
                    color = Color.Black.copy(alpha = 0.25f),
                    start = Offset(0f, y),
                    end = Offset(size.width, y),
                    strokeWidth = 1.5f
                )
                y += step
            }
        }

        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.SpaceBetween,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // TOP STATUS BAR
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 4.dp, vertical = 2.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Question Counter Pill
                Text(
                    text = if (state.phase == GamePhase.PLAYING) {
                        "Q ${state.questionNumber}/${state.totalQuestions}"
                    } else {
                        "20Q // NEURAL"
                    },
                    color = tokens.accent,
                    fontSize = 11.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.testTag("round_counter_text")
                )

                // 8-Bit Dot-Matrix Expression Avatar
                Text(
                    text = if (state.isLoading) "[ ⌁ DEDUCING ⌁ ]" else state.expression.face,
                    color = if (state.isLoading) tokens.accent.copy(alpha = alphaAnim) else tokens.textPrimary,
                    fontSize = 12.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Black,
                    modifier = Modifier.testTag("device_avatar_expression")
                )

                // Confidence readout
                Text(
                    text = "${state.confidence}% CONF",
                    color = tokens.textMuted,
                    fontSize = 10.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.SemiBold
                )
            }

            // CENTER READOUT (Main Prompt / Guess / Victory)
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 4.dp),
                contentAlignment = Alignment.Center
            ) {
                when (state.phase) {
                    GamePhase.PLAYING -> {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = state.currentPrompt ?: "Thinking...",
                                color = tokens.textPrimary,
                                fontSize = 15.sp,
                                lineHeight = 20.sp,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.testTag("current_question_text")
                            )
                        }
                    }
                    GamePhase.GUESSING -> {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "I DEDUCE IT IS A:",
                                color = tokens.textMuted,
                                fontSize = 11.sp,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.sp
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = state.currentGuess?.uppercase() ?: "UNKNOWN",
                                color = tokens.accent,
                                fontSize = 20.sp,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Black,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.testTag("deduced_guess_text")
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "ARE YOU THINKING OF THIS?",
                                color = tokens.textPrimary,
                                fontSize = 10.sp,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                    GamePhase.VICTORY -> {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "★ MIND READ ★",
                                color = tokens.accent,
                                fontSize = 18.sp,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Black
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "I GUESSED: ${state.currentGuess?.uppercase()}",
                                color = tokens.textPrimary,
                                fontSize = 13.sp,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "NEURAL VICTORY IN ${state.questionNumber} QUESTIONS!",
                                color = tokens.textMuted,
                                fontSize = 9.sp,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    }
                    GamePhase.LEARNING -> {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "[ YOU WIN! ]",
                                color = tokens.accent,
                                fontSize = 16.sp,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Black
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "I was thinking of: ${state.currentGuess}",
                                color = tokens.textPrimary,
                                fontSize = 12.sp,
                                fontFamily = FontFamily.Monospace
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Tap 'TEACH' below to expand neural memory",
                                color = tokens.textMuted,
                                fontSize = 10.sp,
                                fontFamily = FontFamily.Monospace,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                    GamePhase.LEARNED_SUCCESS -> {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "⚡ MEMORY EXPANDED ⚡",
                                color = tokens.accent,
                                fontSize = 15.sp,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Black
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "NEW DISTINCTION COMMITTED TO FLASH",
                                color = tokens.textPrimary,
                                fontSize = 10.sp,
                                fontFamily = FontFamily.Monospace,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                    GamePhase.READY -> {
                        Text(
                            text = "THINK OF AN OBJECT\nAND PRESS START",
                            color = tokens.textPrimary,
                            fontSize = 14.sp,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }

            // BOTTOM STATUS LINE
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 4.dp, vertical = 2.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (!state.lastAnswer.isNullOrBlank()) "LAST: [${state.lastAnswer}]" else "64-BIT CORE",
                    color = tokens.textMuted,
                    fontSize = 9.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.SemiBold
                )

                // Top surviving contender hint
                val topCand = state.candidateRanking.firstOrNull()?.name
                Text(
                    text = if (topCand != null) "CAND: $topCand" else "SET: ${state.datasetId.name}",
                    color = tokens.accent.copy(alpha = 0.8f),
                    fontSize = 9.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}
