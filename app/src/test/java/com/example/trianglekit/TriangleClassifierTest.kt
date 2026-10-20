package com.example.trianglekit

import com.example.trianglekit.domain.TriangleClassifier
import com.example.trianglekit.model.TriangleAngleClassification
import com.example.trianglekit.model.TriangleSideClassification
import org.junit.Assert.assertEquals
import org.junit.Test
import java.math.BigDecimal

class TriangleClassifierTest {

    @Test
    fun testEquilateralClassification() {
        val sideClass = TriangleClassifier.classifyBySides(
            BigDecimal("5"), BigDecimal("5"), BigDecimal("5")
        )
        assertEquals(TriangleSideClassification.EQUILATERAL, sideClass)
    }

    @Test
    fun testIsoscelesClassification() {
        val sideClass = TriangleClassifier.classifyBySides(
            BigDecimal("5"), BigDecimal("5"), BigDecimal("6")
        )
        assertEquals(TriangleSideClassification.ISOSCELES, sideClass)
    }

    @Test
    fun testScaleneClassification() {
        val sideClass = TriangleClassifier.classifyBySides(
            BigDecimal("3"), BigDecimal("4"), BigDecimal("5")
        )
        assertEquals(TriangleSideClassification.SCALENE, sideClass)
    }

    @Test
    fun testAcuteClassification() {
        val angleClass = TriangleClassifier.classifyByAngles(
            BigDecimal("60"), BigDecimal("60"), BigDecimal("60")
        )
        assertEquals(TriangleAngleClassification.ACUTE, angleClass)
    }

    @Test
    fun testRightClassification() {
        val angleClass = TriangleClassifier.classifyByAngles(
            BigDecimal("36.87"), BigDecimal("53.13"), BigDecimal("90")
        )
        assertEquals(TriangleAngleClassification.RIGHT, angleClass)
    }

    @Test
    fun testObtuseClassification() {
        val angleClass = TriangleClassifier.classifyByAngles(
            BigDecimal("120"), BigDecimal("30"), BigDecimal("30")
        )
        assertEquals(TriangleAngleClassification.OBTUSE, angleClass)
    }
}
