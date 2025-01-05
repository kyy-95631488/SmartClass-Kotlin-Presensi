package com.callcenter.smartclass.ui.uionly

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.*
import com.callcenter.smartclass.R
import com.callcenter.smartclass.ui.theme.Ocean1
import com.callcenter.smartclass.ui.theme.Ocean6
import com.callcenter.smartclass.ui.theme.Ocean7
import com.callcenter.smartclass.ui.theme.Ocean8
import com.google.accompanist.pager.*
import kotlinx.coroutines.delay

@Composable
fun GoogleLoginScreen(
    onClick: () -> Unit,
    onEmailLoginClick: () -> Unit,
    selectedLanguage: String
) {
    val carouselImages = listOf(
        R.drawable.carousel_1_welcome_new,
        R.drawable.carousel_2_welcome_new_1,
        R.drawable.carousel_3_welcome
    )
    val resources = LocalContext.current.resources

    val carouselTitles = when (selectedLanguage) {
        "en" -> listOf(
            "Welcome to smartclass",
            "Stunting? Stop it! Healthy Children, Bright Future.",
            "Monitor Child's Health"
        )
        "id" -> listOf(
            "Selamat Datang Di smartclass",
            "Stunting? Stop! Anak Sehat, Masa Depan Cerah.",
            "Monitor Kesehatan Anak"
        )
        else -> resources.getStringArray(R.array.carousel_titles_default).toList()
    }

    val carouselDescriptions = when (selectedLanguage) {
        "en" -> listOf(
            "\"smartclass\" is an innovative app that helps parents monitor child growth and provides important information on stunting prevention with accessible health and nutrition tips.",
            "Don't let stunting steal your child's future. With smartclass, monitor your child's development and ensure proper nutrition.",
            "Routinely monitor your child's health. Ensure balanced nutrition, optimal growth, and access to healthcare. Prevent stunting, realize a healthy and smart generation!"
        )
        "id" -> listOf(
            "\"smartclass\" adalah aplikasi inovatif yang membantu orang tua memantau pertumbuhan \n" +
                    "anak dan memberikan informasi penting tentang pencegahan stunting, dengan tips \n" +
                    "kesehatan dan gizi yang mudah diakses.",
            "Jangan biarkan stunting mencuri masa depan anakmu. Dengan smartclass, pantau tumbuh kembang si kecil, pastikan nutrisinya tercukupi.",
            "Monitor kesehatan anak secara rutin. Pastikan nutrisi seimbang, tumbuh kembang optimal, dan akses layanan kesehatan. Cegah stunting, wujudkan generasi sehat dan cerdas!"
        )
        else -> resources.getStringArray(R.array.carousel_descriptions_default).toList()
    }

    @Suppress("DEPRECATION") val pagerState = rememberPagerState() // Updated to use androidx.compose.foundation.pager
    var loading by remember { mutableStateOf(false) }

    // Get screen dimensions
    val configuration = LocalConfiguration.current
    val screenWidth = configuration.screenWidthDp.dp
    val screenHeight = configuration.screenHeightDp.dp

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(
                    start = screenWidth * 0.03f,
                    end = screenWidth * 0.03f,
                    top = screenHeight * 0.01f,
                    bottom = screenHeight * 0.01f
                ),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            CarouselSlider(
                images = carouselImages,
                titles = carouselTitles,
                descriptions = carouselDescriptions,
                pagerState = pagerState,
                screenHeight = screenHeight
            )

            Spacer(modifier = Modifier.weight(1f))

            CustomPagerIndicator(
                pagerState = pagerState,
                modifier = Modifier.padding(vertical = screenHeight * 0.02f)
            )

            GoogleSignInButton(
                onClick = {
                    loading = true
                    onClick()
                },
                loading = loading,
                buttonWidth = screenWidth * 0.85f,
                buttonHeight = screenHeight * 0.06f,
                iconSize = screenHeight * 0.03f,
                textSize = (screenHeight.value * 0.02f).sp // Fixed
            )

            Spacer(modifier = Modifier.height(screenHeight * 0.02f))

            EmailSignInButton(
                onClick = onEmailLoginClick,
                buttonWidth = screenWidth * 0.85f,
                buttonHeight = screenHeight * 0.06f,
                iconSize = screenHeight * 0.03f,
                textSize = (screenHeight.value * 0.02f).sp // Fixed
            )
            Spacer(modifier = Modifier.height(screenHeight * 0.02f))
        }
    }
}

