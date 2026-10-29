package com.example.twentyquestions.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.HelpOutline
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material.icons.filled.VolumeMute
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.twentyquestions.ui.components.HowItWorksDialog
import com.example.twentyquestions.ui.components.KnowledgeInspectorDialog
import com.example.twentyquestions.ui.components.LearningDialog
import com.example.twentyquestions.ui.components.RoundDeviceChassis
import com.example.twentyquestions.ui.theme.ChassisBackground

@Composable
fun MainGameScreen(
    viewModel: TwentyQuestionsViewModel,
    modifier: Modifier = Modifier
) {
    val state by viewModel.uiState.collectAsState()

    Scaffold(
        containerColor = ChassisBackground,
        modifier = modifier.fillMaxSize()
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .statusBarsPadding()
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // TOP BAR
            TopBarView(
                state = state,
                onCycleTheme = { viewModel.cycleTheme() },
                onToggleSound = { viewModel.toggleSound() },
                onOpenInspector = { viewModel.openInspector() },
                onOpenHowItWorks = { viewModel.openHowItWorks() }
            )

            // CENTER PHYSICAL CHASSIS
            Box(
                modifier = Modifier
                    .weight(1f, fill = false)
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 4.dp),
                contentAlignment = Alignment.Center
            ) {
                RoundDeviceChassis(
                    state = state,
                    onAnswer = { viewModel.answer(it) },
                    onConfirmGuess = { viewModel.confirmGuess(it) },
                    onUndo = { viewModel.undo() },
                    onForceGuess = { viewModel.forceGuess() },
                    onStartGame = { viewModel.startGame() },
                    onOpenLearning = { viewModel.confirmGuess(false) }
                )
            }

            // FOOTER STATS
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Psychology,
                        contentDescription = "Learned Memory",
                        tint = Color(0xFF6EE7B7),
                        modifier = Modifier.size(14.dp)
                    )
                    Text(
                        text = "LEARNED: ${state.learnedCount}",
                        color = Color(0xFF6EE7B7),
                        fontSize = 10.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = "WINS: ${state.gamesWon}",
                        color = Color(0xFFFBBF24),
                        fontSize = 10.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "•",
                        color = Color(0xFF64748B),
                        fontSize = 10.sp
                    )
                    Text(
                        text = state.datasetId.label,
                        color = Color(0xFF94A3B8),
                        fontSize = 10.sp,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }
        }

        // DIALOGS
        if (state.showLearning && state.wrongGuess != null) {
            LearningDialog(
                wrongGuess = state.wrongGuess ?: "",
                onCommit = { name, q -> viewModel.commitLearning(name, q) },
                onCancel = { viewModel.cancelLearning() }
            )
        }

        if (state.showInspector) {
            KnowledgeInspectorDialog(
                knowledge = viewModel.getKnowledge(),
                candidateRanking = state.candidateRanking,
                currentDatasetId = state.datasetId,
                onSelectDataset = { viewModel.selectDataset(it) },
                onResetFactory = { viewModel.resetFactory() },
                onClose = { viewModel.closeInspector() }
            )
        }

        if (state.showHowItWorks) {
            HowItWorksDialog(onClose = { viewModel.closeHowItWorks() })
        }
    }
}

@Composable
fun TopBarView(
    state: GameUiState,
    onCycleTheme: () -> Unit,
    onToggleSound: () -> Unit,
    onOpenInspector: () -> Unit,
    onOpenHowItWorks: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 14.dp, vertical = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Logo & Tagline
        Column {
            Text(
                text = "20 QUESTIONS",
                color = Color.White,
                fontSize = 15.sp,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Black,
                letterSpacing = 1.sp
            )
            Text(
                text = "64-BIT NEURAL SYSTEM",
                color = Color(0xFF38BDF8),
                fontSize = 9.sp,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold
            )
        }

        // Action Icons
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            // Theme button
            IconButton(
                onClick = onCycleTheme,
                modifier = Modifier
                    .size(36.dp)
                    .testTag("toggle_theme_button")
            ) {
                Icon(
                    imageVector = Icons.Default.Palette,
                    contentDescription = "Cycle Backlight Theme",
                    tint = Color(0xFF94A3B8),
                    modifier = Modifier.size(20.dp)
                )
            }

            // Sound button
            IconButton(
                onClick = onToggleSound,
                modifier = Modifier
                    .size(36.dp)
                    .testTag("toggle_sound_button")
            ) {
                Icon(
                    imageVector = if (state.soundEnabled) Icons.Default.VolumeUp else Icons.Default.VolumeMute,
                    contentDescription = "Toggle Retro Sounds",
                    tint = if (state.soundEnabled) Color(0xFF34D399) else Color(0xFF64748B),
                    modifier = Modifier.size(20.dp)
                )
            }

            // Memory Inspector
            IconButton(
                onClick = onOpenInspector,
                modifier = Modifier
                    .size(36.dp)
                    .testTag("open_inspector_button")
            ) {
                Icon(
                    imageVector = Icons.Default.Storage,
                    contentDescription = "Neural Memory Inspector",
                    tint = Color(0xFF38BDF8),
                    modifier = Modifier.size(20.dp)
                )
            }

            // How It Works Manual
            IconButton(
                onClick = onOpenHowItWorks,
                modifier = Modifier
                    .size(36.dp)
                    .testTag("open_how_it_works_button")
            ) {
                Icon(
                    imageVector = Icons.Default.HelpOutline,
                    contentDescription = "Operating Manual",
                    tint = Color(0xFF94A3B8),
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}
