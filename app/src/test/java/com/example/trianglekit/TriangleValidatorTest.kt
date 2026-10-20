package com.example.trianglekit

import com.example.trianglekit.domain.TriangleValidator
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.math.BigDecimal

class TriangleValidatorTest {

    @Test
    fun testDecimalInput() {
        val res1 = TriangleValidator.parseNumber("100")
        assertTrue(res1.isSuccess)
        assertEquals(BigDecimal("100"), res1.getOrThrow())

        val res2 = TriangleValidator.parseNumber("100.5")
        assertTrue(res2.isSuccess)
        assertEquals(BigDecimal("100.5"), res2.getOrThrow())
    }

    @Test
    fun testLeadingAndTrailingDecimal() {
        val leading = TriangleValidator.parseNumber(".5")
        assertTrue(leading.isSuccess)
        assertEquals(BigDecimal("0.5"), leading.getOrThrow())

        val trailing = TriangleValidator.parseNumber("10.")
        assertTrue(trailing.isSuccess)
        assertEquals(BigDecimal("10"), trailing.getOrThrow())
    }

    @Test
    fun testScientificNotation() {
        val sci1 = TriangleValidator.parseNumber("1E+5")
        assertTrue(sci1.isSuccess)
        assertEquals(0, BigDecimal("100000").compareTo(sci1.getOrThrow()))

        val sci2 = TriangleValidator.parseNumber("1.5E-2")
        assertTrue(sci2.isSuccess)
        assertEquals(0, BigDecimal("0.015").compareTo(sci2.getOrThrow()))
    }

    @Test
    fun testVerySmallAndLargeNumbers() {
        val small = TriangleValidator.parseNumber("0.000001")
        assertTrue(small.isSuccess)
        assertEquals(0, BigDecimal("0.000001").compareTo(small.getOrThrow()))

        val largeStr = "999999999999999999999999"
        val large = TriangleValidator.parseNumber(largeStr)
        assertTrue(large.isSuccess)
        assertEquals(0, BigDecimal(largeStr).compareTo(large.getOrThrow()))
    }

    @Test
    fun testZeroAndNegativeInput() {
        val zeroRes = TriangleValidator.validatePositiveSide(BigDecimal.ZERO)
        assertTrue(zeroRes.isFailure)

        val negRes = TriangleValidator.validatePositiveSide(BigDecimal("-5"))
        assertTrue(negRes.isFailure)

        val heightZero = TriangleValidator.validatePositiveHeight(BigDecimal.ZERO)
        assertTrue(heightZero.isFailure)
    }

    @Test
    fun testInvalidNumericInput() {
        assertTrue(TriangleValidator.parseNumber("abc").isFailure)
        assertTrue(TriangleValidator.parseNumber("12..5").isFailure)
        assertTrue(TriangleValidator.parseNumber("--").isFailure)
        assertTrue(TriangleValidator.parseNumber("1.2.3").isFailure)
    }

    @Test
    fun testMaximumInputLength() {
        val tooLong = "1".repeat(501)
        val res = TriangleValidator.parseNumber(tooLong)
        assertTrue(res.isFailure)
        assertTrue(res.exceptionOrNull()?.message?.contains("Maximum 500 digits") == true)
    }

    @Test
    fun testTriangleInequality() {
        val valid = TriangleValidator.validateTriangleInequality(
            BigDecimal("3"), BigDecimal("4"), BigDecimal("5")
        )
        assertTrue(valid.isSuccess)
    }

    @Test
    fun testDegenerateTriangle() {
        // a = 3, b = 4, c = 7 -> degenerate triangle (3 + 4 = 7)
        val degenerate = TriangleValidator.validateTriangleInequality(
            BigDecimal("3"), BigDecimal("4"), BigDecimal("7")
        )
        assertTrue(degenerate.isFailure)
        assertEquals("These side lengths cannot form a valid triangle.", degenerate.exceptionOrNull()?.message)
    }

    @Test
    fun testInvalidTriangle() {
        // a = 3, b = 4, c = 8 -> impossible triangle
        val invalid = TriangleValidator.validateTriangleInequality(
            BigDecimal("3"), BigDecimal("4"), BigDecimal("8")
        )
        assertTrue(invalid.isFailure)
        assertEquals("These side lengths cannot form a valid triangle.", invalid.exceptionOrNull()?.message)
    }

    @Test
    fun testAngleValidation() {
        assertTrue(TriangleValidator.validateAngle(BigDecimal("60")).isSuccess)
        assertTrue(TriangleValidator.validateAngle(BigDecimal("0")).isFailure)
        assertTrue(TriangleValidator.validateAngle(BigDecimal("180")).isFailure)
        assertTrue(TriangleValidator.validateAngle(BigDecimal("200")).isFailure)
        assertTrue(TriangleValidator.validateAngle(BigDecimal("-30")).isFailure)
    }

    @Test
    fun testAngleSum() {
        val sumValid = TriangleValidator.validateAngleSum(
            BigDecimal("60"), BigDecimal("60"), BigDecimal("60")
        )
        assertTrue(sumValid.isSuccess)

        val sumInvalid = TriangleValidator.validateAngleSum(
            BigDecimal("60"), BigDecimal("60"), BigDecimal("70")
        )
        assertTrue(sumInvalid.isFailure)
    }

    @Test
    fun testOverdefinedConsistency() {
        // Side-angle relationship: largest side opposite largest angle
        val consistent = TriangleValidator.validateSideAngleOrder(
            BigDecimal("3"), BigDecimal("4"), BigDecimal("5"),
            BigDecimal("36.87"), BigDecimal("53.13"), BigDecimal("90")
        )
        assertTrue(consistent.isSuccess)

        // Contradictory order
        val inconsistent = TriangleValidator.validateSideAngleOrder(
            BigDecimal("3"), BigDecimal("4"), BigDecimal("5"),
            BigDecimal("90"), BigDecimal("53.13"), BigDecimal("36.87")
        )
        assertTrue(inconsistent.isFailure)
    }
}