@Suppress("DEPRECATION")
@Composable
fun CustomPagerIndicator(
    pagerState: PagerState,
    modifier: Modifier = Modifier,
    activeColor: Color = Color(0xFF4285F4),
    inactiveColor: Color = Color.Gray.copy(alpha = 0.5f),
    indicatorCount: Int = pagerState.pageCount
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        repeat(indicatorCount) { index ->
            val isActive = index == pagerState.currentPage
            val color = if (isActive) activeColor else inactiveColor
            val width by animateDpAsState(targetValue = if (isActive) 28.dp else 12.dp)
            val height = 6.dp

            AnimatedVisibility(
                visible = true,
                enter = scaleIn(animationSpec = tween(durationMillis = 300)),
                exit = scaleOut(animationSpec = tween(durationMillis = 300))
            ) {
                Box(
                    modifier = Modifier
                        .width(width)
                        .height(height)
                        .clip(RoundedCornerShape(50))
                        .background(color)
                        .animateContentSize(animationSpec = tween(durationMillis = 300))
                )
            }
            if (index < indicatorCount - 1) {
                Spacer(modifier = Modifier.width(8.dp))
            }
        }
    }
}

@Suppress("DEPRECATION")
@Composable
fun CarouselSlider(
    images: List<Int>,
    titles: List<String>,
    descriptions: List<String>,
    pagerState: PagerState,
    screenHeight: Dp
) {
    val carouselHeight = screenHeight * 0.68f

    var currentPage by remember { mutableIntStateOf(0) }

    LaunchedEffect(Unit) {
        while (true) {
            delay(5000)
            currentPage = (pagerState.currentPage + 1) % images.size
            pagerState.animateScrollToPage(currentPage)
        }
    }

    HorizontalPager(
        count = images.size,
        state = pagerState,
        modifier = Modifier
            .fillMaxWidth()
            .height(carouselHeight)
    ) { page ->
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Image(
                painter = painterResource(id = images[page]),
                contentDescription = null,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(carouselHeight * 0.5f) // 60% of carousel height
                    .clip(RoundedCornerShape(16.dp))
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = titles[page],
                fontSize = (screenHeight.value * 0.022f).sp, // Fixed
                fontWeight = FontWeight.Bold,
                color = Color.Black,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = descriptions[page],
                fontSize = (screenHeight.value * 0.018f).sp, // Fixed
                color = Color.Gray,
                textAlign = TextAlign.Center,
                maxLines = 100,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
fun GoogleSignInButton(
    onClick: () -> Unit,
    loading: Boolean,
    buttonWidth: Dp,
    buttonHeight: Dp,
    iconSize: Dp,
    textSize: TextUnit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isHovered = interactionSource.collectIsHoveredAsState().value
    val backgroundColor = if (isHovered) Ocean7 else Ocean8

    Button(
        onClick = { if (!loading) onClick() },
        shape = RoundedCornerShape(50),
        modifier = Modifier
            .width(buttonWidth)
            .height(buttonHeight),
        colors = ButtonDefaults.buttonColors(
            containerColor = backgroundColor,
            contentColor = Color.White,
            disabledContainerColor = Ocean7
        ),
        elevation = ButtonDefaults.buttonElevation(8.dp),
        interactionSource = interactionSource
    ) {
        if (loading) {
            CircularProgressIndicator(
                modifier = Modifier.size(buttonHeight * 0.5f),
                color = Color.White,
                strokeWidth = 2.dp
            )
        } else {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Image(
                    painter = painterResource(id = R.drawable.ic_google_logo),
                    contentDescription = "Google Logo",
                    modifier = Modifier.size(iconSize)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Sign in with Google",
                    fontSize = textSize,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
fun EmailSignInButton(
    onClick: () -> Unit,
    buttonWidth: Dp,
    buttonHeight: Dp,
    iconSize: Dp,
    textSize: TextUnit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isHovered = interactionSource.collectIsHoveredAsState().value
    val backgroundColor = if (isHovered) Ocean1 else Color.Transparent
    val borderColor = if (isHovered) Ocean6 else Ocean8

    Button(
        onClick = onClick,
        shape = RoundedCornerShape(50),
        modifier = Modifier
            .width(buttonWidth)
            .height(buttonHeight),
        colors = ButtonDefaults.buttonColors(
            containerColor = backgroundColor,
            contentColor = Ocean8,
            disabledContainerColor = Color.LightGray
        ),
        elevation = ButtonDefaults.buttonElevation(0.dp),
        border = BorderStroke(2.dp, borderColor),
        interactionSource = interactionSource
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Image(
                painter = painterResource(id = R.drawable.ic_email),
                contentDescription = "Email Logo",
                modifier = Modifier.size(iconSize)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Sign in with Email",
                fontSize = textSize,
                fontWeight = FontWeight.Bold
            )
        }
    }
}
