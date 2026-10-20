package com.example.trianglekit

import com.example.trianglekit.math.BigDecimalMath
import com.example.trianglekit.math.MathConstants
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.math.BigDecimal
import java.math.MathContext
import java.math.RoundingMode

class BigDecimalMathTest {

    private val mc = MathConstants.DEFAULT_MATH_CONTEXT

    @Test
    fun testHighPrecisionPi() {
        val piStr = MathConstants.PI.toPlainString()
        assertTrue("Pi must have at least 50 digits", piStr.length >= 50)
        assertTrue(piStr.startsWith("3.1415926535897932384626433832795028841971693993751"))
    }

    @Test
    fun testBigDecimalSqrt() {
        val sqrt4 = BigDecimalMath.sqrt(BigDecimal("4"), mc)
        assertEquals(0, sqrt4.compareTo(BigDecimal("2")))

        val sqrt0 = BigDecimalMath.sqrt(BigDecimal.ZERO, mc)
        assertEquals(0, sqrt0.compareTo(BigDecimal.ZERO))

        val sqrt1 = BigDecimalMath.sqrt(BigDecimal.ONE, mc)
        assertEquals(0, sqrt1.compareTo(BigDecimal.ONE))

        val sqrt9 = BigDecimalMath.sqrt(BigDecimal("9"), mc)
        assertEquals(0, sqrt9.compareTo(BigDecimal("3")))

        val sqrt2 = BigDecimalMath.sqrt(BigDecimal("2"), mc)
        val sqrt2Squared = sqrt2.multiply(sqrt2, mc)
        val diff = sqrt2Squared.subtract(BigDecimal("2")).abs()
        assertTrue("sqrt(2)^2 should equal 2 within high precision", diff.compareTo(BigDecimal("1E-50")) < 0)
    }

    @Test
    fun testDegreesRadiansConversion() {
        val rad180 = BigDecimalMath.degreesToRadians(BigDecimal("180"), mc)
        val diffPi = rad180.subtract(MathConstants.PI).abs()
        assertTrue("180 deg in radians should equal Pi", diffPi.compareTo(BigDecimal("1E-50")) < 0)

        val degPi = BigDecimalMath.radiansToDegrees(MathConstants.PI, mc)
        val diff180 = degPi.subtract(BigDecimal("180")).abs()
        assertTrue("Pi in degrees should equal 180", diff180.compareTo(BigDecimal("1E-50")) < 0)

        val rad90 = BigDecimalMath.degreesToRadians(BigDecimal("90"), mc)
        val diffHalfPi = rad90.subtract(MathConstants.PI.divide(BigDecimal("2"), mc)).abs()
        assertTrue("90 deg in radians should equal Pi/2", diffHalfPi.compareTo(BigDecimal("1E-50")) < 0)
    }

    @Test
    fun testHighPrecisionSin() {
        val sin0 = BigDecimalMath.sin(BigDecimal.ZERO, mc)
        assertEquals(0, sin0.compareTo(BigDecimal.ZERO))

        // sin(pi/6) = 0.5
        val piOver6 = MathConstants.PI.divide(BigDecimal("6"), mc)
        val sin30 = BigDecimalMath.sin(piOver6, mc)
        val diff30 = sin30.subtract(BigDecimal("0.5")).abs()
        assertTrue("sin(30 deg) must equal 0.5", diff30.compareTo(BigDecimal("1E-50")) < 0)

        // sin(pi/2) = 1
        val piOver2 = MathConstants.PI.divide(BigDecimal("2"), mc)
        val sin90 = BigDecimalMath.sin(piOver2, mc)
        val diff90 = sin90.subtract(BigDecimal.ONE).abs()
        assertTrue("sin(90 deg) must equal 1", diff90.compareTo(BigDecimal("1E-50")) < 0)

        // sin(pi) = 0
        val sinPi = BigDecimalMath.sin(MathConstants.PI, mc)
        assertTrue("sin(180 deg) must equal 0 within high precision", sinPi.abs().compareTo(BigDecimal("1E-50")) < 0)
    }

    @Test
    fun testHighPrecisionCos() {
        val cos0 = BigDecimalMath.cos(BigDecimal.ZERO, mc)
        assertEquals(0, cos0.compareTo(BigDecimal.ONE))

        // cos(pi/3) = 0.5
        val piOver3 = MathConstants.PI.divide(BigDecimal("3"), mc)
        val cos60 = BigDecimalMath.cos(piOver3, mc)
        val diff60 = cos60.subtract(BigDecimal("0.5")).abs()
        assertTrue("cos(60 deg) must equal 0.5", diff60.compareTo(BigDecimal("1E-50")) < 0)

        // cos(pi/2) = 0
        val piOver2 = MathConstants.PI.divide(BigDecimal("2"), mc)
        val cos90 = BigDecimalMath.cos(piOver2, mc)
        assertTrue("cos(90 deg) must equal 0 within high precision", cos90.abs().compareTo(BigDecimal("1E-50")) < 0)

        // cos(pi) = -1
        val cosPi = BigDecimalMath.cos(MathConstants.PI, mc)
        val diffPi = cosPi.subtract(BigDecimal("-1")).abs()
        assertTrue("cos(180 deg) must equal -1", diffPi.compareTo(BigDecimal("1E-50")) < 0)
    }

