package com.callcenter.smartclass.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Composable
fun BarLoadingIndicator(
    modifier: Modifier = Modifier,
    color: Color = MaterialTheme.colorScheme.primary,
    width: Dp = 200.dp,
    height: Dp = 8.dp
) {
    val infiniteTransition = rememberInfiniteTransition()
    val animatedWidth by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        )
    )

    Canvas(modifier = modifier.size(width, height)) {
        val canvasWidth = size.width
        val animatedBarWidth = canvasWidth * animatedWidth
        drawRoundRect(
            color = color,
            size = androidx.compose.ui.geometry.Size(animatedBarWidth, size.height),
            cornerRadius = androidx.compose.ui.geometry.CornerRadius(x = 8.dp.toPx(), y = 8.dp.toPx())
        )
    }
}
