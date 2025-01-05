package com.callcenter.smartclass.ui.home.admin.tambahposter

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Upload
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil.compose.rememberImagePainter
import com.callcenter.smartclass.ui.components.smartclassCard
import com.callcenter.smartclass.ui.theme.*
import com.google.accompanist.pager.*
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.launch
import java.util.UUID

@OptIn(ExperimentalPagerApi::class, ExperimentalMaterial3Api::class)
@Composable
fun AddPoster(navController: NavController) {

    val isLight = !smartclassTheme.colors.isDark
    val tabs = listOf("Ibu Hamil", "Pencegahan Stunting", "Penanganan Stunting")
    val pagerState = rememberPagerState()
    val coroutineScope = rememberCoroutineScope()

    Surface(
        modifier = Modifier
            .fillMaxSize()
            .background(smartclassTheme.colors.uiBackground),
        color = smartclassTheme.colors.uiBackground
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            TopAppBar(
                title = { Text("Tambah Poster") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
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

            TabRow(
                selectedTabIndex = pagerState.currentPage,
                modifier = Modifier
                    .fillMaxWidth()
                    .background(if (isLight) MinimalBackgroundLight else MinimalBackgroundDark),
                contentColor = if (isLight) MinimalTextLight else MinimalTextDark,
                indicator = { tabPositions ->
                    TabRowDefaults.Indicator(
                        Modifier.tabIndicatorOffset(tabPositions[pagerState.currentPage]),
                        color = if (isLight) DarkBlue else LightBlue
                    )
                },
                containerColor = if (isLight) MinimalBackgroundLight else MinimalBackgroundDark,
                divider = {
                    Divider(
                        color = if (isLight) DarkBlue else LightBlue,
                        thickness = 1.dp
                    )
                }
            ) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = pagerState.currentPage == index,
                        onClick = {
                            coroutineScope.launch {
                                pagerState.animateScrollToPage(index)
                            }
                        },
                        text = { Text(title) }
                    )
                }
            }

            HorizontalPager(
                count = tabs.size,
                state = pagerState,
                modifier = Modifier.weight(1f)
            ) { page ->
                when (page) {
                    0 -> IbuHamil()
                    1 -> PencegahanStunting()
                    2 -> PenangananStunting()
                }
            }
        }
    }
}

@Composable
fun IbuHamil() {
    val isLight = !smartclassTheme.colors.isDark
    val storage = FirebaseStorage.getInstance()

    UploadPoster(
        category = "ibu_hamil",
        isLight = isLight,
        storage = storage
    )
}

@Composable
fun UploadPoster(
    category: String,
    isLight: Boolean,
    storage: FirebaseStorage
) {
    var imageUri by remember { mutableStateOf<Uri?>(null) }
    var uploadStatus by remember { mutableStateOf<String?>(null) }
    var uploadProgress by remember { mutableStateOf<Float?>(null) }

    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        imageUri = uri
    }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .padding(8.dp)
    ) {
        Button(
            onClick = { launcher.launch("image/*") },
            modifier = Modifier.padding(8.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = if (isLight) Ocean7 else Ocean4,
                contentColor = Color.White
            ),
            elevation = ButtonDefaults.buttonElevation(defaultElevation = 8.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Image,
                contentDescription = "Pilih Gambar",
                modifier = Modifier.padding(end = 8.dp)
            )
            Text("Pilih Gambar")
        }

        imageUri?.let { uri ->
            Image(
                painter = rememberImagePainter(uri),
                contentDescription = "Preview Gambar",
                modifier = Modifier
                    .padding(8.dp)
                    .width(566.dp)
                    .height(478.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .border(2.dp, Color.Gray, RoundedCornerShape(8.dp)),
                contentScale = ContentScale.Fit
            )

            Text("Gambar dipilih: ${uri.lastPathSegment}", modifier = Modifier.padding(8.dp))

            Button(
                onClick = {
                    val storageRef = storage.reference.child("poster/$category/${UUID.randomUUID()}")
                    val uploadTask = storageRef.putFile(uri)

                    uploadTask.addOnSuccessListener {
                        storageRef.downloadUrl.addOnSuccessListener { downloadUri ->
                            uploadStatus = "Upload berhasil: $downloadUri"
                        }
                    }.addOnFailureListener { e ->
                        uploadStatus = "Upload gagal: ${e.message}"
                    }

                    uploadTask.addOnProgressListener { taskSnapshot ->
                        val progress = (100.0 * taskSnapshot.bytesTransferred / taskSnapshot.totalByteCount).toFloat()
                        uploadProgress = progress
                    }
                },
                modifier = Modifier.padding(8.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isLight) Ocean7 else Ocean4,
                    contentColor = Color.White
                ),
                elevation = ButtonDefaults.buttonElevation(defaultElevation = 8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Upload,
                    contentDescription = "Unggah Gambar",
                    modifier = Modifier.padding(end = 8.dp)
                )
                Text("Unggah Gambar")
            }

            uploadProgress?.let { progress ->
                LinearProgressIndicator(
                    progress = progress / 100f,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp),
                    color = if (isLight) Color.Green else Ocean9,
                    trackColor = if (isLight) Color.LightGray else Color.DarkGray
                )
                Text("${progress.toInt()}%", modifier = Modifier.padding(8.dp))
            }
        }

        uploadStatus?.let { status ->
            smartclassCard(
                modifier = Modifier.padding(8.dp),
                contentColor = Color.Black
            ) {
                Text(
                    text = status,
                    modifier = Modifier.padding(16.dp),
                    color = if (status.startsWith("Upload berhasil")) Color.Green else Color.Red,
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
    }
}

@Composable
fun PencegahanStunting() {
    val isLight = !smartclassTheme.colors.isDark
    val storage = FirebaseStorage.getInstance()

    UploadPoster(
        category = "pencegahan_stunting",
        isLight = isLight,
        storage = storage
    )
}

@Composable
fun PenangananStunting() {
    val isLight = !smartclassTheme.colors.isDark
    val storage = FirebaseStorage.getInstance()

    UploadPoster(
        category = "penanganan_stunting",
        isLight = isLight,
        storage = storage
    )
}
