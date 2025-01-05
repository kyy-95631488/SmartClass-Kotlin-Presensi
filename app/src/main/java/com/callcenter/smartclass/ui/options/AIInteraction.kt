package com.callcenter.smartclass.ui.options

import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Environment
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.exclude
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ScaffoldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.callcenter.smartclass.BuildConfig
import com.callcenter.smartclass.data.Message
import com.callcenter.smartclass.ui.components.ChatAppBar
import com.callcenter.smartclass.ui.components.MessageBox
import com.callcenter.smartclass.ui.components.MessageInputField
import com.callcenter.smartclass.ui.theme.smartclassTheme
import com.callcenter.smartclass.ui.theme.costum01
import com.google.ai.client.generativeai.*
import com.google.ai.client.generativeai.type.content
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*
import androidx.compose.material3.Icon
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.IconButton
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import java.io.File
import android.Manifest
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.ui.graphics.Color
import android.content.Intent
import android.provider.Settings

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AIInteraction(onClose: () -> Unit) {
    val topBarState = rememberTopAppBarState()
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior(topBarState)

    val messages = remember { mutableStateListOf<Message>() }
    var userMessage by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var displayedText by remember { mutableStateOf("") }
    var chatStarted by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()
    var selectedImages by remember { mutableStateOf(listOf<Bitmap>()) }
    var currentPhotoPath by remember { mutableStateOf("") }
    val context = LocalContext.current
    var permissionDenied by remember { mutableStateOf(false) }

    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        if (success) {
            val bitmap = BitmapFactory.decodeFile(currentPhotoPath)
            if (bitmap != null) {
                selectedImages = selectedImages + bitmap
            }
        }
    }

    val takePicture: () -> Unit = {
        val imageFile = createImageFile(context)
        currentPhotoPath = imageFile.absolutePath
        cameraLauncher.launch(FileProvider.getUriForFile(context, "com.callcenter.smartclass.fileprovider", imageFile))
    }

    val requestCameraPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            takePicture()
        } else {
            permissionDenied = true
        }
    }

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetMultipleContents()
    ) { uris: List<Uri> ->
        selectedImages = uris.map { uri ->
            val inputStream = context.contentResolver.openInputStream(uri)
            BitmapFactory.decodeStream(inputStream)
        }
    }

    fun checkCameraPermission() {
        when (ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA)) {
            PackageManager.PERMISSION_GRANTED -> {
                takePicture()
            }
            else -> {
                requestCameraPermissionLauncher.launch(Manifest.permission.CAMERA)
            }
        }
    }

    if (permissionDenied) {
        AlertDialog(
            onDismissRequest = { permissionDenied = false },
            title = {
                Text(
                    text = "Permission Required",
                    color = Color.Red
                )
            },
            text = {
                Text(
                    text = "Camera permission is required to take pictures.",
                    color = Color.Gray
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        permissionDenied = false
                        checkCameraPermission()
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF0E5E6C)
                    )
                ) {
                    Text("Close", color = Color.White)
                }
            },
            dismissButton = {
                Button(
                    onClick = {
                        permissionDenied = false
                        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                            data = Uri.fromParts("package", context.packageName, null)
                        }
                        context.startActivity(intent)
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF0E5E6C)
                    )
                ) {
                    Text("Open Settings", color = Color.White)
                }
            },
            containerColor = Color.LightGray
        )
    }

    LaunchedEffect(Unit) {
        val fullText = "Selamat datang di AI Interaction!"
        for (i in fullText.indices) {
            displayedText += fullText[i]
            delay(100)
        }
    }

    smartclassTheme {
        Scaffold(
            modifier = Modifier
                .navigationBarsPadding()
                .nestedScroll(scrollBehavior.nestedScrollConnection),
            contentWindowInsets = ScaffoldDefaults.contentWindowInsets
                .exclude(WindowInsets.navigationBars)
                .exclude(WindowInsets.ime),
            topBar = { ChatAppBar(onClose = onClose) },
            containerColor = smartclassTheme.colors.uiBackground,
        ) { paddingValues ->
            Column(
                modifier = Modifier
                    .padding(paddingValues)
                    .padding(horizontal = 16.dp)
                    .fillMaxSize()
            ) {
                if (!chatStarted) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = displayedText,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                        )
                    }
                }

                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    reverseLayout = false,
                ) {
                    items(messages) { message ->
                        MessageBox(message = message)
                    }

                    if (selectedImages.isNotEmpty()) {
                        item {
                            LazyRow(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                items(selectedImages) { image ->
                                    Box(
                                        modifier = Modifier
                                            .size(120.dp)
                                            .padding(4.dp)
                                    ) {
                                        Image(
                                            bitmap = image.asImageBitmap(),
                                            contentDescription = null,
                                            modifier = Modifier
                                                .fillMaxSize()
                                                .clip(RoundedCornerShape(8.dp))
                                        )

                                        IconButton(
                                            onClick = {
                                                selectedImages = selectedImages.filterNot { it == image }
                                            },
                                            modifier = Modifier
                                                .align(Alignment.TopEnd)
                                                .size(32.dp)
                                                .padding(8.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Filled.Delete,
                                                contentDescription = "Delete Image",
                                                modifier = Modifier.size(24.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                    if (isLoading) {
                        item {
                            CircularProgressIndicator(
                                modifier = Modifier.padding(16.dp),
                                color = costum01
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.size(8.dp))

                MessageInputField(
                    onSendMessage = { message ->
                        if (message.isNotEmpty() || selectedImages.isNotEmpty()) {
                            messages.add(Message(message, getCurrentTime(), true))
                            userMessage = ""
                            chatStarted = true
                            coroutineScope.launch {
                                isLoading = true
                                sendMessage(message, selectedImages, messages)
                                isLoading = false
                            }
                        }
                    },
                    onSelectImages = {
                        imagePickerLauncher.launch("image/*")
                    },
                    onTakePicture = {
                        checkCameraPermission()
                    }
                )
            }
        }
    }
}

private fun createImageFile(context: Context): File {
    val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
    val storageDir = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES)
    return File.createTempFile("JPEG_${timeStamp}_", ".jpg", storageDir)
}


private suspend fun sendMessage(userMessage: String, images: List<Bitmap>, messages: SnapshotStateList<Message>) {
    val apiKey = BuildConfig.GEMINI_API_KEY
    val generativeModel = GenerativeModel(
        modelName = "gemini-1.5-flash",
        apiKey = apiKey
    )

    val inputContent = content {
        images.forEach { image(it) }
        text(userMessage)
    }

    try {
        val response = generativeModel.generateContentStream(inputContent)

        val fullResponse = StringBuilder()
        response.collect { chunk ->
            fullResponse.append(chunk.text ?: "")
        }

        if (fullResponse.isNotEmpty()) {
            messages.add(Message(fullResponse.toString(), getCurrentTime(), false))
        }
    } catch (e: Exception) {
        Log.e("AIInteraction", "Error calling Gemini API: ${e.message}")
        messages.add(Message("Error: ${e.message}", getCurrentTime(), false))
    }
}

fun getCurrentTime(): String {
    val sdf = SimpleDateFormat("h:mm a", Locale.getDefault())
    return sdf.format(Date())
}

@Preview(showBackground = true)
@Composable
fun PreviewAIInteraction() {
    AIInteraction(onClose = { })
}
