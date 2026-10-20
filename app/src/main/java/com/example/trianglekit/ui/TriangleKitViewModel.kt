package com.example.trianglekit.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.trianglekit.domain.AreaUnitConverter
import com.example.trianglekit.domain.LengthUnitConverter
import com.example.trianglekit.domain.TriangleClassifier
import com.example.trianglekit.domain.TriangleFormulaProvider
import com.example.trianglekit.domain.TriangleSolver
import com.example.trianglekit.domain.TriangleValidator
import com.example.trianglekit.math.MathConstants
import com.example.trianglekit.model.AreaUnit
import com.example.trianglekit.model.CalculationStep
import com.example.trianglekit.model.LengthUnit
import com.example.trianglekit.model.RightTriangleInputs
import com.example.trianglekit.model.SideReference
import com.example.trianglekit.model.SolveType
import com.example.trianglekit.model.TriangleHistory
import com.example.trianglekit.model.TriangleResult
import com.example.trianglekit.model.TriangleSolveResult
import com.example.trianglekit.repository.AppSettings
import com.example.trianglekit.repository.HistoryRepository
import com.example.trianglekit.repository.SettingsRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.math.BigDecimal

class TriangleKitViewModel(application: Application) : AndroidViewModel(application) {

    private val settingsRepo = SettingsRepository(application)
    private val historyRepo = HistoryRepository(application)

    val settings: StateFlow<AppSettings> = settingsRepo.settings
    val history: StateFlow<List<TriangleHistory>> = historyRepo.history

