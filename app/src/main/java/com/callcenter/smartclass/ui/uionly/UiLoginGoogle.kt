package com.callcenter.smartclass.ui.uionly

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.*
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
//noinspection UsingMaterialAndMaterial3Libraries
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.*
import com.callcenter.smartclass.R
import com.callcenter.smartclass.ui.theme.*

@Composable
fun GoogleLoginScreen(
    onClick: () -> Unit,
    selectedLanguage: String
) {
    val resources = LocalContext.current.resources

    val (onboardingTitles, onboardingDescriptions) = when (selectedLanguage) {
        "en" -> Pair(
            listOf(
                "Welcome to the Student Attendance System",
                "Track Student Attendance",
                "Easy Reporting"
            ),
            listOf(
                "Our system helps you monitor and track the attendance of students effectively and efficiently.",
                "Monitor each student's attendance and gain insights into their participation in class.",
                "Generate easy-to-read reports to assess overall student attendance and make informed decisions."
            )
        )
        "id" -> Pair(
            listOf(
                "Selamat Datang di Sistem Absensi Mahasiswa",
                "Pantau Absensi Mahasiswa",
                "Laporan Mudah"
            ),
            listOf(
                "Sistem kami membantu Anda memantau dan melacak absensi mahasiswa dengan efektif dan efisien.",
                "Pantau absensi setiap mahasiswa dan dapatkan wawasan tentang partisipasi mereka di kelas.",
                "Buat laporan yang mudah dibaca untuk menilai absensi mahasiswa secara keseluruhan dan membuat keputusan yang lebih baik."
            )
        )
        else -> Pair(
            resources.getStringArray(R.array.onboarding_titles_default).toList(),
            resources.getStringArray(R.array.onboarding_descriptions_default).toList()
        )
    }

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
                    horizontal = screenWidth * 0.03f,
                    vertical = screenHeight * 0.01f
                ),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.weight(1f))

            // Logo Image
            Image(
                painter = painterResource(id = R.drawable.assets_logo_smartclass_clean_new),  // Replace with your logo resource
                contentDescription = "App Logo",
                modifier = Modifier
                    .size(screenHeight * 0.18f)  // Adjust size as needed
                    .padding(bottom = screenHeight * 0.03f)
            )

            // Onboarding Content
            OnboardingContent(
                titles = onboardingTitles,
                descriptions = onboardingDescriptions,
                screenHeight = screenHeight
            )

            GoogleSignInButton(
                onClick = {
                    onClick()
                },
                loading = false,
                buttonWidth = screenWidth * 0.85f,
                buttonHeight = screenHeight * 0.06f,
                iconSize = screenHeight * 0.03f,
                textSize = (screenHeight.value * 0.02f).sp
            )

            Spacer(modifier = Modifier.weight(1f))
        }
    }
}

@Composable
fun OnboardingContent(
    titles: List<String>,
    descriptions: List<String>,
    screenHeight: Dp
) {
    val transition = rememberInfiniteTransition()
    val alpha by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        )
    )

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        titles.forEachIndexed { index, title ->
            // Title
            Text(
                text = title,
                fontSize = (screenHeight.value * 0.022f).sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black.copy(alpha = alpha),
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(8.dp))

            // Description
            Text(
                text = descriptions[index],
                fontSize = (screenHeight.value * 0.018f).sp,
                color = Color.Gray.copy(alpha = alpha),
                textAlign = TextAlign.Center,
                maxLines = 100,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(16.dp))
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

    val buttonCornerRadius = 16.dp

    Button(
        onClick = { if (!loading) onClick() },
        shape = RoundedCornerShape(buttonCornerRadius),
        modifier = Modifier
            .width(buttonWidth)
            .height(buttonHeight),
        colors = ButtonDefaults.buttonColors(
            backgroundColor = backgroundColor,
            contentColor = Color.White,
            disabledBackgroundColor = Ocean7
        ),
        elevation = ButtonDefaults.elevation(defaultElevation = 4.dp),
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
