package com.example.twentyquestions.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.twentyquestions.audio.RetroSoundManager
import com.example.twentyquestions.data.KnowledgeRepository
import com.example.twentyquestions.engine.TwentyQuestionsEngine
import com.example.twentyquestions.model.AnswerChoice
import com.example.twentyquestions.model.BacklightTheme
import com.example.twentyquestions.model.CandidateScore
import com.example.twentyquestions.model.DatasetPresetId
import com.example.twentyquestions.model.DeviceExpression
import com.example.twentyquestions.model.GamePhase
import com.example.twentyquestions.model.KnowledgeData
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class GameUiState(
    val phase: GamePhase = GamePhase.PLAYING,
    val currentPrompt: String? = null,
    val questionNumber: Int = 1,
    val totalQuestions: Int = 20,
    val confidence: Int = 20,
    val currentGuess: String? = null,
    val guessExplanation: String? = null,
    val expression: DeviceExpression = DeviceExpression.THINKING,
    val theme: BacklightTheme = BacklightTheme.EMERALD,
    val soundEnabled: Boolean = true,
    val candidateRanking: List<CandidateScore> = emptyList(),
    val learnedCount: Int = 0,
    val datasetId: DatasetPresetId = DatasetPresetId.MASTER,
    val isLoading: Boolean = false,
    val gamesWon: Int = 0,
    val wrongGuess: String? = null,
    val lastAnswer: String? = null,
    val canUndo: Boolean = false,
    val canGuessNow: Boolean = false,
    val showInspector: Boolean = false,
    val showHowItWorks: Boolean = false,
    val showLearning: Boolean = false
)

class TwentyQuestionsViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = KnowledgeRepository(application)
    val soundManager = RetroSoundManager()

    private val engine: TwentyQuestionsEngine

    private val _uiState = MutableStateFlow(GameUiState())
    val uiState: StateFlow<GameUiState> = _uiState.asStateFlow()

    init {
        val initialData = repository.loadDataset(DatasetPresetId.MASTER)
        engine = TwentyQuestionsEngine(initialData, DatasetPresetId.MASTER)

        val firstQ = "Is it a living thing (animal, plant, or organism)?"
        _uiState.update {
            it.copy(
                currentPrompt = firstQ,
                questionNumber = 1,
                gamesWon = repository.getGamesWon(),
                learnedCount = initialData.objects.count { obj -> obj.learned },
                candidateRanking = engine.getCandidateRanking()
            )
        }
    }

    fun startGame() {
        soundManager.playClick()
        engine.resetGame()

        val prompt = if (engine.currentDatasetId == DatasetPresetId.MASTER) {
            "Is it a living thing (animal, plant, or organism)?"
        } else {
            val best = engine.getLocalBestQuestion()
            if (best != null) engine.knowledge.questions[best] ?: best else "Does it make sound?"
        }

        _uiState.update {
            it.copy(
                phase = GamePhase.PLAYING,
                currentPrompt = prompt,
                questionNumber = 1,
                currentGuess = null,
                guessExplanation = null,
                wrongGuess = null,
                lastAnswer = null,
                canUndo = false,
                canGuessNow = false,
                expression = DeviceExpression.THINKING,
                candidateRanking = engine.getCandidateRanking(),
                confidence = engine.currentConfidence,
                isLoading = false
            )
        }
    }

    fun answer(choice: AnswerChoice) {
        val state = _uiState.value
        if (state.phase != GamePhase.PLAYING || state.currentPrompt == null || state.isLoading) return

        when (choice) {
            AnswerChoice.YES -> soundManager.playAnswerYes()
            AnswerChoice.NO -> soundManager.playAnswerNo()
            AnswerChoice.SOMETIMES -> soundManager.playAnswerSometimes()
            AnswerChoice.UNKNOWN -> soundManager.playAnswerUnknown()
        }

        engine.answerQuestion(state.currentPrompt, choice)
        val roundNum = engine.answerHistory.size

        if (roundNum >= state.totalQuestions) {
            executeGuess()
            return
        }

        _uiState.update {
            it.copy(
                isLoading = true,
                lastAnswer = choice.value.uppercase(),
                canUndo = true,
                canGuessNow = roundNum >= 3
            )
        }

        viewModelScope.launch {
            delay(120) // Micro pause to feel like retro processor crunching bits
            val nextResult = engine.getAdaptiveNextQuestion(roundNum + 1)

            if (nextResult.readyToGuess && roundNum >= 8) {
                executeGuess()
            } else {
                val nextExp = when {
                    roundNum > 14 -> DeviceExpression.SMUG
                    roundNum > 7 -> DeviceExpression.CURIOUS
                    else -> DeviceExpression.THINKING
                }

                _uiState.update {
                    it.copy(
                        currentPrompt = nextResult.question,
                        questionNumber = roundNum + 1,
                        confidence = nextResult.confidence,
                        expression = nextExp,
                        candidateRanking = engine.getCandidateRanking(),
                        isLoading = false
                    )
                }
            }
        }
    }

    fun undo() {
        if (_uiState.value.phase != GamePhase.PLAYING || _uiState.value.isLoading) return
        val ok = engine.undoLastAnswer()
        if (ok) {
            soundManager.playClick()
            val best = engine.getLocalBestQuestion()
            val prompt = if (best != null) engine.knowledge.questions[best] ?: best else "Is it found inside a home?"
            _uiState.update {
                it.copy(
                    currentPrompt = prompt,
                    questionNumber = maxOf(1, engine.answerHistory.size + 1),
                    canUndo = engine.answerHistory.isNotEmpty(),
                    canGuessNow = engine.answerHistory.size >= 3,
                    candidateRanking = engine.getCandidateRanking(),
                    confidence = engine.currentConfidence
                )
            }
        }
    }

    fun forceGuess() {
        if (_uiState.value.phase != GamePhase.PLAYING || _uiState.value.isLoading) return
        executeGuess()
    }

    private fun executeGuess() {
        _uiState.update { it.copy(isLoading = true) }
        viewModelScope.launch {
            delay(200)
            val deduction = engine.deduceFinalGuess()
            val exp = if (deduction.confidence > 75) DeviceExpression.SMUG else DeviceExpression.CURIOUS
            _uiState.update {
                it.copy(
                    phase = GamePhase.GUESSING,
                    currentGuess = deduction.guess,
                    guessExplanation = deduction.explanation,
                    confidence = deduction.confidence,
                    expression = exp,
                    currentPrompt = null,
                    isLoading = false
                )
            }
        }
    }

    fun confirmGuess(correct: Boolean) {
        if (_uiState.value.phase != GamePhase.GUESSING) return

        if (correct) {
            soundManager.playVictory()
            val newWins = repository.incrementGamesWon()
            _uiState.update {
                it.copy(
                    phase = GamePhase.VICTORY,
                    expression = DeviceExpression.CELEBRATING,
                    gamesWon = newWins
                )
            }
        } else {
            soundManager.playDefeat()
            val missed = _uiState.value.currentGuess ?: "Mystery Object"
            _uiState.update {
                it.copy(
                    phase = GamePhase.LEARNING,
                    wrongGuess = missed,
                    expression = DeviceExpression.PUZZLED,
                    showLearning = true
                )
            }
        }
    }

    fun commitLearning(correctName: String, newQuestionText: String) {
        val missed = _uiState.value.wrongGuess ?: return
        soundManager.playLearning()

        val updated = engine.learn(missed, correctName, newQuestionText)
        repository.saveLearnedKnowledge(updated)

        _uiState.update {
            it.copy(
                phase = GamePhase.LEARNED_SUCCESS,
                expression = DeviceExpression.LEARNING,
                showLearning = false,
                learnedCount = updated.objects.count { o -> o.learned }
            )
        }
    }

    fun cancelLearning() {
        _uiState.update { it.copy(showLearning = false) }
    }

    fun cycleTheme() {
        soundManager.playClick()
        val themes = BacklightTheme.values()
        val next = themes[(_uiState.value.theme.ordinal + 1) % themes.size]
        _uiState.update { it.copy(theme = next) }
    }

    fun toggleSound() {
        val next = !_uiState.value.soundEnabled
        soundManager.enabled = next
        if (next) soundManager.playClick()
        _uiState.update { it.copy(soundEnabled = next) }
    }

    fun selectDataset(datasetId: DatasetPresetId) {
        soundManager.playClick()
        val loaded = repository.loadDataset(datasetId)
        engine.knowledge = loaded
        engine.currentDatasetId = datasetId
        _uiState.update {
            it.copy(
                datasetId = datasetId,
                learnedCount = loaded.objects.count { o -> o.learned }
            )
        }
        startGame()
    }

    fun resetFactory() {
        soundManager.playClick()
        val defaultData = repository.resetToFactory(_uiState.value.datasetId)
        engine.knowledge = defaultData
        _uiState.update {
            it.copy(
                learnedCount = 0,
                candidateRanking = engine.getCandidateRanking()
            )
        }
        startGame()
    }

    fun openInspector() {
        soundManager.playClick()
        _uiState.update { it.copy(showInspector = true) }
    }

    fun closeInspector() {
        _uiState.update { it.copy(showInspector = false) }
    }

    fun openHowItWorks() {
        soundManager.playClick()
        _uiState.update { it.copy(showHowItWorks = true) }
    }

    fun closeHowItWorks() {
        _uiState.update { it.copy(showHowItWorks = false) }
    }

    fun getKnowledge(): KnowledgeData = engine.knowledge
}
