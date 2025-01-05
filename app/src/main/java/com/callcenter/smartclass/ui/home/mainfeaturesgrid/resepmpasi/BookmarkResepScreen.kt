package com.callcenter.smartclass.ui.home.mainfeaturesgrid.resepmpasi

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import coil.compose.SubcomposeAsyncImage
import coil.request.ImageRequest
import com.callcenter.smartclass.ui.components.smartclassCard
import com.callcenter.smartclass.ui.home.admin.tambahresepmpasi.data.ResepMpasi
import com.callcenter.smartclass.ui.home.mainfeaturesgrid.resepmpasi.viewmodel.BookmarkResepViewModel
import com.callcenter.smartclass.ui.theme.DarkBlue
import com.callcenter.smartclass.ui.theme.smartclassTheme
import com.callcenter.smartclass.ui.theme.LightBlue
import com.callcenter.smartclass.ui.theme.MinimalBackgroundDark
import com.callcenter.smartclass.ui.theme.MinimalBackgroundLight
import com.callcenter.smartclass.ui.theme.MinimalTextDark
import com.callcenter.smartclass.ui.theme.MinimalTextLight
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BookmarkResepScreen(
    navController: NavHostController,
    viewModel: BookmarkResepViewModel = viewModel()
) {
    val bookmarkedRecipes by viewModel.bookmarkedRecipes.collectAsState()
    val coroutineScope = rememberCoroutineScope()

    val backgroundColor = if (isSystemInDarkTheme()) MinimalBackgroundDark else MinimalBackgroundLight
    val textColor = if (isSystemInDarkTheme()) MinimalTextDark else MinimalTextLight

    val isLight = !smartclassTheme.colors.isDark

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Bookmark Resep MPASI", color = textColor) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Kembali", tint = if (isLight) DarkBlue else LightBlue)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = backgroundColor)
            )
        },
        content = { paddingValues ->
            if (bookmarkedRecipes.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(backgroundColor)
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Tidak ada resep yang di-bookmark.",
                        style = MaterialTheme.typography.bodyMedium.copy(color = textColor)
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(backgroundColor)
                        .padding(paddingValues)
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(bookmarkedRecipes) { recipe: ResepMpasi ->
                        smartclassCard(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .clickable {
                                    navController.navigate("resepDetail/${recipe.uuid}")
                                },
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                SubcomposeAsyncImage(
                                    model = ImageRequest.Builder(LocalContext.current)
                                        .data(recipe.thumbnailUrl)
                                        .crossfade(true)
                                        .build(),
                                    contentDescription = "Image for ${recipe.title}",
                                    modifier = Modifier
                                        .size(80.dp)
                                        .clip(RoundedCornerShape(8.dp)),
                                    contentScale = ContentScale.Crop
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Column(
                                    modifier = Modifier
                                        .weight(1f)
                                        .align(Alignment.CenterVertically)
                                ) {
                                    Text(
                                        recipe.title,
                                        style = MaterialTheme.typography.bodyMedium.copy(color = textColor),
                                        maxLines = 2,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        formatTimestampCustom(recipe.timestamp),
                                        style = MaterialTheme.typography.bodySmall.copy(color = textColor.copy(alpha = 0.7f))
                                    )
                                }
                                // Unbookmark Button
                                IconButton(
                                    onClick = {
                                        coroutineScope.launch {
                                            viewModel.removeBookmark(recipe)
                                        }
                                    }
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.Bookmark,
                                        contentDescription = "Unbookmark",
                                        tint = Color.Red
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    )
}

// Utility function to format timestamp
fun formatTimestampCustom(timestamp: Long): String {
    val date = Date(timestamp)
    val sdf = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
    return sdf.format(date)
}