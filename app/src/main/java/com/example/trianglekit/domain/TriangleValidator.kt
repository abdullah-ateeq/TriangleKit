package com.example.trianglekit.domain

import com.example.trianglekit.math.MathConstants
import java.math.BigDecimal

object TriangleValidator {

    const val MAX_INPUT_DIGITS = 500

    /**
     * Parses and validates a raw numeric string.
     * Supports integers, decimals, leading/trailing dot (.5, 10.), and scientific notation (1E+5, 1.5E-2).
     */
    fun parseNumber(input: String?): Result<BigDecimal> {
        if (input.isNullOrBlank()) {
            return Result.failure(IllegalArgumentException("Field cannot be empty."))
        }
        val trimmed = input.trim()

        if (trimmed.length > MAX_INPUT_DIGITS) {
            return Result.failure(IllegalArgumentException("Number is too long. Maximum 500 digits."))
        }

        // Standardize leading/trailing decimal point if necessary
        var normalized = trimmed
        if (normalized.startsWith(".")) {
            normalized = "0$normalized"
        } else if (normalized.startsWith("-.")) {
            normalized = "-0" + normalized.substring(2)
        } else if (normalized.startsWith("+.")) {
            normalized = "0" + normalized.substring(2)
        }
        if (normalized.endsWith(".")) {
            normalized = normalized.substring(0, normalized.length - 1)
        }

        return try {
            val bd = BigDecimal(normalized)
            Result.success(bd)
        } catch (e: Exception) {
            Result.failure(IllegalArgumentException("Enter a valid number."))
        }
    }

    /**
     * Validates that side > 0.
     */
    fun validatePositiveSide(side: BigDecimal, name: String = "Side"): Result<Unit> {
        if (side.compareTo(BigDecimal.ZERO) <= 0) {
            return Result.failure(IllegalArgumentException("$name must be greater than 0."))
        }
        return Result.success(Unit)
    }

    /**
     * Validates that height > 0.
     */
    fun validatePositiveHeight(height: BigDecimal): Result<Unit> {
        if (height.compareTo(BigDecimal.ZERO) <= 0) {
            return Result.failure(IllegalArgumentException("Height must be greater than 0."))
        }
        return Result.success(Unit)
    }

    /**
     * Validates that 0 < angle < 180 degrees.
     */
    fun validateAngle(angle: BigDecimal, name: String = "Angle"): Result<Unit> {
        if (angle.compareTo(BigDecimal.ZERO) <= 0 || angle.compareTo(MathConstants.ONE_EIGHTY) >= 0) {
            return Result.failure(IllegalArgumentException("$name must be strictly between 0° and 180°."))
        }
        return Result.success(Unit)
    }

    /**
     * Validates triangle inequality:
     * a + b > c, a + c > b, b + c > a
     * Degenerate (a + b == c) or impossible triangles are rejected.
     */
    fun validateTriangleInequality(a: BigDecimal, b: BigDecimal, c: BigDecimal): Result<Unit> {
        val ab = a.add(b)
        val ac = a.add(c)
        val bc = b.add(c)

        if (ab.compareTo(c) <= 0 || ac.compareTo(b) <= 0 || bc.compareTo(a) <= 0) {
            return Result.failure(IllegalArgumentException("These side lengths cannot form a valid triangle."))
        }
        return Result.success(Unit)
    }

    /**
     * Validates that two known angles sum to strictly less than 180.
     */
    fun validateTwoAngles(angle1: BigDecimal, angle2: BigDecimal): Result<Unit> {
        val sum = angle1.add(angle2)
        if (sum.compareTo(MathConstants.ONE_EIGHTY) >= 0) {
            return Result.failure(IllegalArgumentException("The sum of angles must be strictly less than 180°."))
        }
        return Result.success(Unit)
    }

    /**
     * Validates that three angles sum to 180 within tolerance.
     */
    fun validateAngleSum(a: BigDecimal, b: BigDecimal, c: BigDecimal): Result<Unit> {
        val sum = a.add(b).add(c)
        val diff = sum.subtract(MathConstants.ONE_EIGHTY).abs()
        if (diff.compareTo(MathConstants.ANGLE_TOLERANCE) > 0) {
            return Result.failure(IllegalArgumentException("The angles must sum to 180° (current sum: $sum°)."))
        }
        return Result.success(Unit)
    }

    /**
     * Validates consistent order: larger sides must be opposite larger angles.
     */
    fun validateSideAngleOrder(
        a: BigDecimal, b: BigDecimal, c: BigDecimal,
        angleA: BigDecimal, angleB: BigDecimal, angleC: BigDecimal
    ): Result<Unit> {
        val sides = listOf(a to angleA, b to angleB, c to angleC)
        for (i in 0 until 3) {
            for (j in i + 1 until 3) {
                val sideCmp = sides[i].first.compareTo(sides[j].first)
                val angleCmp = sides[i].second.compareTo(sides[j].second)

                // If sides are essentially equal, angles must be essentially equal
                val sideDiff = sides[i].first.subtract(sides[j].first).abs()
                val angleDiff = sides[i].second.subtract(sides[j].second).abs()

                if (sideDiff.compareTo(MathConstants.TOLERANCE) <= 0) {
                    if (angleDiff.compareTo(MathConstants.ANGLE_TOLERANCE) > 0) {
                        return Result.failure(IllegalArgumentException("The entered values do not describe the same triangle."))
                    }
                } else if ((sideCmp > 0 && angleCmp < 0) || (sideCmp < 0 && angleCmp > 0)) {
                    return Result.failure(IllegalArgumentException("The entered values do not describe the same triangle."))
                }
            }
        }
        return Result.success(Unit)
    }
}
