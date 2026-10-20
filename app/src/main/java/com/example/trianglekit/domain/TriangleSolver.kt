package com.example.trianglekit.domain

import com.example.trianglekit.math.BigDecimalMath
import com.example.trianglekit.math.MathConstants
import com.example.trianglekit.model.CalculationStep
import com.example.trianglekit.model.RightTriangleInputs
import com.example.trianglekit.model.SideReference
import com.example.trianglekit.model.SolveType
import com.example.trianglekit.model.TriangleAngleClassification
import com.example.trianglekit.model.TriangleResult
import com.example.trianglekit.model.TriangleSideClassification
import com.example.trianglekit.model.TriangleSolveResult
import java.math.BigDecimal
import java.math.MathContext
import java.math.RoundingMode

object TriangleSolver {

    private val ZERO = MathConstants.ZERO
    private val HALF = MathConstants.HALF
    private val TWO = MathConstants.TWO
    private val ONE_EIGHTY = MathConstants.ONE_EIGHTY
    private val NINETY = MathConstants.NINETY
    private val TOLERANCE = MathConstants.TOLERANCE
    private val ANGLE_TOLERANCE = MathConstants.ANGLE_TOLERANCE

    /**
     * Solves SSS (Three Sides).
     */
    fun solveSSS(
        a: BigDecimal,
        b: BigDecimal,
        c: BigDecimal,
        mathContext: MathContext = MathConstants.DEFAULT_MATH_CONTEXT
    ): TriangleSolveResult {
        // Validation
        TriangleValidator.validatePositiveSide(a, "Side a").getOrThrow()
        TriangleValidator.validatePositiveSide(b, "Side b").getOrThrow()
        TriangleValidator.validatePositiveSide(c, "Side c").getOrThrow()
        TriangleValidator.validateTriangleInequality(a, b, c).getOrThrow()

        val steps = mutableListOf<CalculationStep>()

        // Semi-perimeter s = (a + b + c) / 2
        val perimeter = a.add(b, mathContext).add(c, mathContext)
        val s = perimeter.divide(TWO, mathContext)
        steps.add(
            CalculationStep(
                label = "Semi-perimeter",
                expression = "s = (a + b + c) / 2 = ($a + $b + $c) / 2",
                result = s.toPlainString()
            )
        )

        // Heron's formula: Area = sqrt(s(s-a)(s-b)(s-c))
        val sMinusA = s.subtract(a, mathContext)
        val sMinusB = s.subtract(b, mathContext)
        val sMinusC = s.subtract(c, mathContext)
        val radicand = s.multiply(sMinusA, mathContext)
            .multiply(sMinusB, mathContext)
            .multiply(sMinusC, mathContext)

        val area = BigDecimalMath.sqrt(radicand, mathContext)
        steps.add(
            CalculationStep(
                label = "Area (Heron's Formula)",
                expression = "Area = √(s(s - a)(s - b)(s - c))",
                result = area.toPlainString()
            )
        )

        // Angles via Law of Cosines:
        // cos(A) = (b^2 + c^2 - a^2) / (2bc)
        val b2 = b.multiply(b, mathContext)
        val c2 = c.multiply(c, mathContext)
        val a2 = a.multiply(a, mathContext)

        val cosA = b2.add(c2, mathContext).subtract(a2, mathContext)
            .divide(TWO.multiply(b, mathContext).multiply(c, mathContext), mathContext)
        val angleARad = BigDecimalMath.acos(cosA, mathContext)
        val angleA = BigDecimalMath.radiansToDegrees(angleARad, mathContext)
        steps.add(
            CalculationStep(
                label = "Angle A (Law of Cosines)",
                expression = "A = acos((b² + c² - a²) / (2bc))",
                result = "$angleA°"
            )
        )

        // cos(B) = (a^2 + c^2 - b^2) / (2ac)
        val cosB = a2.add(c2, mathContext).subtract(b2, mathContext)
            .divide(TWO.multiply(a, mathContext).multiply(c, mathContext), mathContext)
        val angleBRad = BigDecimalMath.acos(cosB, mathContext)
        val angleB = BigDecimalMath.radiansToDegrees(angleBRad, mathContext)
        steps.add(
            CalculationStep(
                label = "Angle B (Law of Cosines)",
                expression = "B = acos((a² + c² - b²) / (2ac))",
                result = "$angleB°"
            )
        )

        // C = 180 - A - B
        val angleC = ONE_EIGHTY.subtract(angleA, mathContext).subtract(angleB, mathContext)
        steps.add(
            CalculationStep(
                label = "Angle C (Angle Sum)",
                expression = "C = 180° - A - B",
                result = "$angleC°"
            )
        )

        val result = buildTriangleResult(a, b, c, angleA, angleB, angleC, area, steps, mathContext)
        return TriangleSolveResult(
            solutions = listOf(result),
            solveType = SolveType.SSS,
            formulas = TriangleFormulaProvider.getFormulas(SolveType.SSS)
        )
    }

