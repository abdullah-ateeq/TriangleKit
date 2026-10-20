package com.example.trianglekit.math

import java.math.BigDecimal
import java.math.MathContext
import java.math.RoundingMode

object MathConstants {
    /**
     * High-precision Pi with over 60 digits.
     */
    val PI = BigDecimal(
        "3.1415926535897932384626433832795028841971693993751058209749445923078164062862"
    )

    val TWO = BigDecimal("2")
    val THREE = BigDecimal("3")
    val FOUR = BigDecimal("4")
    val HALF = BigDecimal("0.5")
    val ONE_EIGHTY = BigDecimal("180")
    val NINETY = BigDecimal("90")
    val ZERO = BigDecimal.ZERO
    val ONE = BigDecimal.ONE

    val TWO_PI: BigDecimal = PI.multiply(TWO)
    val PI_OVER_TWO: BigDecimal = PI.divide(TWO, MathContext.DECIMAL128)

    /**
     * Default calculation precision (at least 50 digits internally).
     */
    val DEFAULT_MATH_CONTEXT = MathContext(60, RoundingMode.HALF_UP)

    /**
     * Geometric tolerance for high-precision equality checks (e.g., 90-degree check, equality of sides).
     */
    val TOLERANCE = BigDecimal("1E-12")

    /**
     * Looser tolerance for user-input floating approximations if needed.
     */
    val ANGLE_TOLERANCE = BigDecimal("1E-7")
}
