package com.example.trianglekit

import com.example.trianglekit.model.AreaUnit
import com.example.trianglekit.model.CalculationStep
import com.example.trianglekit.model.LengthUnit
import com.example.trianglekit.model.SolveType
import com.example.trianglekit.model.TriangleAngleClassification
import com.example.trianglekit.model.TriangleHistory
import com.example.trianglekit.model.TriangleResult
import com.example.trianglekit.model.TriangleSideClassification
import com.example.trianglekit.repository.AppSettings
import com.example.trianglekit.repository.AppTheme
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.math.BigDecimal
import java.math.RoundingMode
import java.util.UUID

class HistoryLogicTest {

    private fun createDummyResult(): TriangleResult {
        return TriangleResult(
            sideA = BigDecimal("3"),
            sideB = BigDecimal("4"),
            sideC = BigDecimal("5"),
            angleA = BigDecimal("36.87"),
            angleB = BigDecimal("53.13"),
            angleC = BigDecimal("90"),
            area = BigDecimal("6"),
            heightA = BigDecimal("4"),
            heightB = BigDecimal("3"),
            heightC = BigDecimal("2.4"),
            perimeter = BigDecimal("12"),
            semiPerimeter = BigDecimal("6"),
            sideClassification = TriangleSideClassification.SCALENE,
            angleClassification = TriangleAngleClassification.RIGHT
        )
    }

    @Test
    fun testHistorySaveAndLimit() {
        val historyList = mutableListOf<TriangleHistory>()
        val maxLimit = 10

        // Add 15 entries
        for (i in 1..15) {
            val item = TriangleHistory(
                id = UUID.randomUUID().toString(),
                solveType = SolveType.SSS,
                inputs = mapOf("a" to "$i", "b" to "4", "c" to "5"),
                inputUnits = mapOf("a" to LengthUnit.CENTIMETER),
                resultUnit = LengthUnit.CENTIMETER,
                areaUnit = AreaUnit.SQUARE_CENTIMETER,
                results = listOf(createDummyResult()),
                savedAt = System.currentTimeMillis() + i
            )
            historyList.add(0, item)
            while (historyList.size > maxLimit) {
                historyList.removeAt(historyList.size - 1)
            }
        }

        // Must cap at 10
        assertEquals(10, historyList.size)
        // Most recent should have inputs "a" to "15"
        assertEquals("15", historyList.first().inputs["a"])
        // Oldest retained should have inputs "a" to "6"
        assertEquals("6", historyList.last().inputs["a"])
    }

    @Test
    fun testDuplicateHistoryProtection() {
        val historyList = mutableListOf<TriangleHistory>()

        fun addIfDistinct(
            solveType: SolveType,
            inputs: Map<String, String>,
            inputUnits: Map<String, LengthUnit>,
            resultUnit: LengthUnit,
            areaUnit: AreaUnit
        ): Boolean {
            if (historyList.isNotEmpty()) {
                val latest = historyList.first()
                if (latest.solveType == solveType &&
                    latest.inputs == inputs &&
                    latest.inputUnits == inputUnits &&
                    latest.resultUnit == resultUnit &&
                    latest.areaUnit == areaUnit
                ) {
                    return false
                }
            }
            historyList.add(
                0,
                TriangleHistory(
                    id = UUID.randomUUID().toString(),
                    solveType = solveType,
                    inputs = inputs,
                    inputUnits = inputUnits,
                    resultUnit = resultUnit,
                    areaUnit = areaUnit,
                    results = listOf(createDummyResult()),
                    savedAt = System.currentTimeMillis()
                )
            )
            return true
        }

        val inputs = mapOf("a" to "3", "b" to "4", "c" to "5")
        val inputUnits = mapOf("a" to LengthUnit.CENTIMETER)

        // First save succeeds
        assertTrue(addIfDistinct(SolveType.SSS, inputs, inputUnits, LengthUnit.CENTIMETER, AreaUnit.SQUARE_CENTIMETER))
        assertEquals(1, historyList.size)

        // Consecutive duplicate save fails
        assertFalse(addIfDistinct(SolveType.SSS, inputs, inputUnits, LengthUnit.CENTIMETER, AreaUnit.SQUARE_CENTIMETER))
        assertEquals(1, historyList.size)

        // Modified inputs save succeeds
        val newInputs = mapOf("a" to "5", "b" to "5", "c" to "5")
        assertTrue(addIfDistinct(SolveType.SSS, newInputs, inputUnits, LengthUnit.CENTIMETER, AreaUnit.SQUARE_CENTIMETER))
        assertEquals(2, historyList.size)
    }

    @Test
    fun testHistoryDeletion() {
        val id1 = "id-1"
        val id2 = "id-2"
        val historyList = mutableListOf(
            TriangleHistory(
                id = id1,
                solveType = SolveType.SSS,
                inputs = mapOf("a" to "3"),
                inputUnits = emptyMap(),
                resultUnit = LengthUnit.CENTIMETER,
                areaUnit = AreaUnit.SQUARE_CENTIMETER,
                results = listOf(createDummyResult()),
                savedAt = 1000L
            ),
            TriangleHistory(
                id = id2,
                solveType = SolveType.SAS,
                inputs = mapOf("a" to "5"),
                inputUnits = emptyMap(),
                resultUnit = LengthUnit.CENTIMETER,
                areaUnit = AreaUnit.SQUARE_CENTIMETER,
                results = listOf(createDummyResult()),
                savedAt = 2000L
            )
        )

        // Delete item 1
        historyList.removeAll { it.id == id1 }
        assertEquals(1, historyList.size)
        assertEquals(id2, historyList.first().id)

        // Clear all
        historyList.clear()
        assertTrue(historyList.isEmpty())
    }

    @Test
    fun testSettingsDefaultsAndReset() {
        val defaultSettings = AppSettings()
        assertEquals(AppTheme.SYSTEM, defaultSettings.theme)
        assertTrue(defaultSettings.dynamicColor)
        assertTrue(defaultSettings.hapticFeedback)
        assertTrue(defaultSettings.animations)
        assertEquals(6, defaultSettings.decimalPlaces)
        assertEquals(RoundingMode.HALF_UP, defaultSettings.roundingMode)
        assertTrue(defaultSettings.showFormula)
        assertTrue(defaultSettings.showCalculationSteps)
        assertTrue(defaultSettings.showDiagram)
        assertTrue(defaultSettings.showClassification)
        assertTrue(defaultSettings.confirmDelete)

        // Mutated settings
        val modified = defaultSettings.copy(
            theme = AppTheme.DARK,
            decimalPlaces = 4,
            dynamicColor = false
        )
        assertEquals(AppTheme.DARK, modified.theme)
        assertEquals(4, modified.decimalPlaces)
        assertFalse(modified.dynamicColor)

        // Reset to default
        val reset = AppSettings()
        assertEquals(defaultSettings, reset)
    }
}