    /**
     * Solves SAS (Two Sides + Included Angle).
     */
    fun solveSAS(
        a: BigDecimal,
        b: BigDecimal,
        angleC: BigDecimal,
        mathContext: MathContext = MathConstants.DEFAULT_MATH_CONTEXT
    ): TriangleSolveResult {
        TriangleValidator.validatePositiveSide(a, "Side a").getOrThrow()
        TriangleValidator.validatePositiveSide(b, "Side b").getOrThrow()
        TriangleValidator.validateAngle(angleC, "Angle C").getOrThrow()

        val steps = mutableListOf<CalculationStep>()

        // c^2 = a^2 + b^2 - 2ab cos(C)
        val cRad = BigDecimalMath.degreesToRadians(angleC, mathContext)
        val cosC = BigDecimalMath.cos(cRad, mathContext)
        val sinC = BigDecimalMath.sin(cRad, mathContext)

        val a2 = a.multiply(a, mathContext)
        val b2 = b.multiply(b, mathContext)
        val twoAbCosC = TWO.multiply(a, mathContext).multiply(b, mathContext).multiply(cosC, mathContext)
        val c2 = a2.add(b2, mathContext).subtract(twoAbCosC, mathContext)
        val c = BigDecimalMath.sqrt(c2, mathContext)

        steps.add(
            CalculationStep(
                label = "Side c (Law of Cosines)",
                expression = "c = √(a² + b² - 2ab cos(C))",
                result = c.toPlainString()
            )
        )

        // Area = 1/2 a b sin(C)
        val area = HALF.multiply(a, mathContext).multiply(b, mathContext).multiply(sinC, mathContext)
        steps.add(
            CalculationStep(
                label = "Area",
                expression = "Area = 1/2 · a · b · sin(C)",
                result = area.toPlainString()
            )
        )

        // Law of Cosines for A: cos(A) = (b^2 + c^2 - a^2) / (2bc)
        val cSquared = c.multiply(c, mathContext)
        val cosA = b2.add(cSquared, mathContext).subtract(a2, mathContext)
            .divide(TWO.multiply(b, mathContext).multiply(c, mathContext), mathContext)
        val angleARad = BigDecimalMath.acos(cosA, mathContext)
        val angleA = BigDecimalMath.radiansToDegrees(angleARad, mathContext)
        steps.add(
            CalculationStep(
                label = "Angle A (Law of Cosines)",
                expression = "A = acos((b² + c² - a²) / (2bc))",
                result = "$angleA°"
            )
        )

        val angleB = ONE_EIGHTY.subtract(angleA, mathContext).subtract(angleC, mathContext)
        steps.add(
            CalculationStep(
                label = "Angle B (Angle Sum)",
                expression = "B = 180° - A - C",
                result = "$angleB°"
            )
        )

        val result = buildTriangleResult(a, b, c, angleA, angleB, angleC, area, steps, mathContext)
        return TriangleSolveResult(
            solutions = listOf(result),
            solveType = SolveType.SAS,
            formulas = TriangleFormulaProvider.getFormulas(SolveType.SAS)
        )
    }

    /**
     * Solves ASA (Two Angles + Included Side).
     */
    fun solveASA(
        angleA: BigDecimal,
        angleB: BigDecimal,
        sideC: BigDecimal,
        mathContext: MathContext = MathConstants.DEFAULT_MATH_CONTEXT
    ): TriangleSolveResult {
        TriangleValidator.validateAngle(angleA, "Angle A").getOrThrow()
        TriangleValidator.validateAngle(angleB, "Angle B").getOrThrow()
        TriangleValidator.validateTwoAngles(angleA, angleB).getOrThrow()
        TriangleValidator.validatePositiveSide(sideC, "Side c").getOrThrow()

        val steps = mutableListOf<CalculationStep>()

        // C = 180 - A - B
        val angleC = ONE_EIGHTY.subtract(angleA, mathContext).subtract(angleB, mathContext)
        steps.add(
            CalculationStep(
                label = "Angle C (Angle Sum)",
                expression = "C = 180° - A - B",
                result = "$angleC°"
            )
        )

        val sinA = BigDecimalMath.sin(BigDecimalMath.degreesToRadians(angleA, mathContext), mathContext)
        val sinB = BigDecimalMath.sin(BigDecimalMath.degreesToRadians(angleB, mathContext), mathContext)
        val sinC = BigDecimalMath.sin(BigDecimalMath.degreesToRadians(angleC, mathContext), mathContext)

        // a = c * sin(A) / sin(C)
        val a = sideC.multiply(sinA, mathContext).divide(sinC, mathContext)
        steps.add(
            CalculationStep(
                label = "Side a (Law of Sines)",
                expression = "a = c · sin(A) / sin(C)",
                result = a.toPlainString()
            )
        )

        // b = c * sin(B) / sin(C)
        val b = sideC.multiply(sinB, mathContext).divide(sinC, mathContext)
        steps.add(
            CalculationStep(
                label = "Side b (Law of Sines)",
                expression = "b = c · sin(B) / sin(C)",
                result = b.toPlainString()
            )
        )

        val area = HALF.multiply(a, mathContext).multiply(b, mathContext).multiply(sinC, mathContext)
        steps.add(
            CalculationStep(
                label = "Area",
                expression = "Area = 1/2 · a · b · sin(C)",
                result = area.toPlainString()
            )
        )

        val result = buildTriangleResult(a, b, sideC, angleA, angleB, angleC, area, steps, mathContext)
        return TriangleSolveResult(
            solutions = listOf(result),
            solveType = SolveType.ASA,
            formulas = TriangleFormulaProvider.getFormulas(SolveType.ASA)
        )
    }

