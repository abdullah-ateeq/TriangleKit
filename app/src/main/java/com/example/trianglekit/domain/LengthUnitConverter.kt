package com.example.trianglekit.domain

import com.example.trianglekit.math.MathConstants
import com.example.trianglekit.model.LengthUnit
import java.math.BigDecimal
import java.math.MathContext
import java.math.RoundingMode

object LengthUnitConverter {

    fun toMeters(
        value: BigDecimal,
        unit: LengthUnit,
        mathContext: MathContext = MathConstants.DEFAULT_MATH_CONTEXT
    ): BigDecimal {
        return value.multiply(unit.metersFactor, mathContext)
    }

    fun fromMeters(
        metersValue: BigDecimal,
        targetUnit: LengthUnit,
        mathContext: MathContext = MathConstants.DEFAULT_MATH_CONTEXT
    ): BigDecimal {
        if (targetUnit == LengthUnit.METER) return metersValue
        return metersValue.divide(targetUnit.metersFactor, mathContext)
    }

    fun convert(
        value: BigDecimal,
        fromUnit: LengthUnit,
        toUnit: LengthUnit,
        mathContext: MathContext = MathConstants.DEFAULT_MATH_CONTEXT
    ): BigDecimal {
        if (fromUnit == toUnit) return value
        val meters = toMeters(value, fromUnit, mathContext)
        return fromMeters(meters, toUnit, mathContext)
    }
}
