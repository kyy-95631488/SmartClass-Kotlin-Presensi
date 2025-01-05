package com.callcenter.smartclass.ui.home.mainfeaturesgrid.infoproduk.produk

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil.compose.rememberImagePainter
import com.callcenter.smartclass.ui.components.smartclassCard
import com.callcenter.smartclass.ui.home.mainfeaturesgrid.infoproduk.data.Product_Data
import com.callcenter.smartclass.ui.theme.DarkBlue
import com.callcenter.smartclass.ui.theme.DarkText
import com.callcenter.smartclass.ui.theme.smartclassTheme
import com.callcenter.smartclass.ui.theme.LightBlue
import com.callcenter.smartclass.ui.theme.WhiteColor
import java.text.NumberFormat
import java.util.Locale

@Composable
fun ProductItem(productData: Product_Data, brandName: String, onClick: () -> Unit) {

    val isLight = !smartclassTheme.colors.isDark

    val rupiahFormat = remember {
        NumberFormat.getCurrencyInstance(Locale("id", "ID")).apply {
            maximumFractionDigits = 0
            minimumFractionDigits = 0
        }
    }

    smartclassCard(
        modifier = Modifier
            .fillMaxWidth()
            .height(250.dp)
            .clickable { onClick() },
        elevation = 4.dp,
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            horizontalAlignment = Alignment.Start,
            verticalArrangement = Arrangement.Top,
            modifier = Modifier
                .fillMaxSize()
                .padding(8.dp)
        ) {
            Image(
                painter = rememberImagePainter(data = productData.thumbnailUrl),
                contentDescription = productData.title,
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .wrapContentSize()
                    .clip(RoundedCornerShape(8.dp)),
                contentScale = ContentScale.None
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = brandName,
                style = MaterialTheme.typography.bodySmall,
                color = if (isLight) DarkBlue else LightBlue.copy(alpha = 0.7f)
            )
            Text(
                text = productData.title,
                style = MaterialTheme.typography.bodyMedium,
                color = if (isSystemInDarkTheme()) WhiteColor else DarkText,
                maxLines = 2,
                overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.weight(1f))
            Text(
                text = rupiahFormat.format(productData.price),
                style = MaterialTheme.typography.bodySmall,
                color = if (isLight) DarkBlue else LightBlue
            )
        }
    }
}

@Composable
fun CategoryItem(category: String, onClick: () -> Unit) {
    val isSelected = remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .size(width = 180.dp, height = 35.dp)
            .clip(RoundedCornerShape(12.dp))
            .clickable {
                onClick()
                isSelected.value = !isSelected.value
            },
        elevation = CardDefaults.cardElevation(4.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected.value) {
                if (isSystemInDarkTheme()) LightBlue else DarkBlue
            } else {
                if (isSystemInDarkTheme()) DarkBlue else LightBlue
            }
        )
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.fillMaxSize()
        ) {
            Text(
                text = category,
                style = MaterialTheme.typography.bodySmall,
                color = if (isSystemInDarkTheme()) WhiteColor else DarkText
            )
        }
    }
}
