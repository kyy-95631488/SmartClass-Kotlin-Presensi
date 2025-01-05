package com.callcenter.smartclass.ui.options

//noinspection UsingMaterialAndMaterial3Libraries
import android.annotation.SuppressLint
import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import android.util.Log
import androidx.activity.compose.BackHandler
import androidx.annotation.RequiresApi
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.selection.TextSelectionColors
//noinspection UsingMaterialAndMaterial3Libraries
import androidx.compose.material.ContentAlpha
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.outlined.RemoveRedEye
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.ThumbUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.callcenter.smartclass.R
import com.callcenter.smartclass.data.ApiClient
import com.callcenter.smartclass.data.VideoDetailsResponse
import com.callcenter.smartclass.data.VideoItem
import com.callcenter.smartclass.data.VideoResponse
import com.callcenter.smartclass.ui.components.smartclassCard
import com.callcenter.smartclass.ui.components.YouTubeAppBar
import com.callcenter.smartclass.ui.theme.DarkText
import com.callcenter.smartclass.ui.theme.smartclassTheme
import com.callcenter.smartclass.ui.theme.Ocean8
import com.callcenter.smartclass.ui.theme.Ocean9
import com.callcenter.smartclass.ui.theme.WhiteColor
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

@RequiresApi(Build.VERSION_CODES.R)
@Composable
fun YouTubeHealth(onClose: () -> Unit, navController: NavHostController) {
    var videos by remember { mutableStateOf<List<VideoItem>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    val context = LocalContext.current
    val apiKey = context.getString(R.string.youtube_api_key)
    var searchQuery by remember { mutableStateOf("") }
    var showSearchInput by remember { mutableStateOf(false) }

    fun isInternetAvailable(context: Context): Boolean {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val networkCapabilities = connectivityManager.activeNetwork?.let {
            connectivityManager.getNetworkCapabilities(it)
        }
        return networkCapabilities?.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) == true
    }

    val onSearchClick: () -> Unit = {
        if (searchQuery.isNotEmpty()) {
            isLoading = true
            fetchVideos(searchQuery, apiKey) { fetchedVideos, error ->
                if (error != null) {
                    errorMessage = error
                } else {
                    videos = fetchedVideos
                }
                isLoading = false
            }
        }
        showSearchInput = !showSearchInput
    }

    LaunchedEffect(Unit) {
        if (!isInternetAvailable(context)) {
            isLoading = false
            errorMessage = "No Internet Connection"
            return@LaunchedEffect
        }
        fetchVideos("Informasi Stunting Pada Anak DI Indonesia Terbaru 2024", apiKey) { fetchedVideos, error ->
            if (error != null) {
                errorMessage = error
            } else {
                videos = fetchedVideos
            }
            isLoading = false
        }
    }

    BackHandler {
        onClose()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(smartclassTheme.colors.uiBackground)
    ) {
        YouTubeAppBar(
            onClose = {
                onClose()
            },
            onSearchClick = onSearchClick
        )

        if (showSearchInput) {
            TextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                label = { Text("Search", color = smartclassTheme.colors.brand) },
                placeholder = {
                    Text("Enter search term", color = smartclassTheme.colors.brandSecondary.copy(alpha = 0.5f))
                },
                trailingIcon = {
                    IconButton(onClick = { onSearchClick() }) {
                        Icon(
                            imageVector = Icons.Outlined.Search,
                            contentDescription = "Search",
                            tint = smartclassTheme.colors.brand
                        )
                    }
                },
                keyboardOptions = KeyboardOptions.Default.copy(
                    imeAction = ImeAction.Search
                ),
                colors = TextFieldDefaults.colors(
                    focusedTextColor = if (isSystemInDarkTheme()) WhiteColor else DarkText,
                    unfocusedTextColor = if (isSystemInDarkTheme()) WhiteColor else DarkText,
                    disabledTextColor = if (isSystemInDarkTheme()) WhiteColor else DarkText.copy(alpha = ContentAlpha.disabled),
                    errorTextColor = MaterialTheme.colorScheme.error,
                    cursorColor = if (isSystemInDarkTheme()) WhiteColor else DarkText,
                    focusedContainerColor = smartclassTheme.colors.uiBackground,
                    unfocusedContainerColor = smartclassTheme.colors.uiBorder,
                    focusedIndicatorColor = Color(0xFF0E5E6C),
                    unfocusedIndicatorColor = Color.Transparent,
                    selectionColors = TextSelectionColors(
                        handleColor = if (isSystemInDarkTheme()) WhiteColor else DarkText,
                        backgroundColor = if (isSystemInDarkTheme()) WhiteColor else DarkText.copy(alpha = 0.4f)
                    )
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp)
                    .clip(RoundedCornerShape(8.dp))
            )
        }

        Box(modifier = Modifier.fillMaxSize()) {
            when {
                isLoading -> {
                    // Display a list of shimmer effects as placeholders
                    LazyColumn {
                        items(5) { // You can adjust the number of placeholders
                            ShimmerVideoItem()
                            Spacer(modifier = Modifier.height(6.dp))
                        }
                    }
                }
                errorMessage != null -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text(
                            text = errorMessage ?: "An unknown error occurred",
                            color = Color.Red,
                            fontSize = 16.sp,
                            modifier = Modifier.padding(16.dp)
                        )
                    }
                }
                else -> {
                    LazyColumn {
                        items(videos) { video ->
                            VideoItemRow(video) { videoId ->
                                // Navigation to VideoDetailScreen with videoId
                                navController.navigate("videoDetail/$videoId")
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                        }
                    }
                }
            }
        }
    }
}

