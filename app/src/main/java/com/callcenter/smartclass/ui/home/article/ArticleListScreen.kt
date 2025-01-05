@file:OptIn(ExperimentalMaterial3Api::class)

package com.callcenter.smartclass.ui.home.article

//noinspection UsingMaterialAndMaterial3Libraries
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.selection.TextSelectionColors
import androidx.compose.material.ContentAlpha
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import coil.compose.SubcomposeAsyncImage
import coil.request.ImageRequest
import com.callcenter.smartclass.ui.components.smartclassCard
import com.callcenter.smartclass.ui.home.article.viewmodel.list.ArticleViewModel
import com.callcenter.smartclass.ui.home.article.viewmodel.BookmarkViewModel
import com.callcenter.smartclass.ui.theme.*
import com.callcenter.smartclass.ui.theme.ButtonPressedLight
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun ArticleListScreen(
    viewModel: ArticleViewModel = viewModel(),
    bookmarkViewModel: BookmarkViewModel = viewModel(),
    navController: NavHostController,
    onArticleClick: (String) -> Unit,
    onBack: () -> Unit
) {
    val articles by viewModel.articles.collectAsState()
    val filteredArticles by viewModel.filteredArticles.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val selectedCategory by viewModel.selectedCategory.collectAsState()

    val bookmarkedArticles by bookmarkViewModel.bookmarkedArticles.collectAsState()

    val backgroundColor = if (isSystemInDarkTheme()) MinimalBackgroundDark else MinimalBackgroundLight
    val textColor = if (isSystemInDarkTheme()) MinimalTextDark else MinimalTextLight

    fun formatTimestampcostum(timestamp: Long): String {
        val date = Date(timestamp)
        val sdf = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
        return sdf.format(date)
    }

    val isLight = !smartclassTheme.colors.isDark

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Daftar Artikel", color = textColor) },
                navigationIcon = {
                    IconButton(onClick = { onBack() }) {
                        @Suppress("DEPRECATION")
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Kembali", tint = if (isLight) DarkBlue else LightBlue)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = backgroundColor)
            )
        },
        content = { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(backgroundColor)
                    .padding(paddingValues)
                    .padding(16.dp)
            ) {

                CustomSearchBar(
                    searchQuery = searchQuery,
                    onSearchQueryChange = { query -> viewModel.updateSearchQuery(query) },
                    onBookmarkClick = { navController.navigate("bookmark") }
                )

                Spacer(modifier = Modifier.height(16.dp))

                LazyRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    item {
                        CategoryChip(
                            categoryName = "Semua",
                            isSelected = selectedCategory.isEmpty(),
                            onClick = { viewModel.selectCategory("") }
                        )
                    }

                    val categories = articles.map { it.category }.distinct().filter { it.isNotEmpty() }

                    items(categories) { category ->
                        CategoryChip(
                            categoryName = category,
                            isSelected = selectedCategory == category,
                            onClick = { viewModel.selectCategory(category) }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Box(modifier = Modifier.fillMaxSize()) {
                    when {
                        isLoading -> {
                            CircularProgressIndicator(
                                modifier = Modifier.align(Alignment.Center),
                                color = if (isSystemInDarkTheme()) Ocean4 else Ocean7
                            )
                        }
                        errorMessage != null -> {
                            Text(
                                text = errorMessage ?: "Terjadi kesalahan.",
                                style = MaterialTheme.typography.bodyMedium.copy(color = textColor),
                                modifier = Modifier.align(Alignment.Center),
                                textAlign = TextAlign.Center
                            )
                        }
                        articles.isEmpty() -> {
                            Text(
                                text = "Tidak ada artikel tersedia.",
                                style = MaterialTheme.typography.bodyMedium.copy(color = textColor),
                                modifier = Modifier.align(Alignment.Center),
                                textAlign = TextAlign.Center
                            )
                        }
                        else -> {
                            LazyColumn(
                                modifier = Modifier.fillMaxSize(),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                // Featured Article Section
                                item {
                                    LazyRow(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        items(articles) { article ->
                                            val topics = article.topic.split(",").map { it.trim() }

                                            smartclassCard(
                                                modifier = Modifier
                                                    .width(300.dp)
                                                    .clip(RoundedCornerShape(8.dp))
                                                    .clickable {
                                                        viewModel.incrementViewCount(article.uuid)
                                                        onArticleClick(article.uuid)
                                                    },
                                                shape = RoundedCornerShape(8.dp)
                                            ) {
                                                Box(
                                                    modifier = Modifier
                                                        .fillMaxWidth()
                                                        .height(200.dp)
                                                ) {
                                                    SubcomposeAsyncImage(
                                                        model = ImageRequest.Builder(LocalContext.current)
                                                            .data(article.thumbnailUrl)
                                                            .crossfade(true)
                                                            .build(),
                                                        contentDescription = "Image for ${article.title}",
                                                        contentScale = ContentScale.Crop,
                                                        modifier = Modifier.fillMaxSize()
                                                    )
                                                    Box(
                                                        modifier = Modifier
                                                            .fillMaxSize()
                                                            .background(Color.Black.copy(alpha = 0.3f))
                                                            .padding(16.dp)
                                                    ) {
                                                        Column(
                                                            modifier = Modifier.fillMaxSize(),
                                                            verticalArrangement = Arrangement.SpaceBetween
                                                        ) {
                                                            // Display topics at the top
                                                            Row(
                                                                modifier = Modifier
                                                                    .fillMaxWidth()
                                                                    .wrapContentHeight(),
                                                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                                                            ) {
                                                                topics.forEach { topic ->
                                                                    Box(
                                                                        modifier = Modifier
                                                                            .background(
                                                                                color = if (isSystemInDarkTheme()) {
                                                                                    ButtonPressedDark.copy(alpha = 0.3f)
                                                                                } else {
                                                                                    ButtonPressedLight.copy(alpha = 0.3f)
                                                                                },
                                                                                shape = RoundedCornerShape(4.dp)
                                                                            )
                                                                            .border(
                                                                                border = BorderStroke(
                                                                                    1.dp,
                                                                                    if (isSystemInDarkTheme()) Ocean4.copy(alpha = 0.3f) else Ocean7.copy(alpha = 0.3f)
                                                                                ),
                                                                                shape = RoundedCornerShape(4.dp)
                                                                            )
                                                                            .padding(horizontal = 8.dp, vertical = 4.dp)
                                                                    ) {
                                                                        Text(
                                                                            text = topic,
                                                                            style = MaterialTheme.typography.bodyMedium.copy(color = Color.White)
                                                                        )
                                                                    }
                                                                }
                                                            }
                                                            // Display title at the bottom
                                                            Text(
                                                                text = article.title,
                                                                style = MaterialTheme.typography.bodyLarge.copy(
                                                                    color = Color.White,
                                                                    shadow = Shadow(color = Color.Black, blurRadius = 4f)
                                                                ),
                                                                maxLines = 2,
                                                                overflow = TextOverflow.Ellipsis,
                                                                modifier = Modifier.align(Alignment.Start)
                                                            )
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                    Spacer(modifier = Modifier.height(16.dp))
                                }

                                // Article List Section
                                items(filteredArticles) { article ->
                                    smartclassCard(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clip(RoundedCornerShape(8.dp))
                                            .clickable {
                                                viewModel.incrementViewCount(article.uuid)
                                                onArticleClick(article.uuid)
                                            },
                                        shape = RoundedCornerShape(8.dp)
                                    ) {
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(8.dp)
                                        ) {
                                            SubcomposeAsyncImage(
                                                model = ImageRequest.Builder(LocalContext.current)
                                                    .data(article.thumbnailUrl)
                                                    .crossfade(true)
                                                    .build(),
                                                contentDescription = "Image for ${article.title}",
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
                                                    article.title,
                                                    style = MaterialTheme.typography.bodyMedium.copy(color = textColor),
                                                    maxLines = 2,
                                                    overflow = TextOverflow.Ellipsis
                                                )
                                                Spacer(modifier = Modifier.height(4.dp))
                                                Row(
                                                    verticalAlignment = Alignment.CenterVertically
                                                ) {
                                                    // Tanggal
                                                    Text(
                                                        text = formatTimestampcostum(article.timestamp),
                                                        style = MaterialTheme.typography.bodySmall.copy(color = textColor.copy(alpha = 0.7f)),
                                                        modifier = Modifier.padding(end = 8.dp)
                                                    )

                                                    Spacer(modifier = Modifier.width(4.dp))
                                                    // View count
                                                    Icon(
                                                        imageVector = Icons.Filled.Visibility,
                                                        contentDescription = "Jumlah Dilihat",
                                                        tint = textColor.copy(alpha = 0.7f),
                                                        modifier = Modifier.size(16.dp)
                                                    )
                                                    Spacer(modifier = Modifier.width(4.dp))
                                                    Text(
                                                        "${article.viewCount}x",
                                                        style = MaterialTheme.typography.bodySmall.copy(color = textColor.copy(alpha = 0.7f))
                                                    )

                                                    Spacer(modifier = Modifier.width(16.dp))
                                                    // Love count
                                                    Icon(
                                                        imageVector = Icons.Filled.Favorite,
                                                        contentDescription = "Jumlah Love",
                                                        tint = textColor.copy(alpha = 0.7f),
                                                        modifier = Modifier.size(16.dp)
                                                    )
                                                    Spacer(modifier = Modifier.width(4.dp))
                                                    Text(
                                                        "${article.loveCount}",
                                                        style = MaterialTheme.typography.bodySmall.copy(color = textColor.copy(alpha = 0.7f))
                                                    )

                                                    Spacer(modifier = Modifier.width(16.dp))
                                                    // Bookmark Icon
                                                    val isBookmarked = bookmarkedArticles.any { it.uuid == article.uuid }
                                                    IconButton(
                                                        onClick = {
                                                            bookmarkViewModel.toggleBookmark(article)
                                                        }
                                                    ) {
                                                        Icon(
                                                            imageVector = if (isBookmarked) Icons.Filled.Bookmark else Icons.Default.BookmarkBorder,
                                                            contentDescription = if (isBookmarked) "Unbookmark" else "Bookmark",
                                                            tint = if (isBookmarked) Ocean7 else textColor.copy(alpha = 0.7f)
                                                        )
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    )
}

@Composable
fun CategoryChip(
    categoryName: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val backgroundColor = if (isSelected) Ocean7 else Color.Gray.copy(alpha = 0.3f)
    val textColor = if (isSelected) Color.White else Ocean7

    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(20.dp))
            .background(backgroundColor)
            .clickable { onClick() }
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Text(
            text = categoryName,
            style = MaterialTheme.typography.bodySmall.copy(color = textColor),
            modifier = Modifier.align(Alignment.Center)
        )
    }
}

@Composable
fun CustomSearchBar(
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    onBookmarkClick: () -> Unit,
) {
    OutlinedTextField(
        value = searchQuery,
        onValueChange = { onSearchQueryChange(it) },
        placeholder = {
            Text(
                text = "Cari Artikel...",
                style = MaterialTheme.typography.bodyMedium,
                color = if (isSystemInDarkTheme()) WhiteColor else DarkText.copy(alpha = 0.5f)
            )
        },
        leadingIcon = {
            Icon(
                imageVector = Icons.Default.Search,
                contentDescription = "Search Icon",
                tint = if (isSystemInDarkTheme()) WhiteColor else DarkText.copy(alpha = 0.5f)
            )
        },
        trailingIcon = {
            IconButton(onClick = { onBookmarkClick() }) {
                Icon(
                    imageVector = Icons.Default.Bookmark,
                    contentDescription = "Bookmark",
                    tint = if (isSystemInDarkTheme()) WhiteColor else DarkText.copy(alpha = 0.5f)
                )
            }
        },
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp)
            .clip(RoundedCornerShape(24.dp))
            .background(if (isSystemInDarkTheme()) DarkText else WhiteColor),
        singleLine = true,
        colors = OutlinedTextFieldDefaults.colors(
            focusedTextColor = if (isSystemInDarkTheme()) WhiteColor else DarkText,
            unfocusedTextColor = if (isSystemInDarkTheme()) WhiteColor else DarkText,
            disabledTextColor = if (isSystemInDarkTheme()) WhiteColor else DarkText.copy(alpha = ContentAlpha.disabled),
            errorTextColor = MaterialTheme.colorScheme.error,
            cursorColor = if (isSystemInDarkTheme()) WhiteColor else DarkText,
            selectionColors = TextSelectionColors(
                handleColor = if (isSystemInDarkTheme()) WhiteColor else DarkText,
                backgroundColor = if (isSystemInDarkTheme()) WhiteColor else DarkText.copy(alpha = 0.4f)
            ),
            focusedBorderColor = Color.Transparent,
            unfocusedBorderColor = Color.Transparent,
            disabledBorderColor = Color.Transparent,
            errorBorderColor = Color.Transparent,
            focusedLabelColor = if (isSystemInDarkTheme()) WhiteColor else DarkText,
            unfocusedLabelColor = if (isSystemInDarkTheme()) WhiteColor else DarkText.copy(alpha = 0.5f),
            disabledLabelColor = if (isSystemInDarkTheme()) WhiteColor else DarkText.copy(alpha = ContentAlpha.disabled),
            errorLabelColor = MaterialTheme.colorScheme.error,
            focusedContainerColor = Color.Transparent,
            unfocusedContainerColor = Color.Transparent,
            disabledContainerColor = Color.Transparent,
            errorContainerColor = Color.Transparent
        )
    )
}
