package com.example.trianglekit.model

data class TriangleSolveResult(
    val solutions: List<TriangleResult>,
    val solveType: SolveType,
    val formulas: List<String> = emptyList(),
    val note: String? = null
)
