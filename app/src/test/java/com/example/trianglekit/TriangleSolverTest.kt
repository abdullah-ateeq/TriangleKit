package com.example.trianglekit

import com.example.trianglekit.domain.TriangleClassifier
import com.example.trianglekit.domain.TriangleSolver
import com.example.trianglekit.math.MathConstants
import com.example.trianglekit.model.RightTriangleInputs
import com.example.trianglekit.model.SideReference
import com.example.trianglekit.model.SolveType
import com.example.trianglekit.model.TriangleAngleClassification
import com.example.trianglekit.model.TriangleSideClassification
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.math.BigDecimal
import java.math.RoundingMode

class TriangleSolverTest {

    private val mc = MathConstants.DEFAULT_MATH_CONTEXT

    @Test
    fun testCriticalSSS() {
        // a = 3, b = 4, c = 5
        val result = TriangleSolver.solveSSS(
            BigDecimal("3"), BigDecimal("4"), BigDecimal("5"), mc
        )
        assertEquals(1, result.solutions.size)
        val sol = result.solutions.first()

        // Check angles: A ≈ 36.869898°, B ≈ 53.130102°, C = 90°
        val angleARounded = sol.angleA.setScale(6, RoundingMode.HALF_UP)
        val angleBRounded = sol.angleB.setScale(6, RoundingMode.HALF_UP)
        val angleCRounded = sol.angleC.setScale(6, RoundingMode.HALF_UP)

        assertEquals("36.869898", angleARounded.toPlainString())
        assertEquals("53.130102", angleBRounded.toPlainString())
        assertEquals("90.000000", angleCRounded.toPlainString())

        // Angle sum = 180
        val angleSum = sol.angleA.add(sol.angleB).add(sol.angleC)
        assertTrue(angleSum.subtract(BigDecimal("180")).abs().compareTo(MathConstants.ANGLE_TOLERANCE) <= 0)

        // Area = 6
        val areaRounded = sol.area.setScale(6, RoundingMode.HALF_UP)
        assertEquals("6.000000", areaRounded.toPlainString())

        // Heights: ha = 4, hb = 3, hc = 2.4
        assertEquals("4.000000", sol.heightA.setScale(6, RoundingMode.HALF_UP).toPlainString())
        assertEquals("3.000000", sol.heightB.setScale(6, RoundingMode.HALF_UP).toPlainString())
        assertEquals("2.400000", sol.heightC.setScale(6, RoundingMode.HALF_UP).toPlainString())

        // Perimeter = 12, Semi-perimeter = 6
        assertEquals("12.000000", sol.perimeter.setScale(6, RoundingMode.HALF_UP).toPlainString())
        assertEquals("6.000000", sol.semiPerimeter.setScale(6, RoundingMode.HALF_UP).toPlainString())

        // Classification
        assertEquals(TriangleSideClassification.SCALENE, sol.sideClassification)
        assertEquals(TriangleAngleClassification.RIGHT, sol.angleClassification)
    }

    @Test
    fun testCriticalEquilateral() {
        val result = TriangleSolver.solveSSS(
            BigDecimal("5"), BigDecimal("5"), BigDecimal("5"), mc
        )
        val sol = result.solutions.first()

        assertEquals("60.000000", sol.angleA.setScale(6, RoundingMode.HALF_UP).toPlainString())
        assertEquals("60.000000", sol.angleB.setScale(6, RoundingMode.HALF_UP).toPlainString())
        assertEquals("60.000000", sol.angleC.setScale(6, RoundingMode.HALF_UP).toPlainString())

        // Area: (sqrt(3)/4) * 25 ≈ 10.825317547...
        val areaStr = sol.area.setScale(6, RoundingMode.HALF_UP).toPlainString()
        assertEquals("10.825318", areaStr)

        assertEquals(TriangleSideClassification.EQUILATERAL, sol.sideClassification)
        assertEquals(TriangleAngleClassification.ACUTE, sol.angleClassification)
    }

