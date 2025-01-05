package com.callcenter.smartclass.ui.home.admin.tambahproduk.brand

import android.annotation.SuppressLint
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.selection.LocalTextSelectionColors
import androidx.compose.foundation.text.selection.TextSelectionColors
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.TextFieldDefaults
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.rememberImagePainter
import com.callcenter.smartclass.ui.components.smartclassCard
import com.callcenter.smartclass.ui.theme.*
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

// Data class untuk merepresentasikan Brand
data class Brand(
    val id: String = "",
    val name: String = "",
    val imageUrl: String = "",
    val createdAt: Long = 0L
)

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun AddBrand() {
    val coroutineScope = rememberCoroutineScope()

    var brandName by remember { mutableStateOf("") }
    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }
    var isLoading by remember { mutableStateOf(false) }
    var message by remember { mutableStateOf<String?>(null) }

    val storage = FirebaseStorage.getInstance()
    val firestore = FirebaseFirestore.getInstance()

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent(),
        onResult = { uri: Uri? ->
            selectedImageUri = uri
        }
    )

    val isDarkMode = isSystemInDarkTheme()
    val isLight = !smartclassTheme.colors.isDark

    val customTextSelectionColors = TextSelectionColors(
        handleColor = MinimalPrimary,
        backgroundColor = MinimalPrimary.copy(alpha = 0.4f)
    )

    // State untuk daftar brands
    var brands by remember { mutableStateOf<List<Brand>>(emptyList()) }

    // Mengambil daftar brands dari Firestore
    LaunchedEffect(Unit) {
        try {
            val snapshot = firestore.collection("brands")
                .orderBy("createdAt")
                .get()
                .await()
            brands = snapshot.documents.map { doc ->
                doc.toObject(Brand::class.java)?.copy(id = doc.id) ?: Brand(id = doc.id)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            message = "Gagal memuat daftar brand: ${e.message}"
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Form Tambah Brand sebagai item pertama
        item {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Tambah Brand",
                    style = MaterialTheme.typography.titleMedium,
                    color = if (isLight) MinimalTextLight else MinimalTextDark,
                    modifier = Modifier
                        .padding(bottom = 24.dp)
                        .fillMaxWidth(),
                    textAlign = TextAlign.Center
                )

                CompositionLocalProvider(LocalTextSelectionColors provides customTextSelectionColors) {
                    OutlinedTextField(
                        value = brandName,
                        onValueChange = { brandName = it },
                        label = {
                            Text(
                                "Nama Brand",
                                color = if (isLight) MinimalTextLight else MinimalTextDark
                            )
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        colors = TextFieldDefaults.outlinedTextFieldColors(
                            backgroundColor = if (isDarkMode) MinimalBackgroundDark else MinimalBackgroundLight,
                            focusedBorderColor = MinimalPrimary,
                            unfocusedBorderColor = MinimalSecondary,
                            cursorColor = MinimalPrimary,
                            textColor = if (isDarkMode) MinimalTextDark else MinimalTextLight,
                        )
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                if (selectedImageUri != null) {
                    Image(
                        painter = rememberImagePainter(selectedImageUri),
                        contentDescription = "Gambar Brand",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .height(200.dp) // Sesuaikan ukuran gambar
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color.LightGray)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    TextButton(onClick = { selectedImageUri = null }) {
                        Text("Hapus Gambar", color = Color.Gray)
                    }
                } else {
                    Box(
                        modifier = Modifier
                            .height(200.dp) // Sesuaikan ukuran kotak gambar
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color.Gray)
                            .clickable { imagePickerLauncher.launch("image/*") },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Pilih Gambar",
                            color = Color.White,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                Button(
                    onClick = {
                        if (brandName.isNotBlank() && selectedImageUri != null) {
                            coroutineScope.launch {
                                isLoading = true
                                message = null
                                try {
                                    val storageRef = storage.reference.child("brands/${System.currentTimeMillis()}_${selectedImageUri?.lastPathSegment}")
                                    val uploadTask = storageRef.putFile(selectedImageUri!!)
                                    uploadTask.await()

                                    val downloadUrl = storageRef.downloadUrl.await().toString()

                                    val brandData = mapOf(
                                        "name" to brandName,
                                        "imageUrl" to downloadUrl,
                                        "createdAt" to System.currentTimeMillis()
                                    )
                                    val docRef = firestore.collection("brands")
                                        .add(brandData)
                                        .await()

                                    // Update local state
                                    val newBrand = Brand(
                                        id = docRef.id,
                                        name = brandName,
                                        imageUrl = downloadUrl,
                                        createdAt = System.currentTimeMillis()
                                    )
                                    brands = brands + newBrand

                                    brandName = ""
                                    selectedImageUri = null
                                    message = "Brand berhasil ditambahkan!"
                                } catch (e: Exception) {
                                    e.printStackTrace()
                                    message = "Terjadi kesalahan: ${e.message}"
                                } finally {
                                    isLoading = false
                                }
                            }
                        } else {
                            message = "Mohon isi nama brand dan pilih gambar."
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isDarkMode) ButtonDarkColor else ButtonLightColor,
                        contentColor = Color.White
                    ),
                    elevation = ButtonDefaults.buttonElevation(
                        defaultElevation = 8.dp
                    ),
                    enabled = !isLoading
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(
                            color = Color.White,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Menyimpan...", fontSize = 16.sp)
                    } else {
                        Text("Tambah Brand", fontSize = 16.sp)
                    }
                }

                message?.let {
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = it,
                        color = if (it.contains("berhasil", ignoreCase = true)) Color(0xFF4CAF50) else Color(0xFFF44336),
                        style = MaterialTheme.typography.bodySmall,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                Spacer(modifier = Modifier.height(32.dp))
            }
        }

        // Header untuk Daftar Brand
        item {
            Text(
                text = "Daftar Brand",
                style = MaterialTheme.typography.titleMedium,
                color = if (isLight) MinimalTextLight else MinimalTextDark,
                modifier = Modifier
                    .padding(bottom = 16.dp)
                    .fillMaxWidth(),
                textAlign = TextAlign.Center
            )
        }

        // Menampilkan pesan jika belum ada brand
        if (brands.isEmpty()) {
            item {
                Text(
                    text = "Belum ada brand yang ditambahkan.",
                    color = Color.Gray,
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp)
                )
            }
        } else {
            // Menampilkan daftar brand
            items(brands) { brand ->
                BrandItem(
                    brand = brand,
                    onDelete = {
                        coroutineScope.launch {
                            isLoading = true
                            try {
                                // Hapus gambar dari Storage
                                val storageRef = storage.getReferenceFromUrl(brand.imageUrl)
                                storageRef.delete().await()

                                // Hapus dokumen dari Firestore
                                firestore.collection("brands")
                                    .document(brand.id)
                                    .delete()
                                    .await()

                                // Update local state
                                brands = brands.filter { it.id != brand.id }
                                message = "Brand '${brand.name}' berhasil dihapus."
                            } catch (e: Exception) {
                                e.printStackTrace()
                                message = "Gagal menghapus brand: ${e.message}"
                            } finally {
                                isLoading = false
                            }
                        }
                    }
                )
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}

@Composable
fun BrandItem(brand: Brand, onDelete: () -> Unit) {

    val isLight = !smartclassTheme.colors.isDark

    smartclassCard(
        modifier = Modifier
            .fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Image(
                painter = rememberImagePainter(brand.imageUrl),
                contentDescription = brand.name,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(60.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color.LightGray)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = brand.name,
                style = MaterialTheme.typography.bodyLarge,
                color = if (isLight) MinimalTextLight else MinimalTextDark,
                modifier = Modifier.weight(1f)
            )
            IconButton(onClick = onDelete) {
                Icon(
                    imageVector = Icons.Filled.Delete,
                    contentDescription = "Hapus Brand",
                    tint = Color.Red
                )
            }
        }
    }
}
