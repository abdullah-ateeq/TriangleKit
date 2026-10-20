package com.example.trianglekit.domain

import com.example.trianglekit.model.AreaUnit
import com.example.trianglekit.model.LengthUnit
import com.example.trianglekit.model.TriangleResult
import java.math.BigDecimal
import java.math.RoundingMode
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.Locale

object TriangleFormatter {

    fun formatNumber(
        value: BigDecimal,
        decimalPlaces: Int = 6,
        roundingMode: RoundingMode = RoundingMode.HALF_UP
    ): String {
        val rounded = value.setScale(decimalPlaces, roundingMode)
        val stripped = rounded.stripTrailingZeros()
        // If stripped has negative scale (e.g. 100 with scale -2), setScale to 0
        val finalBd = if (stripped.scale() < 0) stripped.setScale(0) else stripped
        return finalBd.toPlainString()
    }

    fun formatLength(
        value: BigDecimal,
        unit: LengthUnit,
        decimalPlaces: Int = 6,
        roundingMode: RoundingMode = RoundingMode.HALF_UP
    ): String {
        return "${formatNumber(value, decimalPlaces, roundingMode)} ${unit.symbol}"
    }

    fun formatArea(
        value: BigDecimal,
        unit: AreaUnit,
        decimalPlaces: Int = 6,
        roundingMode: RoundingMode = RoundingMode.HALF_UP
    ): String {
        return "${formatNumber(value, decimalPlaces, roundingMode)} ${unit.symbol}"
    }

    fun formatAngle(
        value: BigDecimal,
        decimalPlaces: Int = 6,
        roundingMode: RoundingMode = RoundingMode.HALF_UP
    ): String {
        return "${formatNumber(value, decimalPlaces, roundingMode)}°"
    }

    fun formatShareText(
        result: TriangleResult,
        lengthUnit: LengthUnit = LengthUnit.CENTIMETER,
        areaUnit: AreaUnit = AreaUnit.SQUARE_CENTIMETER,
        decimalPlaces: Int = 6,
        roundingMode: RoundingMode = RoundingMode.HALF_UP
    ): String {
        val aFormatted = formatLength(result.sideA, lengthUnit, decimalPlaces, roundingMode)
        val bFormatted = formatLength(result.sideB, lengthUnit, decimalPlaces, roundingMode)
        val cFormatted = formatLength(result.sideC, lengthUnit, decimalPlaces, roundingMode)

        val angleAFormatted = formatAngle(result.angleA, decimalPlaces, roundingMode)
        val angleBFormatted = formatAngle(result.angleB, decimalPlaces, roundingMode)
        val angleCFormatted = formatAngle(result.angleC, decimalPlaces, roundingMode)

        val areaFormatted = formatArea(result.area, areaUnit, decimalPlaces, roundingMode)

        val haFormatted = formatLength(result.heightA, lengthUnit, decimalPlaces, roundingMode)
        val hbFormatted = formatLength(result.heightB, lengthUnit, decimalPlaces, roundingMode)
        val hcFormatted = formatLength(result.heightC, lengthUnit, decimalPlaces, roundingMode)

        val perimeterFormatted = formatLength(result.perimeter, lengthUnit, decimalPlaces, roundingMode)
        val semiFormatted = formatLength(result.semiPerimeter, lengthUnit, decimalPlaces, roundingMode)

        val classification = TriangleClassifier.getFullClassification(
            result.sideClassification,
            result.angleClassification
        )

        return """
            TriangleKit

            Sides:
            a = $aFormatted
            b = $bFormatted
            c = $cFormatted

            Angles:
            A = $angleAFormatted
            B = $angleBFormatted
            C = $angleCFormatted

            Area: $areaFormatted

            Heights:
            hₐ = $haFormatted
            hᵦ = $hbFormatted
            h𝑐 = $hcFormatted

            Perimeter: $perimeterFormatted
            Semi-perimeter: $semiFormatted

            Type: $classification
        """.trimIndent()
    }
}