    /**
     * Solves AAS (Two Angles + One Side).
     */
    fun solveAAS(
        angleA: BigDecimal,
        angleB: BigDecimal,
        knownSide: BigDecimal,
        knownSideType: SideReference,
        mathContext: MathContext = MathConstants.DEFAULT_MATH_CONTEXT
    ): TriangleSolveResult {
        TriangleValidator.validateAngle(angleA, "Angle A").getOrThrow()
        TriangleValidator.validateAngle(angleB, "Angle B").getOrThrow()
        TriangleValidator.validateTwoAngles(angleA, angleB).getOrThrow()
        TriangleValidator.validatePositiveSide(knownSide, "Side $knownSideType").getOrThrow()

        val steps = mutableListOf<CalculationStep>()

        // C = 180 - A - B
        val angleC = ONE_EIGHTY.subtract(angleA, mathContext).subtract(angleB, mathContext)
        steps.add(
            CalculationStep(
                label = "Angle C (Angle Sum)",
                expression = "C = 180° - A - B",
                result = "$angleC°"
            )
        )

        val sinA = BigDecimalMath.sin(BigDecimalMath.degreesToRadians(angleA, mathContext), mathContext)
        val sinB = BigDecimalMath.sin(BigDecimalMath.degreesToRadians(angleB, mathContext), mathContext)
        val sinC = BigDecimalMath.sin(BigDecimalMath.degreesToRadians(angleC, mathContext), mathContext)

        val a: BigDecimal
        val b: BigDecimal
        val c: BigDecimal

        when (knownSideType) {
            SideReference.A -> {
                a = knownSide
                b = a.multiply(sinB, mathContext).divide(sinA, mathContext)
                c = a.multiply(sinC, mathContext).divide(sinA, mathContext)
                steps.add(
                    CalculationStep(
                        label = "Side b (Law of Sines)",
                        expression = "b = a · sin(B) / sin(A)",
                        result = b.toPlainString()
                    )
                )
                steps.add(
                    CalculationStep(
                        label = "Side c (Law of Sines)",
                        expression = "c = a · sin(C) / sin(A)",
                        result = c.toPlainString()
                    )
                )
            }
            SideReference.B -> {
                b = knownSide
                a = b.multiply(sinA, mathContext).divide(sinB, mathContext)
                c = b.multiply(sinC, mathContext).divide(sinB, mathContext)
                steps.add(
                    CalculationStep(
                        label = "Side a (Law of Sines)",
                        expression = "a = b · sin(A) / sin(B)",
                        result = a.toPlainString()
                    )
                )
                steps.add(
                    CalculationStep(
                        label = "Side c (Law of Sines)",
                        expression = "c = b · sin(C) / sin(B)",
                        result = c.toPlainString()
                    )
                )
            }
            SideReference.C -> {
                c = knownSide
                a = c.multiply(sinA, mathContext).divide(sinC, mathContext)
                b = c.multiply(sinB, mathContext).divide(sinC, mathContext)
                steps.add(
                    CalculationStep(
                        label = "Side a (Law of Sines)",
                        expression = "a = c · sin(A) / sin(C)",
                        result = a.toPlainString()
                    )
                )
                steps.add(
                    CalculationStep(
                        label = "Side b (Law of Sines)",
                        expression = "b = c · sin(B) / sin(C)",
                        result = b.toPlainString()
                    )
                )
            }
        }

        val area = HALF.multiply(a, mathContext).multiply(b, mathContext).multiply(sinC, mathContext)
        steps.add(
            CalculationStep(
                label = "Area",
                expression = "Area = 1/2 · a · b · sin(C)",
                result = area.toPlainString()
            )
        )

        val result = buildTriangleResult(a, b, c, angleA, angleB, angleC, area, steps, mathContext)
        return TriangleSolveResult(
            solutions = listOf(result),
            solveType = SolveType.AAS,
            formulas = TriangleFormulaProvider.getFormulas(SolveType.AAS)
        )
    }