    @Test
    fun testCriticalIsosceles() {
        val result = TriangleSolver.solveSSS(
            BigDecimal("5"), BigDecimal("5"), BigDecimal("6"), mc
        )
        val sol = result.solutions.first()

        assertEquals(TriangleSideClassification.ISOSCELES, sol.sideClassification)
        assertEquals(TriangleAngleClassification.ACUTE, sol.angleClassification)

        // Height to side c (6): sqrt(5^2 - 3^2) = 4
        assertEquals("4.000000", sol.heightC.setScale(6, RoundingMode.HALF_UP).toPlainString())
        // Area: 1/2 * 6 * 4 = 12
        assertEquals("12.000000", sol.area.setScale(6, RoundingMode.HALF_UP).toPlainString())
    }

    @Test
    fun testCriticalSAS() {
        // a = 5, b = 7, C = 60°
        // c^2 = 25 + 49 - 2*5*7*cos(60) = 74 - 35 = 39 -> c = sqrt(39) ≈ 6.244998
        val result = TriangleSolver.solveSAS(
            BigDecimal("5"), BigDecimal("7"), BigDecimal("60"), mc
        )
        val sol = result.solutions.first()

        val cRounded = sol.sideC.setScale(6, RoundingMode.HALF_UP)
        assertEquals("6.244998", cRounded.toPlainString())

        // Area: 1/2 * 5 * 7 * sin(60) = 35 * sqrt(3) / 4 ≈ 15.155445
        val areaRounded = sol.area.setScale(6, RoundingMode.HALF_UP)
        assertEquals("15.155445", areaRounded.toPlainString())

        // Angle sum = 180
        val sum = sol.angleA.add(sol.angleB).add(sol.angleC)
        assertTrue(sum.subtract(BigDecimal("180")).abs().compareTo(MathConstants.ANGLE_TOLERANCE) <= 0)
    }

    @Test
    fun testCriticalASA() {
        // A = 40°, B = 60°, c = 10 -> C = 80°
        val result = TriangleSolver.solveASA(
            BigDecimal("40"), BigDecimal("60"), BigDecimal("10"), mc
        )
        val sol = result.solutions.first()

        assertEquals("80.000000", sol.angleC.setScale(6, RoundingMode.HALF_UP).toPlainString())
        // Law of Sines: a = 10 * sin(40) / sin(80) ≈ 6.527036
        assertEquals("6.527036", sol.sideA.setScale(6, RoundingMode.HALF_UP).toPlainString())
        // b = 10 * sin(60) / sin(80) ≈ 8.793852
        assertEquals("8.793852", sol.sideB.setScale(6, RoundingMode.HALF_UP).toPlainString())
    }

    @Test
    fun testCriticalAAS() {
        // A = 30°, B = 80°, a = 5 -> C = 70°
        val result = TriangleSolver.solveAAS(
            BigDecimal("30"), BigDecimal("80"), BigDecimal("5"), SideReference.A, mc
        )
        val sol = result.solutions.first()

        assertEquals("70.000000", sol.angleC.setScale(6, RoundingMode.HALF_UP).toPlainString())
        // b = 5 * sin(80) / sin(30) = 10 * sin(80) ≈ 9.848078
        assertEquals("9.848078", sol.sideB.setScale(6, RoundingMode.HALF_UP).toPlainString())
        // c = 5 * sin(70) / sin(30) = 10 * sin(70) ≈ 9.396926
        assertEquals("9.396926", sol.sideC.setScale(6, RoundingMode.HALF_UP).toPlainString())
    }

    @Test
    fun testCriticalSSATwoSolutions() {
        // Ambiguous SSA: a = 10, b = 12, A = 30°
        // h = 12 * sin(30) = 6. Since h (6) < a (10) < b (12), exactly 2 solutions exist!
        val result = TriangleSolver.solveSSA(
            BigDecimal("10"), BigDecimal("12"), BigDecimal("30"), mc
        )
        assertEquals(2, result.solutions.size)

        val sol1 = result.solutions[0]
        val sol2 = result.solutions[1]

        // Solution 1: B is acute
        // sin(B) = 12 * sin(30) / 10 = 0.6 -> B ≈ 36.869898°
        assertEquals("36.869898", sol1.angleB.setScale(6, RoundingMode.HALF_UP).toPlainString())
        // Solution 2: B is obtuse
        // B = 180 - 36.869898 = 143.130102°
        assertEquals("143.130102", sol2.angleB.setScale(6, RoundingMode.HALF_UP).toPlainString())

        // Angle sum for both is 180
        val sum1 = sol1.angleA.add(sol1.angleB).add(sol1.angleC)
        val sum2 = sol2.angleA.add(sol2.angleB).add(sol2.angleC)
        assertTrue(sum1.subtract(BigDecimal("180")).abs().compareTo(MathConstants.ANGLE_TOLERANCE) <= 0)
        assertTrue(sum2.subtract(BigDecimal("180")).abs().compareTo(MathConstants.ANGLE_TOLERANCE) <= 0)
    }

