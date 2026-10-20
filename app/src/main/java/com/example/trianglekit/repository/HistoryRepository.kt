package com.example.trianglekit.repository

import android.content.Context
import android.content.SharedPreferences
import com.example.trianglekit.model.AreaUnit
import com.example.trianglekit.model.CalculationStep
import com.example.trianglekit.model.LengthUnit
import com.example.trianglekit.model.SolveType
import com.example.trianglekit.model.TriangleAngleClassification
import com.example.trianglekit.model.TriangleHistory
import com.example.trianglekit.model.TriangleResult
import com.example.trianglekit.model.TriangleSideClassification
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.json.JSONArray
import org.json.JSONObject
import java.math.BigDecimal
import java.util.UUID

class HistoryRepository(context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences("trianglekit_history", Context.MODE_PRIVATE)

    private val _history = MutableStateFlow<List<TriangleHistory>>(emptyList())
    val history: StateFlow<List<TriangleHistory>> = _history.asStateFlow()

    init {
        _history.value = loadHistory()
    }

    fun addHistory(
        solveType: SolveType,
        inputs: Map<String, String>,
        inputUnits: Map<String, LengthUnit>,
        resultUnit: LengthUnit,
        areaUnit: AreaUnit,
        results: List<TriangleResult>
    ): Boolean {
        if (results.isEmpty()) return false

        val current = _history.value.toMutableList()

        // Consecutive duplicate check
        if (current.isNotEmpty()) {
            val latest = current.first()
            if (latest.solveType == solveType &&
                latest.inputs == inputs &&
                latest.inputUnits == inputUnits &&
                latest.resultUnit == resultUnit &&
                latest.areaUnit == areaUnit
            ) {
                // Exact duplicate of most recent entry; don't add duplicate
                return false
            }
        }

        val item = TriangleHistory(
            id = UUID.randomUUID().toString(),
            solveType = solveType,
            inputs = inputs,
            inputUnits = inputUnits,
            resultUnit = resultUnit,
            areaUnit = areaUnit,
            results = results,
            savedAt = System.currentTimeMillis()
        )

        current.add(0, item)
        // Enforce maximum 10 items
        while (current.size > 10) {
            current.removeAt(current.size - 1)
        }

        saveHistory(current)
        _history.value = current
        return true
    }

    fun deleteHistory(id: String) {
        val current = _history.value.filter { it.id != id }
        saveHistory(current)
        _history.value = current
    }

    fun clearHistory() {
        saveHistory(emptyList())
        _history.value = emptyList()
    }

    private fun loadHistory(): List<TriangleHistory> {
        val jsonStr = prefs.getString(KEY_HISTORY, null) ?: return emptyList()
        val list = mutableListOf<TriangleHistory>()

        return try {
            val jsonArray = JSONArray(jsonStr)
            for (i in 0 until jsonArray.length()) {
                val obj = jsonArray.getJSONObject(i)
                val id = obj.getString("id")
                val solveType = SolveType.valueOf(obj.getString("solveType"))

                val inputsMap = mutableMapOf<String, String>()
                val inputsObj = obj.getJSONObject("inputs")
                inputsObj.keys().forEach { key ->
                    inputsMap[key] = inputsObj.getString(key)
                }

                val inputUnitsMap = mutableMapOf<String, LengthUnit>()
                val inputUnitsObj = obj.getJSONObject("inputUnits")
                inputUnitsObj.keys().forEach { key ->
                    inputUnitsMap[key] = LengthUnit.valueOf(inputUnitsObj.getString(key))
                }

                val resultUnit = LengthUnit.valueOf(obj.getString("resultUnit"))
                val areaUnit = AreaUnit.valueOf(obj.getString("areaUnit"))
                val savedAt = obj.getLong("savedAt")

                val resultsList = mutableListOf<TriangleResult>()
                val resultsArray = obj.getJSONArray("results")
                for (j in 0 until resultsArray.length()) {
                    val resObj = resultsArray.getJSONObject(j)
                    val sideA = BigDecimal(resObj.getString("sideA"))
                    val sideB = BigDecimal(resObj.getString("sideB"))
                    val sideC = BigDecimal(resObj.getString("sideC"))
                    val angleA = BigDecimal(resObj.getString("angleA"))
                    val angleB = BigDecimal(resObj.getString("angleB"))
                    val angleC = BigDecimal(resObj.getString("angleC"))
                    val area = BigDecimal(resObj.getString("area"))
                    val heightA = BigDecimal(resObj.getString("heightA"))
                    val heightB = BigDecimal(resObj.getString("heightB"))
                    val heightC = BigDecimal(resObj.getString("heightC"))
                    val perimeter = BigDecimal(resObj.getString("perimeter"))
                    val semiPerimeter = BigDecimal(resObj.getString("semiPerimeter"))
                    val sideClass = TriangleSideClassification.valueOf(resObj.getString("sideClass"))
                    val angleClass = TriangleAngleClassification.valueOf(resObj.getString("angleClass"))

                    val stepsList = mutableListOf<CalculationStep>()
                    if (resObj.has("steps")) {
                        val stepsArray = resObj.getJSONArray("steps")
                        for (k in 0 until stepsArray.length()) {
                            val stepObj = stepsArray.getJSONObject(k)
                            stepsList.add(
                                CalculationStep(
                                    label = stepObj.getString("label"),
                                    expression = stepObj.getString("expression"),
                                    result = stepObj.getString("result")
                                )
                            )
                        }
                    }

                    resultsList.add(
                        TriangleResult(
                            sideA = sideA,
                            sideB = sideB,
                            sideC = sideC,
                            angleA = angleA,
                            angleB = angleB,
                            angleC = angleC,
                            area = area,
                            heightA = heightA,
                            heightB = heightB,
                            heightC = heightC,
                            perimeter = perimeter,
                            semiPerimeter = semiPerimeter,
                            sideClassification = sideClass,
                            angleClassification = angleClass,
                            steps = stepsList
                        )
                    )
                }

                list.add(
                    TriangleHistory(
                        id = id,
                        solveType = solveType,
                        inputs = inputsMap,
                        inputUnits = inputUnitsMap,
                        resultUnit = resultUnit,
                        areaUnit = areaUnit,
                        results = resultsList,
                        savedAt = savedAt
                    )
                )
            }
            list
        } catch (e: Exception) {
            emptyList()
        }
    }

    private fun saveHistory(list: List<TriangleHistory>) {
        try {
            val jsonArray = JSONArray()
            for (item in list) {
                val obj = JSONObject()
                obj.put("id", item.id)
                obj.put("solveType", item.solveType.name)

                val inputsObj = JSONObject()
                item.inputs.forEach { (k, v) -> inputsObj.put(k, v) }
                obj.put("inputs", inputsObj)

                val inputUnitsObj = JSONObject()
                item.inputUnits.forEach { (k, v) -> inputUnitsObj.put(k, v.name) }
                obj.put("inputUnits", inputUnitsObj)

                obj.put("resultUnit", item.resultUnit.name)
                obj.put("areaUnit", item.areaUnit.name)
                obj.put("savedAt", item.savedAt)

                val resultsArray = JSONArray()
                for (res in item.results) {
                    val resObj = JSONObject()
                    resObj.put("sideA", res.sideA.toPlainString())
                    resObj.put("sideB", res.sideB.toPlainString())
                    resObj.put("sideC", res.sideC.toPlainString())
                    resObj.put("angleA", res.angleA.toPlainString())
                    resObj.put("angleB", res.angleB.toPlainString())
                    resObj.put("angleC", res.angleC.toPlainString())
                    resObj.put("area", res.area.toPlainString())
                    resObj.put("heightA", res.heightA.toPlainString())
                    resObj.put("heightB", res.heightB.toPlainString())
                    resObj.put("heightC", res.heightC.toPlainString())
                    resObj.put("perimeter", res.perimeter.toPlainString())
                    resObj.put("semiPerimeter", res.semiPerimeter.toPlainString())
                    resObj.put("sideClass", res.sideClassification.name)
                    resObj.put("angleClass", res.angleClassification.name)

                    val stepsArray = JSONArray()
                    for (step in res.steps) {
                        val stepObj = JSONObject()
                        stepObj.put("label", step.label)
                        stepObj.put("expression", step.expression)
                        stepObj.put("result", step.result)
                        stepsArray.put(stepObj)
                    }
                    resObj.put("steps", stepsArray)

                    resultsArray.put(resObj)
                }
                obj.put("results", resultsArray)

                jsonArray.put(obj)
            }
            prefs.edit().putString(KEY_HISTORY, jsonArray.toString()).apply()
        } catch (e: Exception) {
            // Log / ignore
        }
    }

    companion object {
        private const val KEY_HISTORY = "calculations_history"
    }
}
