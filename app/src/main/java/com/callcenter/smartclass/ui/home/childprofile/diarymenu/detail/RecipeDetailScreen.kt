package com.callcenter.smartclass.ui.home.childprofile.diarymenu.detail

import android.os.Build
import android.text.Html
import android.text.method.LinkMovementMethod
import android.view.ViewGroup
import android.widget.TextView
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Cake
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.outlined.BookmarkBorder
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.SubcomposeAsyncImage
import coil.request.ImageRequest
import com.callcenter.smartclass.R
import com.callcenter.smartclass.ui.theme.DarkBlue
import com.callcenter.smartclass.ui.theme.smartclassTheme
import com.callcenter.smartclass.ui.theme.LightBlue
import com.callcenter.smartclass.ui.theme.MinimalBackgroundDark
import com.callcenter.smartclass.ui.theme.MinimalBackgroundLight
import com.callcenter.smartclass.ui.theme.MinimalTextDark
import com.callcenter.smartclass.ui.theme.MinimalTextLight
import com.callcenter.smartclass.ui.theme.Ocean4
import com.callcenter.smartclass.ui.theme.Ocean8
import com.callcenter.smartclass.ui.ui.home.childprofile.diarymenu.viewmodel.RecipeDetailViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecipeDetailScreen(
    recipeId: String?,
    onBack: () -> Unit
) {
    if (recipeId == null) {
        Text("Resep tidak ditemukan.")
        return
    }

    val viewModel: RecipeDetailViewModel = viewModel()
    val recipeDetail by viewModel.recipeDetail.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()

    // Scroll and visibility states
    val scrollState = rememberScrollState()
    var isFloatingVisible by remember { mutableStateOf(true) }
    var lastScrollPosition by remember { mutableStateOf(0) }

    // Example user interaction states
    val userHasFavorited by viewModel.userHasFavorited.collectAsState(initial = false)
    val isBookmarked by viewModel.isBookmarked.collectAsState(initial = false)

    // Fetch recipe detail when recipeId changes
    LaunchedEffect(recipeId) {
        viewModel.loadRecipeDetail(recipeId)
    }

    // Handle scroll to show/hide floating bar
    LaunchedEffect(scrollState.value) {
        val currentOffset = scrollState.value
        isFloatingVisible = when {
            currentOffset > lastScrollPosition -> false
            currentOffset < lastScrollPosition -> true
            else -> isFloatingVisible
        }
        lastScrollPosition = currentOffset
    }

    // Theme colors
    val backgroundColor = if (isSystemInDarkTheme()) MinimalBackgroundDark else MinimalBackgroundLight
    val textColor = if (isSystemInDarkTheme()) MinimalTextDark else MinimalTextLight

    val isLight = !smartclassTheme.colors.isDark

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(text = "Detail Resep", color = textColor) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        @Suppress("DEPRECATION")
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back",
                            tint = if (isLight) DarkBlue else LightBlue
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = backgroundColor)
            )
        },
        content = { paddingValues ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(backgroundColor)
                    .padding(paddingValues)
            ) {
                when {
                    isLoading -> {
                        // Loading Indicator
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(color = Ocean4)
                        }
                    }
                    errorMessage != null -> {
                        // Error Message
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = errorMessage ?: "Terjadi kesalahan.",
                                style = MaterialTheme.typography.bodyLarge.copy(color = textColor),
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(16.dp)
                            )
                        }
                    }
                    recipeDetail != null -> {
                        // Main Content with Floating Bar
                        Box(
                            modifier = Modifier.fillMaxSize()
                        ) {
                            // Scrollable Recipe Content
                            Column(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .verticalScroll(scrollState)
                                    .padding(16.dp)
                            ) {
                                // Recipe Image
                                SubcomposeAsyncImage(
                                    model = ImageRequest.Builder(LocalContext.current)
                                        .data(recipeDetail!!.thumbnailUrl)
                                        .crossfade(true)
                                        .build(),
                                    contentDescription = "Thumbnail",
                                    loading = {
                                        Box(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .height(200.dp)
                                                .clip(RoundedCornerShape(8.dp))
                                                .background(Color.Gray.copy(alpha = 0.2f)),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            CircularProgressIndicator(
                                                color = Ocean4,
                                                modifier = Modifier.size(24.dp)
                                            )
                                        }
                                    },
                                    error = {
                                        Image(
                                            painter = painterResource(id = R.drawable.empty_state_search),
                                            contentDescription = "Placeholder Image",
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .height(200.dp)
                                                .clip(RoundedCornerShape(8.dp)),
                                            contentScale = ContentScale.Crop
                                        )
                                    },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(200.dp)
                                        .clip(RoundedCornerShape(8.dp)),
                                    contentScale = ContentScale.Crop
                                )

                                Spacer(modifier = Modifier.height(16.dp))

                                // Recipe Title
                                Text(
                                    text = recipeDetail!!.title,
                                    style = MaterialTheme.typography.headlineMedium.copy(color = textColor),
                                    modifier = Modifier.fillMaxWidth()
                                )

                                Spacer(modifier = Modifier.height(8.dp))

                                // Recipe Category
                                // Tabel informasi resep
                                // Tabel informasi resep
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(Color.Gray.copy(alpha = 0.1f))
                                        .padding(8.dp)
                                ) {
                                    // Baris 1: Kategori dan Waktu Masak
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        // Kategori dengan Ikon
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            modifier = Modifier.weight(1f)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Category,
                                                contentDescription = "Kategori",
                                                tint = textColor,
                                                modifier = Modifier.size(20.dp)
                                            )
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text(
                                                text = "Kategori",
                                                style = MaterialTheme.typography.bodyMedium.copy(color = textColor)
                                            )
                                        }

                                        // Waktu Masak dengan Ikon
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            modifier = Modifier.weight(1f)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Schedule,
                                                contentDescription = "Waktu Masak",
                                                tint = textColor,
                                                modifier = Modifier.size(20.dp)
                                            )
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text(
                                                text = "Waktu Masak",
                                                style = MaterialTheme.typography.bodyMedium.copy(color = textColor)
                                            )
                                        }
                                    }

                                    Divider(color = Color.Gray.copy(alpha = 0.5f), thickness = 1.dp, modifier = Modifier.padding(vertical = 4.dp))

                                    // Baris 2: Isi untuk Kategori dan Waktu Masak
                                    Row(
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Text(
                                            text = "${recipeDetail!!.category}",
                                            style = MaterialTheme.typography.bodyMedium.copy(color = textColor),
                                            modifier = Modifier.weight(1f)
                                        )
                                        Text(
                                            text = "${recipeDetail!!.cookingTime}",
                                            style = MaterialTheme.typography.bodyMedium.copy(color = textColor),
                                            modifier = Modifier.weight(1f)
                                        )
                                    }

                                    Spacer(modifier = Modifier.height(8.dp))

                                    // Baris 3: Usia dan Porsi
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        // Usia dengan Ikon
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            modifier = Modifier.weight(1f)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Cake,
                                                contentDescription = "Usia",
                                                tint = textColor,
                                                modifier = Modifier.size(20.dp)
                                            )
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text(
                                                text = "Usia",
                                                style = MaterialTheme.typography.bodyMedium.copy(color = textColor)
                                            )
                                        }

                                        // Porsi dengan Ikon
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            modifier = Modifier.weight(1f)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.People,
                                                contentDescription = "Porsi",
                                                tint = textColor,
                                                modifier = Modifier.size(20.dp)
                                            )
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text(
                                                text = "Porsi",
                                                style = MaterialTheme.typography.bodyMedium.copy(color = textColor)
                                            )
                                        }
                                    }

                                    Divider(color = Color.Gray.copy(alpha = 0.5f), thickness = 1.dp, modifier = Modifier.padding(vertical = 4.dp))

                                    // Baris 4: Isi untuk Usia dan Porsi
                                    Row(
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Text(
                                            text = "${recipeDetail!!.age} tahun",
                                            style = MaterialTheme.typography.bodyMedium.copy(color = textColor),
                                            modifier = Modifier.weight(1f)
                                        )
                                        Text(
                                            text = "${recipeDetail!!.servings} orang",
                                            style = MaterialTheme.typography.bodyMedium.copy(color = textColor),
                                            modifier = Modifier.weight(1f)
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(8.dp))

                                // Fun Facts
                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 8.dp),
                                    shape = RoundedCornerShape(8.dp),
                                    colors = CardDefaults.cardColors(
                                        containerColor = if (isSystemInDarkTheme()) MinimalBackgroundDark else MinimalBackgroundLight
                                    ),
                                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                                ) {
                                    Text(
                                        text = "Fun Facts: ${recipeDetail!!.funFacts}",
                                        style = MaterialTheme.typography.bodyMedium.copy(color = textColor),
                                        modifier = Modifier
                                            .padding(16.dp)
                                            .fillMaxWidth()
                                    )
                                }

                                Spacer(modifier = Modifier.height(16.dp))

                                // Recipe Details Title
                                Text(
                                    text = "Detail Resep",
                                    style = MaterialTheme.typography.titleMedium.copy(color = textColor),
                                    modifier = Modifier.fillMaxWidth()
                                )

                                Spacer(modifier = Modifier.height(8.dp))

                                // Recipe Content using AndroidView for rich text
                                AndroidView(
                                    modifier = Modifier.fillMaxWidth(),
                                    factory = { context ->
                                        TextView(context).apply {
                                            setTextColor(textColor.toArgb())
                                            movementMethod = LinkMovementMethod.getInstance()
                                            setPadding(0, 0, 0, 0)
                                            layoutParams = ViewGroup.LayoutParams(
                                                ViewGroup.LayoutParams.MATCH_PARENT,
                                                ViewGroup.LayoutParams.WRAP_CONTENT
                                            )
                                        }
                                    },
                                    update = { textView ->
                                        val htmlContent = recipeDetail!!.content
                                        val styledText = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                                            Html.fromHtml(htmlContent, Html.FROM_HTML_MODE_LEGACY)
                                        } else {
                                            @Suppress("DEPRECATION")
                                            Html.fromHtml(htmlContent)
                                        }
                                        textView.text = styledText
                                        textView.invalidate()
                                    }
                                )
                            }

                            // Floating Action Bar with Favorite and Bookmark
