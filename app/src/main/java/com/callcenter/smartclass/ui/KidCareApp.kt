@file:OptIn(
    ExperimentalSharedTransitionApi::class
)

package com.callcenter.smartclass.ui

import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionLayout
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.material.FloatingActionButton
import androidx.compose.material.Icon
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.SnackbarHost
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import com.callcenter.smartclass.ui.components.smartclassScaffold
import com.callcenter.smartclass.ui.components.smartclassSnackbar
import com.callcenter.smartclass.ui.components.remembersmartclassScaffoldState
import com.callcenter.smartclass.ui.home.DeliveryOptionsPanel
import com.callcenter.smartclass.ui.home.DestinationBar
import com.callcenter.smartclass.ui.home.HomeSections
import com.callcenter.smartclass.ui.home.smartclassBottomBar
import com.callcenter.smartclass.ui.home.addHomeGraph
import com.callcenter.smartclass.ui.home.childprofile.AddChildProfileScreen
import com.callcenter.smartclass.ui.home.composableWithCompositionLocal
import com.callcenter.smartclass.ui.navigation.MainDestinations
import com.callcenter.smartclass.ui.navigation.remembersmartclassNavController
import com.callcenter.smartclass.ui.options.AIInteraction
import com.callcenter.smartclass.ui.options.YouTubeHealth
import com.callcenter.smartclass.ui.theme.smartclassTheme
import com.callcenter.smartclass.ui.theme.Ocean8
import com.callcenter.smartclass.ui.uionly.VerifyEmailScreen
import com.google.firebase.auth.FirebaseAuth
import kotlin.math.roundToInt

@RequiresApi(Build.VERSION_CODES.R)
@Preview
@Composable
fun smartclassApp() {
    smartclassTheme {
        val smartclassNavController = remembersmartclassNavController()
        SharedTransitionLayout {
            CompositionLocalProvider(
                LocalSharedTransitionScope provides this
            ) {
                NavHost(
                    navController = smartclassNavController.navController,
                    startDestination = MainDestinations.HOME_ROUTE
                ) {

                    composableWithCompositionLocal(
                        route = MainDestinations.HOME_ROUTE
                    ) { backStackEntry ->
                        MainContainer()
                    }
                }
            }
        }
    }
}