    private val _uiState = MutableStateFlow(
        TriangleKitUiState(
            lengthUnit = settings.value.defaultLengthUnit,
            areaUnit = settings.value.defaultAreaUnit,
            inputUnits = defaultUnitsFor(SolveType.AUTO, settings.value.defaultLengthUnit)
        )
    )
    val uiState: StateFlow<TriangleKitUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            history.collect { histList ->
                _uiState.update { it.copy(history = histList) }
            }
        }
    }

    private fun defaultUnitsFor(type: SolveType, defaultUnit: LengthUnit): Map<String, LengthUnit> {
        return mapOf(
            "a" to defaultUnit,
            "b" to defaultUnit,
            "c" to defaultUnit,
            "base" to defaultUnit,
            "height" to defaultUnit
        )
    }

    fun setSolveType(type: SolveType) {
        _uiState.update { current ->
            current.copy(
                solveType = type,
                result = null,
                selectedSolutionIndex = 0,
                errorMessage = null,
                inputs = emptyMap(),
                inputUnits = defaultUnitsFor(type, current.lengthUnit)
            )
        }
    }

    fun updateInput(key: String, value: String) {
        _uiState.update { current ->
            val newInputs = current.inputs.toMutableMap()
            if (value.isBlank()) {
                newInputs.remove(key)
            } else {
                newInputs[key] = value
            }
            current.copy(inputs = newInputs, errorMessage = null)
        }
    }

    fun updateInputUnit(key: String, unit: LengthUnit) {
        _uiState.update { current ->
            val newUnits = current.inputUnits.toMutableMap()
            newUnits[key] = unit
            current.copy(inputUnits = newUnits)
        }
    }

    fun setResultLengthUnit(unit: LengthUnit) {
        _uiState.update { current ->
            val newState = current.copy(lengthUnit = unit)
            // If already calculated, re-solve or convert results
            if (current.result != null) {
                solveCurrent(newState)
            }
            newState
        }
    }

    fun setResultAreaUnit(unit: AreaUnit) {
        _uiState.update { current ->
            val newState = current.copy(areaUnit = unit)
            if (current.result != null) {
                solveCurrent(newState)
            }
            newState
        }
    }

    fun setSelectedSolutionIndex(index: Int) {
        _uiState.update { it.copy(selectedSolutionIndex = index) }
    }

    fun clearNotification() {
        _uiState.update { it.copy(userNotification = null) }
    }

    fun reset() {
        _uiState.update { current ->
            TriangleKitUiState(
                solveType = SolveType.AUTO,
                inputs = emptyMap(),
                inputUnits = defaultUnitsFor(SolveType.AUTO, current.lengthUnit),
                lengthUnit = current.lengthUnit,
                areaUnit = current.areaUnit,
                history = current.history
            )
        }
    }

    fun solve() {
        solveCurrent(_uiState.value)
    }

    private fun solveCurrent(state: TriangleKitUiState) {
        _uiState.update { it.copy(isCalculating = true, errorMessage = null) }

        viewModelScope.launch {
            try {
                val rawResult = withContext(Dispatchers.Default) {
                    performCalculation(state)
                }

                // Convert lengths and area from internal meters to display units
                val convertedSolutions = rawResult.solutions.map { sol ->
                    convertSolutionToDisplayUnits(sol, state.lengthUnit, state.areaUnit)
                }

                val convertedResult = rawResult.copy(solutions = convertedSolutions)

                _uiState.update {
                    it.copy(
                        result = convertedResult,
                        selectedSolutionIndex = 0,
                        isCalculating = false,
                        errorMessage = if (convertedResult.solutions.isEmpty()) {
                            convertedResult.note ?: "No valid triangle exists for these values."
                        } else null
                    )
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        result = null,
                        isCalculating = false,
                        errorMessage = e.message ?: "Calculation error."
                    )
                }
            }
        }
    }

    private fun performCalculation(state: TriangleKitUiState): TriangleSolveResult {
        val inputs = state.inputs
        val inputUnits = state.inputUnits
        val mc = MathConstants.DEFAULT_MATH_CONTEXT

        fun parseSideInMeters(key: String): BigDecimal? {
            val raw = inputs[key] ?: return null
            val value = TriangleValidator.parseNumber(raw).getOrThrow()
            TriangleValidator.validatePositiveSide(value, "Side $key").getOrThrow()
            val unit = inputUnits[key] ?: state.lengthUnit
            return LengthUnitConverter.toMeters(value, unit, mc)
        }

        fun parseAngle(key: String): BigDecimal? {
            val raw = inputs[key] ?: return null
            val value = TriangleValidator.parseNumber(raw).getOrThrow()
            TriangleValidator.validateAngle(value, "Angle $key").getOrThrow()
            return value
        }

        return when (state.solveType) {
            SolveType.SSS -> {
                val a = parseSideInMeters("a") ?: throw IllegalArgumentException("Enter Side a.")
                val b = parseSideInMeters("b") ?: throw IllegalArgumentException("Enter Side b.")
                val c = parseSideInMeters("c") ?: throw IllegalArgumentException("Enter Side c.")
                TriangleSolver.solveSSS(a, b, c, mc)
            }
            SolveType.SAS -> {
                val a = parseSideInMeters("a") ?: throw IllegalArgumentException("Enter Side a.")
                val b = parseSideInMeters("b") ?: throw IllegalArgumentException("Enter Side b.")
                val angleC = parseAngle("angleC") ?: throw IllegalArgumentException("Enter Angle C.")
                TriangleSolver.solveSAS(a, b, angleC, mc)
            }
            SolveType.ASA -> {
                val angleA = parseAngle("angleA") ?: throw IllegalArgumentException("Enter Angle A.")
                val angleB = parseAngle("angleB") ?: throw IllegalArgumentException("Enter Angle B.")
                val c = parseSideInMeters("c") ?: throw IllegalArgumentException("Enter Side c.")
                TriangleSolver.solveASA(angleA, angleB, c, mc)
            }
            SolveType.AAS -> {
                val angleA = parseAngle("angleA") ?: throw IllegalArgumentException("Enter Angle A.")
                val angleB = parseAngle("angleB") ?: throw IllegalArgumentException("Enter Angle B.")
                val a = parseSideInMeters("a")
                val b = parseSideInMeters("b")
                val c = parseSideInMeters("c")

                when {
                    a != null -> TriangleSolver.solveAAS(angleA, angleB, a, SideReference.A, mc)
                    b != null -> TriangleSolver.solveAAS(angleA, angleB, b, SideReference.B, mc)
                    c != null -> TriangleSolver.solveAAS(angleA, angleB, c, SideReference.C, mc)
                    else -> throw IllegalArgumentException("Enter one side length (a, b, or c).")
                }
            }
            SolveType.SSA -> {
                val a = parseSideInMeters("a") ?: throw IllegalArgumentException("Enter Side a.")
                val b = parseSideInMeters("b") ?: throw IllegalArgumentException("Enter Side b.")
                val angleA = parseAngle("angleA") ?: throw IllegalArgumentException("Enter Angle A.")
                TriangleSolver.solveSSA(a, b, angleA, mc)
            }
            SolveType.RIGHT_TRIANGLE -> {
                val a = parseSideInMeters("a")
                val b = parseSideInMeters("b")
                val c = parseSideInMeters("c")
                val angleA = parseAngle("angleA")
                val angleB = parseAngle("angleB")

                TriangleSolver.solveRightTriangle(
                    RightTriangleInputs(
                        sideA = a,
                        sideB = b,
                        hypotenuse = c,
                        angleA = angleA,
                        angleB = angleB
                    ),
                    mc
                )
            }
            SolveType.BASE_HEIGHT -> {
                val base = parseSideInMeters("base") ?: throw IllegalArgumentException("Enter Base.")
                val height = parseSideInMeters("height") ?: throw IllegalArgumentException("Enter Height.")
                val areaSqMeters = TriangleSolver.calculateBaseHeightArea(base, height, mc)

                val step = CalculationStep(
                    label = "Area",
                    expression = "Area = 1/2 · base · height = 1/2 · $base · $height",
                    result = areaSqMeters.toPlainString()
                )

                val res = TriangleResult(
                    sideA = base,
                    sideB = BigDecimal.ZERO,
                    sideC = BigDecimal.ZERO,
                    angleA = BigDecimal.ZERO,
                    angleB = BigDecimal.ZERO,
                    angleC = BigDecimal.ZERO,
                    area = areaSqMeters,
                    heightA = height,
                    heightB = BigDecimal.ZERO,
                    heightC = BigDecimal.ZERO,
                    perimeter = BigDecimal.ZERO,
                    semiPerimeter = BigDecimal.ZERO,
                    sideClassification = com.example.trianglekit.model.TriangleSideClassification.SCALENE,
                    angleClassification = com.example.trianglekit.model.TriangleAngleClassification.ACUTE,
                    steps = listOf(step)
                )

                TriangleSolveResult(
                    solutions = listOf(res),
                    solveType = SolveType.BASE_HEIGHT,
                    formulas = TriangleFormulaProvider.getFormulas(SolveType.BASE_HEIGHT),
                    note = "Base + height alone cannot determine all three sides and angles of an arbitrary triangle. Add two side lengths or an angle to solve the complete triangle."
                )
            }
            SolveType.AUTO -> {
                val a = parseSideInMeters("a")
                val b = parseSideInMeters("b")
                val c = parseSideInMeters("c")
                val angleA = parseAngle("angleA")
                val angleB = parseAngle("angleB")
                val angleC = parseAngle("angleC")

                TriangleSolver.autoSolve(a, b, c, angleA, angleB, angleC, mc)
            }
        }
    }

    private fun convertSolutionToDisplayUnits(
        sol: TriangleResult,
        targetLengthUnit: LengthUnit,
        targetAreaUnit: AreaUnit
    ): TriangleResult {
        val mc = MathConstants.DEFAULT_MATH_CONTEXT
        return sol.copy(
            sideA = LengthUnitConverter.fromMeters(sol.sideA, targetLengthUnit, mc),
            sideB = LengthUnitConverter.fromMeters(sol.sideB, targetLengthUnit, mc),
            sideC = LengthUnitConverter.fromMeters(sol.sideC, targetLengthUnit, mc),
            heightA = LengthUnitConverter.fromMeters(sol.heightA, targetLengthUnit, mc),
            heightB = LengthUnitConverter.fromMeters(sol.heightB, targetLengthUnit, mc),
            heightC = LengthUnitConverter.fromMeters(sol.heightC, targetLengthUnit, mc),
            perimeter = LengthUnitConverter.fromMeters(sol.perimeter, targetLengthUnit, mc),
            semiPerimeter = LengthUnitConverter.fromMeters(sol.semiPerimeter, targetLengthUnit, mc),
            area = AreaUnitConverter.fromSquareMeters(sol.area, targetAreaUnit, mc)
        )
    }

    fun saveCurrentCalculation(): Boolean {
        val currentState = _uiState.value
        val res = currentState.result ?: return false
        if (res.solutions.isEmpty()) return false

        val added = historyRepo.addHistory(
            solveType = res.solveType,
            inputs = currentState.inputs,
            inputUnits = currentState.inputUnits,
            resultUnit = currentState.lengthUnit,
            areaUnit = currentState.areaUnit,
            results = res.solutions
        )

        _uiState.update {
            it.copy(
                userNotification = if (added) "Calculation saved to history." else "Calculation already in history."
            )
        }
        return added
    }

    fun restoreHistoryItem(item: TriangleHistory) {
        _uiState.update { current ->
            current.copy(
                solveType = item.solveType,
                inputs = item.inputs,
                inputUnits = item.inputUnits,
                lengthUnit = item.resultUnit,
                areaUnit = item.areaUnit,
                result = TriangleSolveResult(
                    solutions = item.results,
                    solveType = item.solveType,
                    formulas = TriangleFormulaProvider.getFormulas(item.solveType)
                ),
                selectedSolutionIndex = 0,
                errorMessage = null
            )
        }
    }

    fun deleteHistoryItem(id: String) {
        historyRepo.deleteHistory(id)
    }

    fun clearAllHistory() {
        historyRepo.clearHistory()
        _uiState.update { it.copy(userNotification = "History cleared.") }
    }

    fun updateSettings(newSettings: AppSettings) {
        settingsRepo.updateSettings(newSettings)
    }

    fun resetSettings() {
        settingsRepo.resetToDefaults()
        _uiState.update { it.copy(userNotification = "Settings reset to defaults.") }
    }
}
