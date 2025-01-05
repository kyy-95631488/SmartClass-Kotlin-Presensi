package com.callcenter.smartclass.ui.home.childprofile.diarymenu

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.ContentAlpha
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Fastfood
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.material3.FilterChipDefaults.filterChipBorder
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.viewmodel.compose.viewModel
import com.callcenter.smartclass.ui.home.childprofile.diarymenu.data.Recipe
import com.callcenter.smartclass.ui.home.childprofile.diarymenu.viewmodel.RecipeViewModel
import com.callcenter.smartclass.ui.theme.MinimalAccent
import com.callcenter.smartclass.ui.theme.MinimalBackgroundDark
import com.callcenter.smartclass.ui.theme.MinimalBackgroundLight
import com.callcenter.smartclass.ui.theme.MinimalPrimary
import com.callcenter.smartclass.ui.theme.MinimalSecondary
import com.callcenter.smartclass.ui.theme.MinimalTextDark
import com.callcenter.smartclass.ui.theme.MinimalTextLight
import com.callcenter.smartclass.ui.theme.Neutral4

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecipeSelectionDialog(
    onDismiss: () -> Unit,
    onRecipeSelected: (Recipe) -> Unit,
    viewModel: RecipeViewModel = viewModel()
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedFilter by remember { mutableStateOf<String?>(null) }

    val recipes by viewModel.filteredRecipes.collectAsState()
    val isDarkMode = isSystemInDarkTheme()
    val availableLetters by viewModel.availableFirstLetters.collectAsState()

    LaunchedEffect(searchQuery, selectedFilter) {
        viewModel.filterRecipes(searchQuery, selectedFilter)
    }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(500.dp),
            color = if (isDarkMode) Neutral4.copy(alpha = 0.9f) else Neutral4.copy(alpha = 0.9f)
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Pilih Resep",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                        modifier = Modifier.align(Alignment.CenterHorizontally),
                        color = if (!isDarkMode) Color.White else Color.White
                    )
                    Spacer(modifier = Modifier.height(16.dp))

                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        label = { Text("Cari Resep", color = if (!isSystemInDarkTheme()) MinimalTextLight else MinimalTextDark) },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Search,
                                contentDescription = null,
                                tint = if (!isSystemInDarkTheme()) MinimalTextLight else MinimalTextDark
                            )
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = if (isDarkMode) MinimalBackgroundDark else MinimalBackgroundLight,
                            unfocusedContainerColor = if (isDarkMode) MinimalBackgroundDark else MinimalBackgroundLight,
                            focusedBorderColor = MinimalPrimary,
                            unfocusedBorderColor = MinimalSecondary,
                            cursorColor = MinimalPrimary,
                            focusedTextColor = if (!isDarkMode) MinimalTextLight else MinimalTextDark,
                            unfocusedTextColor = if (!isDarkMode) MinimalTextLight else MinimalTextDark,
                            disabledTextColor = if (!isDarkMode) MinimalTextLight.copy(alpha = ContentAlpha.disabled) else MinimalTextDark.copy(alpha = ContentAlpha.disabled)
                        )
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Start
                    ) {
                        availableLetters.forEach { filter ->
                            FilterChip(
                                selected = selectedFilter == filter,
                                onClick = {
                                    selectedFilter = if (selectedFilter == filter) null else filter
                                },
                                enabled = true,
                                label = { Text(filter) },
                                modifier = Modifier.padding(end = 4.dp),
                                colors = FilterChipDefaults.filterChipColors(
                                    containerColor = if (selectedFilter == filter) Color(0xFF6BDEE4) else Color.White,
                                    labelColor = if (selectedFilter == filter) Color.White else Color.Black,
                                    disabledContainerColor = Color.LightGray,
                                    disabledLabelColor = Color.Gray,
                                    selectedContainerColor = Color(0xFF6BDEE4),
                                    selectedLabelColor = Color.White,
                                    selectedLeadingIconColor = Color.White,
                                    selectedTrailingIconColor = Color.White
                                ),
                                border = filterChipBorder(
                                    enabled = true,
                                    selected = selectedFilter == filter,
                                    borderColor = if (selectedFilter == filter) Color(0xFF6BDEE4) else Color.Gray,
                                    borderWidth = 1.dp
                                )
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    LazyColumn(modifier = Modifier.weight(1f)) {
                        items(recipes) { recipe ->
                            RecipeItem(recipe = recipe, onClick = {
                                onRecipeSelected(recipe)
                                onDismiss()
                            })
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Button(
                        onClick = onDismiss,
                        modifier = Modifier.align(Alignment.End),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isDarkMode) Color(0xD36BDEE4) else Color(0xFF6BDEE4),
                            contentColor = Color.White
                        ),
                        elevation = ButtonDefaults.buttonElevation(
                            defaultElevation = 8.dp,
                            pressedElevation = 12.dp,
                            disabledElevation = 0.dp
                        ),
                        shape = RoundedCornerShape(50)
                    ) {
                        Text("Tutup")
                    }
                }
            }
        }
    }
}

@Composable
fun RecipeItem(recipe: Recipe, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 8.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFFE1FFFC)
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 4.dp
        )
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = Icons.Default.Fastfood,
                contentDescription = null,
                modifier = Modifier
                    .size(48.dp)
                    .padding(8.dp),
                tint = Color(0xFF6BDEE4)
            )
            Text(
                text = recipe.title,
                style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold),
                modifier = Modifier.padding(8.dp),
                color = if (!isSystemInDarkTheme()) MinimalTextLight else MinimalTextLight
            )
        }
    }
}
