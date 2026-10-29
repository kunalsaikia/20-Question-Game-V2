package com.example.twentyquestions.model

import kotlinx.serialization.Serializable

enum class AnswerChoice(val value: String, val weight: Float?) {
    YES("yes", 1.0f),
    NO("no", 0.0f),
    SOMETIMES("sometimes", 0.5f),
    UNKNOWN("unknown", null);

    companion object {
        fun fromString(str: String): AnswerChoice = when (str.lowercase().trim()) {
            "yes", "y" -> YES
            "no", "n" -> NO
            "sometimes", "maybe", "s" -> SOMETIMES
            else -> UNKNOWN
        }
    }
}

@Serializable
data class GameObject(
    val name: String,
    val attributes: Map<String, Float> = emptyMap(),
    val category: String? = null,
    val learned: Boolean = false
)

@Serializable
data class QuestionGate(
    val entityA: String,
    val entityB: String
)

@Serializable
data class KnowledgeData(
    val objects: List<GameObject> = emptyList(),
    val questions: Map<String, String> = emptyMap(),
    val gates: Map<String, QuestionGate> = emptyMap(),
    val currentDatasetId: String? = "master"
)

data class AnswerRecord(
    val qKey: String,
    val prompt: String,
    val answer: AnswerChoice,
    val weight: Float?,
    val roundNumber: Int
)

enum class GamePhase {
    READY,
    PLAYING,
    GUESSING,
    VICTORY,
    LEARNING,
    LEARNED_SUCCESS
}

enum class BacklightTheme(val displayName: String) {
    EMERALD("Emerald"),
    AMBER("Amber"),
    CYAN("Cyan"),
    RUBY("Ruby")
}

enum class DeviceExpression(val face: String) {
    IDLE("[ • _ • ]"),
    THINKING("[ • ~ • ]"),
    CURIOUS("[ o _ O ]"),
    SMUG("[ ¬ ‿ ¬ ]"),
    CELEBRATING("[ ^ ‿ ^ ]"),
    PUZZLED("[ ? _ ? ]"),
    LEARNING("[ ⌁ LEARN ⌁ ]")
}

data class CandidateScore(
    val name: String,
    val score: Float,
    val matchPercentage: Int
)

data class NextQuestionResult(
    val question: String,
    val confidence: Int,
    val topCandidate: String? = null,
    val topCandidates: List<String> = emptyList(),
    val readyToGuess: Boolean = false,
    val source: String = "local"
)

data class GuessResult(
    val guess: String,
    val confidence: Int,
    val explanation: String? = null
)

enum class DatasetPresetId(val label: String, val filename: String) {
    MASTER("MASTER (120)", "default_knowledge.json"),
    BIG("BIG.CSV (100)", "big_dataset.json"),
    MEDIUM("MED.CSV (13)", "medium_dataset.json"),
    SMALL("SMALL.CSV (12)", "small_dataset.json"),
    MICRO("MICRO.CSV (5)", "micro_dataset.json")
}
