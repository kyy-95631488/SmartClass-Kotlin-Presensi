package com.callcenter.smartclass.ui.home.admin.tambahresepmpasi

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Base64
import android.view.MotionEvent
import android.webkit.JavascriptInterface
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.selection.LocalTextSelectionColors
import androidx.compose.foundation.text.selection.TextSelectionColors
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Label
import androidx.compose.material.icons.filled.Subject
import androidx.compose.material.icons.filled.Title
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.navigation.NavController
import com.callcenter.smartclass.ui.home.admin.tambahresepmpasi.data.ResepMpasi
import com.callcenter.smartclass.ui.theme.*
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import java.util.UUID

@SuppressLint("JavascriptInterface", "ClickableViewAccessibility")
@Composable
fun AddResepMpasi(navController: NavController) {
    val coroutineScope = rememberCoroutineScope()
    val firestore = FirebaseFirestore.getInstance()
    val context = LocalContext.current

    val backgroundColor = if (isSystemInDarkTheme()) DarkBackgroundColor else LightBackgroundColor
    val backgroundColorHex = backgroundColor.toHex()
    val backgroundColorArgb = backgroundColor.toArgb()

    val textColor = if (isSystemInDarkTheme()) WhiteColor else DarkText
    val textColorHex = textColor.toHex()

    var title by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("") }
    var topic by remember { mutableStateOf("") } // Ganti dari 'description' menjadi 'topic'
    var thumbnailUri by remember { mutableStateOf<Uri?>(null) }
    var thumbnailBitmap by remember { mutableStateOf<ImageBitmap?>(null) }
    var thumbnailUrl by remember { mutableStateOf("") }
    var contentDeferred by remember { mutableStateOf<CompletableDeferred<String>?>(null) }
    var webViewRef by remember { mutableStateOf<WebView?>(null) }

    val thumbnailPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            thumbnailUri = it
            thumbnailBitmap = convertUriToImageBitmap(context, it)
        }
    }

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            val base64Image = convertImageToBase64(context, it)
            base64Image?.let { base64 ->
                webViewRef?.evaluateJavascript("insertImage('data:image/*;base64,$base64')", null)
            }
        }
    }

    val customTextSelectionColors = TextSelectionColors(
        handleColor = if (isSystemInDarkTheme()) WhiteColor else DarkText,
        backgroundColor = if (isSystemInDarkTheme()) WhiteColor else DarkText.copy(alpha = 0.4f)
    )

    smartclassTheme {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = backgroundColor
        ) {
            Column(
                modifier = Modifier
                    .padding(24.dp)
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Top
            ) {
                Text(
                    text = "Tambah Resep MPASI",
                    style = MaterialTheme.typography.h5.copy(
                        color = textColor
                    ),
                    modifier = Modifier.padding(bottom = 24.dp)
                )

                // Input Judul
                CompositionLocalProvider(
                    LocalTextSelectionColors provides customTextSelectionColors
                ) {
                    OutlinedTextField(
                        value = title,
                        onValueChange = { title = it },
                        label = { Text("Judul Resep", color = textColor) },
                        placeholder = { Text("Masukkan judul resep", color = textColor) },
                        leadingIcon = {
                            Icon(Icons.Filled.Title, contentDescription = "Judul", tint = if (isSystemInDarkTheme()) Ocean4 else Ocean7)
                        },
                        singleLine = true,
                        colors = TextFieldDefaults.outlinedTextFieldColors(
                            focusedBorderColor = if (isSystemInDarkTheme()) Ocean4 else Ocean7,
                            unfocusedBorderColor = if (isSystemInDarkTheme()) Ocean3 else Ocean6,
                            cursorColor = if (isSystemInDarkTheme()) Ocean4 else Ocean7,
                            textColor = textColor
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp)
                    )
                }

                // Input Kategori
                CompositionLocalProvider(
                    LocalTextSelectionColors provides customTextSelectionColors
                ) {
                    OutlinedTextField(
                        value = category,
                        onValueChange = { category = it },
                        label = { Text("Kategori", color = textColor) },
                        placeholder = { Text("Masukkan kategori resep", color = textColor) },
                        leadingIcon = {
                            Icon(Icons.Filled.Label, contentDescription = "Kategori", tint = if (isSystemInDarkTheme()) Ocean4 else Ocean7)
                        },
                        singleLine = true,
                        colors = TextFieldDefaults.outlinedTextFieldColors(
                            focusedBorderColor = if (isSystemInDarkTheme()) Ocean4 else Ocean7,
                            unfocusedBorderColor = if (isSystemInDarkTheme()) Ocean3 else Ocean6,
                            cursorColor = if (isSystemInDarkTheme()) Ocean4 else Ocean7,
                            textColor = textColor
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp)
                    )
                }

                // Input Topik (sebelumnya Deskripsi)
                CompositionLocalProvider(
                    LocalTextSelectionColors provides customTextSelectionColors
                ) {
                    OutlinedTextField(
                        value = topic,
                        onValueChange = { topic = it },
                        label = { Text("Topik", color = textColor) },
                        placeholder = { Text("Masukkan topik resep", color = textColor) },
                        leadingIcon = {
                            Icon(Icons.Filled.Subject, contentDescription = "Topik", tint = if (isSystemInDarkTheme()) Ocean4 else Ocean7)
                        },
                        colors = TextFieldDefaults.outlinedTextFieldColors(
                            focusedBorderColor = if (isSystemInDarkTheme()) Ocean4 else Ocean7,
                            unfocusedBorderColor = if (isSystemInDarkTheme()) Ocean3 else Ocean6,
                            cursorColor = if (isSystemInDarkTheme()) Ocean4 else Ocean7,
                            textColor = textColor
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp)
                    )
                }

                // Pilih Thumbnail
                OutlinedButton(
                    onClick = { thumbnailPickerLauncher.launch("image/*") },
                    colors = ButtonDefaults.outlinedButtonColors(
                        backgroundColor = if (isSystemInDarkTheme()) Ocean4 else Ocean7,
                        contentColor = Color.White
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp)
                ) {
                    Text("Pilih Thumbnail")
                }

                thumbnailBitmap?.let { bitmap ->
                    Image(
                        bitmap = bitmap,
                        contentDescription = "Thumbnail Preview",
                        modifier = Modifier
                            .size(150.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .border(1.dp, Color.Gray, RoundedCornerShape(8.dp))
                            .padding(vertical = 8.dp),
                        contentScale = ContentScale.Fit
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Editor Konten Resep
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp)
                        .padding(vertical = 8.dp)
                ) {
                    AndroidView(
                        factory = { ctx ->
                            WebView(ctx).apply {
                                webViewRef = this
                                settings.javaScriptEnabled = true
                                settings.domStorageEnabled = true
                                isVerticalScrollBarEnabled = true
                                isHorizontalScrollBarEnabled = false
                                overScrollMode = WebView.OVER_SCROLL_ALWAYS
                                webViewClient = object : WebViewClient() {
                                    override fun onPageFinished(view: WebView?, url: String?) {
                                        super.onPageFinished(view, url)
                                        evaluateJavascript("document.body.style.backgroundColor = '$backgroundColorHex';", null)
                                        evaluateJavascript("document.getElementById('editor-container').style.backgroundColor = '$backgroundColorHex';", null)
                                        evaluateJavascript("document.getElementById('toolbar').style.backgroundColor = '$backgroundColorHex';", null)
                                        evaluateJavascript("document.body.style.color = '$textColorHex';", null)
                                        evaluateJavascript("quill.root.style.color = '$textColorHex';", null)
                                    }
                                }
                                addJavascriptInterface(
                                    WebAppInterface(
                                        onPickImage = { imagePickerLauncher.launch("image/*") },
                                        onReceiveContent = { htmlContent ->
                                            contentDeferred?.complete(htmlContent)
                                        }
                                    ),
                                    "Android"
                                )
                                loadUrl("file:///android_asset/quill_editor.html")

                                setOnTouchListener { v, event ->
                                    when (event.action) {
                                        MotionEvent.ACTION_DOWN, MotionEvent.ACTION_MOVE -> {
                                            v.parent.requestDisallowInterceptTouchEvent(true)
                                        }
                                        MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                                            v.parent.requestDisallowInterceptTouchEvent(false)
                                        }
                                    }
                                    false
                                }
                            }
                        },
                        update = { webView ->
                            webView.setBackgroundColor(backgroundColorArgb)
                        },
                        modifier = Modifier.fillMaxSize()
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                var uploadProgress by remember { mutableStateOf(0f) }
                var isUploading by remember { mutableStateOf(false) }

                if (isUploading) {
                    LinearProgressIndicator(
                        progress = uploadProgress,
                        color = if (isSystemInDarkTheme()) Ocean4 else Ocean7,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp)
                    )
                }

                // Tombol Batal dan Simpan
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    OutlinedButton(
                        onClick = { navController.popBackStack() },
                        colors = ButtonDefaults.outlinedButtonColors(
                            backgroundColor = Color.Transparent,
                            contentColor = if (isSystemInDarkTheme()) TextDarkColor else TextLightColor
                        ),
                        shape = RoundedCornerShape(8.dp),
                        border = BorderStroke(1.dp, if (isSystemInDarkTheme()) Ocean4 else Ocean7),
                        modifier = Modifier
                            .weight(1f)
                            .height(50.dp)
                    ) {
                        Text("Batal", style = MaterialTheme.typography.button)
                    }

                    Spacer(modifier = Modifier.width(16.dp))

                    Button(
                        onClick = {
                            coroutineScope.launch {
                                if (thumbnailUri != null) {
                                    isUploading = true
                                    uploadProgress = 0f
                                    val url = uploadThumbnail(thumbnailUri!!) { progress ->
                                        uploadProgress = progress
                                    }
                                    isUploading = false
                                    if (url != null) {
                                        thumbnailUrl = url
                                    } else {
                                        Toast.makeText(context, "Gagal mengunggah thumbnail", Toast.LENGTH_SHORT).show()
                                        return@launch
                                    }
                                }

                                contentDeferred = CompletableDeferred()
                                webViewRef?.evaluateJavascript("getContent()", null)
                                val contentValue = contentDeferred?.await() ?: ""

                                if (title.isBlank() || category.isBlank()) {
                                    Toast.makeText(context, "Judul dan Kategori tidak boleh kosong", Toast.LENGTH_SHORT).show()
                                } else {
                                    try {
                                        val resepMpasi = ResepMpasi(
                                            title = title.trim(),
                                            category = category.trim(),
                                            topic = topic.trim(),
                                            content = contentValue.trim(),
                                            thumbnailUrl = thumbnailUrl
                                        )
                                        firestore.collection("resep_mpasi")
                                            .add(resepMpasi)
                                            .addOnSuccessListener {
                                                Toast.makeText(context, "Resep berhasil disimpan", Toast.LENGTH_SHORT).show()
                                                navController.popBackStack()
                                            }
                                            .addOnFailureListener { e ->
                                                Toast.makeText(context, "Gagal menyimpan resep: ${e.message}", Toast.LENGTH_SHORT).show()
                                            }
                                    } catch (e: Exception) {
                                        e.printStackTrace()
                                        Toast.makeText(context, "Terjadi kesalahan: ${e.message}", Toast.LENGTH_SHORT).show()
                                    }
                                }
                            }
                        },
                        colors = ButtonDefaults.buttonColors(
                            backgroundColor = if (isSystemInDarkTheme()) Ocean4 else Ocean7,
                            contentColor = Color.White
                        ),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .weight(1f)
                            .height(50.dp)
                    ) {
                        Text("Simpan", style = MaterialTheme.typography.button)
                    }
                }
            }
        }
    }
}

