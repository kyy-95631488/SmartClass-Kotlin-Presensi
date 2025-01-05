package com.callcenter.smartclass.ui.home.mainfeaturesgrid.infoproduk.brand

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil.compose.rememberImagePainter
import com.callcenter.smartclass.ui.components.smartclassCard
import com.callcenter.smartclass.ui.home.mainfeaturesgrid.infoproduk.data.Brand_Data
import com.callcenter.smartclass.ui.theme.DarkText
import com.callcenter.smartclass.ui.theme.WhiteColor

@Composable
fun BrandItem(brandData: Brand_Data, isSelected: Boolean, onClick: () -> Unit) {
    smartclassCard(
        modifier = Modifier
            .width(100.dp)
            .clickable { onClick() },
        elevation = if (isSelected) 8.dp else 4.dp,
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(8.dp)
        ) {
            Image(
                painter = rememberImagePainter(data = brandData.imageUrl),
                contentDescription = brandData.name,
                modifier = Modifier
                    .size(80.dp)
                    .clip(RoundedCornerShape(12.dp)),
                contentScale = ContentScale.Fit
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = brandData.name,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(horizontal = 4.dp),
                maxLines = 1,
                color = if (isSystemInDarkTheme()) WhiteColor else DarkText
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "${brandData.productCount} Produk",
                style = MaterialTheme.typography.bodySmall,
                color = if (isSystemInDarkTheme()) WhiteColor.copy(alpha = 0.7f) else DarkText.copy(alpha = 0.7f)
            )
        }
    }
}
