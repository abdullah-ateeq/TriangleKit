package com.example.trianglekit.domain

import com.example.trianglekit.math.MathConstants
import com.example.trianglekit.model.TriangleAngleClassification
import com.example.trianglekit.model.TriangleSideClassification
import java.math.BigDecimal

object TriangleClassifier {

    fun classifyBySides(
        a: BigDecimal,
        b: BigDecimal,
        c: BigDecimal,
        tolerance: BigDecimal = MathConstants.TOLERANCE
    ): TriangleSideClassification {
        val abEqual = a.subtract(b).abs().compareTo(tolerance) <= 0
        val bcEqual = b.subtract(c).abs().compareTo(tolerance) <= 0
        val acEqual = a.subtract(c).abs().compareTo(tolerance) <= 0

        return when {
            abEqual && bcEqual -> TriangleSideClassification.EQUILATERAL
            abEqual || bcEqual || acEqual -> TriangleSideClassification.ISOSCELES
            else -> TriangleSideClassification.SCALENE
        }
    }

    fun classifyByAngles(
        angleA: BigDecimal,
        angleB: BigDecimal,
        angleC: BigDecimal,
        tolerance: BigDecimal = MathConstants.ANGLE_TOLERANCE
    ): TriangleAngleClassification {
        val angles = listOf(angleA, angleB, angleC)
        val ninety = MathConstants.NINETY

        val hasRight = angles.any { it.subtract(ninety).abs().compareTo(tolerance) <= 0 }
        if (hasRight) {
            return TriangleAngleClassification.RIGHT
        }

        val hasObtuse = angles.any { it.subtract(ninety).compareTo(tolerance) > 0 }
        if (hasObtuse) {
            return TriangleAngleClassification.OBTUSE
        }

        return TriangleAngleClassification.ACUTE
    }

    fun getFullClassification(
        sideClass: TriangleSideClassification,
        angleClass: TriangleAngleClassification
    ): String {
        return when {
            sideClass == TriangleSideClassification.EQUILATERAL -> "Equilateral Acute Triangle"
            sideClass == TriangleSideClassification.ISOSCELES && angleClass == TriangleAngleClassification.RIGHT -> "Isosceles Right Triangle"
            sideClass == TriangleSideClassification.ISOSCELES -> "Isosceles ${angleClass.displayName} Triangle"
            sideClass == TriangleSideClassification.SCALENE && angleClass == TriangleAngleClassification.RIGHT -> "Scalene Right Triangle"
            else -> "${sideClass.displayName} ${angleClass.displayName} Triangle"
        }
    }
}
