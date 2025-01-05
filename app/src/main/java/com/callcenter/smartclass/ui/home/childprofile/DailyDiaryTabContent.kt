package com.callcenter.smartclass.ui.home.childprofile

import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.callcenter.smartclass.ui.home.childprofile.diaryaktivitas.DailyActivityCard
import com.callcenter.smartclass.ui.home.childprofile.diarymenu.diarymenu.DailyMenuCard

@Composable
fun DailyDiaryTabContent(navController: NavController, childId: String) {
    val configuration = LocalConfiguration.current
    val screenWidth = configuration.screenWidthDp

    val isTablet = screenWidth >= 600

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        if (isTablet) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                DailyMenuCard(
                    modifier = Modifier
                        .weight(1f)
                        .height(IntrinsicSize.Min),
                    childId = childId,
                    navController = navController
                )
                DailyActivityCard(
                    modifier = Modifier
                        .weight(1f)
                        .height(IntrinsicSize.Min),
                    navController = navController,
                    childId = childId
                )
            }
        } else {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                DailyMenuCard(
                    modifier = Modifier.fillMaxWidth(),
                    childId = childId,
                    navController = navController
                )
                DailyActivityCard(
                    modifier = Modifier.fillMaxWidth(),
                    navController = navController,
                    childId = childId
                )
            }
        }
    }
}
