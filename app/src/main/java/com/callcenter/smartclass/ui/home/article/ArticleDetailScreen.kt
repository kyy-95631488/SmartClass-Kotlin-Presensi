package com.callcenter.smartclass.ui.home.article

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
import com.callcenter.smartclass.ui.home.article.viewmodel.ArticleDetailViewModel
import com.callcenter.smartclass.ui.home.article.viewmodel.BookmarkViewModel
import com.callcenter.smartclass.ui.theme.DarkBlue
import com.callcenter.smartclass.ui.theme.smartclassTheme
import com.callcenter.smartclass.ui.theme.LightBlue
import com.callcenter.smartclass.ui.theme.MinimalBackgroundDark
import com.callcenter.smartclass.ui.theme.MinimalBackgroundLight
import com.callcenter.smartclass.ui.theme.MinimalTextDark
import com.callcenter.smartclass.ui.theme.MinimalTextLight
import com.callcenter.smartclass.ui.theme.Ocean4
import com.callcenter.smartclass.ui.theme.Ocean8
import com.google.firebase.auth.FirebaseAuth
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ArticleDetailScreen(
    uuid: String,
    viewModel: ArticleDetailViewModel = viewModel(),
    bookmarkViewModel: BookmarkViewModel = viewModel(),
    onBack: () -> Unit
) {
    val userId = FirebaseAuth.getInstance().currentUser?.uid ?: ""

    // State holders
    val article by viewModel.article.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()

    // Scroll and visibility states
    val scrollState = rememberScrollState()
    var isFloatingVisible by remember { mutableStateOf(true) }
    var lastScrollPosition by remember { mutableStateOf(0) }

    val userHasLoved = article?.lovedBy?.contains(userId) == true

    // Collect bookmarked articles
    val bookmarkedArticles by bookmarkViewModel.bookmarkedArticles.collectAsState()
    val isBookmarked = article?.let { bookmarkedArticles.any { it.uuid == it.uuid } } ?: false

    // Fetch article when UUID changes
    LaunchedEffect(key1 = uuid) {
        viewModel.fetchArticleByUuid(uuid)
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
                title = { Text(text = "Detail Artikel", color = textColor) },
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
                                style = MaterialTheme.typography.bodyMedium.copy(color = textColor),
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(16.dp)
                            )
                        }
                    }
                    article != null -> {
                        // Main Content with Floating Bar
                        Box(
                            modifier = Modifier.fillMaxSize()
                        ) {
                            // Scrollable Article Content
                            Column(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .verticalScroll(scrollState)
                                    .padding(16.dp)
                            ) {
                                // Article Image
                                SubcomposeAsyncImage(
                                    model = ImageRequest.Builder(LocalContext.current)
                                        .data(article!!.thumbnailUrl)
                                        .crossfade(true)
                                        .build(),
                                    contentDescription = "Image for ${article!!.title}",
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

                                // Article Title
                                Text(
                                    text = article!!.title,
                                    style = MaterialTheme.typography.headlineMedium.copy(color = textColor),
                                    modifier = Modifier.fillMaxWidth()
                                )

                                Spacer(modifier = Modifier.height(8.dp))

                                // Article Topic
                                Text(
                                    text = "Topik: ${article!!.topic}",
                                    style = MaterialTheme.typography.bodySmall.copy(color = Color.Gray),
                                    modifier = Modifier.fillMaxWidth()
                                )

                                Spacer(modifier = Modifier.height(4.dp))

                                // Article Timestamp
                                Text(
                                    text = "Diterbitkan pada: ${formatTimestamp(article!!.timestamp)}",
                                    style = MaterialTheme.typography.bodySmall.copy(color = Color.Gray),
                                    modifier = Modifier.fillMaxWidth()
                                )

                                Spacer(modifier = Modifier.height(16.dp))

                                // Article Content using AndroidView
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
                                        val htmlContent = article!!.content
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

                            // Floating Bar dengan Fungsionalitas Love dan Bookmark
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
                                        .clip(RoundedCornerShape(16.dp)) // Mengecilkan dari 24.dp ke 16.dp
                                        .background(
                                            if (isSystemInDarkTheme()) MinimalBackgroundLight else MinimalBackgroundDark
                                        )
                                        .padding(horizontal = 8.dp, vertical = 4.dp), // Mengecilkan padding dari 12.dp & 8.dp ke 8.dp & 4.dp
                                    horizontalArrangement = Arrangement.Center,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    // Tombol Love
                                    IconButton(onClick = { viewModel.updateLoveCount(uuid, userId) }) {
                                        Icon(
                                            imageVector = if (userHasLoved) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                                            contentDescription = "Love",
                                            tint = if (userHasLoved) Ocean8 else Color.Gray,
                                            modifier = Modifier.size(20.dp) // Mengecilkan ukuran ikon, misalnya dari default ke 20.dp
                                        )
                                    }

                                    Spacer(modifier = Modifier.width(8.dp)) // Mengecilkan jarak antar ikon dari 16.dp ke 8.dp

                                    // Tombol Bookmark
                                    IconButton(onClick = {
                                        val currentArticle = article ?: return@IconButton
                                        bookmarkViewModel.toggleBookmark(currentArticle)
                                    }) {
                                        Icon(
                                            imageVector = if (isBookmarked) Icons.Filled.Bookmark else Icons.Outlined.BookmarkBorder,
                                            contentDescription = if (isBookmarked) "Unbookmark" else "Bookmark",
                                            tint = if (isBookmarked) Ocean8 else Color.Gray,
                                            modifier = Modifier.size(20.dp) // Mengecilkan ukuran ikon bookmark
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
