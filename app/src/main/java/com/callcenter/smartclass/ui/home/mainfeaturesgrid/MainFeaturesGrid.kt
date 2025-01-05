package com.callcenter.smartclass.ui.home.mainfeaturesgrid

import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FamilyRestroom
import androidx.compose.material.icons.filled.FoodBank
import androidx.compose.material.icons.filled.HealthAndSafety
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.MedicalServices
import androidx.compose.material.icons.filled.Preview
import androidx.compose.material.icons.filled.ShowChart
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.callcenter.smartclass.R
import com.callcenter.smartclass.ui.components.smartclassCard
import com.callcenter.smartclass.ui.theme.MinimalTextDark
import com.callcenter.smartclass.ui.theme.MinimalTextLight

@Composable
fun MainFeaturesGrid(navController: NavController) {
    @Suppress("DEPRECATION") val buttons: List<Pair<Int, ImageVector>> = listOf(
        R.string.predict to Icons.Default.ShowChart,
        R.string.predictv2 to Icons.Default.Preview,
        R.string.ibu_hamil to Icons.Default.FamilyRestroom,
        R.string.pencegahan_stunting to Icons.Default.HealthAndSafety,
        R.string.penanganan_stunting to Icons.Default.MedicalServices,
        R.string.info_produk to Icons.Default.Info,
        R.string.resep_mpasi to Icons.Default.FoodBank
    )

    val textColor = if (isSystemInDarkTheme()) MinimalTextDark else MinimalTextLight

    LazyRow(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        items(buttons) { (textRes, icon) ->
            smartclassCard(
                modifier = Modifier
                    .width(130.dp)
                    .height(50.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .clickable {
                        when (textRes) {
                            R.string.predict -> navController.navigate("predict")
                            R.string.predictv2 -> navController.navigate("predictv2")
                            R.string.ibu_hamil -> navController.navigate("ibu_hamil")
                            R.string.pencegahan_stunting -> navController.navigate("pencegahan_stunting")
                            R.string.penanganan_stunting -> navController.navigate("penanganan_stunting")
                            R.string.info_produk -> navController.navigate("info_produk")
                            R.string.resep_mpasi -> navController.navigate("resepMpasiList")
                        }
                    },
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(8.dp)
                ) {
                    Icon(
                        icon,
                        contentDescription = stringResource(id = textRes),
                        modifier = Modifier.size(20.dp),
                        tint = Color(0xff00a1c7)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        stringResource(id = textRes),
                        style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp),
                        textAlign = TextAlign.Center,
                        color = textColor,
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth()
                    )
                }
            }
        }
    }
}
