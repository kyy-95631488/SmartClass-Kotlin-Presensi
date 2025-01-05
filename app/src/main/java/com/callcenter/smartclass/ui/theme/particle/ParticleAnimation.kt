package com.callcenter.smartclass.ui.theme.particle

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.ui.geometry.Offset
import kotlinx.coroutines.delay
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.Modifier
import kotlin.math.hypot

@Composable
fun ParticleAnimation(width: Int, height: Int) {
    val particles = remember { mutableStateListOf<Particle>() }

    // Control the frame rate
    LaunchedEffect(Unit) {
        while (true) {
            // Limit the number of new particles created
            if (particles.size < 100) { // Max 100 particles
                val newParticle = Particle(
                    x = (0 until width).random().toFloat(),
                    y = (0 until height).random().toFloat(),
                    radius = (5..10).random().toFloat(),
                    alpha = 1f,
                    speedX = (-1..1).random().toFloat(),
                    speedY = (-5..-1).random().toFloat()
                )
                particles.add(newParticle)
            }

            // Update particle positions
            particles.forEach { particle ->
                particle.x += particle.speedX
                particle.y += particle.speedY
                particle.alpha = (particle.alpha - 0.02f).coerceAtLeast(0f) // Keep alpha >= 0
            }

            // Remove particles that have faded out
            particles.removeAll { it.alpha <= 0 }

            // Control the frame rate (60 FPS)
            delay(16)
        }
    }

    Canvas(modifier = Modifier.fillMaxSize()) {
        val connectionDistance = 100f
        particles.forEach { particle ->
            drawCircle(
                color = Color.Gray.copy(alpha = particle.alpha),
                radius = particle.radius,
                center = Offset(particle.x, particle.y)
            )

            // Draw connections to nearby particles
            particles.forEach { otherParticle ->
                val distance = hypot(otherParticle.x - particle.x, otherParticle.y - particle.y)
                if (distance < connectionDistance && otherParticle != particle) {
                    drawLine(
                        color = Color.Gray.copy(alpha = (1 - (distance / connectionDistance)) * particle.alpha),
                        start = Offset(particle.x, particle.y),
                        end = Offset(otherParticle.x, otherParticle.y),
                        strokeWidth = 1f
                    )
                }
            }
        }
    }
}