@Suppress("unused")
class WebAppInterface(
    private val onPickImage: () -> Unit,
    private val onReceiveContent: (String) -> Unit
) {
    @JavascriptInterface
    fun pickImage() {
        onPickImage()
    }

    @JavascriptInterface
    fun receiveContent(html: String) {
        onReceiveContent(html)
    }
}

suspend fun uploadThumbnail(uri: Uri, onProgress: (Float) -> Unit): String? =
    suspendCancellableCoroutine { continuation ->
        try {
            val storage = FirebaseStorage.getInstance()
            val storageRef = storage.reference
            val thumbnailRef = storageRef.child("thumbnails/${UUID.randomUUID()}.jpg")
            val uploadTask = thumbnailRef.putFile(uri)

            uploadTask.addOnProgressListener { taskSnapshot ->
                val progress =
                    (100.0 * taskSnapshot.bytesTransferred) / taskSnapshot.totalByteCount
                onProgress(progress.toFloat() / 100f)
            }.addOnSuccessListener {
                thumbnailRef.downloadUrl.addOnSuccessListener { downloadUrl ->
                    if (continuation.isActive) {
                        continuation.resumeWith(Result.success(downloadUrl.toString()))
                    }
                }.addOnFailureListener { e ->
                    if (continuation.isActive) {
                        continuation.resumeWith(Result.failure(e))
                    }
                }
            }.addOnFailureListener { e ->
                if (continuation.isActive) {
                    continuation.resumeWith(Result.failure(e))
                }
            }

            continuation.invokeOnCancellation {
                uploadTask.cancel()
            }
        } catch (e: Exception) {
            if (continuation.isActive) {
                continuation.resumeWith(Result.failure(e))
            }
        }
    }

fun convertUriToImageBitmap(context: Context, uri: Uri): ImageBitmap? {
    return try {
        val inputStream = context.contentResolver.openInputStream(uri)
        val bitmap = BitmapFactory.decodeStream(inputStream)
        inputStream?.close()
        bitmap?.asImageBitmap()
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }
}

fun convertImageToBase64(context: Context, uri: Uri): String? {
    return try {
        val inputStream = context.contentResolver.openInputStream(uri)
        val bytes = inputStream?.readBytes()
        inputStream?.close()
        bytes?.let {
            Base64.encodeToString(it, Base64.NO_WRAP)
        }
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }
}

fun Color.toHex(): String {
    return String.format("#%02X%02X%02X",
        (this.red * 255).toInt(),
        (this.green * 255).toInt(),
        (this.blue * 255).toInt()
    )
}
