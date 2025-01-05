package com.callcenter.smartclass.ui.home.imagecarousel

import android.annotation.SuppressLint
import android.content.res.Configuration
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImagePainter
import coil.compose.rememberAsyncImagePainter
import com.callcenter.smartclass.R
import com.callcenter.smartclass.ui.components.smartclassCard
import com.google.accompanist.pager.HorizontalPager
import com.google.accompanist.pager.rememberPagerState
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Suppress("DEPRECATION")
@SuppressLint("UnusedBoxWithConstraintsScope")
@Composable
fun ImageCarousel() {
    val images = listOf(
        R.drawable.assets_banner_01,
        R.drawable.assets_banner_01,
        R.drawable.assets_banner_01
    )

    val pagerState = rememberPagerState()
    val coroutineScope = rememberCoroutineScope()
    var autoScrollJob by remember { mutableStateOf<Job?>(null) }

    DisposableEffect(Unit) {
        autoScrollJob = coroutineScope.launch {
            while (true) {
                delay(3000)
                val nextPage = (pagerState.currentPage + 1) % images.size
                pagerState.animateScrollToPage(nextPage)
            }
        }
        onDispose {
            autoScrollJob?.cancel()
        }
    }

    val configuration = LocalConfiguration.current
    val screenWidth = configuration.screenWidthDp.dp
    val screenHeight = configuration.screenHeightDp.dp
    val carouselHeight = if (configuration.orientation == Configuration.ORIENTATION_LANDSCAPE) {
        screenHeight * 0.4f
    } else {
        screenWidth * 9 / 16
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 14.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(carouselHeight),
            contentAlignment = Alignment.BottomCenter
        ) {
            HorizontalPager(
                count = images.size,
                modifier = Modifier.fillMaxSize(),
                state = pagerState
            ) { page ->
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(
                            start = if (page == 0) 0.dp else 8.dp,
                            end = if (page == images.size - 1) 0.dp else 8.dp
                        )
                ) {
                    smartclassCard(
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier.wrapContentSize()
                    ) {
                        val painter = rememberAsyncImagePainter(model = images[page])
                        Box(modifier = Modifier.wrapContentSize()) {
                            Image(
                                painter = painter,
                                contentDescription = "Carousel Image $page",
                                modifier = Modifier
                                    .wrapContentSize()
                                    .clip(RoundedCornerShape(8.dp)),
                                contentScale = ContentScale.None
                            )

                            val painterState = painter.state
                            if (painterState is AsyncImagePainter.State.Loading ||
                                painterState is AsyncImagePainter.State.Error
                            ) {
                                ShimmerEffect(modifier = Modifier.matchParentSize())
                            }
                        }
                    }
                }
            }

            // PagerIndicator sekarang berada di dalam Box bersama HorizontalPager
            PagerIndicator(currentPage = pagerState.currentPage, totalPages = images.size)
        }
    }
}

@Composable
fun ShimmerEffect(
    modifier: Modifier = Modifier
) {
    val shimmerColors = listOf(
        Color.LightGray.copy(alpha = 0.9f),
        Color.LightGray.copy(alpha = 0.3f),
        Color.LightGray.copy(alpha = 0.9f)
    )

    val transition = rememberInfiniteTransition(label = "")
    val translateAnim by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1000f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = ""
    )

    val brush = Brush.linearGradient(
        colors = shimmerColors,
        start = Offset(translateAnim - 1000f, translateAnim - 1000f),
        end = Offset(translateAnim, translateAnim)
    )

    Box(
        modifier = modifier.background(brush = brush)
    )
}

@Composable
fun PagerIndicator(
    currentPage: Int,
    totalPages: Int
) {
    Row(
        modifier = Modifier
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        for (i in 0 until totalPages) {
            val isSelected = i == currentPage
            val indicatorWidth by animateDpAsState(
                targetValue = if (isSelected) 24.dp else 12.dp,
                animationSpec = tween(durationMillis = 300), label = ""
            )

            Box(
                modifier = Modifier
                    .height(8.dp)
                    .width(indicatorWidth)
                    .background(
                        color = if (isSelected) Color(0xFF4285F4) else Color.Gray.copy(alpha = 0.5f),
                        shape = RoundedCornerShape(4.dp)
                    )
            )
        }
    }
}
