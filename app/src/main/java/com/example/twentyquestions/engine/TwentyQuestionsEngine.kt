package com.example.twentyquestions.engine

import com.example.twentyquestions.model.AnswerChoice
import com.example.twentyquestions.model.AnswerRecord
import com.example.twentyquestions.model.CandidateScore
import com.example.twentyquestions.model.DatasetPresetId
import com.example.twentyquestions.model.GameObject
import com.example.twentyquestions.model.GuessResult
import com.example.twentyquestions.model.KnowledgeData
import com.example.twentyquestions.model.NextQuestionResult
import com.example.twentyquestions.model.QuestionGate
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt

class TwentyQuestionsEngine(
    var knowledge: KnowledgeData,
    var currentDatasetId: DatasetPresetId = DatasetPresetId.MASTER
) {
    val scores: MutableMap<String, Float> = mutableMapOf()
    val askedQuestions: MutableList<String> = mutableListOf()
    val answerHistory: MutableList<AnswerRecord> = mutableListOf()
    var currentConfidence: Int = 20
        private set
    var currentTopCandidate: String? = null
        private set

    init {
        resetGame()
    }

    fun resetGame() {
        scores.clear()
        for (obj in knowledge.objects) {
            scores[obj.name] = 0f
        }
        askedQuestions.clear()
        answerHistory.clear()
        currentConfidence = 20
        currentTopCandidate = null
    }

    fun getCandidateRanking(): List<CandidateScore> {
        val answeredCount = answerHistory.count { it.weight != null }
        val list = scores.map { (name, score) ->
            var matchPercentage = 100
            if (answeredCount > 0) {
                val raw = 1.0f - abs(score) / answeredCount.toFloat()
                matchPercentage = max(0, min(100, (raw * 100f).roundToInt()))
            }
            CandidateScore(name = name, score = score, matchPercentage = matchPercentage)
        }.sortedByDescending { it.score }

        currentTopCandidate = list.firstOrNull()?.name
        return list
    }

    fun getAdaptiveNextQuestion(roundNumber: Int): NextQuestionResult {
        // Master dataset starts with foundational living question
        if (answerHistory.isEmpty() && currentDatasetId == DatasetPresetId.MASTER) {
            return NextQuestionResult(
                question = "Is it a living thing (animal, plant, or organism)?",
                confidence = 25,
                topCandidate = null,
                topCandidates = emptyList(),
                readyToGuess = false,
                source = "local"
            )
        }

        val localQKey = getLocalBestQuestion()
        val prompt = if (localQKey != null) {
            knowledge.questions[localQKey] ?: localQKey
        } else {
            "Is it found inside a typical home?"
        }

        val ranking = getCandidateRanking()
        if (ranking.size > 1) {
            val diff = abs(ranking[0].score - ranking[1].score)
            val conf = min(98.0, 30.0 + answerHistory.size * 3.5 + diff * 15.0)
            currentConfidence = conf.roundToInt()
        }

        val ready = answerHistory.size >= 20 || (
            ranking.size > 1 &&
            abs(ranking[0].score - ranking[1].score) >= 2.5f &&
            answerHistory.size >= 8
        )

        return NextQuestionResult(
            question = prompt,
            confidence = currentConfidence,
            topCandidate = ranking.firstOrNull()?.name,
            topCandidates = ranking.take(3).map { it.name },
            readyToGuess = ready,
            source = "local"
        )
    }

    /**
     * EarthToJake Minimax Information-Jump Algorithm
     * Evaluates unasked questions against plausible candidates to maximize worst-case partition.
     */
    fun getLocalBestQuestion(): String? {
        var livingEliminated = false
        var inanimateEliminated = false
        var electricityEliminated = false
        var foodEliminated = false
        var wearableEliminated = false

        for (h in answerHistory) {
            val lower = h.prompt.lowercase()
            if (lower.contains("living thing") || lower.contains("animal or organism") || lower.contains("is it alive")) {
                if (h.answer == AnswerChoice.NO) livingEliminated = true
                if (h.answer == AnswerChoice.YES) inanimateEliminated = true
            }
            if (lower.contains("electricity") || lower.contains("batteries")) {
                if (h.answer == AnswerChoice.NO) electricityEliminated = true
            }
            if (lower.contains("eat or drink") || lower.contains("edible") || lower.contains("food")) {
                if (h.answer == AnswerChoice.NO) foodEliminated = true
            }
            if (lower.contains("worn on") || lower.contains("clothing") || lower.contains("applied to the human body")) {
                if (h.answer == AnswerChoice.NO) wearableEliminated = true
            }
        }

        val ranking = getCandidateRanking()
        val topScore = ranking.firstOrNull()?.score ?: 0f
        val plausibleCandidates = ranking
            .filter { abs(it.score - topScore) <= 2.0f }
            .take(20)

        val topCandidateNames = plausibleCandidates.take(3).map { it.name.lowercase() }

        val biologicalKeys = setOf(
            "is_alive", "is_animal", "is_mammal", "is_bird", "is_reptile", "is_fish",
            "is_amphibian", "is_insect", "is_invertebrate", "has_hair", "has_feathers",
            "lays_eggs", "feeds_milk", "airborne", "aquatic", "predator", "toothed",
            "has_backbone", "breathes_air", "venomous", "has_fins", "zero_legs",
            "two_legs", "four_legs", "five_legs", "six_legs", "eight_legs",
            "has_tail", "warm_blooded", "is_endangered", "lives_in_groups", "has_trunk",
            "cat_size_or_larger"
        )

        val unasked = knowledge.questions.keys.filter { qKey ->
            if (askedQuestions.contains(qKey)) return@filter false

            if (livingEliminated && biologicalKeys.contains(qKey)) return@filter false

            if (inanimateEliminated && setOf("is_man_made", "uses_electricity", "made_of_metal", "is_liquid", "is_worn").contains(qKey)) {
                return@filter false
            }

            if (electricityEliminated && qKey == "uses_electricity") return@filter false
            if (foodEliminated && qKey == "can_eat") return@filter false
            if (wearableEliminated && qKey == "is_worn") return@filter false

            // Gate checking for learned questions
            val gate = knowledge.gates[qKey]
            if (gate != null) {
                val matches = topCandidateNames.contains(gate.entityA.lowercase()) ||
                        topCandidateNames.contains(gate.entityB.lowercase())
                if (!matches) return@filter false
            }

            true
        }

        if (unasked.isEmpty()) return null
        if (plausibleCandidates.size <= 1) return unasked.first()

        var bestQ = unasked.first()
        var bestWorstCase = -1.0f

        for (qKey in unasked) {
            var yesWeight = 0.0f
            var noWeight = 0.0f

            for (cand in plausibleCandidates) {
                val obj = knowledge.objects.find { it.name == cand.name }
                val value = obj?.attributes?.get(qKey) ?: 0.5f
                when {
                    value >= 0.7f -> yesWeight += 1.0f
                    value <= 0.3f -> noWeight += 1.0f
                    else -> {
                        yesWeight += 0.5f
                        noWeight += 0.5f
                    }
                }
            }

            val worstCase = min(yesWeight, noWeight)
            if (worstCase > bestWorstCase) {
                bestWorstCase = worstCase
                bestQ = qKey
            }
        }

        return bestQ
    }

    /**
     * Applies difference penalty formula:
     * scores[obj.name] -= abs(weight - objVal)
     */
    fun answerQuestion(qText: String, answer: AnswerChoice) {
        val weight = answer.weight

        var qKey = knowledge.questions.entries.find {
            it.value.trim().equals(qText.trim(), ignoreCase = true)
        }?.key

        if (qKey == null) {
            qKey = qText.lowercase().replace(Regex("[^a-z0-9_]"), "_").take(30)
        }

        if (weight != null) {
            for (obj in knowledge.objects) {
                val objVal = obj.attributes[qKey] ?: 0.5f
                val penalty = abs(weight - objVal)
                scores[obj.name] = (scores[obj.name] ?: 0f) - penalty
            }
        }

        askedQuestions.add(qKey)
        answerHistory.add(
            AnswerRecord(
                qKey = qKey,
                prompt = qText,
                answer = answer,
                weight = weight,
                roundNumber = askedQuestions.size
            )
        )
    }

    fun undoLastAnswer(): Boolean {
        if (answerHistory.isEmpty()) return false
        answerHistory.removeAt(answerHistory.lastIndex)
        askedQuestions.removeAt(askedQuestions.lastIndex)

        scores.clear()
        for (obj in knowledge.objects) {
            scores[obj.name] = 0f
        }

        for (record in answerHistory) {
            val w = record.weight
            if (w != null) {
                for (obj in knowledge.objects) {
                    val objVal = obj.attributes[record.qKey] ?: 0.5f
                    scores[obj.name] = (scores[obj.name] ?: 0f) - abs(w - objVal)
                }
            }
        }
        return true
    }

    fun deduceFinalGuess(): GuessResult {
        val ranking = getCandidateRanking()
        val best = ranking.firstOrNull()?.name ?: "Mystery Object"
        return GuessResult(
            guess = best,
            confidence = currentConfidence,
            explanation = "Evaluated against 64-bit neural matrix difference penalties."
        )
    }

    fun getGuess(): String {
        return getCandidateRanking().firstOrNull()?.name ?: "Mystery Object"
    }

    /**
     * Gated neural learning: learns new object and question distinguishing it from wrongGuess.
     */
    fun learn(wrongGuess: String, correctName: String, newQText: String): KnowledgeData {
        val formattedName = correctName.trim().replaceFirstChar { it.uppercase() }
        var newQKey = newQText.lowercase().trim()
            .replace(Regex("[^a-z0-9_ ]"), "")
            .replace(Regex("\\s+"), "_")

        if (newQKey.isBlank()) {
            newQKey = "custom_q_${System.currentTimeMillis()}"
        }

        val cleanPrompt = if (newQText.trim().endsWith("?")) newQText.trim() else "${newQText.trim()}?"

        val updatedQuestions = HashMap(knowledge.questions).apply {
            put(newQKey, cleanPrompt)
        }

        val updatedGates = HashMap(knowledge.gates).apply {
            put(newQKey, QuestionGate(entityA = formattedName, entityB = wrongGuess))
        }

        val updatedObjects = knowledge.objects.map { obj ->
            val attrs = HashMap(obj.attributes)
            if (obj.name.equals(wrongGuess, ignoreCase = true)) {
                attrs[newQKey] = 0.0f
            } else {
                attrs[newQKey] = 0.5f
            }
            obj.copy(attributes = attrs)
        }.toMutableList()

        val newAttributes = mutableMapOf<String, Float>()
        for (q in updatedQuestions.keys) {
            newAttributes[q] = 0.5f
        }
        newAttributes[newQKey] = 1.0f

        updatedObjects.add(
            GameObject(
                name = formattedName,
                attributes = newAttributes,
                category = "Learned",
                learned = true
            )
        )

        val updatedKnowledge = KnowledgeData(
            objects = updatedObjects,
            questions = updatedQuestions,
            gates = updatedGates,
            currentDatasetId = currentDatasetId.name.lowercase()
        )

        this.knowledge = updatedKnowledge
        resetGame()
        return updatedKnowledge
    }
}
