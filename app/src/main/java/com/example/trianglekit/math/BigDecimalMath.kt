package com.example.trianglekit.math

import java.math.BigDecimal
import java.math.MathContext
import java.math.RoundingMode

/**
 * Deterministic, high-precision BigDecimal mathematical functions.
 * Does NOT convert to Double or Float for authoritative geometry calculations.
 */
object BigDecimalMath {

    private val ZERO = MathConstants.ZERO
    private val ONE = MathConstants.ONE
    private val TWO = MathConstants.TWO
    private val PI = MathConstants.PI
    private val TWO_PI = MathConstants.TWO_PI
    private val PI_OVER_TWO = MathConstants.PI.divide(TWO, MathConstants.DEFAULT_MATH_CONTEXT)
    private val ONE_EIGHTY = MathConstants.ONE_EIGHTY

    /**
     * Compute square root using Newton-Raphson iteration in pure BigDecimal.
     */
    fun sqrt(value: BigDecimal, mathContext: MathContext): BigDecimal {
        val compareToZero = value.compareTo(ZERO)
        if (compareToZero < 0) {
            throw IllegalArgumentException("Cannot compute square root of negative number: $value")
        }
        if (compareToZero == 0) return ZERO
        if (value.compareTo(ONE) == 0) return ONE

        // We compute with extra guard digits to guarantee precision
        val workMc = MathContext(mathContext.precision + 10, RoundingMode.HALF_UP)
        val targetDiff = BigDecimal.ONE.movePointLeft(mathContext.precision + 2)

        // Initial estimate without toDouble():
        // For value > 1, start with value / 2.
        // For value < 1, start with 1.
        var x = if (value.compareTo(ONE) > 0) {
            value.divide(TWO, workMc)
        } else {
            ONE
        }

        // Newton-Raphson: x_{k+1} = (x_k + value / x_k) / 2
        for (i in 0 until 120) {
            val nextX = x.add(value.divide(x, workMc), workMc).divide(TWO, workMc)
            val diff = nextX.subtract(x, workMc).abs()
            x = nextX
            if (diff.compareTo(targetDiff) <= 0) {
                break
            }
        }

        return x.round(mathContext)
    }

    /**
     * Degrees to radians: radians = degrees * PI / 180
     */
    fun degreesToRadians(degrees: BigDecimal, mathContext: MathContext): BigDecimal {
        val workMc = MathContext(mathContext.precision + 8, RoundingMode.HALF_UP)
        return degrees.multiply(PI, workMc).divide(ONE_EIGHTY, workMc).round(mathContext)
    }

    /**
     * Radians to degrees: degrees = radians * 180 / PI
     */
    fun radiansToDegrees(radians: BigDecimal, mathContext: MathContext): BigDecimal {
        val workMc = MathContext(mathContext.precision + 8, RoundingMode.HALF_UP)
        return radians.multiply(ONE_EIGHTY, workMc).divide(PI, workMc).round(mathContext)
    }

    /**
     * Sine of radians using range reduction and Maclaurin series.
     */
    fun sin(radians: BigDecimal, mathContext: MathContext): BigDecimal {
        if (radians.compareTo(ZERO) == 0) return ZERO

        val workMc = MathContext(mathContext.precision + 10, RoundingMode.HALF_UP)

        // Step 1: Range reduction to [-2PI, 2PI]
        // r = radians - floor(radians / TWO_PI) * TWO_PI
        val numPeriods = radians.divide(TWO_PI, 0, RoundingMode.FLOOR)
        var r = radians.subtract(numPeriods.multiply(TWO_PI, workMc), workMc)

        // Adjust to [-PI, PI]
        if (r.compareTo(PI) > 0) {
            r = r.subtract(TWO_PI, workMc)
        } else if (r.compareTo(PI.negate()) < 0) {
            r = r.add(TWO_PI, workMc)
        }

        // Adjust to [-PI/2, PI/2]
        // sin(x) = sin(PI - x) for x in [PI/2, PI]
        // sin(x) = -sin(PI + x) for x in [-PI, -PI/2]
        var sign = 1
        if (r.compareTo(PI_OVER_TWO) > 0) {
            r = PI.subtract(r, workMc)
        } else if (r.compareTo(PI_OVER_TWO.negate()) < 0) {
            r = PI.negate().subtract(r, workMc)
        }

        if (r.compareTo(ZERO) < 0) {
            sign = -sign
            r = r.negate()
        }

        // Maclaurin series: r - r^3/3! + r^5/5! - ...
        val targetTolerance = BigDecimal.ONE.movePointLeft(mathContext.precision + 4)
        val rSquared = r.multiply(r, workMc)

        var term = r
        var sum = r
        var n = 1

        while (true) {
            val denom = BigDecimal((2 * n) * (2 * n + 1))
            term = term.multiply(rSquared, workMc).divide(denom, workMc).negate()
            sum = sum.add(term, workMc)
            if (term.abs().compareTo(targetTolerance) < 0 || n > 80) {
                break
            }
            n++
        }

        val result = if (sign < 0) sum.negate() else sum
        return result.round(mathContext)
    }