//                            AnimatedVisibility(
//                                visible = isFloatingVisible,
//                                enter = slideInVertically { it },
//                                exit = slideOutVertically { it },
//                                modifier = Modifier
//                                    .align(Alignment.BottomCenter)
//                                    .padding(16.dp)
//                            ) {
//                                Row(
//                                    modifier = Modifier
//                                        .clip(RoundedCornerShape(24.dp))
//                                        .background(
//                                            if (isSystemInDarkTheme()) MinimalBackgroundLight else MinimalBackgroundDark
//                                        )
//                                        .padding(horizontal = 12.dp, vertical = 8.dp),
//                                    horizontalArrangement = Arrangement.Center,
//                                    verticalAlignment = Alignment.CenterVertically
//                                ) {
//                                    // Favorite Button
//                                    IconButton(onClick = { viewModel.toggleFavorite(recipeId) }) {
//                                        Icon(
//                                            imageVector = if (userHasFavorited) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
//                                            contentDescription = "Favorite",
//                                            tint = if (userHasFavorited) Ocean8 else Color.Gray
//                                        )
//                                    }
//
//                                    Spacer(modifier = Modifier.width(16.dp))
//
//                                    // Bookmark Button
//                                    IconButton(onClick = { viewModel.toggleBookmark(recipeId) }) {
//                                        Icon(
//                                            imageVector = if (isBookmarked) Icons.Filled.Bookmark else Icons.Outlined.BookmarkBorder,
//                                            contentDescription = if (isBookmarked) "Unbookmark" else "Bookmark",
//                                            tint = if (isBookmarked) Ocean8 else Color.Gray
//                                        )
//                                    }
//                                }
//                            }
                        }
                    }
                    else -> {
                        // Fallback UI if needed
                        Text("Tidak ada data tersedia.", modifier = Modifier.align(Alignment.Center))
                    }
                }
            }
        }
    )
}