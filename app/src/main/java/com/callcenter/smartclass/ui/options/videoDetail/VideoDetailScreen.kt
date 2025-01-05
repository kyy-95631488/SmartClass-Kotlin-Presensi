package com.callcenter.smartclass.ui.options.videoDetail

import android.annotation.SuppressLint
import android.app.Activity
import android.content.pm.ActivityInfo
import android.os.Build
import android.text.Spanned
import androidx.activity.compose.BackHandler
import androidx.annotation.RequiresApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.zIndex
import androidx.core.text.HtmlCompat
import androidx.core.view.WindowCompat
import coil.compose.rememberAsyncImagePainter
import coil.request.ImageRequest
import com.callcenter.smartclass.R
import com.callcenter.smartclass.data.*
import com.callcenter.smartclass.ui.components.smartclassCard
import com.callcenter.smartclass.ui.components.smartclassDivider
import com.callcenter.smartclass.ui.theme.smartclassTheme.colors
import com.callcenter.smartclass.ui.theme.Ocean8
import com.callcenter.smartclass.ui.theme.Ocean9
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.YouTubePlayer
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.listeners.AbstractYouTubePlayerListener
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.views.YouTubePlayerView
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

@SuppressLint("SourceLockedOrientationActivity")
@RequiresApi(Build.VERSION_CODES.R)
@Composable
fun VideoDetailScreen(videoId: String, onBack: () -> Unit) {
    val context = LocalContext.current
    val activity = remember(context) { context as? Activity }

    var isFullscreen by rememberSaveable { mutableStateOf(false) }

    var videoItem by remember { mutableStateOf<VideoItem?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    var comments by remember { mutableStateOf<List<Comment>>(emptyList()) }
    var channelAvatarUrl by remember { mutableStateOf<String?>(null) }
    var isLoadingComments by remember { mutableStateOf(true) }
    var isLoadingChannel by remember { mutableStateOf(true) }
    var errorComments by remember { mutableStateOf<String?>(null) }
    var errorChannel by remember { mutableStateOf<String?>(null) }

    val youTubePlayerView = remember { YouTubePlayerView(context) }

    DisposableEffect(Unit) {
        // Add YouTubePlayerView to lifecycle observers
        (context as? androidx.lifecycle.LifecycleOwner)?.lifecycle?.addObserver(youTubePlayerView)

        onDispose {
            youTubePlayerView.release()
        }
    }

    LaunchedEffect(videoId) {
        val apiKey = context.getString(R.string.youtube_api_key)
        fetchVideoDetails(videoId, apiKey) { fetchedVideo, error ->
            if (error != null) {
                errorMessage = error
            } else {
                videoItem = fetchedVideo
                fetchedVideo?.let { video ->
                    fetchComments(video.id.videoId, apiKey) { fetchedComments, commentError ->
                        if (commentError != null) {
                            errorComments = commentError
                        } else {
                            comments = fetchedComments
                        }
                        isLoadingComments = false
                    }

                    fetchChannelDetails(video.snippet.channelId, apiKey) { avatarUrl, channelError ->
                        if (avatarUrl != null) {
                            channelAvatarUrl = avatarUrl
                        } else {
                            errorChannel = channelError
                        }
                        isLoadingChannel = false
                    }
                }
            }
            isLoading = false
        }
    }

    LaunchedEffect(isFullscreen) {
        activity?.let { act ->
            if (isFullscreen) {
                // Lock orientation to landscape
                act.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
                // Hide system UI
                WindowCompat.setDecorFitsSystemWindows(act.window, false)
                act.window.insetsController?.apply {
                    hide(android.view.WindowInsets.Type.statusBars() or android.view.WindowInsets.Type.navigationBars())
                    systemBarsBehavior = android.view.WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
                }
            } else {
                // Unlock orientation to portrait
                act.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
                // Show system UI
                WindowCompat.setDecorFitsSystemWindows(act.window, true)
                act.window.insetsController?.show(android.view.WindowInsets.Type.statusBars() or android.view.WindowInsets.Type.navigationBars())
            }
        }
    }

    // Menangani tombol back
    BackHandler(enabled = !isFullscreen) {
        onBack()
    }

    BackHandler(enabled = isFullscreen) {
        isFullscreen = false
    }

    Box(modifier = Modifier.fillMaxSize()) {
        if (isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = Color(0xFF0E5E6C))
            }
        } else if (errorMessage != null) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(text = errorMessage ?: "Terjadi kesalahan", color = Color.Red, fontSize = 16.sp)
            }
        } else {
            videoItem?.let { video ->
                if (!isFullscreen) {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp)
                    ) {
                        item {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .aspectRatio(16 / 9f)
                                    .padding(bottom = 16.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(Color.Black)
                            ) {
                                AndroidView(factory = { youTubePlayerView }, modifier = Modifier.fillMaxSize()) { playerView ->
                                    playerView.addYouTubePlayerListener(object : AbstractYouTubePlayerListener() {
                                        override fun onReady(youTubePlayer: YouTubePlayer) {
                                            youTubePlayer.loadVideo(video.id.videoId, 0f)
                                        }
                                    })
                                }

                                IconButton(
                                    onClick = { isFullscreen = true },
                                    modifier = Modifier
                                        .align(Alignment.BottomEnd)
                                        .padding(8.dp)
                                        .background(Color.Black.copy(alpha = 0.5f), shape = CircleShape)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Fullscreen,
                                        contentDescription = "Fullscreen",
                                        tint = Color.White
                                    )
                                }
                            }

                            smartclassCard(
                                modifier = Modifier
                                    .padding(bottom = 16.dp)
                                    .fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.padding(bottom = 8.dp)
                                    ) {
                                        if (isLoadingChannel) {
                                            Icon(
                                                imageVector = Icons.Default.AccountCircle,
                                                contentDescription = "User Avatar",
                                                modifier = Modifier
                                                    .size(40.dp)
                                                    .clip(CircleShape)
                                                    .background(Color.Gray.copy(alpha = 0.2f))
                                                    .padding(4.dp),
                                                tint = Color.Gray
                                            )
                                        } else if (errorChannel != null) {
                                            Icon(
                                                imageVector = Icons.Default.AccountCircle,
                                                contentDescription = "User Avatar",
                                                modifier = Modifier
                                                    .size(40.dp)
                                                    .clip(CircleShape)
                                                    .background(Color.Gray.copy(alpha = 0.2f))
                                                    .padding(4.dp),
                                                tint = Color.Gray
                                            )
                                        } else {
                                            Image(
                                                painter = rememberAsyncImagePainter(
                                                    model = ImageRequest.Builder(context)
                                                        .data(channelAvatarUrl)
                                                        .crossfade(true)
                                                        .placeholder(R.drawable.ic_account_circle)
                                                        .error(R.drawable.ic_account_circle)
                                                        .build(),
                                                ),
                                                contentDescription = "User Avatar",
                                                modifier = Modifier
                                                    .size(40.dp)
                                                    .clip(CircleShape)
                                            )
                                        }

                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = video.snippet.channelTitle,
                                            color = Ocean9.copy(0.5f),
                                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold)
                                        )
                                    }

                                    Text(
                                        text = video.snippet.title,
                                        color = Ocean8,
                                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                                        modifier = Modifier.padding(bottom = 8.dp)
                                    )

                                    Text(
                                        text = video.snippet.description,
                                        color = colors.textSecondary,
                                        style = MaterialTheme.typography.bodyMedium,
                                        modifier = Modifier
                                            .padding(bottom = 8.dp)
                                            .fillMaxWidth(),
                                        maxLines = Int.MAX_VALUE,
                                        overflow = TextOverflow.Ellipsis
                                    )

                                    Row(
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(
                                                imageVector = Icons.Default.ThumbUp,
                                                contentDescription = "Likes",
                                                tint = Color.Gray,
                                                modifier = Modifier.size(20.dp)
                                            )
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text(
                                                text = "${video.statistics?.likeCount ?: 0} Likes",
                                                color = Ocean9.copy(0.5f))
                                        }
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(
                                                imageVector = Icons.Default.Visibility,
                                                contentDescription = "Views",
                                                tint = Color.Gray,
                                                modifier = Modifier.size(20.dp)
                                            )
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text(
                                                text = "${video.statistics?.viewCount ?: 0} Views",
                                                color = Ocean9.copy(0.5f)
                                            )
                                        }
                                    }
                                }
                            }

                            Text(
                                text = "Komentar",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                modifier = Modifier.padding(vertical = 8.dp)
                            )
                            if (isLoadingComments) {
                                CircularProgressIndicator(modifier = Modifier.padding(16.dp))
                            } else if (errorComments != null) {
                                Text(
                                    text = errorComments ?: "Error fetching comments",
                                    color = Color.Red,
                                    modifier = Modifier.padding(16.dp)
                                )
                            } else {
                                Column(modifier = Modifier.fillMaxWidth()) {
                                    comments.take(10).forEach { comment ->
                                        CommentItem(
                                            userName = comment.snippet.authorDisplayName,
                                            comment = comment.snippet.textDisplay,
                                            profileImageUrl = comment.snippet.authorProfileImageUrl
                                        )
                                        smartclassDivider(modifier = Modifier.padding(vertical = 4.dp))
                                    }
                                }
                            }
                        }
                    }
                }

                if (isFullscreen) {
                    FullscreenPlayer(
                        onCloseFullscreen = { isFullscreen = false },
                        youTubePlayerView = youTubePlayerView
                    )
                }
            } ?: run {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            }
        }
    }
}