    @Test
    fun testCriticalSSAOneSolution() {
        // a = 12, b = 10, A = 30°. Since a >= b, only 1 solution exists.
        val result = TriangleSolver.solveSSA(
            BigDecimal("12"), BigDecimal("10"), BigDecimal("30"), mc
        )
        assertEquals(1, result.solutions.size)
    }

    @Test
    fun testCriticalSSANoSolution() {
        // a = 4, b = 12, A = 30°. h = 12 * sin(30) = 6. Side a (4) < h (6), impossible!
        val result = TriangleSolver.solveSSA(
            BigDecimal("4"), BigDecimal("12"), BigDecimal("30"), mc
        )
        assertTrue(result.solutions.isEmpty())
        assertTrue(result.note?.contains("No valid triangle exists") == true)
    }

    @Test
    fun testCriticalRightTriangle() {
        // Leg a = 3, Leg b = 4 -> c = 5, C = 90°, Area = 6
        val result = TriangleSolver.solveRightTriangle(
            RightTriangleInputs(sideA = BigDecimal("3"), sideB = BigDecimal("4")), mc
        )
        val sol = result.solutions.first()

        assertEquals("5.000000", sol.sideC.setScale(6, RoundingMode.HALF_UP).toPlainString())
        assertEquals("90.000000", sol.angleC.setScale(6, RoundingMode.HALF_UP).toPlainString())
        assertEquals("6.000000", sol.area.setScale(6, RoundingMode.HALF_UP).toPlainString())
    }

    @Test
    fun testCriticalBaseHeightArea() {
        val area = TriangleSolver.calculateBaseHeightArea(BigDecimal("10"), BigDecimal("5"), mc)
        assertEquals(0, area.compareTo(BigDecimal("25")))
    }

    @Test
    fun testAutoSolveDetection() {
        // a, b, c -> SSS
        val sssRes = TriangleSolver.autoSolve(BigDecimal("3"), BigDecimal("4"), BigDecimal("5"), null, null, null, mc)
        assertEquals(SolveType.SSS, sssRes.solveType)

        // a, b, C -> SAS
        val sasRes = TriangleSolver.autoSolve(BigDecimal("5"), BigDecimal("7"), null, null, null, BigDecimal("60"), mc)
        assertEquals(SolveType.SAS, sasRes.solveType)

        // A, B, c -> ASA
        val asaRes = TriangleSolver.autoSolve(null, null, BigDecimal("10"), BigDecimal("40"), BigDecimal("60"), null, mc)
        assertEquals(SolveType.ASA, asaRes.solveType)

        // A, B, a -> AAS
        val aasRes = TriangleSolver.autoSolve(BigDecimal("5"), null, null, BigDecimal("30"), BigDecimal("80"), null, mc)
        assertEquals(SolveType.AAS, aasRes.solveType)

        // a, b, A -> SSA
        val ssaRes = TriangleSolver.autoSolve(BigDecimal("10"), BigDecimal("12"), null, BigDecimal("30"), null, null, mc)
        assertEquals(SolveType.SSA, ssaRes.solveType)
    }

    @Test(expected = IllegalArgumentException::class)
    fun testAutoSolveInsufficientInfo() {
        // Only 2 values -> throws not enough information
        TriangleSolver.autoSolve(BigDecimal("5"), BigDecimal("7"), null, null, null, null, mc)
    }

    @Test(expected = IllegalArgumentException::class)
    fun testAutoSolveInconsistentOverdefined() {
        // a = 3, b = 4, c = 5, but angle A = 90 (which is wrong, angle C is 90)
        TriangleSolver.autoSolve(
            BigDecimal("3"), BigDecimal("4"), BigDecimal("5"),
            BigDecimal("90"), BigDecimal("45"), BigDecimal("45"), mc
        )
    }
}
