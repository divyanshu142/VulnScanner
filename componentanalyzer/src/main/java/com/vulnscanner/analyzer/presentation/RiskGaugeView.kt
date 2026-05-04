package com.vulnscanner.analyzer.presentation

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vulnscanner.analyzer.data.model.RiskLevel
import kotlin.math.cos
import kotlin.math.sin

/**
 * RiskGaugeView — Custom Composable Gauge Meter
 *
 * Renders a semi-circular gauge (180°) with:
 *   - Color gradient track: Green → Yellow → Red
 *   - Animated needle that sweeps to the score position
 *   - Score text + Risk level label at center
 *   - Tick marks at 0, 25, 50, 75, 100
 *
 * Uses Canvas API with Paint/drawArc — equivalent to Android View's onDraw(canvas).
 * Animation: Animatable(0f) → score, powered by LaunchedEffect.
 */
@Composable
fun RiskGaugeView(
    score: Int,
    modifier: Modifier = Modifier,
    size: Dp = 240.dp,
    animationDuration: Int = 1200
) {
    val riskLevel = RiskLevel.fromScore(score)

    // Animated value: sweeps from 0 → score over animationDuration ms
    val animatedScore = remember { Animatable(0f) }
    LaunchedEffect(score) {
        animatedScore.animateTo(
            targetValue = score.toFloat(),
            animationSpec = tween(durationMillis = animationDuration)
        )
    }

    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(contentAlignment = Alignment.Center) {
            Canvas(modifier = modifier.size(size)) {
                val canvasWidth = this.size.width
                val canvasHeight = this.size.height
                val cx = canvasWidth / 2f
                val cy = canvasHeight * 0.72f          // Shift center down for semi-circle
                val radius = canvasWidth * 0.38f
                val strokeWidth = canvasWidth * 0.07f

                // ── Background track (gray arc) ──────────────────────────────
                drawArc(
                    color = Color(0xFF2A2A3A),
                    startAngle = 180f,
                    sweepAngle = 180f,
                    useCenter = false,
                    topLeft = Offset(cx - radius, cy - radius),
                    size = Size(radius * 2, radius * 2),
                    style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                )

                // ── Gradient colored track (Green → Yellow → Red) ────────────
                val gradientBrush = Brush.sweepGradient(
                    colorStops = arrayOf(
                        0.0f  to Color(0xFF00C853),   // Green  (0)
                        0.25f to Color(0xFF76FF03),   // Lime
                        0.37f to Color(0xFFFFD600),   // Yellow (50)
                        0.46f to Color(0xFFFF6D00),   // Orange
                        0.5f  to Color(0xFFD50000),   // Red    (100)
                        1.0f  to Color(0xFF00C853)    // wrap
                    ),
                    center = Offset(cx, cy)
                )

                // We clip the gradient to only the filled portion
                val fillSweep = (animatedScore.value / 100f) * 180f
                drawArc(
                    brush = gradientBrush,
                    startAngle = 180f,
                    sweepAngle = fillSweep,
                    useCenter = false,
                    topLeft = Offset(cx - radius, cy - radius),
                    size = Size(radius * 2, radius * 2),
                    style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                )

                // ── Tick marks ───────────────────────────────────────────────
                drawTickMarks(cx, cy, radius, strokeWidth, canvasWidth)

                // ── Needle ───────────────────────────────────────────────────
                drawNeedle(
                    cx = cx,
                    cy = cy,
                    radius = radius,
                    scorePercent = animatedScore.value / 100f,
                    needleWidth = canvasWidth * 0.012f
                )

                // ── Center hub ───────────────────────────────────────────────
                drawCircle(
                    color = Color(0xFF1A1A2E),
                    radius = strokeWidth * 0.7f,
                    center = Offset(cx, cy)
                )
                drawCircle(
                    color = Color(0xFF4FC3F7),
                    radius = strokeWidth * 0.3f,
                    center = Offset(cx, cy)
                )
            }
        }

        // Score text
        Text(
            text = "${animatedScore.value.toInt()}",
            fontSize = 42.sp,
            fontWeight = FontWeight.ExtraBold,
            color = riskColorFor(riskLevel)
        )
        Text(
            text = riskLevel.label,
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium,
            color = riskColorFor(riskLevel).copy(alpha = 0.8f)
        )
    }
}

// ─── Helpers ────────────────────────────────────────────────────────────────

private fun DrawScope.drawTickMarks(
    cx: Float, cy: Float,
    radius: Float, strokeWidth: Float, canvasWidth: Float
) {
    val tickAngles = listOf(180f, 202.5f, 225f, 247.5f, 270f, 292.5f, 315f, 337.5f, 360f)
    val outerR = radius + strokeWidth * 0.6f
    val innerR = radius - strokeWidth * 0.6f

    tickAngles.forEach { angleDeg ->
        val rad = Math.toRadians(angleDeg.toDouble())
        val sx = cx + outerR * cos(rad).toFloat()
        val sy = cy + outerR * sin(rad).toFloat()
        val ex = cx + innerR * cos(rad).toFloat()
        val ey = cy + innerR * sin(rad).toFloat()
        drawLine(
            color = Color(0xFF555577),
            start = Offset(sx, sy),
            end = Offset(ex, ey),
            strokeWidth = canvasWidth * 0.008f
        )
    }
}

private fun DrawScope.drawNeedle(
    cx: Float, cy: Float,
    radius: Float, scorePercent: Float, needleWidth: Float
) {
    // Map 0–1 score to 180°–360° (left to right across semi-circle)
    val angleDeg = 180f + scorePercent * 180f
    val rad = Math.toRadians(angleDeg.toDouble())

    val needleLength = radius * 0.88f
    val tipX = cx + needleLength * cos(rad).toFloat()
    val tipY = cy + needleLength * sin(rad).toFloat()

    // Shadow
    drawLine(
        color = Color.Black.copy(alpha = 0.4f),
        start = Offset(cx + 2f, cy + 2f),
        end = Offset(tipX + 2f, tipY + 2f),
        strokeWidth = needleWidth * 1.4f,
        cap = StrokeCap.Round
    )
    // Needle
    drawLine(
        color = Color(0xFFFFFFFF),
        start = Offset(cx, cy),
        end = Offset(tipX, tipY),
        strokeWidth = needleWidth,
        cap = StrokeCap.Round
    )
}

private fun riskColorFor(level: RiskLevel): Color = when (level) {
    RiskLevel.LOW      -> Color(0xFF00C853)
    RiskLevel.MEDIUM   -> Color(0xFFFFD600)
    RiskLevel.HIGH     -> Color(0xFFFF6D00)
    RiskLevel.CRITICAL -> Color(0xFFD50000)
}