@RequiresApi(Build.VERSION_CODES.R)
@Composable
fun MainContainer(
    modifier: Modifier = Modifier
) {
    val auth = FirebaseAuth.getInstance()
    val user = auth.currentUser
    var isEmailVerified by remember { mutableStateOf(user?.isEmailVerified == true) }
    var showVerifyEmailScreen by remember { mutableStateOf(!isEmailVerified && user != null) }

    LaunchedEffect(user) {
        auth.addAuthStateListener { firebaseAuth ->
            val currentUser = firebaseAuth.currentUser
            isEmailVerified = currentUser?.isEmailVerified == true
            showVerifyEmailScreen = !isEmailVerified && currentUser != null
        }
    }

    if (showVerifyEmailScreen) {
        VerifyEmailScreen(
            onVerificationCompleted = {
                showVerifyEmailScreen = false
            }
        )
    } else {
        val smartclassScaffoldState = remembersmartclassScaffoldState()
        val navController = remembersmartclassNavController().navController
        val navBackStackEntry by navController.currentBackStackEntryAsState()
        val currentRoute = navBackStackEntry?.destination?.route
        Log.d("NavigationRoute", "Current route: $currentRoute")

        val showBottomBar = HomeSections.entries.any { it.route == currentRoute }

        var showOptionPanel by remember { mutableStateOf(false) }

        // Tambahkan variabel status untuk DestinationBar
        var isDestinationBarVisible by rememberSaveable { mutableStateOf(true) }

        // State untuk posisi FAB
        var offsetX by remember { mutableStateOf(0f) }
        var offsetY by remember { mutableStateOf(0f) }

        smartclassScaffold(
            bottomBar = {
                if (showBottomBar) {
                    smartclassBottomBar(
                        tabs = HomeSections.entries.toTypedArray(),
                        currentRoute = currentRoute ?: HomeSections.FEED.route,
                        navigateToRoute = navController::navigate,
                        modifier = Modifier.navigationBarsPadding()
                    )
                }
            },
            floatingActionButton = {
                // FloatingActionButton yang dapat digeser
                FloatingActionButton(
                    onClick = { isDestinationBarVisible = !isDestinationBarVisible },
                    backgroundColor = Ocean8,
                    contentColor = Color.White,
                    modifier = Modifier
                        .size(40.dp) // Ukuran FAB standar
                        .offset {
                            IntOffset(offsetX.roundToInt(), offsetY.roundToInt())
                        }
                        .pointerInput(Unit) {
                            detectDragGestures { change, dragAmount ->
                                change.consume()
                                offsetX += dragAmount.x
                                offsetY += dragAmount.y
                            }
                        }
                ) {
                    Icon(
                        imageVector = if (isDestinationBarVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                        contentDescription = if (isDestinationBarVisible) "Hide Destination Bar" else "Show Destination Bar",
                        modifier = Modifier.size(20.dp)
                    )
                }
            },
            modifier = modifier,
            snackbarHost = {
                SnackbarHost(
                    hostState = it,
                    modifier = Modifier.systemBarsPadding(),
                    snackbar = { snackbarData -> smartclassSnackbar(snackbarData) }
                )
            },
            snackBarHostState = smartclassScaffoldState.snackBarHostState,
        ) { padding ->
            Box(modifier = Modifier.fillMaxSize()) {
                Column(modifier = Modifier.fillMaxSize()) {
                    // Kondisikan tampilan DestinationBar
                    if (isDestinationBarVisible && currentRoute != MainDestinations.YOUTUBE_HEALTH_ROUTE &&
                        currentRoute != MainDestinations.AI_INTERACTION_ROUTE
                    ) {
                        DestinationBar(
                            modifier = Modifier.fillMaxWidth(),
                            navigateTo = { route ->
                                navController.navigate(route)
                            }
                        )
                    }

                    NavHost(
                        navController = navController,
                        startDestination = HomeSections.FEED.route,
                        modifier = Modifier.weight(1f)
                    ) {
                        addHomeGraph(
                            navController = navController,
                            modifier = Modifier
                                .padding(padding)
                                .consumeWindowInsets(padding)
                        )
                        composable(MainDestinations.YOUTUBE_HEALTH_ROUTE) { backStackEntry ->
                            YouTubeHealth(
                                onClose = { navController.navigateUp() },
                                navController = navController
                            )
                        }
                        composable(MainDestinations.AI_INTERACTION_ROUTE) { backStackEntry ->
                            AIInteraction(onClose = { navController.navigateUp() })
                        }
                        composable(MainDestinations.ADD_CHILD_PROFILE_ROUTE) {
                            AddChildProfileScreen(navController = navController)
                        }
                    }
                }

                Box(
                    modifier = Modifier
                        .fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    AnimatedVisibility(
                        visible = showOptionPanel,
                        enter = slideInVertically(
                            initialOffsetY = { fullHeight -> fullHeight },
                            animationSpec = tween(durationMillis = 300)
                        ) + fadeIn(animationSpec = tween(durationMillis = 300)),
                        exit = slideOutVertically(
                            targetOffsetY = { fullHeight -> fullHeight },
                            animationSpec = tween(durationMillis = 300)
                        ) + fadeOut(animationSpec = tween(durationMillis = 300))
                    ) {
                        DeliveryOptionsPanel(
                            modifier = Modifier
                                .fillMaxWidth(0.8f)
                                .wrapContentHeight(),
                            onDismiss = { showOptionPanel = false },
                            onOptionSelected = { option ->
                                when (option) {
                                    1 -> {
                                        navController.navigate(MainDestinations.AI_INTERACTION_ROUTE)
                                        showOptionPanel = false
                                    }
                                    2 -> {
                                        navController.navigate(MainDestinations.YOUTUBE_HEALTH_ROUTE)
                                        showOptionPanel = false
                                    }
                                    3 -> {
                                        // Tindakan untuk Option 3
                                        showOptionPanel = false
                                    }
                                }
                            }
                        )
                    }
                }
            }
        }
    }
}

val LocalNavAnimatedVisibilityScope = compositionLocalOf<AnimatedVisibilityScope?> { null }
val LocalSharedTransitionScope = compositionLocalOf<SharedTransitionScope?> { null }
