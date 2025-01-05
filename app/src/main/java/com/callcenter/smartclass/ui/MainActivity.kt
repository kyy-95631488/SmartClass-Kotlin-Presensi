package com.callcenter.smartclass.ui

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.annotation.RequiresApi
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.tooling.preview.Preview
import com.callcenter.smartclass.ui.funcauth.FunLoginGoogle
import com.callcenter.smartclass.ui.uionly.SplashScreen
import com.google.firebase.auth.FirebaseAuth
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue

class MainActivity : ComponentActivity() {
    private lateinit var auth: FirebaseAuth

    @RequiresApi(Build.VERSION_CODES.R)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        auth = FirebaseAuth.getInstance()
        createNotificationChannel()

        setContent {
            smartclassAppWithSplash()
        }
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channelName = "Jadwal Menu Anak"
            val channelDescription = "Notifikasi untuk jadwal menu harian anak."
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel("jadwal_menu_channel", channelName, importance).apply {
                description = channelDescription
            }

            val notificationManager: NotificationManager =
                getSystemService(NotificationManager::class.java)
            notificationManager.createNotificationChannel(channel)
        }
    }

    @RequiresApi(Build.VERSION_CODES.R)
    @Composable
    fun smartclassAppWithSplash() {
        var showSplashScreen by remember { mutableStateOf(true) }

        if (showSplashScreen) {
            SplashScreen(onTimeout = {
                showSplashScreen = false
                checkAuthentication()
            })
        } else {
            smartclassApp()
        }
    }

    @RequiresApi(Build.VERSION_CODES.R)
    private fun checkAuthentication() {
        val currentUser = auth.currentUser

        if (currentUser != null) {
            currentUser.getIdToken(true).addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val idToken = task.result?.token
                    if (idToken != null && TokenValidator.validateToken(idToken)) {
                        Log.d("MainActivity", "Token is valid.")
                        setContent { smartclassApp() }
                    } else {
                        Log.d("MainActivity", "Token is invalid or expired.")
                        redirectToLogin()
                    }
                } else {
                    Log.e("MainActivity", "Failed to get token.", task.exception)
                    redirectToLogin()
                }
            }
        } else {
            redirectToLogin()
        }
    }

    private fun redirectToLogin() {
        val intent = Intent(this, FunLoginGoogle::class.java)
        startActivity(intent)
        finish()
    }
}

@Preview(showBackground = true)
@Composable
fun SplashScreenPreview() {
    MaterialTheme {
        SplashScreen {}
    }
}
