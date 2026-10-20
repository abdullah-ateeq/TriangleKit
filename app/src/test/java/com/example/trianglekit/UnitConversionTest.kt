package com.example.trianglekit

import com.example.trianglekit.domain.AreaUnitConverter
import com.example.trianglekit.domain.LengthUnitConverter
import com.example.trianglekit.model.AreaUnit
import com.example.trianglekit.model.LengthUnit
import org.junit.Assert.assertEquals
import org.junit.Test
import java.math.BigDecimal

class UnitConversionTest {

    @Test
    fun testLengthConversion() {
        // 100 cm = 1 m
        val metersFromCm = LengthUnitConverter.toMeters(BigDecimal("100"), LengthUnit.CENTIMETER)
        assertEquals(0, metersFromCm.compareTo(BigDecimal("1")))

        // 1000 mm = 1 m
        val metersFromMm = LengthUnitConverter.toMeters(BigDecimal("1000"), LengthUnit.MILLIMETER)
        assertEquals(0, metersFromMm.compareTo(BigDecimal("1")))

        // 1 km = 1000 m
        val metersFromKm = LengthUnitConverter.toMeters(BigDecimal("1"), LengthUnit.KILOMETER)
        assertEquals(0, metersFromKm.compareTo(BigDecimal("1000")))

        // 1 m to cm
        val cmFromMeters = LengthUnitConverter.fromMeters(BigDecimal("1"), LengthUnit.CENTIMETER)
        assertEquals(0, cmFromMeters.compareTo(BigDecimal("100")))
    }

    @Test
    fun testAreaConversion() {
        // 10000 cm² = 1 m²
        val sqMeters = AreaUnitConverter.toSquareMeters(BigDecimal("10000"), AreaUnit.SQUARE_CENTIMETER)
        assertEquals(0, sqMeters.compareTo(BigDecimal("1")))

        // 1 m² = 10000 cm²
        val sqCm = AreaUnitConverter.fromSquareMeters(BigDecimal("1"), AreaUnit.SQUARE_CENTIMETER)
        assertEquals(0, sqCm.compareTo(BigDecimal("10000")))

        // 1000000 mm² = 1 m²
        val sqMm = AreaUnitConverter.toSquareMeters(BigDecimal("1000000"), AreaUnit.SQUARE_MILLIMETER)
        assertEquals(0, sqMm.compareTo(BigDecimal("1")))
    }

    @Test
    fun testMixedLengthUnits() {
        // a = 1 m, b = 50 cm, c = 1000 mm
        val aMeters = LengthUnitConverter.toMeters(BigDecimal("1"), LengthUnit.METER)
        val bMeters = LengthUnitConverter.toMeters(BigDecimal("50"), LengthUnit.CENTIMETER)
        val cMeters = LengthUnitConverter.toMeters(BigDecimal("1000"), LengthUnit.MILLIMETER)

        assertEquals(0, aMeters.compareTo(BigDecimal("1")))
        assertEquals(0, bMeters.compareTo(BigDecimal("0.5")))
        assertEquals(0, cMeters.compareTo(BigDecimal("1")))

        // Check triangle inequality: 1 + 0.5 = 1.5 > 1, 1 + 1 = 2 > 0.5. Valid!
        val sumAB = aMeters.add(bMeters)
        assertEquals(1, sumAB.compareTo(cMeters))
    }
}
