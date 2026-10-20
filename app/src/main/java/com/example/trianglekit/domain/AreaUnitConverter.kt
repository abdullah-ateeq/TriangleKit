package com.example.trianglekit.domain

import com.example.trianglekit.math.MathConstants
import com.example.trianglekit.model.AreaUnit
import java.math.BigDecimal
import java.math.MathContext

object AreaUnitConverter {

    fun toSquareMeters(
        value: BigDecimal,
        unit: AreaUnit,
        mathContext: MathContext = MathConstants.DEFAULT_MATH_CONTEXT
    ): BigDecimal {
        return value.multiply(unit.squareMetersFactor, mathContext)
    }

    fun fromSquareMeters(
        sqMetersValue: BigDecimal,
        targetUnit: AreaUnit,
        mathContext: MathContext = MathConstants.DEFAULT_MATH_CONTEXT
    ): BigDecimal {
        if (targetUnit == AreaUnit.SQUARE_METER) return sqMetersValue
        return sqMetersValue.divide(targetUnit.squareMetersFactor, mathContext)
    }

    fun convert(
        value: BigDecimal,
        fromUnit: AreaUnit,
        toUnit: AreaUnit,
        mathContext: MathContext = MathConstants.DEFAULT_MATH_CONTEXT
    ): BigDecimal {
        if (fromUnit == toUnit) return value
        val sqMeters = toSquareMeters(value, fromUnit, mathContext)
        return fromSquareMeters(sqMeters, toUnit, mathContext)
    }
}
