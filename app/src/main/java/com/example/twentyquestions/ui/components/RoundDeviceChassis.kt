package com.example.twentyquestions.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.twentyquestions.model.AnswerChoice
import com.example.twentyquestions.ui.GameUiState

@Composable
fun RoundDeviceChassis(
    state: GameUiState,
    onAnswer: (AnswerChoice) -> Unit,
    onConfirmGuess: (Boolean) -> Unit,
    onUndo: () -> Unit,
    onForceGuess: () -> Unit,
    onStartGame: () -> Unit,
    onOpenLearning: () -> Unit,
    modifier: Modifier = Modifier
) {
    val themeTokens = getThemeTokens(state.theme)

    Box(
        modifier = modifier
            .testTag("round_device_chassis")
            .widthIn(max = 540.dp)
            .aspectRatio(0.92f)
            .padding(8.dp),
        contentAlignment = Alignment.Center
    ) {
        // Outer circular ambient glow
        Box(
            modifier = Modifier
                .fillMaxSize(0.96f)
                .clip(CircleShape)
                .background(themeTokens.border.copy(alpha = 0.12f))
        )

        // Main Circular Outer Chassis Bezel
        Box(
            modifier = Modifier
                .fillMaxSize()
                .shadow(elevation = 16.dp, shape = CircleShape)
                .clip(CircleShape)
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            Color(0xFF2D3546),
                            Color(0xFF181D29),
                            Color(0xFF0C0F17),
                            Color(0xFF05070B)
                        ),
                        center = Offset.Unspecified,
                        radius = 800f
                    )
                )
                .border(6.dp, Color(0xFF374151), CircleShape)
                .border(2.dp, Color(0xFF1E2430), CircleShape)
                .padding(14.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 8.dp, vertical = 6.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                // TOP CHASSIS HARDWARE ACCENTS: Speaker Grille & Screw Rivets
                Row(
                    modifier = Modifier
                        .fillMaxWidth(0.7f)
                        .padding(top = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Left screw rivet
                    ScrewRivet()

                    // Top center speaker grill slots
                    SpeakerGrille()

                    // Right screw rivet
                    ScrewRivet()
                }

                // DEVICE BRANDING
                Text(
                    text = "RADICA // 20Q NEURAL CORE",
                    color = Color(0xFF94A3B8),
                    fontSize = 9.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )

                // CENTER RECESSED LCD SCREEN
                LcdScreenView(
                    state = state,
                    modifier = Modifier
                        .fillMaxWidth(0.88f)
                        .weight(1f)
                        .padding(vertical = 4.dp)
                )

                // BOTTOM CONTROLS SECTION
                TactileButtonsView(
                    state = state,
                    onAnswer = onAnswer,
                    onConfirmGuess = onConfirmGuess,
                    onUndo = onUndo,
                    onForceGuess = onForceGuess,
                    onStartGame = onStartGame,
                    onOpenLearning = onOpenLearning,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(2.dp))
            }
        }
    }
}

@Composable
fun SpeakerGrille() {
    Column(
        verticalArrangement = Arrangement.spacedBy(2.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        repeat(3) {
            Box(
                modifier = Modifier
                    .size(width = 40.dp, height = 3.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(Color(0xFF0A0C11))
                    .border(0.5.dp, Color(0xFF334155), RoundedCornerShape(2.dp))
            )
        }
    }
}

@Composable
fun ScrewRivet() {
    Box(
        modifier = Modifier
            .size(10.dp)
            .clip(CircleShape)
            .background(Color(0xFF1E2430))
            .border(1.dp, Color(0xFF475569), CircleShape),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.size(6.dp)) {
            drawLine(
                color = Color(0xFF64748B),
                start = Offset(0f, size.height / 2),
                end = Offset(size.width, size.height / 2),
                strokeWidth = 1.5f
            )
        }
    }
}