    @Test
    fun testHighPrecisionAtan() {
        val atan0 = BigDecimalMath.atan(BigDecimal.ZERO, mc)
        assertEquals(0, atan0.compareTo(BigDecimal.ZERO))

        // atan(1) = pi/4
        val atan1 = BigDecimalMath.atan(BigDecimal.ONE, mc)
        val piOver4 = MathConstants.PI.divide(BigDecimal("4"), mc)
        val diff = atan1.subtract(piOver4).abs()
        assertTrue("atan(1) must equal pi/4", diff.compareTo(BigDecimal("1E-50")) < 0)
    }

    @Test
    fun testHighPrecisionAsin() {
        val asin0 = BigDecimalMath.asin(BigDecimal.ZERO, mc)
        assertEquals(0, asin0.compareTo(BigDecimal.ZERO))

        // asin(0.5) = pi/6
        val asinHalf = BigDecimalMath.asin(BigDecimal("0.5"), mc)
        val piOver6 = MathConstants.PI.divide(BigDecimal("6"), mc)
        val diff = asinHalf.subtract(piOver6).abs()
        assertTrue("asin(0.5) must equal pi/6", diff.compareTo(BigDecimal("1E-50")) < 0)

        // asin(1) = pi/2
        val asin1 = BigDecimalMath.asin(BigDecimal.ONE, mc)
        val piOver2 = MathConstants.PI.divide(BigDecimal("2"), mc)
        assertTrue("asin(1) must equal pi/2", asin1.subtract(piOver2).abs().compareTo(BigDecimal("1E-50")) < 0)

        // asin(-1) = -pi/2
        val asinMinus1 = BigDecimalMath.asin(BigDecimal("-1"), mc)
        assertTrue("asin(-1) must equal -pi/2", asinMinus1.subtract(piOver2.negate()).abs().compareTo(BigDecimal("1E-50")) < 0)
    }

    @Test
    fun testHighPrecisionAcos() {
        // acos(1) = 0
        val acos1 = BigDecimalMath.acos(BigDecimal.ONE, mc)
        assertEquals(0, acos1.compareTo(BigDecimal.ZERO))

        // acos(0.5) = pi/3
        val acosHalf = BigDecimalMath.acos(BigDecimal("0.5"), mc)
        val piOver3 = MathConstants.PI.divide(BigDecimal("3"), mc)
        assertTrue("acos(0.5) must equal pi/3", acosHalf.subtract(piOver3).abs().compareTo(BigDecimal("1E-50")) < 0)

        // acos(0) = pi/2
        val acos0 = BigDecimalMath.acos(BigDecimal.ZERO, mc)
        val piOver2 = MathConstants.PI.divide(BigDecimal("2"), mc)
        assertTrue("acos(0) must equal pi/2", acos0.subtract(piOver2).abs().compareTo(BigDecimal("1E-50")) < 0)

        // acos(-1) = pi
        val acosMinus1 = BigDecimalMath.acos(BigDecimal("-1"), mc)
        assertTrue("acos(-1) must equal pi", acosMinus1.subtract(MathConstants.PI).abs().compareTo(BigDecimal("1E-50")) < 0)
    }

    @Test
    fun testHighPrecisionAtan2() {
        val valAtan2Pos = BigDecimalMath.atan2(BigDecimal("1"), BigDecimal("1"), mc)
        val piOver4 = MathConstants.PI.divide(BigDecimal("4"), mc)
        assertTrue(valAtan2Pos.subtract(piOver4).abs().compareTo(BigDecimal("1E-50")) < 0)

        val valAtan2NegX = BigDecimalMath.atan2(BigDecimal("1"), BigDecimal("-1"), mc)
        val threePiOver4 = MathConstants.PI.multiply(BigDecimal("3")).divide(BigDecimal("4"), mc)
        assertTrue(valAtan2NegX.subtract(threePiOver4).abs().compareTo(BigDecimal("1E-50")) < 0)
    }

    @Test
    fun testBigDecimalNoBinaryArtifacts() {
        val a = BigDecimal("0.1")
        val b = BigDecimal("0.2")
        val sum = a.add(b)
        assertEquals(BigDecimal("0.3"), sum)
    }
}
