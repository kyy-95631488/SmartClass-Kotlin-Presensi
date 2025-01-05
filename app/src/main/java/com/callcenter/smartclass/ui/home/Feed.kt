@file:OptIn(ExperimentalSharedTransitionApi::class)

package com.callcenter.smartclass.ui.home

import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.callcenter.smartclass.model.SharedPreferencesHelper
import com.callcenter.smartclass.ui.components.smartclassSurface
import com.callcenter.smartclass.ui.funcauth.FunLoginGoogle
import com.callcenter.smartclass.ui.home.article.ArticleRecommendation
import com.callcenter.smartclass.ui.home.childprofile.ChildProfileSection
import com.callcenter.smartclass.ui.home.imagecarousel.ImageCarousel
import com.callcenter.smartclass.ui.home.mainfeaturesgrid.MainFeaturesGrid
import com.callcenter.smartclass.ui.home.welcome.WelcomeDialog
import com.callcenter.smartclass.ui.theme.smartclassTheme
import com.google.firebase.auth.FirebaseAuth

@RequiresApi(Build.VERSION_CODES.R)
@Composable
fun Feed(
    modifier: Modifier = Modifier,
    navController: NavController
) {
    val context = LocalContext.current
    val sharedPreferences = context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
    var showDialog by remember { mutableStateOf(sharedPreferences.getBoolean("show_welcome_dialog", true)) }

    val token = SharedPreferencesHelper.getToken(context)
    val isTokenValid = token != null && !SharedPreferencesHelper.isTokenExpired(context)

    val firebaseUser = FirebaseAuth.getInstance().currentUser
    val userName = firebaseUser?.displayName ?: "User"

    // Cek apakah pengguna login via email
    val isEmailLogin = firebaseUser?.providerData?.any { it.providerId == "password" } ?: false

    if (showDialog) {
        WelcomeDialog(userName = userName, onDismiss = {
            showDialog = false
            sharedPreferences.edit().putBoolean("show_welcome_dialog", false).apply()
        })
    }

    if (isTokenValid || isEmailLogin) {
        // Akses diizinkan jika token valid atau login via email
        FeedContent(modifier, navController)
    } else {
        // Jika token tidak valid dan bukan login via email, arahkan ke FunLoginGoogle
        LaunchedEffect(Unit) {
            // Hapus token yang tidak valid
            SharedPreferencesHelper.clearToken(context)
            // Buat intent untuk memulai aktivitas FunLoginGoogle
            val intent = Intent(context, FunLoginGoogle::class.java).apply {
                // Tambahkan flag untuk menghapus aktivitas sebelumnya dari back stack
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            }
            context.startActivity(intent)
        }
    }
}

@Composable
fun FeedContent(
    modifier: Modifier = Modifier,
    navController: NavController
) {
    smartclassSurface(modifier = modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(8.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            item {
                ChildProfileSection(navController)
            }
            item {

                Box(
                    modifier = Modifier
                        .fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    MainFeaturesGrid(navController)
                }
            }
            item {
                ImageCarousel()
            }
            item {
                ArticleRecommendation(
                    onArticleClick = { uuid ->
                    navController.navigate("articleDetail/$uuid")
                },
                    onSeeMoreClick = {
                        navController.navigate("articleList")
                    })
            }
        }
    }
}

@RequiresApi(Build.VERSION_CODES.R)
@Preview("default")
@Preview("dark theme", uiMode = Configuration.UI_MODE_NIGHT_YES)
@Preview("large font", fontScale = 2f)
@Composable
fun HomePreview() {
    smartclassTheme {
        val navController = rememberNavController()
        Feed(navController = navController)
    }
}