    /**
     * Solves SSA (Two Sides + Non-Included Angle) — The Ambiguous Case.
     */
    fun solveSSA(
        sideA: BigDecimal,
        sideB: BigDecimal,
        angleA: BigDecimal,
        mathContext: MathContext = MathConstants.DEFAULT_MATH_CONTEXT
    ): TriangleSolveResult {
        TriangleValidator.validatePositiveSide(sideA, "Side a").getOrThrow()
        TriangleValidator.validatePositiveSide(sideB, "Side b").getOrThrow()
        TriangleValidator.validateAngle(angleA, "Angle A").getOrThrow()

        val formulas = TriangleFormulaProvider.getFormulas(SolveType.SSA)
        val sinA = BigDecimalMath.sin(BigDecimalMath.degreesToRadians(angleA, mathContext), mathContext)
        val h = sideB.multiply(sinA, mathContext)

        val isAcute = angleA.compareTo(NINETY) < 0

        // Case 1: Angle A is obtuse or right (>= 90°)
        if (!isAcute) {
            // Must have sideA > sideB
            if (sideA.compareTo(sideB) <= 0) {
                return TriangleSolveResult(
                    solutions = emptyList(),
                    solveType = SolveType.SSA,
                    formulas = formulas,
                    note = "No valid triangle exists for these values. When angle A ≥ 90°, side a must be strictly greater than side b."
                )
            }
            // Exactly 1 solution with acute angle B
            val sinB = sideB.multiply(sinA, mathContext).divide(sideA, mathContext)
            val angleBRad = BigDecimalMath.asin(sinB, mathContext)
            val angleB = BigDecimalMath.radiansToDegrees(angleBRad, mathContext)
            val angleC = ONE_EIGHTY.subtract(angleA, mathContext).subtract(angleB, mathContext)
            val sinC = BigDecimalMath.sin(BigDecimalMath.degreesToRadians(angleC, mathContext), mathContext)
            val sideC = sideA.multiply(sinC, mathContext).divide(sinA, mathContext)
            val area = HALF.multiply(sideA, mathContext).multiply(sideB, mathContext).multiply(sinC, mathContext)

            val steps = mutableListOf(
                CalculationStep("Altitude h", "h = b · sin(A)", h.toPlainString()),
                CalculationStep("Angle B (Law of Sines)", "sin(B) = (b · sin(A)) / a", "$angleB°"),
                CalculationStep("Angle C (Angle Sum)", "C = 180° - A - B", "$angleC°"),
                CalculationStep("Side c (Law of Sines)", "c = a · sin(C) / sin(A)", sideC.toPlainString()),
                CalculationStep("Area", "Area = 1/2 · a · b · sin(C)", area.toPlainString())
            )

            val sol = buildTriangleResult(sideA, sideB, sideC, angleA, angleB, angleC, area, steps, mathContext)
            return TriangleSolveResult(listOf(sol), SolveType.SSA, formulas)
        }

        // Case 2: Angle A is acute (< 90°)
        val diffWithH = sideA.subtract(h, mathContext)

        // If a < h (strictly less by tolerance): No solution
        if (diffWithH.compareTo(TOLERANCE.negate()) < 0) {
            return TriangleSolveResult(
                solutions = emptyList(),
                solveType = SolveType.SSA,
                formulas = formulas,
                note = "No valid triangle exists for these values. Side a ($sideA) is shorter than the minimum altitude h ($h)."
            )
        }

        // If a == h (within tolerance): Exactly 1 solution (Right Triangle at B)
        if (diffWithH.abs().compareTo(TOLERANCE) <= 0) {
            val angleB = NINETY
            val angleC = ONE_EIGHTY.subtract(angleA, mathContext).subtract(angleB, mathContext)
            val sinC = BigDecimalMath.sin(BigDecimalMath.degreesToRadians(angleC, mathContext), mathContext)
            val sideC = sideA.multiply(sinC, mathContext).divide(sinA, mathContext)
            val area = HALF.multiply(sideA, mathContext).multiply(sideB, mathContext).multiply(sinC, mathContext)

            val steps = mutableListOf(
                CalculationStep("Altitude h", "h = b · sin(A)", h.toPlainString()),
                CalculationStep("Angle B", "Since a = h, B = 90°", "90°"),
                CalculationStep("Angle C", "C = 180° - A - 90°", "$angleC°"),
                CalculationStep("Side c", "c = a · sin(C) / sin(A)", sideC.toPlainString()),
                CalculationStep("Area", "Area = 1/2 · a · b · sin(C)", area.toPlainString())
            )

            val sol = buildTriangleResult(sideA, sideB, sideC, angleA, angleB, angleC, area, steps, mathContext)
            return TriangleSolveResult(listOf(sol), SolveType.SSA, formulas)
        }

        // If a >= b: Exactly 1 solution (Angle B must be acute)
        if (sideA.compareTo(sideB) >= 0) {
            val sinB = sideB.multiply(sinA, mathContext).divide(sideA, mathContext)
            val angleBRad = BigDecimalMath.asin(sinB, mathContext)
            val angleB = BigDecimalMath.radiansToDegrees(angleBRad, mathContext)
            val angleC = ONE_EIGHTY.subtract(angleA, mathContext).subtract(angleB, mathContext)
            val sinC = BigDecimalMath.sin(BigDecimalMath.degreesToRadians(angleC, mathContext), mathContext)
            val sideC = sideA.multiply(sinC, mathContext).divide(sinA, mathContext)
            val area = HALF.multiply(sideA, mathContext).multiply(sideB, mathContext).multiply(sinC, mathContext)

            val steps = mutableListOf(
                CalculationStep("Altitude h", "h = b · sin(A)", h.toPlainString()),
                CalculationStep("Case Analysis", "Since a ≥ b, exactly one acute solution exists.", "1 Solution"),
                CalculationStep("Angle B", "B = asin((b · sin(A)) / a)", "$angleB°"),
                CalculationStep("Angle C", "C = 180° - A - B", "$angleC°"),
                CalculationStep("Side c", "c = a · sin(C) / sin(A)", sideC.toPlainString()),
                CalculationStep("Area", "Area = 1/2 · a · b · sin(C)", area.toPlainString())
            )

            val sol = buildTriangleResult(sideA, sideB, sideC, angleA, angleB, angleC, area, steps, mathContext)
            return TriangleSolveResult(listOf(sol), SolveType.SSA, formulas)
        }

        // Otherwise h < a < b: TWO SOLUTIONS!
        val sinB = sideB.multiply(sinA, mathContext).divide(sideA, mathContext)
        val angleB1Rad = BigDecimalMath.asin(sinB, mathContext)
        val angleB1 = BigDecimalMath.radiansToDegrees(angleB1Rad, mathContext)
        val angleB2 = ONE_EIGHTY.subtract(angleB1, mathContext)

        val solutions = mutableListOf<TriangleResult>()

        // Solution 1 (Acute B)
        val angleC1 = ONE_EIGHTY.subtract(angleA, mathContext).subtract(angleB1, mathContext)
        if (angleC1.compareTo(ZERO) > 0) {
            val sinC1 = BigDecimalMath.sin(BigDecimalMath.degreesToRadians(angleC1, mathContext), mathContext)
            val sideC1 = sideA.multiply(sinC1, mathContext).divide(sinA, mathContext)
            val area1 = HALF.multiply(sideA, mathContext).multiply(sideB, mathContext).multiply(sinC1, mathContext)
            val steps1 = mutableListOf(
                CalculationStep("Ambiguous Case", "h < a < b produces two valid triangles", "2 Solutions"),
                CalculationStep("Solution 1 Angle B", "B₁ = asin(sin(B))", "$angleB1°"),
                CalculationStep("Solution 1 Angle C", "C₁ = 180° - A - B₁", "$angleC1°"),
                CalculationStep("Solution 1 Side c", "c₁ = a · sin(C₁) / sin(A)", sideC1.toPlainString()),
                CalculationStep("Solution 1 Area", "Area₁ = 1/2 · a · b · sin(C₁)", area1.toPlainString())
            )
            solutions.add(buildTriangleResult(sideA, sideB, sideC1, angleA, angleB1, angleC1, area1, steps1, mathContext))
        }

        // Solution 2 (Obtuse B)
        val angleC2 = ONE_EIGHTY.subtract(angleA, mathContext).subtract(angleB2, mathContext)
        if (angleC2.compareTo(ZERO) > 0) {
            val sinC2 = BigDecimalMath.sin(BigDecimalMath.degreesToRadians(angleC2, mathContext), mathContext)
            val sideC2 = sideA.multiply(sinC2, mathContext).divide(sinA, mathContext)
            val area2 = HALF.multiply(sideA, mathContext).multiply(sideB, mathContext).multiply(sinC2, mathContext)
            val steps2 = mutableListOf(
                CalculationStep("Ambiguous Case", "h < a < b produces two valid triangles", "2 Solutions"),
                CalculationStep("Solution 2 Angle B", "B₂ = 180° - B₁", "$angleB2°"),
                CalculationStep("Solution 2 Angle C", "C₂ = 180° - A - B₂", "$angleC2°"),
                CalculationStep("Solution 2 Side c", "c₂ = a · sin(C₂) / sin(A)", sideC2.toPlainString()),
                CalculationStep("Solution 2 Area", "Area₂ = 1/2 · a · b · sin(C₂)", area2.toPlainString())
            )
            solutions.add(buildTriangleResult(sideA, sideB, sideC2, angleA, angleB2, angleC2, area2, steps2, mathContext))
        }

        return TriangleSolveResult(
            solutions = solutions,
            solveType = SolveType.SSA,
            formulas = formulas,
            note = if (solutions.size == 2) "Two possible triangles found (Ambiguous SSA case)." else null
        )
    }

