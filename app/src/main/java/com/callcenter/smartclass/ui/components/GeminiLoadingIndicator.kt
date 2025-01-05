package com.callcenter.smartclass.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Composable
fun GeminiLoadingIndicator(
    modifier: Modifier = Modifier,
    color: Color = MaterialTheme.colorScheme.primary,
    size: Dp = 48.dp,
    strokeWidth: Float = 4f
) {

    val infiniteTransition = rememberInfiniteTransition()

    val rotation1 by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        )
    )

    val rotation2 by infiniteTransition.animateFloat(
        initialValue = 360f,
        targetValue = 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        )
    )

    Canvas(modifier = modifier.size(size)) {
        val radius = size.toPx() / 2
        val circleRadius = radius * 0.8f

        rotate(rotation1) {
            drawCircle(
                color = color.copy(alpha = 0.6f),
                radius = circleRadius,
                style = androidx.compose.ui.graphics.drawscope.Stroke(strokeWidth)
            )
        }

        rotate(rotation2) {
            drawCircle(
                color = color,
                radius = circleRadius,
                style = androidx.compose.ui.graphics.drawscope.Stroke(strokeWidth)
            )
        }
    }
}