private fun fetchVideos(
    query: String,
    apiKey: String,
    onResult: (List<VideoItem>, String?) -> Unit
) {
    ApiClient.api.searchVideos(query = query, apiKey = apiKey).enqueue(object : Callback<VideoResponse> {
        override fun onResponse(call: Call<VideoResponse>, response: Response<VideoResponse>) {
            if (response.isSuccessful) {
                val videoItems = response.body()?.items ?: emptyList()
                onResult(videoItems, null)

                videoItems.forEach { video ->
                    ApiClient.api.getVideoDetails(id = video.id.videoId, apiKey = apiKey).enqueue(object : Callback<VideoDetailsResponse> {
                        override fun onResponse(call: Call<VideoDetailsResponse>, response: Response<VideoDetailsResponse>) {
                            if (response.isSuccessful) {
                                val details = response.body()?.items?.firstOrNull()
                                details?.let {
                                    val updatedVideo = video.copy(statistics = it.statistics, snippet = it.snippet)
                                    onResult(videoItems.map { if (it.id.videoId == video.id.videoId) updatedVideo else it }, null)
                                }
                            }
                        }

                        override fun onFailure(call: Call<VideoDetailsResponse>, t: Throwable) {
                            Log.e("VideoDetails", "Failed to fetch video details: ${t.message}")
                        }
                    })
                }
            } else {
                onResult(emptyList(), "API Error: ${response.message()}")
            }
        }

        override fun onFailure(call: Call<VideoResponse>, t: Throwable) {
            onResult(emptyList(), "Failed to fetch videos: ${t.message}")
        }
    })
}

@SuppressLint("UnusedBoxWithConstraintsScope")
@Composable
fun ShimmerVideoItem() {

    smartclassCard(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 6.dp),
        shape = RoundedCornerShape(16.dp),
        elevation = 8.dp
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            ShimmerEffect(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .clip(RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp))
            )

            Spacer(modifier = Modifier.height(8.dp))

            Column(modifier = Modifier.padding(12.dp)) {
                ShimmerEffect(modifier = Modifier.height(24.dp).fillMaxWidth())
                Spacer(modifier = Modifier.height(4.dp))
                ShimmerEffect(
                    modifier = Modifier
                        .height(24.dp)
                        .fillMaxWidth()
                )
            }
        }
    }
}

