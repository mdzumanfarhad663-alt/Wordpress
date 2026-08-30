package com.example.ui

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.ui.theme.CyberCyan
import com.example.ui.theme.EmeraldActive
import com.example.ui.theme.IndigoGlow
import kotlin.math.sin

@Composable
fun WaveformVisualizer(
    isListening: Boolean,
    isSpeaking: Boolean,
    soundLevel: Float,
    speakerRole: String?,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "waveform")
    val phase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 6.28f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "phase"
    )

    val primaryBarColor = when {
        isSpeaking -> IndigoGlow
        speakerRole == "YOU_BANGLA" -> CyberCyan
        speakerRole == "PARTNER_ENGLISH" -> EmeraldActive
        else -> MaterialTheme.colorScheme.primary
    }

    val secondaryBarColor = when {
        isSpeaking -> CyberCyan
        speakerRole == "YOU_BANGLA" -> EmeraldActive
        else -> IndigoGlow
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(56.dp),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.matchParentSize()) {
            val barCount = 24
            val barWidth = 6.dp.toPx()
            val totalSpacing = size.width - (barCount * barWidth)
            val spacing = totalSpacing / (barCount - 1)
            val maxHeight = size.height * 0.85f
            val minHeight = 6.dp.toPx()

            val brush = Brush.verticalGradient(
                colors = listOf(primaryBarColor, secondaryBarColor)
            )

            for (i in 0 until barCount) {
                val x = i * (barWidth + spacing)
                val normalizedIndex = i.toFloat() / barCount

                val waveHeight = if (isListening || isSpeaking) {
                    val dynamicEnergy = if (isListening) soundLevel.coerceIn(0.15f, 1f) else 0.5f
                    val sinFactor = (sin((normalizedIndex * 4 * Math.PI + phase).toDouble()).toFloat() + 1f) / 2f
                    (minHeight + (maxHeight - minHeight) * sinFactor * dynamicEnergy).coerceIn(minHeight, maxHeight)
                } else {
                    minHeight
                }

                val top = (size.height - waveHeight) / 2f

                drawRoundRect(
                    brush = brush,
                    topLeft = Offset(x, top),
                    size = Size(barWidth, waveHeight),
                    cornerRadius = CornerRadius(barWidth / 2f, barWidth / 2f)
                )
            }
        }
    }
}