    /**
     * Solves Right Triangle given supported combinations.
     */
    fun solveRightTriangle(
        knownValues: RightTriangleInputs,
        mathContext: MathContext = MathConstants.DEFAULT_MATH_CONTEXT
    ): TriangleSolveResult {
        val steps = mutableListOf<CalculationStep>()
        val angleC = NINETY

        val a: BigDecimal
        val b: BigDecimal
        val c: BigDecimal
        val angleA: BigDecimal
        val angleB: BigDecimal

        when {
            // Case 1: Leg a + Leg b
            knownValues.sideA != null && knownValues.sideB != null -> {
                a = knownValues.sideA
                b = knownValues.sideB
                TriangleValidator.validatePositiveSide(a, "Leg a").getOrThrow()
                TriangleValidator.validatePositiveSide(b, "Leg b").getOrThrow()

                val a2 = a.multiply(a, mathContext)
                val b2 = b.multiply(b, mathContext)
                c = BigDecimalMath.sqrt(a2.add(b2, mathContext), mathContext)
                steps.add(CalculationStep("Hypotenuse c (Pythagorean Theorem)", "c = √(a² + b²)", c.toPlainString()))

                val angleARad = BigDecimalMath.atan(a.divide(b, mathContext), mathContext)
                angleA = BigDecimalMath.radiansToDegrees(angleARad, mathContext)
                angleB = NINETY.subtract(angleA, mathContext)
                steps.add(CalculationStep("Angle A", "A = atan(a / b)", "$angleA°"))
                steps.add(CalculationStep("Angle B", "B = 90° - A", "$angleB°"))
            }

            // Case 2: Leg a + Hypotenuse c
            knownValues.sideA != null && knownValues.hypotenuse != null -> {
                a = knownValues.sideA
                c = knownValues.hypotenuse
                TriangleValidator.validatePositiveSide(a, "Leg a").getOrThrow()
                TriangleValidator.validatePositiveSide(c, "Hypotenuse c").getOrThrow()
                if (c.compareTo(a) <= 0) {
                    throw IllegalArgumentException("Hypotenuse c must be strictly greater than leg a.")
                }

                val c2 = c.multiply(c, mathContext)
                val a2 = a.multiply(a, mathContext)
                b = BigDecimalMath.sqrt(c2.subtract(a2, mathContext), mathContext)
                steps.add(CalculationStep("Leg b (Pythagorean Theorem)", "b = √(c² - a²)", b.toPlainString()))

                val angleARad = BigDecimalMath.asin(a.divide(c, mathContext), mathContext)
                angleA = BigDecimalMath.radiansToDegrees(angleARad, mathContext)
                angleB = NINETY.subtract(angleA, mathContext)
                steps.add(CalculationStep("Angle A", "A = asin(a / c)", "$angleA°"))
                steps.add(CalculationStep("Angle B", "B = 90° - A", "$angleB°"))
            }

            // Case 3: Leg b + Hypotenuse c
            knownValues.sideB != null && knownValues.hypotenuse != null -> {
                b = knownValues.sideB
                c = knownValues.hypotenuse
                TriangleValidator.validatePositiveSide(b, "Leg b").getOrThrow()
                TriangleValidator.validatePositiveSide(c, "Hypotenuse c").getOrThrow()
                if (c.compareTo(b) <= 0) {
                    throw IllegalArgumentException("Hypotenuse c must be strictly greater than leg b.")
                }

                val c2 = c.multiply(c, mathContext)
                val b2 = b.multiply(b, mathContext)
                a = BigDecimalMath.sqrt(c2.subtract(b2, mathContext), mathContext)
                steps.add(CalculationStep("Leg a (Pythagorean Theorem)", "a = √(c² - b²)", a.toPlainString()))

                val angleBRad = BigDecimalMath.asin(b.divide(c, mathContext), mathContext)
                angleB = BigDecimalMath.radiansToDegrees(angleBRad, mathContext)
                angleA = NINETY.subtract(angleB, mathContext)
                steps.add(CalculationStep("Angle B", "B = asin(b / c)", "$angleB°"))
                steps.add(CalculationStep("Angle A", "A = 90° - B", "$angleA°"))
            }

            // Case 4: Hypotenuse c + Angle A
            knownValues.hypotenuse != null && knownValues.angleA != null -> {
                c = knownValues.hypotenuse
                angleA = knownValues.angleA
                TriangleValidator.validatePositiveSide(c, "Hypotenuse c").getOrThrow()
                if (angleA.compareTo(ZERO) <= 0 || angleA.compareTo(NINETY) >= 0) {
                    throw IllegalArgumentException("Acute angle A must be strictly between 0° and 90°.")
                }
                angleB = NINETY.subtract(angleA, mathContext)

                val aRad = BigDecimalMath.degreesToRadians(angleA, mathContext)
                a = c.multiply(BigDecimalMath.sin(aRad, mathContext), mathContext)
                b = c.multiply(BigDecimalMath.cos(aRad, mathContext), mathContext)
                steps.add(CalculationStep("Angle B", "B = 90° - A", "$angleB°"))
                steps.add(CalculationStep("Leg a", "a = c · sin(A)", a.toPlainString()))
                steps.add(CalculationStep("Leg b", "b = c · cos(A)", b.toPlainString()))
            }

            // Case 5: Hypotenuse c + Angle B
            knownValues.hypotenuse != null && knownValues.angleB != null -> {
                c = knownValues.hypotenuse
                angleB = knownValues.angleB
                TriangleValidator.validatePositiveSide(c, "Hypotenuse c").getOrThrow()
                if (angleB.compareTo(ZERO) <= 0 || angleB.compareTo(NINETY) >= 0) {
                    throw IllegalArgumentException("Acute angle B must be strictly between 0° and 90°.")
                }
                angleA = NINETY.subtract(angleB, mathContext)

                val bRad = BigDecimalMath.degreesToRadians(angleB, mathContext)
                b = c.multiply(BigDecimalMath.sin(bRad, mathContext), mathContext)
                a = c.multiply(BigDecimalMath.cos(bRad, mathContext), mathContext)
                steps.add(CalculationStep("Angle A", "A = 90° - B", "$angleA°"))
                steps.add(CalculationStep("Leg a", "a = c · cos(B)", a.toPlainString()))
                steps.add(CalculationStep("Leg b", "b = c · sin(B)", b.toPlainString()))
            }

            // Case 6: Leg a + Angle A
            knownValues.sideA != null && knownValues.angleA != null -> {
                a = knownValues.sideA
                angleA = knownValues.angleA
                TriangleValidator.validatePositiveSide(a, "Leg a").getOrThrow()
                if (angleA.compareTo(ZERO) <= 0 || angleA.compareTo(NINETY) >= 0) {
                    throw IllegalArgumentException("Acute angle A must be strictly between 0° and 90°.")
                }
                angleB = NINETY.subtract(angleA, mathContext)

                val aRad = BigDecimalMath.degreesToRadians(angleA, mathContext)
                val sinA = BigDecimalMath.sin(aRad, mathContext)
                c = a.divide(sinA, mathContext)
                val c2 = c.multiply(c, mathContext)
                val a2 = a.multiply(a, mathContext)
                b = BigDecimalMath.sqrt(c2.subtract(a2, mathContext), mathContext)
                steps.add(CalculationStep("Angle B", "B = 90° - A", "$angleB°"))
                steps.add(CalculationStep("Hypotenuse c", "c = a / sin(A)", c.toPlainString()))
                steps.add(CalculationStep("Leg b", "b = √(c² - a²)", b.toPlainString()))
            }

            // Case 7: Leg a + Angle B
            knownValues.sideA != null && knownValues.angleB != null -> {
                a = knownValues.sideA
                angleB = knownValues.angleB
                TriangleValidator.validatePositiveSide(a, "Leg a").getOrThrow()
                if (angleB.compareTo(ZERO) <= 0 || angleB.compareTo(NINETY) >= 0) {
                    throw IllegalArgumentException("Acute angle B must be strictly between 0° and 90°.")
                }
                angleA = NINETY.subtract(angleB, mathContext)

                val bRad = BigDecimalMath.degreesToRadians(angleB, mathContext)
                val cosB = BigDecimalMath.cos(bRad, mathContext)
                c = a.divide(cosB, mathContext)
                val sinB = BigDecimalMath.sin(bRad, mathContext)
                b = c.multiply(sinB, mathContext)
                steps.add(CalculationStep("Angle A", "A = 90° - B", "$angleA°"))
                steps.add(CalculationStep("Hypotenuse c", "c = a / cos(B)", c.toPlainString()))
                steps.add(CalculationStep("Leg b", "b = c · sin(B)", b.toPlainString()))
            }

            // Case 8: Leg b + Angle B
            knownValues.sideB != null && knownValues.angleB != null -> {
                b = knownValues.sideB
                angleB = knownValues.angleB
                TriangleValidator.validatePositiveSide(b, "Leg b").getOrThrow()
                if (angleB.compareTo(ZERO) <= 0 || angleB.compareTo(NINETY) >= 0) {
                    throw IllegalArgumentException("Acute angle B must be strictly between 0° and 90°.")
                }
                angleA = NINETY.subtract(angleB, mathContext)

                val bRad = BigDecimalMath.degreesToRadians(angleB, mathContext)
                val sinB = BigDecimalMath.sin(bRad, mathContext)
                c = b.divide(sinB, mathContext)
                val c2 = c.multiply(c, mathContext)
                val b2 = b.multiply(b, mathContext)
                a = BigDecimalMath.sqrt(c2.subtract(b2, mathContext), mathContext)
                steps.add(CalculationStep("Angle A", "A = 90° - B", "$angleA°"))
                steps.add(CalculationStep("Hypotenuse c", "c = b / sin(B)", c.toPlainString()))
                steps.add(CalculationStep("Leg a", "a = √(c² - b²)", a.toPlainString()))
            }

            // Case 9: Leg b + Angle A
            knownValues.sideB != null && knownValues.angleA != null -> {
                b = knownValues.sideB
                angleA = knownValues.angleA
                TriangleValidator.validatePositiveSide(b, "Leg b").getOrThrow()
                if (angleA.compareTo(ZERO) <= 0 || angleA.compareTo(NINETY) >= 0) {
                    throw IllegalArgumentException("Acute angle A must be strictly between 0° and 90°.")
                }
                angleB = NINETY.subtract(angleA, mathContext)

                val aRad = BigDecimalMath.degreesToRadians(angleA, mathContext)
                val cosA = BigDecimalMath.cos(aRad, mathContext)
                c = b.divide(cosA, mathContext)
                val sinA = BigDecimalMath.sin(aRad, mathContext)
                a = c.multiply(sinA, mathContext)
                steps.add(CalculationStep("Angle B", "B = 90° - A", "$angleB°"))
                steps.add(CalculationStep("Hypotenuse c", "c = b / cos(A)", c.toPlainString()))
                steps.add(CalculationStep("Leg a", "a = c · sin(A)", a.toPlainString()))
            }

            else -> {
                throw IllegalArgumentException("Provide two known values for the right triangle (e.g., two legs, leg + hypotenuse, or side + angle).")
            }
        }

        val area = HALF.multiply(a, mathContext).multiply(b, mathContext)
        steps.add(CalculationStep("Area", "Area = 1/2 · a · b", area.toPlainString()))

        val result = buildTriangleResult(a, b, c, angleA, angleB, angleC, area, steps, mathContext)
        return TriangleSolveResult(
            solutions = listOf(result),
            solveType = SolveType.RIGHT_TRIANGLE,
            formulas = TriangleFormulaProvider.getFormulas(SolveType.RIGHT_TRIANGLE)
        )
    }