@Composable
fun ShimmerEffect(
    modifier: Modifier = Modifier,
    shimmerColor: Color = Color.LightGray.copy(alpha = 0.6f)
) {
    val transition = rememberInfiniteTransition()
    val translateAnim by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1000f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1200, easing = LinearEasing)
        )
    )

    Box(
        modifier = modifier
            .background(
                Brush.linearGradient(
                    colors = listOf(
                        shimmerColor.copy(alpha = 0.6f),
                        shimmerColor.copy(alpha = 0.2f),
                        shimmerColor.copy(alpha = 0.6f)
                    ),
                    start = Offset(translateAnim - 1000f, translateAnim - 1000f),
                    end = Offset(translateAnim, translateAnim)
                )
            )
    )
}


@SuppressLint("UnusedBoxWithConstraintsScope")
@Composable
fun VideoItemRow(video: VideoItem, onClick: (String) -> Unit) {
    val colors = smartclassTheme.colors
    val typography = MaterialTheme.typography

    smartclassCard(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 6.dp)
            .clickable { onClick(video.id.videoId) },
        shape = RoundedCornerShape(16.dp),
        elevation = 8.dp
    ) {
        Column(
            modifier = Modifier.fillMaxWidth()
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
            ) {
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(video.snippet.thumbnails.high.url)
                        .crossfade(true)
                        .placeholder(R.drawable.empty_state_search)
                        .error(R.drawable.empty_state_search)
                        .build(),
                    contentDescription = video.snippet.title,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp))
                )
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(80.dp)
                        .align(Alignment.BottomCenter)
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.7f))
                            )
                        )
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Column(
                modifier = Modifier
                    .padding(horizontal = 12.dp, vertical = 8.dp)
            ) {
                Text(
                    text = video.snippet.title,
                    color = Ocean9,
                    style = typography.titleMedium,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = video.snippet.description,
                    color = colors.textSecondary,
                    style = typography.bodyMedium,
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(8.dp))

//                Row(
//                    verticalAlignment = Alignment.CenterVertically,
//                    horizontalArrangement = Arrangement.SpaceBetween,
//                    modifier = Modifier.fillMaxWidth()
//                ) {
//                    Row(verticalAlignment = Alignment.CenterVertically) {
//                        Icon(
//                            imageVector = Icons.Outlined.ThumbUp,
//                            contentDescription = "Likes",
//                            tint = colors.brand,
//                            modifier = Modifier.size(20.dp)
//                        )
//                        Spacer(modifier = Modifier.width(4.dp))
//                        Text(
//                            text = video.statistics?.likeCount ?: "0",
//                            color = Ocean8,
//                            style = typography.bodySmall
//                        )
//                        Spacer(modifier = Modifier.width(16.dp))
//                        Icon(
//                            imageVector = Icons.Outlined.RemoveRedEye,
//                            contentDescription = "Views",
//                            tint = colors.brand,
//                            modifier = Modifier.size(20.dp)
//                        )
//                        Spacer(modifier = Modifier.width(4.dp))
//                        Text(
//                            text = video.statistics?.viewCount ?: "0",
//                            color = Ocean8,
//                            style = typography.bodySmall
//                        )
//                    }
//                    Icon(
//                        imageVector = Icons.Default.MoreVert,
//                        contentDescription = "More Options",
//                        tint = colors.textPrimary,
//                        modifier = Modifier.size(20.dp)
//                    )
//                }
            }
        }
    }
}

@RequiresApi(Build.VERSION_CODES.R)
@Preview(showBackground = true)
@Composable
fun PreviewYouTubeHealth() {
    smartclassTheme {
        val navController = rememberNavController()
        YouTubeHealth(onClose = { }, navController = navController)
    }
}
