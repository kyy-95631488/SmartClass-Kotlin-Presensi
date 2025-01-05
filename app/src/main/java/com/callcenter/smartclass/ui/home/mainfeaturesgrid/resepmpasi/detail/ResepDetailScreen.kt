package com.callcenter.smartclass.ui.home.mainfeaturesgrid.resepmpasi.detail

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
import androidx.compose.material.icons.filled.Favorite
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
import com.callcenter.smartclass.ui.home.article.Base64ImageGetter
import com.callcenter.smartclass.ui.home.mainfeaturesgrid.resepmpasi.viewmodel.BookmarkResepViewModel
import com.callcenter.smartclass.ui.home.mainfeaturesgrid.resepmpasi.viewmodel.ResepDetailViewModel
import com.callcenter.smartclass.ui.theme.*
import com.google.firebase.auth.FirebaseAuth
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ResepDetailScreen(
    uuid: String,
    viewModel: ResepDetailViewModel = viewModel(),
    bookmarkViewModel: BookmarkResepViewModel = viewModel(),
    onBack: () -> Unit
) {
    val userId = FirebaseAuth.getInstance().currentUser?.uid ?: ""

    val recipe by viewModel.recipe.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()

    val scrollState = rememberScrollState()
    var isFloatingVisible by remember { mutableStateOf(true) }
    var lastScrollPosition by remember { mutableStateOf(0) }

    val currentRecipe = recipe

    val userHasLoved = currentRecipe?.lovedBy?.contains(userId) == true

    val bookmarkedRecipes by bookmarkViewModel.bookmarkedRecipes.collectAsState()
    val isBookmarked = currentRecipe?.let { recipeNonNull ->
        bookmarkedRecipes.any { it.uuid == recipeNonNull.uuid }
    } ?: false

    LaunchedEffect(key1 = uuid) {
        viewModel.fetchRecipeByUuid(uuid)
    }

    LaunchedEffect(scrollState.value) {
        val currentOffset = scrollState.value
        isFloatingVisible = when {
            currentOffset > lastScrollPosition -> false
            currentOffset < lastScrollPosition -> true
            else -> isFloatingVisible
        }
        lastScrollPosition = currentOffset
    }

    val backgroundColor = if (isSystemInDarkTheme()) MinimalBackgroundDark else MinimalBackgroundLight
    val textColor = if (isSystemInDarkTheme()) MinimalTextDark else MinimalTextLight

    val isLight = !smartclassTheme.colors.isDark

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(text = "Detail Resep", color = textColor) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
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
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(color = Ocean4)
                        }
                    }
                    errorMessage != null -> {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = errorMessage ?: "Terjadi kesalahan.",
                                style = MaterialTheme.typography.bodyMedium.copy(color = textColor),
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(16.dp)
                            )
                        }
                    }
                    currentRecipe != null -> {
                        Box(
                            modifier = Modifier.fillMaxSize()
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .verticalScroll(scrollState)
                                    .padding(16.dp)
                            ) {
                                SubcomposeAsyncImage(
                                    model = ImageRequest.Builder(LocalContext.current)
                                        .data(currentRecipe.thumbnailUrl)
                                        .crossfade(true)
                                        .build(),
                                    contentDescription = "Image for ${currentRecipe.title}",
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

                                Text(
                                    text = currentRecipe.title,
                                    style = MaterialTheme.typography.headlineMedium.copy(color = textColor),
                                    modifier = Modifier.fillMaxWidth()
                                )

                                Spacer(modifier = Modifier.height(8.dp))

                                Text(
                                    text = "Topik: ${currentRecipe.topic}",
                                    style = MaterialTheme.typography.bodySmall.copy(color = Color.Gray),
                                    modifier = Modifier.fillMaxWidth()
                                )

                                Spacer(modifier = Modifier.height(4.dp))

                                Text(
                                    text = "Diterbitkan pada: ${formatTimestamp(currentRecipe.timestamp)}",
                                    style = MaterialTheme.typography.bodySmall.copy(color = Color.Gray),
                                    modifier = Modifier.fillMaxWidth()
                                )

                                Spacer(modifier = Modifier.height(16.dp))

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
                                        val htmlContent = currentRecipe.content
                                        val imageGetter = Base64ImageGetter(textView)
                                        val styledText = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                                            Html.fromHtml(
                                                htmlContent,
                                                Html.FROM_HTML_MODE_LEGACY,
                                                imageGetter,
                                                null
                                            )
                                        } else {
                                            @Suppress("DEPRECATION")
                                            Html.fromHtml(htmlContent, imageGetter, null)
                                        }
                                        textView.text = styledText
                                        textView.invalidate()
                                    }
                                )
                            }

                            AnimatedVisibility(
                                visible = isFloatingVisible,
                                enter = slideInVertically { it },
                                exit = slideOutVertically { it },
                                modifier = Modifier
                                    .align(Alignment.BottomCenter)
                                    .padding(16.dp)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(16.dp)) // mengecilkan corner dari 24.dp ke 16.dp
                                        .background(
                                            if (isSystemInDarkTheme()) MinimalBackgroundLight else MinimalBackgroundDark
                                        )
                                        .padding(horizontal = 8.dp, vertical = 4.dp), // mengecilkan padding
                                    horizontalArrangement = Arrangement.Center,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    IconButton(
                                        onClick = { viewModel.updateLoveCount(uuid, userId) },
                                        modifier = Modifier.size(36.dp) // ukuran IconButton lebih kecil, misal 36.dp
                                    ) {
                                        Icon(
                                            imageVector = if (userHasLoved) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                                            contentDescription = "Love",
                                            tint = if (userHasLoved) Ocean8 else Color.Gray,
                                            modifier = Modifier.size(20.dp) // mengecilkan ukuran icon
                                        )
                                    }

                                    Spacer(modifier = Modifier.width(8.dp)) // mengecilkan jarak antar ikon

                                    IconButton(
                                        onClick = { bookmarkViewModel.toggleBookmark(currentRecipe) },
                                        modifier = Modifier.size(36.dp) // ukuran IconButton lebih kecil
                                    ) {
                                        Icon(
                                            imageVector = if (isBookmarked) Icons.Filled.Bookmark else Icons.Outlined.BookmarkBorder,
                                            contentDescription = if (isBookmarked) "Unbookmark" else "Bookmark",
                                            tint = if (isBookmarked) Ocean8 else Color.Gray,
                                            modifier = Modifier.size(20.dp) // mengecilkan ukuran icon
                                        )
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
fun formatTimestamp(timestamp: Long): String {
    val sdf = remember { SimpleDateFormat("dd MMM yyyy HH:mm", Locale.getDefault()) }
    return remember(timestamp) { sdf.format(Date(timestamp)) }
}
