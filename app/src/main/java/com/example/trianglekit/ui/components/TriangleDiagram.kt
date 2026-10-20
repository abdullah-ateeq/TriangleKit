package com.example.trianglekit.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.unit.dp
import com.example.trianglekit.math.MathConstants
import com.example.trianglekit.model.TriangleAngleClassification
import com.example.trianglekit.model.TriangleResult
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun TriangleDiagram(
    result: TriangleResult?,
    modifier: Modifier = Modifier
) {
    val strokeColor = MaterialTheme.colorScheme.primary
    val fillColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.08f)
    val textPrimary = MaterialTheme.colorScheme.onSurface.toArgb()
    val textSecondary = MaterialTheme.colorScheme.onSurfaceVariant.toArgb()
    val accentColor = MaterialTheme.colorScheme.secondary.toArgb()

    Canvas(
        modifier = modifier
            .fillMaxWidth()
            .height(200.dp)
            .padding(16.dp)
    ) {
        val width = size.width
        val height = size.height

        if (result == null || result.sideA.compareTo(java.math.BigDecimal.ZERO) == 0) {
            // Draw standard reference triangle diagram
            //             C
            //            / \
            //         b /   \ a
            //          /     \
            //         /_______\
            //        A    c    B
            val padX = 40f
            val padY = 30f
            val ptA = Offset(padX, height - padY)
            val ptB = Offset(width - padX, height - padY)
            val ptC = Offset(width * 0.45f, padY)

            val path = Path().apply {
                moveTo(ptA.x, ptA.y)
                lineTo(ptB.x, ptB.y)
                lineTo(ptC.x, ptC.y)
                close()
            }

            drawPath(path, color = fillColor)
            drawPath(
                path,
                color = strokeColor,
                style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round)
            )

            // Draw labels
            drawContext.canvas.nativeCanvas.apply {
                val paint = android.graphics.Paint().apply {
                    color = textPrimary
                    textSize = 34f
                    isAntiAlias = true
                    textAlign = android.graphics.Paint.Align.CENTER
                    typeface = android.graphics.Typeface.DEFAULT_BOLD
                }
                val paintSub = android.graphics.Paint().apply {
                    color = accentColor
                    textSize = 28f
                    isAntiAlias = true
                    textAlign = android.graphics.Paint.Align.CENTER
                }

                // Vertices
                drawText("A", ptA.x - 20f, ptA.y + 10f, paint)
                drawText("B", ptB.x + 20f, ptB.y + 10f, paint)
                drawText("C", ptC.x, ptC.y - 12f, paint)

                // Sides
                drawText("c", (ptA.x + ptB.x) / 2f, ptA.y + 28f, paintSub)
                drawText("b", (ptA.x + ptC.x) / 2f - 24f, (ptA.y + ptC.y) / 2f, paintSub)
                drawText("a", (ptB.x + ptC.x) / 2f + 24f, (ptB.y + ptC.y) / 2f, paintSub)
            }
            return@Canvas
        }

        // Geometric scaling from calculated angles and sides
        val angleARad = result.angleA.multiply(MathConstants.PI, java.math.MathContext.DECIMAL32)
            .divide(MathConstants.ONE_EIGHTY, java.math.MathContext.DECIMAL32).toFloat()
        val sideBFloat = result.sideB.toFloat()
        val sideCFloat = result.sideC.toFloat()

        // Unscaled coordinates:
        // A at (0, 0)
        // B at (c, 0)
        // C at (b * cos(A), b * sin(A))
        val rawAx = 0f
        val rawAy = 0f
        val rawBx = sideCFloat
        val rawBy = 0f
        val rawCx = (sideBFloat * cos(angleARad))
        val rawCy = (sideBFloat * sin(angleARad))

        val minX = minOf(rawAx, rawBx, rawCx)
        val maxX = maxOf(rawAx, rawBx, rawCx)
        val minY = minOf(rawAy, rawBy, rawCy)
        val maxY = maxOf(rawAy, rawBy, rawCy)

        val spanX = (maxX - minX).coerceAtLeast(1e-4f)
        val spanY = (maxY - minY).coerceAtLeast(1e-4f)

        val paddingX = 48f
        val paddingY = 36f
        val availW = width - 2 * paddingX
        val availH = height - 2 * paddingY

        val scale = minOf(availW / spanX, availH / spanY)

        val offsetX = paddingX + (availW - spanX * scale) / 2f - minX * scale
        val offsetY = height - paddingY - (availH - spanY * scale) / 2f + minY * scale

        // Screen coords (Y flipped)
        val ptA = Offset(rawAx * scale + offsetX, offsetY - rawAy * scale)
        val ptB = Offset(rawBx * scale + offsetX, offsetY - rawBy * scale)
        val ptC = Offset(rawCx * scale + offsetX, offsetY - rawCy * scale)

        val trianglePath = Path().apply {
            moveTo(ptA.x, ptA.y)
            lineTo(ptB.x, ptB.y)
            lineTo(ptC.x, ptC.y)
            close()
        }

        drawPath(trianglePath, color = fillColor)
        drawPath(
            trianglePath,
            color = strokeColor,
            style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round)
        )

        // Right angle marker if right triangle
        if (result.angleClassification == TriangleAngleClassification.RIGHT) {
            val markerSize = 22f
            when {
                result.angleC.subtract(MathConstants.NINETY).abs().compareTo(MathConstants.ANGLE_TOLERANCE) <= 0 -> {
                    // Right angle at C
                    val vCA = (ptA - ptC)
                    val vCB = (ptB - ptC)
                    val lenCA = kotlin.math.sqrt(vCA.x * vCA.x + vCA.y * vCA.y).coerceAtLeast(1f)
                    val lenCB = kotlin.math.sqrt(vCB.x * vCB.x + vCB.y * vCB.y).coerceAtLeast(1f)
                    val uA = Offset(vCA.x / lenCA * markerSize, vCA.y / lenCA * markerSize)
                    val uB = Offset(vCB.x / lenCB * markerSize, vCB.y / lenCB * markerSize)

                    val p1 = ptC + uA
                    val p2 = ptC + uA + uB
                    val p3 = ptC + uB

                    val squarePath = Path().apply {
                        moveTo(p1.x, p1.y)
                        lineTo(p2.x, p2.y)
                        lineTo(p3.x, p3.y)
                    }
                    drawPath(squarePath, color = strokeColor, style = Stroke(width = 2.dp.toPx()))
                }
                result.angleA.subtract(MathConstants.NINETY).abs().compareTo(MathConstants.ANGLE_TOLERANCE) <= 0 -> {
                    // Right angle at A
                    val p1 = ptA + Offset(markerSize, 0f)
                    val p2 = ptA + Offset(markerSize, -markerSize)
                    val p3 = ptA + Offset(0f, -markerSize)
                    val squarePath = Path().apply {
                        moveTo(p1.x, p1.y)
                        lineTo(p2.x, p2.y)
                        lineTo(p3.x, p3.y)
                    }
                    drawPath(squarePath, color = strokeColor, style = Stroke(width = 2.dp.toPx()))
                }
                result.angleB.subtract(MathConstants.NINETY).abs().compareTo(MathConstants.ANGLE_TOLERANCE) <= 0 -> {
                    // Right angle at B
                    val p1 = ptB + Offset(-markerSize, 0f)
                    val p2 = ptB + Offset(-markerSize, -markerSize)
                    val p3 = ptB + Offset(0f, -markerSize)
                    val squarePath = Path().apply {
                        moveTo(p1.x, p1.y)
                        lineTo(p2.x, p2.y)
                        lineTo(p3.x, p3.y)
                    }
                    drawPath(squarePath, color = strokeColor, style = Stroke(width = 2.dp.toPx()))
                }
            }
        }

        // Labels
        drawContext.canvas.nativeCanvas.apply {
            val paintVertex = android.graphics.Paint().apply {
                color = textPrimary
                textSize = 32f
                isAntiAlias = true
                textAlign = android.graphics.Paint.Align.CENTER
                typeface = android.graphics.Typeface.DEFAULT_BOLD
            }
            val paintSide = android.graphics.Paint().apply {
                color = accentColor
                textSize = 28f
                isAntiAlias = true
                textAlign = android.graphics.Paint.Align.CENTER
            }

            // Vertex labels with subtle offset
            drawText("A", ptA.x - 22f, ptA.y + 12f, paintVertex)
            drawText("B", ptB.x + 22f, ptB.y + 12f, paintVertex)
            drawText("C", ptC.x, ptC.y - 14f, paintVertex)

            // Side labels at midpoints
            val midAB = (ptA + ptB) / 2f
            val midBC = (ptB + ptC) / 2f
            val midCA = (ptC + ptA) / 2f

            drawText("c", midAB.x, midAB.y + 26f, paintSide)
            drawText("a", midBC.x + 24f, midBC.y - 4f, paintSide)
            drawText("b", midCA.x - 24f, midCA.y - 4f, paintSide)
        }
    }
}