private fun fetchVideoDetails(
    videoId: String,
    apiKey: String,
    onResult: (VideoItem?, String?) -> Unit
) {
    ApiClient.api.getVideoDetails(
        id = videoId,
        apiKey = apiKey
    ).enqueue(object : Callback<VideoDetailsResponse> {
        override fun onResponse(call: Call<VideoDetailsResponse>, response: Response<VideoDetailsResponse>) {
            if (response.isSuccessful) {
                val videoDetailItem = response.body()?.items?.firstOrNull()
                if (videoDetailItem != null) {
                    val videoItem = VideoItem(
                        id = VideoId(videoDetailItem.id),
                        snippet = videoDetailItem.snippet,
                        statistics = videoDetailItem.statistics,
                        videoUrl = null
                    )
                    onResult(videoItem, null)
                } else {
                    onResult(null, "Video tidak ditemukan")
                }
            } else {
                onResult(null, "API Error: ${response.message()}")
            }
        }

        override fun onFailure(call: Call<VideoDetailsResponse>, t: Throwable) {
            onResult(null, "Gagal mengambil detail video: ${t.message}")
        }
    })
}

private fun fetchComments(
    videoId: String,
    apiKey: String,
    onResult: (List<Comment>, String?) -> Unit
) {
    ApiClient.api.getComments(
        videoId = videoId,
        apiKey = apiKey,
        maxResults = 10
    ).enqueue(object : Callback<CommentResponse> {
        override fun onResponse(call: Call<CommentResponse>, response: Response<CommentResponse>) {
            if (response.isSuccessful) {
                val commentItems = response.body()?.items?.map { it.snippet.topLevelComment } ?: emptyList()
                onResult(commentItems, null)
            } else {
                onResult(emptyList(), "API Error: ${response.message()}")
            }
        }

        override fun onFailure(call: Call<CommentResponse>, t: Throwable) {
            onResult(emptyList(), "Failed to fetch comments: ${t.message}")
        }
    })
}