    /**
     * Cosine of radians: cos(x) = sin(PI/2 - x) or Maclaurin series.
     */
    fun cos(radians: BigDecimal, mathContext: MathContext): BigDecimal {
        if (radians.compareTo(ZERO) == 0) return ONE

        val workMc = MathContext(mathContext.precision + 10, RoundingMode.HALF_UP)

        // Reduce to [0, 2PI)
        val numPeriods = radians.divide(TWO_PI, 0, RoundingMode.FLOOR)
        var r = radians.subtract(numPeriods.multiply(TWO_PI, workMc), workMc).abs()

        // Cosine symmetry: cos(x) = cos(-x) and cos(2PI - x) = cos(x)
        if (r.compareTo(PI) > 0) {
            r = TWO_PI.subtract(r, workMc)
        }

        var sign = 1
        if (r.compareTo(PI_OVER_TWO) > 0) {
            r = PI.subtract(r, workMc)
            sign = -1
        }

        // Maclaurin series: 1 - r^2/2! + r^4/4! - ...
        val targetTolerance = BigDecimal.ONE.movePointLeft(mathContext.precision + 4)
        val rSquared = r.multiply(r, workMc)

        var term = ONE
        var sum = ONE
        var n = 1

        while (true) {
            val denom = BigDecimal((2 * n - 1) * (2 * n))
            term = term.multiply(rSquared, workMc).divide(denom, workMc).negate()
            sum = sum.add(term, workMc)
            if (term.abs().compareTo(targetTolerance) < 0 || n > 80) {
                break
            }
            n++
        }

        val result = if (sign < 0) sum.negate() else sum
        return result.round(mathContext)
    }

    /**
     * Arctangent using argument reduction and Maclaurin series.
     */
    fun atan(value: BigDecimal, mathContext: MathContext): BigDecimal {
        if (value.compareTo(ZERO) == 0) return ZERO

        val workMc = MathContext(mathContext.precision + 12, RoundingMode.HALF_UP)
        var isNegative = false
        var x = value
        if (x.compareTo(ZERO) < 0) {
            isNegative = true
            x = x.negate()
        }

        // If x > 1, use atan(x) = PI/2 - atan(1/x)
        val greaterThanOne = x.compareTo(ONE) > 0
        if (greaterThanOne) {
            x = ONE.divide(x, workMc)
        }

        // Now x is in (0, 1].
        // Apply reduction formula atan(x) = 2 * atan( x / (1 + sqrt(1 + x^2)) ) twice
        // This brings x < 0.2 where Maclaurin series converges extremely fast.
        var multiplier = 1
        repeat(2) {
            val onePlusXSquared = ONE.add(x.multiply(x, workMc), workMc)
            val denom = ONE.add(sqrt(onePlusXSquared, workMc), workMc)
            x = x.divide(denom, workMc)
            multiplier *= 2
        }

        // Maclaurin series: x - x^3/3 + x^5/5 - x^7/7 + ...
        val targetTolerance = BigDecimal.ONE.movePointLeft(mathContext.precision + 6)
        val xSquared = x.multiply(x, workMc)

        var currentPower = x
        var sum = x
        var n = 1

        while (true) {
            currentPower = currentPower.multiply(xSquared, workMc)
            val denom = BigDecimal(2 * n + 1)
            val term = currentPower.divide(denom, workMc)
            sum = if (n % 2 == 1) sum.subtract(term, workMc) else sum.add(term, workMc)

            if (term.abs().compareTo(targetTolerance) < 0 || n > 80) {
                break
            }
            n++
        }

        var result = sum.multiply(BigDecimal(multiplier), workMc)

        if (greaterThanOne) {
            val piOverTwo = PI.divide(TWO, workMc)
            result = piOverTwo.subtract(result, workMc)
        }

        if (isNegative) {
            result = result.negate()
        }

        return result.round(mathContext)
    }