    /**
     * Calculates Area = 1/2 * base * height.
     */
    fun calculateBaseHeightArea(
        base: BigDecimal,
        height: BigDecimal,
        mathContext: MathContext = MathConstants.DEFAULT_MATH_CONTEXT
    ): BigDecimal {
        TriangleValidator.validatePositiveSide(base, "Base").getOrThrow()
        TriangleValidator.validatePositiveHeight(height).getOrThrow()
        return HALF.multiply(base, mathContext).multiply(height, mathContext)
    }

    /**
     * Auto Solve: parses user inputs for a, b, c, A, B, C and detects the appropriate solve mode.
     */
    fun autoSolve(
        a: BigDecimal?,
        b: BigDecimal?,
        c: BigDecimal?,
        angleA: BigDecimal?,
        angleB: BigDecimal?,
        angleC: BigDecimal?,
        mathContext: MathContext = MathConstants.DEFAULT_MATH_CONTEXT
    ): TriangleSolveResult {
        val sides = listOfNotNull(a, b, c)
        val angles = listOfNotNull(angleA, angleB, angleC)

        if (sides.isEmpty() && angles.isEmpty()) {
            throw IllegalArgumentException("Enter known triangle sides or angles to solve.")
        }

        if (sides.size + angles.size < 3) {
            throw IllegalArgumentException("Not enough information to solve the triangle. Add another side or a compatible angle.")
        }

        if (sides.isEmpty() && angles.size == 3) {
            throw IllegalArgumentException("Not enough information: An infinite number of triangles have these angles. Enter at least one side length.")
        }

        // Case 1: SSS (3 sides)
        if (a != null && b != null && c != null) {
            val res = solveSSS(a, b, c, mathContext)
            // Consistency check if angles were also provided
            if (angleA != null || angleB != null || angleC != null) {
                val sol = res.solutions.first()
                if (angleA != null && sol.angleA.subtract(angleA).abs().compareTo(ANGLE_TOLERANCE) > 0) {
                    throw IllegalArgumentException("The entered values do not describe the same triangle.")
                }
                if (angleB != null && sol.angleB.subtract(angleB).abs().compareTo(ANGLE_TOLERANCE) > 0) {
                    throw IllegalArgumentException("The entered values do not describe the same triangle.")
                }
                if (angleC != null && sol.angleC.subtract(angleC).abs().compareTo(ANGLE_TOLERANCE) > 0) {
                    throw IllegalArgumentException("The entered values do not describe the same triangle.")
                }
            }
            return res
        }

        // Case 2: SAS (2 sides + included angle)
        if (a != null && b != null && angleC != null) return solveSAS(a, b, angleC, mathContext)
        if (b != null && c != null && angleA != null) {
            // Remap (b, c, A) -> solveSAS where sides are b and c with included angle A
            val sasRes = solveSAS(b, c, angleA, mathContext)
            // Note: SAS returns (a_param, b_param, c_result) where third side is opposite the angle.
            // Here the third side is side a!
            val raw = sasRes.solutions.first()
            val remapped = raw.copy(
                sideA = raw.sideC,
                sideB = raw.sideA,
                sideC = raw.sideB,
                angleA = raw.angleC,
                angleB = raw.angleA,
                angleC = raw.angleB
            )
            return sasRes.copy(solutions = listOf(remapped))
        }
        if (a != null && c != null && angleB != null) {
            val sasRes = solveSAS(a, c, angleB, mathContext)
            val raw = sasRes.solutions.first()
            val remapped = raw.copy(
                sideA = raw.sideA,
                sideB = raw.sideC,
                sideC = raw.sideB,
                angleA = raw.angleA,
                angleB = raw.angleC,
                angleC = raw.angleB
            )
            return sasRes.copy(solutions = listOf(remapped))
        }

        // Case 3: ASA (2 angles + included side)
        if (angleA != null && angleB != null && c != null) return solveASA(angleA, angleB, c, mathContext)
        if (angleB != null && angleC != null && a != null) {
            val asaRes = solveASA(angleB, angleC, a, mathContext)
            val raw = asaRes.solutions.first()
            val remapped = raw.copy(
                sideA = raw.sideC,
                sideB = raw.sideA,
                sideC = raw.sideB,
                angleA = raw.angleC,
                angleB = raw.angleA,
                angleC = raw.angleB
            )
            return asaRes.copy(solutions = listOf(remapped))
        }
        if (angleA != null && angleC != null && b != null) {
            val asaRes = solveASA(angleA, angleC, b, mathContext)
            val raw = asaRes.solutions.first()
            val remapped = raw.copy(
                sideA = raw.sideA,
                sideB = raw.sideC,
                sideC = raw.sideB,
                angleA = raw.angleA,
                angleB = raw.angleC,
                angleC = raw.angleB
            )
            return asaRes.copy(solutions = listOf(remapped))
        }

        // Case 4: AAS (2 angles + non-included side)
        if (angleA != null && angleB != null) {
            if (a != null) return solveAAS(angleA, angleB, a, SideReference.A, mathContext)
            if (b != null) return solveAAS(angleA, angleB, b, SideReference.B, mathContext)
        }
        if (angleA != null && angleC != null) {
            if (a != null) return solveAAS(angleA, angleC, a, SideReference.A, mathContext)
            if (c != null) return solveAAS(angleA, angleC, c, SideReference.C, mathContext)
        }
        if (angleB != null && angleC != null) {
            if (b != null) return solveAAS(angleB, angleC, b, SideReference.B, mathContext)
            if (c != null) return solveAAS(angleB, angleC, c, SideReference.C, mathContext)
        }

        // Case 5: SSA (2 sides + non-included angle)
        if (a != null && b != null && angleA != null) return solveSSA(a, b, angleA, mathContext)
        if (a != null && b != null && angleB != null) {
            val ssaRes = solveSSA(b, a, angleB, mathContext)
            val swapped = ssaRes.solutions.map {
                it.copy(
                    sideA = it.sideB,
                    sideB = it.sideA,
                    angleA = it.angleB,
                    angleB = it.angleA
                )
            }
            return ssaRes.copy(solutions = swapped)
        }
        if (a != null && c != null && angleA != null) {
            val ssaRes = solveSSA(a, c, angleA, mathContext)
            val swapped = ssaRes.solutions.map {
                it.copy(
                    sideB = it.sideC,
                    sideC = it.sideB,
                    angleB = it.angleC,
                    angleC = it.angleB
                )
            }
            return ssaRes.copy(solutions = swapped)
        }
        if (a != null && c != null && angleC != null) {
            val ssaRes = solveSSA(c, a, angleC, mathContext)
            val swapped = ssaRes.solutions.map {
                it.copy(
                    sideA = it.sideB,
                    sideC = it.sideA,
                    angleA = it.angleB,
                    angleC = it.angleA
                )
            }
            return ssaRes.copy(solutions = swapped)
        }
        if (b != null && c != null && angleB != null) {
            val ssaRes = solveSSA(b, c, angleB, mathContext)
            val swapped = ssaRes.solutions.map {
                it.copy(
                    sideA = it.sideC,
                    sideC = it.sideB,
                    angleA = it.angleC,
                    angleC = it.angleB
                )
            }
            return ssaRes.copy(solutions = swapped)
        }
        if (b != null && c != null && angleC != null) {
            val ssaRes = solveSSA(c, b, angleC, mathContext)
            val swapped = ssaRes.solutions.map {
                it.copy(
                    sideB = it.sideC,
                    sideC = it.sideA,
                    angleB = it.angleC,
                    angleC = it.angleA
                )
            }
            return ssaRes.copy(solutions = swapped)
        }

        throw IllegalArgumentException("The entered values cannot uniquely solve the triangle. Add another side or a compatible angle.")
    }

