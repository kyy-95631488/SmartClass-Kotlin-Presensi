package com.callcenter.smartclass.ui.home.mainfeaturesgrid.ibuhamil

import android.app.DownloadManager
import android.content.Context
import android.net.Uri
import android.os.Environment
import android.util.Log
import android.widget.Toast
import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Download
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import com.callcenter.smartclass.R
import com.callcenter.smartclass.ui.theme.DarkBlue
import com.callcenter.smartclass.ui.theme.DarkText
import com.callcenter.smartclass.ui.theme.smartclassTheme
import com.callcenter.smartclass.ui.theme.LightBlue
import com.callcenter.smartclass.ui.theme.MinimalBackgroundDark
import com.callcenter.smartclass.ui.theme.MinimalBackgroundLight
import com.callcenter.smartclass.ui.theme.MinimalTextDark
import com.callcenter.smartclass.ui.theme.MinimalTextLight
import com.callcenter.smartclass.ui.theme.WhiteColor
import com.google.accompanist.pager.*
import com.google.android.gms.tasks.Tasks
import com.google.firebase.storage.FirebaseStorage

@OptIn(ExperimentalPagerApi::class, ExperimentalMaterial3Api::class)
@Composable
fun IbuHamilScreen(navController: NavController) {
    val storage = FirebaseStorage.getInstance()
    val storageRef = storage.reference.child("poster/ibu_hamil")

    var imageUrls by remember { mutableStateOf<List<String>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }

    val isLight = !smartclassTheme.colors.isDark

    var focusedImageUrl by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        storageRef.listAll()
            .addOnSuccessListener { listResult ->
                val urls = mutableListOf<String>()
                val tasks = listResult.items.map { it.downloadUrl }

                Tasks.whenAllComplete(tasks).addOnCompleteListener {
                    for (task in tasks) {
                        if (task.isSuccessful) {
                            task.result?.let { url ->
                                urls.add(url.toString())
                            }
                        } else {
                            Log.e("IbuHamilCarousel", "Error fetching download URL", task.exception)
                        }
                    }
                    imageUrls = urls
                    isLoading = false
                }
            }
            .addOnFailureListener { exception ->
                error = exception.message
                isLoading = false
                Log.e("IbuHamilCarousel", "Error listing files", exception)
            }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("Ibu Hamil") },
                    navigationIcon = {
                        IconButton(onClick = { navController.popBackStack() }) {
                            @Suppress("DEPRECATION")
                            Icon(
                                Icons.Filled.ArrowBack,
                                contentDescription = "Back",
                                tint = if (isLight) DarkBlue else LightBlue
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = if (isLight) MinimalBackgroundLight else MinimalBackgroundDark,
                        titleContentColor = if (isLight) MinimalTextLight else MinimalTextDark,
                        navigationIconContentColor = if (isLight) DarkText else WhiteColor,
                        actionIconContentColor = if (isLight) DarkText else WhiteColor
                    )
                )
            },
            containerColor = smartclassTheme.colors.uiBackground,
            content = { paddingValues ->
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                        .padding(16.dp)
                        .then(
                            if (focusedImageUrl != null)
                                Modifier.blur(20.dp)
                            else
                                Modifier
                        ),
                    verticalArrangement = Arrangement.Top,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth(),
                        verticalArrangement = Arrangement.Top,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        when {
                            isLoading -> {
                                ShimmerImagePlaceholder(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(200.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                )
                            }
                            error != null -> {
                                Text(
                                    text = "Gagal memuat gambar: $error",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.error,
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                            imageUrls.isNotEmpty() -> {
                                @Suppress("DEPRECATION") val pagerState = rememberPagerState()

                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                ) {
                                    @Suppress("DEPRECATION")
                                    HorizontalPager(
                                        count = imageUrls.size,
                                        state = pagerState,
                                        modifier = Modifier
                                            .fillMaxWidth(),
                                        contentPadding = PaddingValues(horizontal = 16.dp),
                                        verticalAlignment = Alignment.Top
                                    ) { page ->
                                        PosterImageCarousel(
                                            url = imageUrls[page],
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(8.dp),
                                            onImageClick = { url ->
                                                focusedImageUrl = url
                                            }
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(32.dp))
                            }
                            else -> {
                                Text(
                                    text = "Tidak ada gambar untuk ditampilkan.",
                                    style = MaterialTheme.typography.bodyMedium,
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                        }
                    }

                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 16.dp)
                    ) {
                        QuoteCard(
                            quote = "\"Jaga asupan nutrisi, sayangi dirimu, dan persiapkan masa depan anak yang lebih baik tanpa stunting.\"",
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 16.dp)
                        )
                        BottomImage(imageResId = R.drawable.assets_image_ibu_hamil)
                    }
                }
            }
        )

        focusedImageUrl?.let { url ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.5f))
                    .pointerInput(Unit) {
                    }
            ) {
                var scale by remember { mutableFloatStateOf(1f) }
                var offset by remember { mutableStateOf(Offset.Zero) }

                Image(
                    painter = rememberAsyncImagePainter(url),
                    contentDescription = "Focused Poster",
                    contentScale = ContentScale.Fit,
                    modifier = Modifier
                        .fillMaxWidth()
                        .wrapContentHeight()
                        .align(Alignment.Center)
                        .padding(32.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .graphicsLayer(
                            scaleX = scale,
                            scaleY = scale,
                            translationX = offset.x,
                            translationY = offset.y
                        )
                        .pointerInput(Unit) {
                            detectTransformGestures { _, pan, zoom, _ ->
                                scale = (scale * zoom).coerceIn(1f, 5f)
                                offset += pan
                            }
                        }
                )

                IconButton(
                    onClick = { focusedImageUrl = null },
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(32.dp)
                        .background(
                            color = Color.Black.copy(alpha = 0.5f),
                            shape = CircleShape
                        )
                ) {
                    @Suppress("DEPRECATION")
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "Close",
                        tint = Color.White
                    )
                }
            }
        }
    }
}

