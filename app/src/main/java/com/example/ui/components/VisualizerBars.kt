package com.example.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.ui.theme.ElectricCyan
import com.example.ui.theme.LuminousViolet
import com.example.ui.theme.NeonPink

@Composable
fun VisualizerBars(
    amplitudes: List<Float>,
    modifier: Modifier = Modifier,
    barHeight: Dp = 48.dp,
    barCount: Int = 24,
    primaryColor: Color = ElectricCyan,
    secondaryColor: Color = LuminousViolet
) {
    val gradient = Brush.verticalGradient(
        colors = listOf(
            NeonPink,
            secondaryColor,
            primaryColor
        )
    )

    Canvas(
        modifier = modifier
            .fillMaxWidth()
            .height(barHeight)
    ) {
        val totalWidth = size.width
        val canvasHeight = size.height
        val count = barCount.coerceAtMost(amplitudes.size)
        if (count == 0) return@Canvas

        val barSpacing = totalWidth / (count * 1.6f)
        val barWidth = barSpacing * 0.7f
        val startX = (totalWidth - (count * (barWidth + barSpacing) - barSpacing)) / 2f

        for (i in 0 until count) {
            val amp = amplitudes.getOrElse(i) { 0.15f }.coerceIn(0.08f, 1.0f)
            val currentBarHeight = canvasHeight * amp
            val x = startX + i * (barWidth + barSpacing)
            val y = (canvasHeight - currentBarHeight) / 2f

            drawRoundRect(
                brush = gradient,
                topLeft = Offset(x, y),
                size = Size(barWidth, currentBarHeight),
                cornerRadius = CornerRadius(barWidth / 2f, barWidth / 2f)
            )
        }
    }
}
