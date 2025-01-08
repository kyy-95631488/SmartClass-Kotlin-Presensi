package com.callcenter.smartclass.ui.home.pesanan

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavController
import com.callcenter.smartclass.ui.theme.smartclassTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OrderStatusScreen(
    navController: NavController
) {
    val isLight = !smartclassTheme.colors.isDark

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(smartclassTheme.colors.uiBackground)
    ) {
        when {

        }
    }
}