@Composable
fun ShimmerImagePlaceholder(modifier: Modifier = Modifier) {
    ShimmerEffect(
        modifier = modifier,
        shimmerColor = Color.LightGray.copy(alpha = 0.6f)
    )
}

@Composable
fun ShimmerEffect(
    modifier: Modifier = Modifier,
    shimmerColor: Color = Color.LightGray.copy(alpha = 0.6f)
) {
    val transition = rememberInfiniteTransition(label = "")
    val translateAnim by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1000f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1200, easing = LinearEasing)
        ), label = ""
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

@Composable
fun PosterImageCarousel(
    url: String,
    modifier: Modifier = Modifier,
    onImageClick: (String) -> Unit
) {
    val context = LocalContext.current

    fun downloadImage(context: Context, url: String) {
        val request = DownloadManager.Request(Uri.parse(url))
            .setTitle("Download Poster")
            .setDescription("Mengunduh poster ibu hamil.")
            .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
            .setAllowedOverMetered(true)
            .setAllowedOverRoaming(true)
            .setDestinationInExternalPublicDir(
                Environment.DIRECTORY_DOWNLOADS,
                "poster_ibu_hamil_${System.currentTimeMillis()}.jpg"
            )

        val downloadManager = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
        downloadManager.enqueue(request)

        Toast.makeText(context, "Pengunduhan dimulai...", Toast.LENGTH_SHORT).show()
    }

    Box(modifier = modifier) {
        Image(
            painter = rememberAsyncImagePainter(url),
            contentDescription = "Poster Ibu Hamil",
            contentScale = ContentScale.Fit,
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight()
                .clip(RoundedCornerShape(16.dp))
                .clickable { onImageClick(url) }
        )

        IconButton(
            onClick = { downloadImage(context, url) },
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(8.dp)
                .background(
                    color = Color.Black.copy(alpha = 0.5f),
                    shape = CircleShape
                )
        ) {
            Icon(
                imageVector = Icons.Default.Download,
                contentDescription = "Download Poster",
                tint = Color.White
            )
        }
    }
}

@Composable
fun BottomImage(imageResId: Int, modifier: Modifier = Modifier) {
    Image(
        painter = painterResource(id = imageResId),
        contentDescription = "Gambar Kutipan",
        contentScale = ContentScale.Fit,
        modifier = modifier
            .size(120.dp)
            .clip(CircleShape)
            .shadow(4.dp, CircleShape)
    )
}

@Composable
fun QuoteCard(quote: String, modifier: Modifier = Modifier) {
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFFE3F2FD)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        modifier = modifier
    ) {
        Text(
            text = quote,
            style = MaterialTheme.typography.bodyLarge,
            color = Color.Black,
            modifier = Modifier
                .padding(16.dp),
            textAlign = TextAlign.Center
        )
    }
}
