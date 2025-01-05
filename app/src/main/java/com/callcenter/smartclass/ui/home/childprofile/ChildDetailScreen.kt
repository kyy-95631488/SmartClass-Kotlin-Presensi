package com.callcenter.smartclass.ui.home.childprofile

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.material.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.callcenter.smartclass.ui.theme.*
import com.google.accompanist.pager.*
import kotlinx.coroutines.launch

@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalPagerApi::class)
@Composable
fun ChildDetailScreen(childId: String, navController: NavController) {
    val pagerState = rememberPagerState(initialPage = 0)
    val coroutineScope = rememberCoroutineScope()
    val isLight = !smartclassTheme.colors.isDark
    val tabBackgroundColor = smartclassTheme.colors.uiBackground

    val tabs = listOf("Data Anak", "Diari Harian")

    Column {
        TabRow(
            selectedTabIndex = pagerState.currentPage,
            backgroundColor = tabBackgroundColor,
            contentColor = if (isLight) MinimalTextLight else MinimalTextDark,
            modifier = Modifier
                .shadow(4.dp)
                .fillMaxWidth(),
            indicator = { tabPositions ->
                TabRowDefaults.Indicator(
                    Modifier.tabIndicatorOffset(tabPositions[pagerState.currentPage]),
                    color = if (isLight) DarkBlue else LightBlue
                )
            },
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
                    modifier = Modifier.background(
                        color = if (pagerState.currentPage == index) tabBackgroundColor else Color.Transparent
                    ),
                    text = {
                        Text(
                            title,
                            style = MaterialTheme.typography.h6.copy(
                                color = if (pagerState.currentPage == index) {
                                    if (isLight) MinimalTextLight else MinimalTextDark
                                } else if (isLight) MinimalTextLight else MinimalTextDark
                            )
                        )
                    }
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        HorizontalPager(
            count = tabs.size,
            state = pagerState,
            modifier = Modifier
                .fillMaxSize()
        ) { page ->
            when (page) {
                0 -> ChildDataTabContent(childId = childId, navController = navController)
                1 -> DailyDiaryTabContent(navController = navController, childId = childId)
            }
        }
    }
}
