package com.callcenter.smartclass.ui.home.welcome

import androidx.compose.animation.*
import androidx.compose.animation.core.EaseInOut
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.callcenter.smartclass.R
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun WelcomeDialog(userName: String, onDismiss: () -> Unit) {
    var visible by remember { mutableStateOf(true) }
    var imageVisible by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()

    val configuration = LocalConfiguration.current
    val screenWidth = configuration.screenWidthDp.dp
    val dialogPadding = if (screenWidth < 400.dp) 8.dp else 16.dp
    val imageSize = if (screenWidth < 400.dp) 200.dp else 264.dp

    Dialog(onDismissRequest = onDismiss) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(dialogPadding),
            contentAlignment = Alignment.Center
        ) {
            AnimatedVisibility(
                visible = visible,
                enter = fadeIn(animationSpec = tween(durationMillis = 400, easing = EaseInOut)),
                exit = fadeOut(animationSpec = tween(durationMillis = 300, easing = EaseInOut))
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(dialogPadding)
                        .background(
                            color = Color(0xFF00889E),
                            shape = RoundedCornerShape(16.dp)
                        )
                    // .animateContentSize(animationSpec = tween(300)) // Dihilangkan untuk mencegah animasi geter
                ) {
                    LaunchedEffect(Unit) {
                        delay(100)
                        imageVisible = true
                    }

                    AnimatedVisibility(
                        visible = imageVisible,
                        enter = fadeIn(animationSpec = tween(durationMillis = 500, easing = EaseInOut)),
                        exit = fadeOut(animationSpec = tween(durationMillis = 300, easing = EaseInOut))
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.assets_welcome_home),
                            contentDescription = null,
                            modifier = Modifier.size(imageSize)
                        )
                    }

                    Text(
                        text = "Selamat datang, $userName! ^_^",
                        color = Color.White,
                        style = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier.padding(dialogPadding)
                    )
                    Text(
                        text = "Tips: smartclass adalah aplikasi kesehatan yang memanfaatkan model machine learning untuk memberikan informasi dan saran kesehatan. Kami hadir untuk membantu Anda tanpa perlu berkonsultasi langsung dengan dokter.",
                        color = Color.White,
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(horizontal = dialogPadding)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Button(
                        onClick = {
                            visible = false
                            coroutineScope.launch {
                                delay(300)
                                onDismiss()
                            }
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color.White,
                            contentColor = Color(0xFF0C5D6B)
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .padding(dialogPadding)
                            .height(48.dp)
                            .fillMaxWidth()
                    ) {
                        Text(text = "Tutup", style = MaterialTheme.typography.labelLarge)
                    }
                }
            }
        }
    }
}
