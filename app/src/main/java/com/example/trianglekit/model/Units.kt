package com.example.trianglekit.model

import java.math.BigDecimal

enum class LengthUnit(val symbol: String, val displayName: String, val metersFactor: BigDecimal) {
    MILLIMETER("mm", "Millimeter", BigDecimal("0.001")),
    CENTIMETER("cm", "Centimeter", BigDecimal("0.01")),
    METER("m", "Meter", BigDecimal("1")),
    KILOMETER("km", "Kilometer", BigDecimal("1000")),
    INCH("in", "Inch", BigDecimal("0.0254")),
    FOOT("ft", "Foot", BigDecimal("0.3048")),
    YARD("yd", "Yard", BigDecimal("0.9144"));

    companion object {
        fun fromSymbol(symbol: String): LengthUnit {
            return entries.find { it.symbol.equals(symbol, ignoreCase = true) } ?: CENTIMETER
        }
    }
}

enum class AreaUnit(val symbol: String, val displayName: String, val squareMetersFactor: BigDecimal) {
    SQUARE_MILLIMETER("mm²", "Square Millimeter", BigDecimal("0.000001")), // (0.001)^2
    SQUARE_CENTIMETER("cm²", "Square Centimeter", BigDecimal("0.0001")), // (0.01)^2
    SQUARE_METER("m²", "Square Meter", BigDecimal("1")), // (1)^2
    SQUARE_KILOMETER("km²", "Square Kilometer", BigDecimal("1000000")), // (1000)^2
    SQUARE_INCH("in²", "Square Inch", BigDecimal("0.00064516")), // (0.0254)^2
    SQUARE_FOOT("ft²", "Square Foot", BigDecimal("0.09290304")), // (0.3048)^2
    SQUARE_YARD("yd²", "Square Yard", BigDecimal("0.83612736")); // (0.9144)^2

    companion object {
        fun fromSymbol(symbol: String): AreaUnit {
            return entries.find { it.symbol.equals(symbol, ignoreCase = true) } ?: SQUARE_CENTIMETER
        }

        fun fromLengthUnit(lengthUnit: LengthUnit): AreaUnit {
            return when (lengthUnit) {
                LengthUnit.MILLIMETER -> SQUARE_MILLIMETER
                LengthUnit.CENTIMETER -> SQUARE_CENTIMETER
                LengthUnit.METER -> SQUARE_METER
                LengthUnit.KILOMETER -> SQUARE_KILOMETER
                LengthUnit.INCH -> SQUARE_INCH
                LengthUnit.FOOT -> SQUARE_FOOT
                LengthUnit.YARD -> SQUARE_YARD
            }
        }
    }
}
