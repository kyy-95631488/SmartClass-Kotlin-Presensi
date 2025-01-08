package com.callcenter.smartclass.ui.uionly

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.callcenter.smartclass.R
import kotlinx.coroutines.delay
import kotlin.math.pow

@Composable
fun SplashScreen(onTimeout: () -> Unit) {
    var isVisible by remember { mutableStateOf(true) }

    val fadeInAnimation = remember { Animatable(0f) }
    val scaleAnimation = remember { Animatable(0.8f) }
    val slideUpAnimation = remember { Animatable(50f) }

    LaunchedEffect(Unit) {

        fadeInAnimation.animateTo(
            targetValue = 1f,
            animationSpec = tween(
                durationMillis = 1000,
                easing = EaseOutCubic
            )
        )

        scaleAnimation.animateTo(
            targetValue = 1f,
            animationSpec = tween(
                durationMillis = 1000,
                easing = EaseOutBack
            )
        )

        slideUpAnimation.animateTo(
            targetValue = 0f,
            animationSpec = tween(
                durationMillis = 1000,
                easing = EaseOutQuad
            )
        )

        delay(3000)
        isVisible = false
        delay(500)
        onTimeout()
    }

    AnimatedVisibility(
        visible = isVisible,
        enter = androidx.compose.animation.fadeIn(),
        exit = androidx.compose.animation.fadeOut()
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.White)
                .graphicsLayer {
                    alpha = fadeInAnimation.value
                    scaleX = scaleAnimation.value
                    scaleY = scaleAnimation.value
                    translationY = slideUpAnimation.value
                },
            contentAlignment = Alignment.Center
        ) {
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.SpaceBetween,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                AnimatedLogo(
                    drawableId = R.drawable.assets_logo_smartclass_clean_new,
                    description = "Top Logo",
                    modifier = Modifier
                        .padding(top = 32.dp)
                        .size(50.dp)
                )

                AnimatedPartnerSection()

                AnimatedImage(
                    drawableId = R.drawable.assets_splashcreen_bottom,
                    description = "Stunting",
                    modifier = Modifier
                        .padding(bottom = 32.dp)
                        .height(180.dp)
                        .fillMaxWidth()
                )
            }
        }
    }
}

@Composable
fun AnimatedLogo(drawableId: Int, description: String, modifier: Modifier = Modifier) {
    var visible by remember { mutableStateOf(false) }

    val alphaAnim by animateFloatAsState(
        targetValue = if (visible) 1f else 0f,
        animationSpec = tween(durationMillis = 1000), label = ""
    )
    val scaleAnim by animateFloatAsState(
        targetValue = if (visible) 1f else 0.8f,
        animationSpec = tween(durationMillis = 1000), label = ""
    )

    LaunchedEffect(Unit) {
        visible = true
    }

    Image(
        painter = painterResource(id = drawableId),
        contentDescription = description,
        modifier = modifier
            .alpha(alphaAnim)
            .scale(scaleAnim)
    )
}

@Composable
fun AnimatedPartnerSection() {
    var visible by remember { mutableStateOf(false) }

    val alphaAnim by animateFloatAsState(
        targetValue = if (visible) 1f else 0f,
        animationSpec = tween(durationMillis = 1000, delayMillis = 500), label = ""
    )
    val slideAnim by animateDpAsState(
        targetValue = if (visible) 0.dp else 50.dp,
        animationSpec = tween(durationMillis = 1000, delayMillis = 500, easing = EaseOutQuadEasing),
        label = ""
    )

    LaunchedEffect(Unit) {
        visible = true
    }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .alpha(alphaAnim)
            .offset(y = slideAnim)
    ) {
        Text(
            text = "Made in From",
            style = MaterialTheme.typography.titleMedium,
            color = Color.Black.copy(0.4f)
        )
        Row(
            horizontalArrangement = Arrangement.spacedBy(26.dp),
            modifier = Modifier.padding(top = 8.dp)
        ) {
            AnimatedImage(
                drawableId = R.drawable.assets_logo_unindra,
                description = "Universitas Indraprasta PGRI",
                modifier = Modifier.size(75.dp)
            )
        }
    }
}

@Composable
fun AnimatedImage(drawableId: Int, description: String, modifier: Modifier = Modifier) {
    var visible by remember { mutableStateOf(false) }

    val fadeAnim by animateFloatAsState(
        targetValue = if (visible) 1f else 0f,
        animationSpec = tween(durationMillis = 1000, delayMillis = 100), label = ""
    )
    val scaleAnim by animateFloatAsState(
        targetValue = if (visible) 1f else 0.8f,
        animationSpec = tween(durationMillis = 1000, delayMillis = 100), label = ""
    )

    LaunchedEffect(Unit) {
        visible = true
    }

    Image(
        painter = painterResource(id = drawableId),
        contentDescription = description,
        modifier = modifier
            .alpha(fadeAnim)
            .scale(scaleAnim)
    )
}

val EaseOutCubic: Easing = CubicBezierEasing(0.215f, 0.61f, 0.355f, 1f)

val EaseOutBack: Easing = BackOutEasing(1.70158f)

val EaseOutQuadEasing: Easing = Easing { it * (2 - it) }

fun BackOutEasing(s: Float): Easing {
    return Easing { x ->
        val c1 = s
        val c3 = c1 + 1
        1 + c3 * (x - 1).toDouble().pow(3.0).toFloat() + c1 * (x - 1).toDouble().pow(2.0).toFloat()
    }
}
