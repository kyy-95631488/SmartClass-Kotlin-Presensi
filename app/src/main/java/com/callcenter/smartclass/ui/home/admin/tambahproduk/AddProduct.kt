package com.callcenter.smartclass.ui.home.admin.tambahproduk

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.callcenter.smartclass.ui.home.admin.tambahproduk.brand.AddBrand
import com.callcenter.smartclass.ui.home.admin.tambahproduk.produk.AddProductUtama
import com.callcenter.smartclass.ui.theme.*
import com.google.accompanist.pager.ExperimentalPagerApi
import com.google.accompanist.pager.HorizontalPager
import com.google.accompanist.pager.rememberPagerState
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class, ExperimentalPagerApi::class)
@Composable
fun AddProduct(navController: NavController) {
    val isLight = !smartclassTheme.colors.isDark
    val tabs = listOf("Tambah Produk", "Tambah Brand")

    val pagerState = rememberPagerState(initialPage = 0)
    val coroutineScope = rememberCoroutineScope()

    smartclassTheme {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("Tambah Produk & Brand") },
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
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(smartclassTheme.colors.uiBackground)
                        .padding(paddingValues)
                ) {
                    // Tab Row yang disinkronkan dengan Pager
                    TabRow(
                        selectedTabIndex = pagerState.currentPage,
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(if (isLight) MinimalBackgroundLight else MinimalBackgroundDark),
                        contentColor = if (isLight) MinimalTextLight else MinimalTextDark,
                        indicator = { tabPositions ->
                            TabRowDefaults.Indicator(
                                Modifier.tabIndicatorOffset(tabPositions[pagerState.currentPage]),
                                color = if (isLight) DarkBlue else LightBlue
                            )
                        },
                        containerColor = if (isLight) MinimalBackgroundLight else MinimalBackgroundDark,
                        divider = {
                            Divider(
                                color = if (isLight) DarkBlue else LightBlue,
                                thickness = 1.dp
                            )
                        }
                    ) {
                        tabs.forEachIndexed { index, title ->
                            Tab(
                                selected = pagerState.currentPage == index,
                                onClick = {
                                    coroutineScope.launch {
                                        pagerState.animateScrollToPage(index)
                                    }
                                },
                                text = { Text(title) }
                            )
                        }
                    }

                    // Horizontal Pager untuk swipe antar halaman
                    HorizontalPager(
                        count = tabs.size,
                        state = pagerState,
                        modifier = Modifier.fillMaxSize()
                    ) { page ->
                        when (page) {
                            0 -> AddProductUtama()
                            1 -> AddBrand()
                        }
                    }
                }
            }
        )
    }
}