private fun fetchChannelDetails(
    channelId: String,
    apiKey: String,
    onResult: (String?, String?) -> Unit
) {
    ApiClient.api.getChannelDetails(
        channelId = channelId,
        apiKey = apiKey
    ).enqueue(object : Callback<ChannelResponse> {
        override fun onResponse(call: Call<ChannelResponse>, response: Response<ChannelResponse>) {
            if (response.isSuccessful) {
                val channelItems = response.body()?.items
                if (!channelItems.isNullOrEmpty()) {
                    val avatarUrl = channelItems[0].snippet.thumbnails.high.url
                    onResult(avatarUrl, null)
                } else {
                    onResult(null, "Channel tidak ditemukan")
                }
            } else {
                onResult(null, "API Error: ${response.message()}")
            }
        }

        override fun onFailure(call: Call<ChannelResponse>, t: Throwable) {
            onResult(null, "Gagal mengambil detail channel: ${t.message}")
        }
    })
}

@Composable
fun CommentItem(userName: String, comment: String, profileImageUrl: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    ) {

        val context = LocalContext.current
        Image(
            painter = rememberAsyncImagePainter(
                model = ImageRequest.Builder(context)
                    .data(profileImageUrl)
                    .crossfade(true)
                    .placeholder(R.drawable.ic_account_circle)
                    .error(R.drawable.ic_account_circle)
                    .build(),
            ),
            contentDescription = "User Avatar",
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Column {
            Text(
                text = userName,
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = htmlToAnnotatedString(comment),
                style = MaterialTheme.typography.bodySmall,
                maxLines = Int.MAX_VALUE,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

fun htmlToAnnotatedString(html: String): AnnotatedString {
    val spanned: Spanned = HtmlCompat.fromHtml(html, HtmlCompat.FROM_HTML_MODE_COMPACT)
    val annotatedString = buildAnnotatedString {
        var start = 0
        spanned.getSpans(0, spanned.length, Any::class.java).forEach { span ->
            val spanStart = spanned.getSpanStart(span)
            val spanEnd = spanned.getSpanEnd(span)
            if (spanStart > start) {
                append(spanned.subSequence(start, spanStart))
            }
            when (span) {
                is android.text.style.StyleSpan -> {
                    when (span.style) {
                        android.graphics.Typeface.BOLD -> {
                            withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) {
                                append(spanned.subSequence(spanStart, spanEnd))
                            }
                        }
                        android.graphics.Typeface.ITALIC -> {
                            withStyle(style = SpanStyle(fontStyle = FontStyle.Italic)) {
                                append(spanned.subSequence(spanStart, spanEnd))
                            }
                        }
                        // Tambahkan style lainnya sesuai kebutuhan
                    }
                }
                is android.text.style.UnderlineSpan -> {
                    withStyle(style = SpanStyle(textDecoration = TextDecoration.Underline)) {
                        append(spanned.subSequence(spanStart, spanEnd))
                    }
                }
                // Tambahkan span lainnya seperti ForegroundColorSpan untuk warna, dsb.
            }
            start = spanEnd
        }
        if (start < spanned.length) {
            append(spanned.subSequence(start, spanned.length))
        }
    }
    return annotatedString
}

@SuppressLint("UnusedBoxWithConstraintsScope")
@Composable
fun FullscreenPlayer(
    onCloseFullscreen: () -> Unit,
    youTubePlayerView: YouTubePlayerView
) {

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .zIndex(10f)
    ) {
        AndroidView(
            factory = { youTubePlayerView },
            modifier = Modifier
                .aspectRatio(16 / 9f)
                .align(Alignment.Center)
        ) { playerView ->
            // Listener sudah ditambahkan di VideoDetailScreen, tidak perlu ditambahkan lagi
        }

        IconButton(
            onClick = onCloseFullscreen,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(16.dp)
                .background(Color.Black.copy(alpha = 0.5f), shape = CircleShape)
        ) {
            Icon(
                imageVector = Icons.Default.Close,
                contentDescription = "Close Fullscreen",
                tint = Color.White
            )
        }
    }
}
