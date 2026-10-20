package com.example.trianglekit.domain

import com.example.trianglekit.model.SolveType

object TriangleFormulaProvider {

    fun getFormulas(solveType: SolveType): List<String> {
        return when (solveType) {
            SolveType.SSS -> listOf(
                "Semi-perimeter: s = (a + b + c) / 2",
                "Heron's Formula: Area = √(s(s - a)(s - b)(s - c))",
                "Law of Cosines: A = acos((b² + c² - a²) / (2bc))",
                "Law of Cosines: B = acos((a² + c² - b²) / (2ac))",
                "Angle Sum: C = 180° - A - B",
                "Altitudes: hₐ = 2Area / a, hᵦ = 2Area / b, h𝑐 = 2Area / c"
            )
            SolveType.SAS -> listOf(
                "Law of Cosines: c = √(a² + b² - 2ab cos(C))",
                "Area = 1/2 ab sin(C)",
                "Law of Cosines: A = acos((b² + c² - a²) / (2bc))",
                "Angle Sum: B = 180° - A - C",
                "Altitudes: hₐ = 2Area / a, hᵦ = 2Area / b, h𝑐 = 2Area / c"
            )
            SolveType.ASA -> listOf(
                "Angle Sum: C = 180° - A - B",
                "Law of Sines: a = c · sin(A) / sin(C)",
                "Law of Sines: b = c · sin(B) / sin(C)",
                "Area = 1/2 ab sin(C)",
                "Altitudes: hₐ = 2Area / a, hᵦ = 2Area / b, h𝑐 = 2Area / c"
            )
            SolveType.AAS -> listOf(
                "Angle Sum: Missing Angle = 180° - (Sum of known angles)",
                "Law of Sines: a / sin(A) = b / sin(B) = c / sin(C)",
                "Area = 1/2 ab sin(C)",
                "Altitudes: hₐ = 2Area / a, hᵦ = 2Area / b, h𝑐 = 2Area / c"
            )
            SolveType.SSA -> listOf(
                "Altitude: h = b · sin(A)",
                "Law of Sines: sin(B) = (b · sin(A)) / a",
                "Ambiguous Analysis: Compare a with h and b",
                "Angle B: B₁ = asin(sin(B)), B₂ = 180° - B₁",
                "Angle C: C = 180° - A - B",
                "Side c: c = a · sin(C) / sin(A)",
                "Area = 1/2 ab sin(C)"
            )
            SolveType.RIGHT_TRIANGLE -> listOf(
                "Pythagorean Theorem: a² + b² = c²",
                "Trigonometry: sin(A) = a / c, cos(A) = b / c",
                "Angle Sum: A + B = 90°, C = 90°",
                "Area = 1/2 · a · b",
                "Altitudes: hₐ = b, hᵦ = a, h𝑐 = (a · b) / c"
            )
            SolveType.BASE_HEIGHT -> listOf(
                "Area = 1/2 · base · height",
                "Note: Complete triangle requires two additional sides or angles."
            )
            SolveType.AUTO -> listOf(
                "Auto-detected standard triangle solving formula configuration."
            )
        }
    }
}
