package com.callcenter.smartclass.ui.home.management

import android.annotation.SuppressLint
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.Image
import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.callcenter.smartclass.R
import com.callcenter.smartclass.data.AdminFunction
import com.callcenter.smartclass.ui.funcauth.viewmodel.UserViewModel
import com.callcenter.smartclass.ui.theme.AccentColor
import com.callcenter.smartclass.ui.theme.BgColor
import com.callcenter.smartclass.ui.theme.ButtonHoverDark
import com.callcenter.smartclass.ui.theme.ButtonHoverLight
import com.callcenter.smartclass.ui.theme.ButtonPressedDark
import com.callcenter.smartclass.ui.theme.ButtonPressedLight
import com.callcenter.smartclass.ui.theme.DarkBlue
import com.callcenter.smartclass.ui.theme.DarkText
import com.callcenter.smartclass.ui.theme.FunctionalRed
import com.callcenter.smartclass.ui.theme.FunctionalRedDark
import com.callcenter.smartclass.ui.theme.LightBlue
import com.callcenter.smartclass.ui.theme.MinimalBackgroundDark
import com.callcenter.smartclass.ui.theme.MinimalBackgroundLight
import com.callcenter.smartclass.ui.theme.MinimalTextDark
import com.callcenter.smartclass.ui.theme.MinimalTextLight
import com.callcenter.smartclass.ui.theme.Neutral8
import com.callcenter.smartclass.ui.theme.WhiteColor
import com.callcenter.smartclass.ui.theme.smartclassTheme
import kotlinx.coroutines.launch

@SuppressLint("UnusedBoxWithConstraintsScope")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun mainAdmin(navController: NavController, userViewModel: UserViewModel = viewModel()) {
    smartclassTheme {
        val isLight = !smartclassTheme.colors.isDark

        val snackbarHostState = remember { SnackbarHostState() }
        val coroutineScope = rememberCoroutineScope()

        val isAdmin by userViewModel.isAdmin.collectAsState()
        val isLoading by userViewModel.isLoading.collectAsState()

        Scaffold(
            snackbarHost = {
                SnackbarHost(snackbarHostState) { data ->
                    Snackbar(
                        snackbarData = data,
                        containerColor = if (isLight) FunctionalRed else FunctionalRedDark,
                        contentColor = if (isLight) WhiteColor else LightBlue,
                        actionColor = if (isLight) AccentColor else Neutral8
                    )
                }
            },
            topBar = {
                TopAppBar(
                    title = { Text("Admin Panel") },
                    navigationIcon = {
                        IconButton(onClick = { navController.popBackStack() }) {
                            @Suppress("DEPRECATION")
                            Icon(
                                Icons.Filled.ArrowBack,
                                contentDescription = "Back",
                                tint = if (isLight) DarkBlue else LightBlue
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = if (isLight) MinimalBackgroundLight else MinimalBackgroundDark,
                        titleContentColor = if (isLight) MinimalTextLight else MinimalTextDark,
                        navigationIconContentColor = if (isLight) DarkText else WhiteColor,
                        actionIconContentColor = if (isLight) DarkText else WhiteColor
                    )
                )
            },
            content = { paddingValues ->
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                        .background(if (isLight) BgColor else MinimalBackgroundDark)
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    when {
                        isLoading -> {
                            CircularProgressIndicator()
                        }
                        isAdmin -> {
                            AdminContent(navController, isLight)
                        }
                        else -> {
                            LaunchedEffect(Unit) {
                                coroutineScope.launch {
                                    snackbarHostState.showSnackbar("Akses ditolak. Hanya admin yang dapat mengakses halaman ini.")
                                    kotlinx.coroutines.delay(2000)
                                    navController.popBackStack()
                                }
                            }
                        }
                    }
                }
            }
        )
    }
}

@SuppressLint("UnusedBoxWithConstraintsScope")
@Composable
fun AdminContent(navController: NavController, isLight: Boolean) {
    Column(
        modifier = Modifier
            .fillMaxSize()
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            colors = CardDefaults.cardColors(
                containerColor = if (isLight) WhiteColor else Neutral8
            ),
            shape = RoundedCornerShape(12.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Column(
                modifier = Modifier
                    .padding(16.dp)
                    .fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Image(
                    painter = painterResource(id = R.drawable.assets_banner_admin_panel),
                    contentDescription = "Welcome",
                    contentScale = ContentScale.Fit,
                    modifier = Modifier
                        .fillMaxWidth() // Adjust the image width to match the parent width
                        .wrapContentHeight() // Automatically adjust the height based on the aspect ratio
                )
                Spacer(modifier = Modifier.height(16.dp))
            }
        }

        Text(
            text = "Manage",
            style = MaterialTheme.typography.titleMedium,
            color = if (isLight) DarkText else WhiteColor,
            modifier = Modifier
                .padding(vertical = 8.dp)
                .align(Alignment.CenterHorizontally)
        )

        val adminFunctions = listOf(
            AdminFunction("Tambah Akun Siswa", Icons.Filled.Add)
        )

        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(adminFunctions) { function ->
                AdminFunctionCard(function, isLight, navController)
            }
        }
    }
}

@Composable
fun AdminFunctionCard(function: AdminFunction, isLight: Boolean, navController: NavController) {

    val interactionSource = remember { MutableInteractionSource() }

    val isPressed by interactionSource.collectIsPressedAsState()
    val isHovered by interactionSource.collectIsHoveredAsState()

    val backgroundColor by animateColorAsState(
        targetValue = when {
            isPressed -> if (isLight) ButtonPressedLight else ButtonPressedDark
            isHovered -> if (isLight) ButtonHoverLight else ButtonHoverDark
            else -> if (isLight) WhiteColor else Neutral8
        }
    )

    val elevation by animateDpAsState(
        targetValue = if (isPressed) 8.dp else 2.dp
    )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(elevation)
            .clickable(
                interactionSource = interactionSource,
                indication = LocalIndication.current,
                onClick = {

                    when (function.name) {
                        "Tambah Akun Siswa" -> navController.navigate("add_siswa")
                    }
                }
            ),
        colors = CardDefaults.cardColors(
            containerColor = backgroundColor
        ),
        shape = RoundedCornerShape(8.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = elevation)
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = function.icon,
                contentDescription = function.name,
                modifier = Modifier.size(32.dp),
                tint = if (isLight) DarkBlue else LightBlue
            )
            Spacer(modifier = Modifier.width(16.dp))
            Text(
                text = function.name,
                style = MaterialTheme.typography.titleSmall,
                color = if (isLight) DarkText else WhiteColor,
                modifier = Modifier.weight(1f)
            )
            @Suppress("DEPRECATION")
            Icon(
                Icons.Filled.ArrowForward,
                contentDescription = "Navigate",
                tint = if (isLight) DarkBlue else LightBlue
            )
        }
    }
}
