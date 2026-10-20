package com.example.trianglekit

import com.example.trianglekit.domain.TriangleFormatter
import com.example.trianglekit.model.AreaUnit
import com.example.trianglekit.model.LengthUnit
import com.example.trianglekit.model.TriangleAngleClassification
import com.example.trianglekit.model.TriangleResult
import com.example.trianglekit.model.TriangleSideClassification
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.math.BigDecimal
import java.math.RoundingMode

class TriangleFormatterTest {

    @Test
    fun testTrailingZeroRemoval() {
        // "6 cm²" not "6.000000 cm²"
        val formattedArea = TriangleFormatter.formatArea(
            BigDecimal("6.000000"), AreaUnit.SQUARE_CENTIMETER, 6, RoundingMode.HALF_UP
        )
        assertEquals("6 cm²", formattedArea)

        // "5 cm" not "5.000000 cm"
        val formattedLength = TriangleFormatter.formatLength(
            BigDecimal("5.000000"), LengthUnit.CENTIMETER, 6, RoundingMode.HALF_UP
        )
        assertEquals("5 cm", formattedLength)

        // "90°" not "90.000000°"
        val formattedAngle = TriangleFormatter.formatAngle(
            BigDecimal("90.000000"), 6, RoundingMode.HALF_UP
        )
        assertEquals("90°", formattedAngle)

        // "36.869898°"
        val formattedDecAngle = TriangleFormatter.formatAngle(
            BigDecimal("36.8698976458"), 6, RoundingMode.HALF_UP
        )
        assertEquals("36.869898°", formattedDecAngle)
    }

    @Test
    fun testRoundingModes() {
        val valHalf = BigDecimal("2.5")
        // HALF_UP: 2.5 -> 3
        assertEquals("3", TriangleFormatter.formatNumber(valHalf, 0, RoundingMode.HALF_UP))
        // HALF_DOWN: 2.5 -> 2
        assertEquals("2", TriangleFormatter.formatNumber(valHalf, 0, RoundingMode.HALF_DOWN))
        // CEILING: 2.1 -> 3
        assertEquals("3", TriangleFormatter.formatNumber(BigDecimal("2.1"), 0, RoundingMode.CEILING))
        // FLOOR: 2.9 -> 2
        assertEquals("2", TriangleFormatter.formatNumber(BigDecimal("2.9"), 0, RoundingMode.FLOOR))
    }

    @Test
    fun testShareTextFormatting() {
        val res = TriangleResult(
            sideA = BigDecimal("3"),
            sideB = BigDecimal("4"),
            sideC = BigDecimal("5"),
            angleA = BigDecimal("36.869898"),
            angleB = BigDecimal("53.130102"),
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

        val shareText = TriangleFormatter.formatShareText(res)
        assertTrue(shareText.contains("TriangleKit"))
        assertTrue(shareText.contains("a = 3 cm"))
        assertTrue(shareText.contains("b = 4 cm"))
        assertTrue(shareText.contains("c = 5 cm"))
        assertTrue(shareText.contains("A = 36.869898°"))
        assertTrue(shareText.contains("B = 53.130102°"))
        assertTrue(shareText.contains("C = 90°"))
        assertTrue(shareText.contains("Area: 6 cm²"))
        assertTrue(shareText.contains("hₐ = 4 cm"))
        assertTrue(shareText.contains("hᵦ = 3 cm"))
        assertTrue(shareText.contains("h𝑐 = 2.4 cm"))
        assertTrue(shareText.contains("Perimeter: 12 cm"))
        assertTrue(shareText.contains("Type: Scalene Right Triangle"))
    }
}
