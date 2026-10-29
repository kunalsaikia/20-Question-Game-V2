package com.example.twentyquestions.data

import android.content.Context
import com.example.twentyquestions.model.DatasetPresetId
import com.example.twentyquestions.model.GameObject
import com.example.twentyquestions.model.KnowledgeData
import com.example.twentyquestions.model.QuestionGate
import org.json.JSONArray
import org.json.JSONObject

class KnowledgeRepository(private val context: Context) {

    private val prefs = context.getSharedPreferences("twenty_questions_prefs_v3", Context.MODE_PRIVATE)

    companion object {
        private const val KEY_LEARNED_PAYLOAD = "learned_knowledge_payload"
        private const val KEY_GAMES_WON = "games_won_count"
        private const val KEY_ACTIVE_DATASET = "active_dataset_id"
    }

    fun getGamesWon(): Int = prefs.getInt(KEY_LEARNED_PAYLOAD + KEY_GAMES_WON, 0)

    fun incrementGamesWon(): Int {
        val next = getGamesWon() + 1
        prefs.edit().putInt(KEY_LEARNED_PAYLOAD + KEY_GAMES_WON, next).apply()
        return next
    }

    fun loadDataset(datasetId: DatasetPresetId): KnowledgeData {
        prefs.edit().putString(KEY_ACTIVE_DATASET, datasetId.name).apply()

        // 1. Load base asset
        val baseData = try {
            val jsonString = context.assets.open(datasetId.filename).bufferedReader().use { it.readText() }
            parseKnowledgeJson(jsonString, datasetId.name.lowercase())
        } catch (e: Exception) {
            e.printStackTrace()
            getEmbeddedFallback(datasetId)
        }

        // 2. Merge learned objects and gates if in MASTER mode
        if (datasetId == DatasetPresetId.MASTER) {
            val savedLearned = prefs.getString(KEY_LEARNED_PAYLOAD, null)
            if (!savedLearned.isNullOrBlank()) {
                try {
                    val learnedData = parseKnowledgeJson(savedLearned, "master")
                    val mergedQuestions = HashMap(baseData.questions).apply {
                        putAll(learnedData.questions)
                    }
                    val mergedGates = HashMap(baseData.gates).apply {
                        putAll(learnedData.gates)
                    }
                    val mergedObjects = baseData.objects.toMutableList()
                    for (lo in learnedData.objects) {
                        if (mergedObjects.none { it.name.equals(lo.name, ignoreCase = true) }) {
                            mergedObjects.add(lo)
                        }
                    }
                    return KnowledgeData(
                        objects = mergedObjects,
                        questions = mergedQuestions,
                        gates = mergedGates,
                        currentDatasetId = "master"
                    )
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }

        return baseData
    }

    fun saveLearnedKnowledge(data: KnowledgeData) {
        try {
            val learnedObjects = data.objects.filter { it.learned }
            val root = JSONObject()
            val questionsObj = JSONObject()
            data.questions.forEach { (k, v) -> questionsObj.put(k, v) }
            root.put("questions", questionsObj)

            val gatesObj = JSONObject()
            data.gates.forEach { (k, gate) ->
                val g = JSONObject().apply {
                    put("entityA", gate.entityA)
                    put("entityB", gate.entityB)
                }
                gatesObj.put(k, g)
            }
            root.put("gates", gatesObj)

            val objectsArr = JSONArray()
            for (obj in learnedObjects) {
                val o = JSONObject()
                o.put("name", obj.name)
                o.put("category", obj.category ?: "Learned")
                o.put("learned", true)
                val attrs = JSONObject()
                obj.attributes.forEach { (k, v) -> attrs.put(k, v.toDouble()) }
                o.put("attributes", attrs)
                objectsArr.put(o)
            }
            root.put("objects", objectsArr)

            prefs.edit().putString(KEY_LEARNED_PAYLOAD, root.toString()).apply()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun resetToFactory(datasetId: DatasetPresetId): KnowledgeData {
        prefs.edit().remove(KEY_LEARNED_PAYLOAD).apply()
        return loadDataset(datasetId)
    }

    fun parseKnowledgeJson(jsonString: String, datasetId: String): KnowledgeData {
        val root = JSONObject(jsonString)
        val questionsMap = mutableMapOf<String, String>()
        if (root.has("questions")) {
            val qObj = root.getJSONObject("questions")
            for (key in qObj.keys()) {
                questionsMap[key] = qObj.getString(key)
            }
        }

        val gatesMap = mutableMapOf<String, QuestionGate>()
        if (root.has("gates")) {
            val gObj = root.getJSONObject("gates")
            for (key in gObj.keys()) {
                val item = gObj.getJSONObject(key)
                gatesMap[key] = QuestionGate(
                    entityA = item.optString("entityA"),
                    entityB = item.optString("entityB")
                )
            }
        }

        val objectsList = mutableListOf<GameObject>()
        if (root.has("objects")) {
            val oArr = root.getJSONArray("objects")
            for (i in 0 until oArr.length()) {
                val oItem = oArr.getJSONObject(i)
                val name = oItem.getString("name")
                val category = oItem.optString("category", null)
                val learned = oItem.optBoolean("learned", false)
                val attrs = mutableMapOf<String, Float>()
                if (oItem.has("attributes")) {
                    val aObj = oItem.getJSONObject("attributes")
                    for (aKey in aObj.keys()) {
                        attrs[aKey] = aObj.getDouble(aKey).toFloat()
                    }
                }
                objectsList.add(GameObject(name, attrs, category, learned))
            }
        }

        return KnowledgeData(
            objects = objectsList,
            questions = questionsMap,
            gates = gatesMap,
            currentDatasetId = datasetId
        )
    }

    private fun getEmbeddedFallback(datasetId: DatasetPresetId): KnowledgeData {
        val questions = mapOf(
            "is_alive" to "Is it a living thing (animal, plant, or organism)?",
            "is_man_made" to "Was it manufactured or built by humans?",
            "uses_electricity" to "Does it use electricity, batteries, or digital technology?",
            "fits_in_hand" to "Can you comfortably hold or pick it up in one hand?",
            "can_eat" to "Can humans eat or drink it (food/beverage)?",
            "has_hair" to "Does it have hair or fur?",
            "four_legs" to "Does it have four legs?"
        )
        val objects = listOf(
            GameObject("Cat", mapOf("is_alive" to 1f, "is_man_made" to 0f, "uses_electricity" to 0f, "fits_in_hand" to 0.5f, "can_eat" to 0f, "has_hair" to 1f, "four_legs" to 1f), "Animal"),
            GameObject("Smartphone", mapOf("is_alive" to 0f, "is_man_made" to 1f, "uses_electricity" to 1f, "fits_in_hand" to 1f, "can_eat" to 0f, "has_hair" to 0f, "four_legs" to 0f), "Technology"),
            GameObject("Apple", mapOf("is_alive" to 0.5f, "is_man_made" to 0f, "uses_electricity" to 0f, "fits_in_hand" to 1f, "can_eat" to 1f, "has_hair" to 0f, "four_legs" to 0f), "Food"),
            GameObject("Bicycle", mapOf("is_alive" to 0f, "is_man_made" to 1f, "uses_electricity" to 0f, "fits_in_hand" to 0f, "can_eat" to 0f, "has_hair" to 0f, "four_legs" to 0f), "Vehicle")
        )
        return KnowledgeData(objects = objects, questions = questions, gates = emptyMap(), currentDatasetId = datasetId.name.lowercase())
    }
}
