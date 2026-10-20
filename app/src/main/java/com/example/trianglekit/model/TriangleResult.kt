package com.example.trianglekit.model

import java.math.BigDecimal

data class TriangleResult(
    val sideA: BigDecimal,
    val sideB: BigDecimal,
    val sideC: BigDecimal,
    val angleA: BigDecimal,
    val angleB: BigDecimal,
    val angleC: BigDecimal,
    val area: BigDecimal,
    val heightA: BigDecimal,
    val heightB: BigDecimal,
    val heightC: BigDecimal,
    val perimeter: BigDecimal,
    val semiPerimeter: BigDecimal,
    val sideClassification: TriangleSideClassification,
    val angleClassification: TriangleAngleClassification,
    val steps: List<CalculationStep> = emptyList()
)
