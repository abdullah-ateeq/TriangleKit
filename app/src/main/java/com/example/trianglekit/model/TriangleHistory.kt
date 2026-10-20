package com.example.trianglekit.model

data class TriangleHistory(
    val id: String,
    val solveType: SolveType,
    val inputs: Map<String, String>,
    val inputUnits: Map<String, LengthUnit>,
    val resultUnit: LengthUnit,
    val areaUnit: AreaUnit,
    val results: List<TriangleResult>,
    val savedAt: Long
)
