package com.callcenter.smartclass.ui.home.admin.tambahproduk.produk

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.callcenter.smartclass.ui.home.admin.tambahproduk.produk.data.Brand
import com.callcenter.smartclass.ui.theme.smartclassTheme
import com.callcenter.smartclass.ui.theme.MinimalPrimary
import com.callcenter.smartclass.ui.theme.MinimalSecondary
import com.callcenter.smartclass.ui.theme.MinimalTextDark
import com.callcenter.smartclass.ui.theme.MinimalTextLight
import kotlin.collections.forEach

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BrandDropdown(
    brands: List<Brand>,
    selectedBrand: String?,
    onBrandSelected: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    val selectedBrandName = brands.find { it.id == selectedBrand }?.name ?: ""

    val isLight = !smartclassTheme.colors.isDark
    val isDarkMode = isSystemInDarkTheme()

    val textColor = if (isLight) MinimalTextLight else MinimalTextDark
    val iconColor = if (isLight) MinimalTextLight else MinimalTextDark
    val menuBackgroundColor = smartclassTheme.colors.uiBackground
    val menuContentColor = if (isLight) MinimalTextLight else MinimalTextDark

    val rotationAngle by animateFloatAsState(
        targetValue = if (expanded) 0f else 270f,
        animationSpec = tween(durationMillis = 300)
    )

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = {
            if (brands.isNotEmpty()) {
                expanded = !expanded
            }
        }
    ) {
        OutlinedTextField(
            value = selectedBrandName,
            onValueChange = {},
            readOnly = true,
            label = { Text("Brand", color = textColor) },
            trailingIcon = {
                Icon(
                    imageVector = Icons.Default.ArrowDropDown,
                    contentDescription = null,
                    tint = iconColor,
                    modifier = Modifier.rotate(rotationAngle)
                )
            },
            colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(
                focusedBorderColor = MinimalPrimary,
                unfocusedBorderColor = MinimalSecondary,
                cursorColor = MinimalPrimary,
            ),
            textStyle = LocalTextStyle.current.copy(color = textColor),
            modifier = Modifier
                .fillMaxWidth()
                .menuAnchor()
        )

        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier
                .background(menuBackgroundColor)
                .border(
                    width = 1.dp,
                    color = if (isDarkMode) Color.White else Color.Black,
                    shape = RoundedCornerShape(4.dp)
                )
        ) {
            if (brands.isEmpty()) {
                DropdownMenuItem(
                    text = { Text("Tidak ada brand tersedia.", color = Color.Gray) },
                    onClick = {}
                )
            } else {
                brands.forEach { brand ->
                    DropdownMenuItem(
                        text = { Text(brand.name ?: "Unnamed Brand", color = menuContentColor) },
                        onClick = {
                            onBrandSelected(brand.id)
                            expanded = false
                        },
                        modifier = Modifier
                            .background(menuBackgroundColor)
                    )
                }
            }
        }
    }
}
