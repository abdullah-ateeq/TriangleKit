package com.example.trianglekit.ui

import com.example.trianglekit.model.AreaUnit
import com.example.trianglekit.model.LengthUnit
import com.example.trianglekit.model.SolveType
import com.example.trianglekit.model.TriangleHistory
import com.example.trianglekit.model.TriangleSolveResult

data class TriangleKitUiState(
    val solveType: SolveType = SolveType.AUTO,
    val inputs: Map<String, String> = emptyMap(),
    val inputUnits: Map<String, LengthUnit> = emptyMap(),
    val lengthUnit: LengthUnit = LengthUnit.CENTIMETER,
    val areaUnit: AreaUnit = AreaUnit.SQUARE_CENTIMETER,
    val result: TriangleSolveResult? = null,
    val selectedSolutionIndex: Int = 0,
    val history: List<TriangleHistory> = emptyList(),
    val errorMessage: String? = null,
    val isCalculating: Boolean = false,
    val userNotification: String? = null
)