    /**
     * Arcsine: asin(x) = atan(x / sqrt(1 - x^2))
     */
    fun asin(value: BigDecimal, mathContext: MathContext): BigDecimal {
        val workMc = MathContext(mathContext.precision + 10, RoundingMode.HALF_UP)
        val tolerance = MathConstants.TOLERANCE

        // Clamp slight numerical inaccuracies
        var x = value
        if (x.compareTo(ONE) > 0 && x.subtract(ONE).compareTo(tolerance) <= 0) {
            x = ONE
        } else if (x.compareTo(ONE.negate()) < 0 && ONE.negate().subtract(x).compareTo(tolerance) <= 0) {
            x = ONE.negate()
        }

        if (x.compareTo(ONE) > 0 || x.compareTo(ONE.negate()) < 0) {
            throw IllegalArgumentException("asin argument out of range [-1, 1]: $value")
        }

        if (x.compareTo(ONE) == 0) {
            return PI.divide(TWO, workMc).round(mathContext)
        }
        if (x.compareTo(ONE.negate()) == 0) {
            return PI.divide(TWO, workMc).negate().round(mathContext)
        }
        if (x.compareTo(ZERO) == 0) {
            return ZERO
        }

        // 1 - x^2
        val oneMinusXSquared = ONE.subtract(x.multiply(x, workMc), workMc)
        val denom = sqrt(oneMinusXSquared, workMc)
        val ratio = x.divide(denom, workMc)

        return atan(ratio, workMc).round(mathContext)
    }

    /**
     * Arccosine: acos(x) = PI/2 - asin(x)
     */
    fun acos(value: BigDecimal, mathContext: MathContext): BigDecimal {
        val workMc = MathContext(mathContext.precision + 10, RoundingMode.HALF_UP)
        val tolerance = MathConstants.TOLERANCE

        var x = value
        if (x.compareTo(ONE) > 0 && x.subtract(ONE).compareTo(tolerance) <= 0) {
            x = ONE
        } else if (x.compareTo(ONE.negate()) < 0 && ONE.negate().subtract(x).compareTo(tolerance) <= 0) {
            x = ONE.negate()
        }

        if (x.compareTo(ONE) > 0 || x.compareTo(ONE.negate()) < 0) {
            throw IllegalArgumentException("acos argument out of range [-1, 1]: $value")
        }

        if (x.compareTo(ONE) == 0) return ZERO
        if (x.compareTo(ONE.negate()) == 0) return PI.round(mathContext)

        val asinVal = asin(x, workMc)
        val piOverTwo = PI.divide(TWO, workMc)
        return piOverTwo.subtract(asinVal, workMc).round(mathContext)
    }

    /**
     * Arctangent of two variables atan2(y, x).
     */
    fun atan2(y: BigDecimal, x: BigDecimal, mathContext: MathContext): BigDecimal {
        val workMc = MathContext(mathContext.precision + 10, RoundingMode.HALF_UP)
        val pi = PI
        val piOverTwo = pi.divide(TWO, workMc)

        return when {
            x.compareTo(ZERO) > 0 -> atan(y.divide(x, workMc), workMc).round(mathContext)
            x.compareTo(ZERO) < 0 && y.compareTo(ZERO) >= 0 -> atan(y.divide(x, workMc), workMc).add(pi, workMc).round(mathContext)
            x.compareTo(ZERO) < 0 && y.compareTo(ZERO) < 0 -> atan(y.divide(x, workMc), workMc).subtract(pi, workMc).round(mathContext)
            x.compareTo(ZERO) == 0 && y.compareTo(ZERO) > 0 -> piOverTwo.round(mathContext)
            x.compareTo(ZERO) == 0 && y.compareTo(ZERO) < 0 -> piOverTwo.negate().round(mathContext)
            else -> ZERO // x == 0 and y == 0
        }
    }
}
