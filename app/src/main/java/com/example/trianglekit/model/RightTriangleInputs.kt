package com.example.trianglekit.model

import java.math.BigDecimal

data class RightTriangleInputs(
    val sideA: BigDecimal? = null,
    val sideB: BigDecimal? = null,
    val hypotenuse: BigDecimal? = null,
    val angleA: BigDecimal? = null,
    val angleB: BigDecimal? = null
)