    private fun buildTriangleResult(
        a: BigDecimal,
        b: BigDecimal,
        c: BigDecimal,
        angleA: BigDecimal,
        angleB: BigDecimal,
        angleC: BigDecimal,
        area: BigDecimal,
        steps: List<CalculationStep>,
        mathContext: MathContext
    ): TriangleResult {
        // Enforce Angle Sum normalization for tiny floating rounding deviations
        // e.g. 59.999999999999999° -> 60°
        var normAngleA = angleA
        var normAngleB = angleB
        var normAngleC = angleC

        // Check if any is 90 within tolerance
        if (normAngleA.subtract(NINETY).abs().compareTo(ANGLE_TOLERANCE) <= 0) normAngleA = NINETY
        if (normAngleB.subtract(NINETY).abs().compareTo(ANGLE_TOLERANCE) <= 0) normAngleB = NINETY
        if (normAngleC.subtract(NINETY).abs().compareTo(ANGLE_TOLERANCE) <= 0) normAngleC = NINETY

        val perimeter = a.add(b, mathContext).add(c, mathContext)
        val semiPerimeter = perimeter.divide(TWO, mathContext)

        // Altitudes: ha = 2A / a, hb = 2A / b, hc = 2A / c
        val twoArea = TWO.multiply(area, mathContext)
        val heightA = twoArea.divide(a, mathContext)
        val heightB = twoArea.divide(b, mathContext)
        val heightC = twoArea.divide(c, mathContext)

        val sideClass = TriangleClassifier.classifyBySides(a, b, c)
        val angleClass = TriangleClassifier.classifyByAngles(normAngleA, normAngleB, normAngleC)

        return TriangleResult(
            sideA = a,
            sideB = b,
            sideC = c,
            angleA = normAngleA,
            angleB = normAngleB,
            angleC = normAngleC,
            area = area,
            heightA = heightA,
            heightB = heightB,
            heightC = heightC,
            perimeter = perimeter,
            semiPerimeter = semiPerimeter,
            sideClassification = sideClass,
            angleClassification = angleClass,
            steps = steps
        )
    }
}
