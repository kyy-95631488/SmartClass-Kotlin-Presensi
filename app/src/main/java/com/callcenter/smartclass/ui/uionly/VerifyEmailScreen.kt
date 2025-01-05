package com.callcenter.smartclass.ui.uionly

import android.content.Intent
import androidx.activity.ComponentActivity
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.callcenter.smartclass.ui.funcauth.FunLoginGoogle
import com.callcenter.smartclass.ui.theme.*
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch

// Inside your composable function
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VerifyEmailScreen(onVerificationCompleted: () -> Unit) {
    val auth = FirebaseAuth.getInstance()
    val user = auth.currentUser
    val coroutineScope = rememberCoroutineScope()
    var isSending by remember { mutableStateOf(false) }
    var message by remember { mutableStateOf("") }
    val context = LocalContext.current
    val configuration = LocalConfiguration.current
    val screenWidth = configuration.screenWidthDp

    // Define the default button color and color when pressed or sending
    val defaultButtonColor = Ocean5

    // Create an InteractionSource to detect interactions
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    // Set button color to Ocean8 if sending, else default or pressed color
    val buttonColor = if (isSending) Ocean8 else if (isPressed) Ocean8 else defaultButtonColor

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(BgColor, LightBackground)
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .padding(16.dp)
                .shadow(12.dp, RoundedCornerShape(24.dp)),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = WhiteColor),
            elevation = CardDefaults.cardElevation(defaultElevation = 16.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    imageVector = Icons.Default.Email,
                    contentDescription = "Email Icon",
                    tint = AccentColor,
                    modifier = Modifier
                        .size(100.dp)
                        .padding(bottom = 16.dp)
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Verifikasi Email Anda",
                    style = MaterialTheme.typography.headlineMedium.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = if (screenWidth < 600) 24.sp else 30.sp
                    ),
                    color = TextColor,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Kami telah mengirimkan email verifikasi ke alamat email Anda. Silakan periksa inbox Anda dan klik tautan verifikasi untuk mengaktifkan akun Anda.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = GrayColor,
                    modifier = Modifier.padding(horizontal = 16.dp),
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(24.dp))

                AnimatedVisibility(
                    visible = message.isNotEmpty(),
                    enter = fadeIn(animationSpec = tween(500)),
                    exit = fadeOut(animationSpec = tween(500))
                ) {
                    Text(
                        text = message,
                        style = MaterialTheme.typography.bodySmall,
                        color = when {
                            message.startsWith("Berhasil") -> Success
                            message.startsWith("Gagal") -> Error
                            else -> GrayColor
                        },
                        modifier = Modifier
                            .padding(vertical = 8.dp)
                            .fillMaxWidth(),
                        textAlign = TextAlign.Center
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Button(
                    onClick = {
                        if (user != null) {
                            coroutineScope.launch {
                                isSending = true
                                user.sendEmailVerification().addOnCompleteListener { task ->
                                    isSending = false
                                    message = if (task.isSuccessful) {
                                        "Berhasil mengirim ulang email verifikasi."
                                    } else {
                                        "Gagal mengirim ulang email: ${task.exception?.message}"
                                    }
                                }
                            }
                        }
                    },
                    enabled = !isSending,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = buttonColor,
                        contentColor = WhiteColor
                    ),
                    interactionSource = interactionSource,
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                ) {
                    if (isSending) {
                        CircularProgressIndicator(
                            modifier = Modifier
                                .size(24.dp)
                                .padding(end = 8.dp),
                            color = WhiteColor,
                            strokeWidth = 2.dp
                        )
                    }
                    Text(
                        "Kirim Ulang Email Verifikasi",
                        style = MaterialTheme.typography.labelLarge
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedButton(
                    onClick = {
                        coroutineScope.launch {
                            user?.reload()?.addOnCompleteListener { task ->
                                if (task.isSuccessful) {
                                    if (user.isEmailVerified) {
                                        onVerificationCompleted()
                                    } else {
                                        message = "Email masih belum diverifikasi."
                                    }
                                } else {
                                    message = "Gagal memuat status verifikasi: ${task.exception?.message}"
                                }
                            }
                        }
                    },
                    colors = ButtonDefaults.outlinedButtonColors(
                        containerColor = WhiteColor,
                        contentColor = Ocean8
                    ),
                    shape = RoundedCornerShape(16.dp),
                    border = BorderStroke(1.dp, Ocean8),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                ) {
                    Text(
                        "Periksa Status Verifikasi",
                        style = MaterialTheme.typography.labelLarge,
                        color = Ocean8
                    )
                }
            }
        }

        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(16.dp)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            OutlinedButton(
                onClick = {
                    auth.signOut()
                    if (context is ComponentActivity) {
                        val intent = Intent(context, FunLoginGoogle::class.java).apply {
                            flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                        }
                        context.startActivity(intent)
                        context.finish()
                    }
                },
                colors = ButtonDefaults.outlinedButtonColors(
                    containerColor = WhiteColor,
                    contentColor = Ocean8
                ),
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.dp, Ocean8),
                modifier = Modifier
                    .fillMaxWidth(0.5f)
                    .height(50.dp)
            ) {
                Text("Keluar", style = MaterialTheme.typography.labelLarge, color = Ocean8)
            }
        }
    }
}
