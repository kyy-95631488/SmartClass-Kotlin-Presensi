package com.callcenter.smartclass.ui.home.mainfeaturesgrid.infoproduk.detail

import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.callcenter.smartclass.ui.theme.smartclassTheme
import com.callcenter.smartclass.ui.theme.Ocean4
import com.callcenter.smartclass.ui.theme.Ocean7

@Composable
fun QuantityStepper(
    quantity: Int,
    onQuantityChange: (Int) -> Unit,
    maxQuantity: Int
) {
    val isDarkMode = isSystemInDarkTheme()
    val isLight = !smartclassTheme.colors.isDark

    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        IconButton(
            onClick = { if (quantity > 1) onQuantityChange(quantity - 1) },
            enabled = quantity > 1,
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(
                    color = if (quantity > 1) if (isDarkMode) Ocean4 else Ocean7 else if (isDarkMode) Ocean4 else Ocean7.copy(alpha = 0.3f)
                )
        ) {
            Icon(
                imageVector = Icons.Default.Remove,
                contentDescription = "Kurangi",
                tint = Color.White
            )
        }

        Text(
            text = quantity.toString(),
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
            modifier = Modifier
                .padding(horizontal = 16.dp)
                .width(40.dp),
            textAlign = TextAlign.Center
        )

        IconButton(
            onClick = { if (quantity < maxQuantity) onQuantityChange(quantity + 1) },
            enabled = quantity < maxQuantity,
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(
                    color = if (quantity < maxQuantity) if (isDarkMode) Ocean4 else Ocean7 else if (isDarkMode) Ocean4 else Ocean7.copy(alpha = 0.3f)
                )
        ) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = "Tambah",
                tint = Color.White
            )
        }
    }
}


