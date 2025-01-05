package com.callcenter.smartclass.ui.home.admin.tambahproduk.produk

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.selection.LocalTextSelectionColors
import androidx.compose.foundation.text.selection.TextSelectionColors
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.TextFieldDefaults
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Image
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.rememberImagePainter
import com.callcenter.smartclass.ui.home.admin.tambahproduk.produk.data.Brand
import com.callcenter.smartclass.ui.home.admin.tambahproduk.produk.data.validateProductInput
import com.callcenter.smartclass.ui.theme.*
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

@Composable
fun AddProductUtama() {
    val coroutineScope = rememberCoroutineScope()
    val firestore = FirebaseFirestore.getInstance()
    val storage = FirebaseStorage.getInstance()

    // Variabel state
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var price by remember { mutableStateOf("") }
    var stock by remember { mutableStateOf("") } // State untuk stok
    var category by remember { mutableStateOf("") } // State untuk kategori
    var selectedBrand by remember { mutableStateOf<String?>(null) }
    var brandsList by remember { mutableStateOf<List<Brand>>(emptyList()) }

    var thumbnailUri by remember { mutableStateOf<Uri?>(null) }

    // List untuk menyimpan hingga 5 gambar opsional
    var optionalImages by remember { mutableStateOf<List<Uri?>>(listOf(null, null, null, null, null)) }

    var isLoading by remember { mutableStateOf(false) }
    var message by remember { mutableStateOf<String?>(null) }

    // Launcher untuk memilih gambar
    val thumbnailPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent(),
        onResult = { uri: Uri? ->
            thumbnailUri = uri
        }
    )

    val imagePickerLaunchers = List(5) { index ->
        rememberLauncherForActivityResult(
            contract = ActivityResultContracts.GetContent(),
            onResult = { uri: Uri? ->
                optionalImages = optionalImages.toMutableList().apply { set(index, uri) }
            }
        )
    }

    val isDarkMode = isSystemInDarkTheme()
    val isLight = !smartclassTheme.colors.isDark

    val customTextSelectionColors = TextSelectionColors(
        handleColor = if (isDarkMode) MinimalPrimary else MinimalPrimary,
        backgroundColor = if (isDarkMode) MinimalPrimary else MinimalPrimary.copy(alpha = 0.4f)
    )

    // Mengambil daftar brand dari Firestore
    LaunchedEffect(Unit) {
        try {
            val brandsSnapshot = firestore.collection("brands").get().await()
            brandsList = brandsSnapshot.documents.mapNotNull { doc ->
                doc.toObject(Brand::class.java)?.copy(id = doc.id)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            message = "Gagal mengambil daftar brand."
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        contentAlignment = Alignment.TopStart
    ) {
        Column(
            modifier = Modifier.fillMaxWidth()
        ) {
            // Judul Produk
            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                label = { Text("Judul Produk", color = if (isLight) MinimalTextLight else MinimalTextDark) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                singleLine = true,
                colors = TextFieldDefaults.outlinedTextFieldColors(
                    backgroundColor = if (isDarkMode) MinimalBackgroundDark else MinimalBackgroundLight,
                    focusedBorderColor = MinimalPrimary,
                    unfocusedBorderColor = MinimalSecondary,
                    cursorColor = MinimalPrimary,
                    textColor = if (isDarkMode) MinimalTextDark else MinimalTextLight,
                )
            )

            CompositionLocalProvider(LocalTextSelectionColors provides customTextSelectionColors) {
                // Deskripsi Produk
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = {
                        Text(
                            "Deskripsi Produk",
                            color = if (isLight) MinimalTextLight else MinimalTextDark
                        )
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(150.dp)
                        .padding(vertical = 8.dp),
                    maxLines = 5,
                    colors = TextFieldDefaults.outlinedTextFieldColors(
                        backgroundColor = if (isDarkMode) MinimalBackgroundDark else MinimalBackgroundLight,
                        focusedBorderColor = MinimalPrimary,
                        unfocusedBorderColor = MinimalSecondary,
                        cursorColor = MinimalPrimary,
                        textColor = if (isDarkMode) MinimalTextDark else MinimalTextLight,
                    )
                )
            }

            CompositionLocalProvider(LocalTextSelectionColors provides customTextSelectionColors) {
                // Harga Produk
                OutlinedTextField(
                    value = price,
                    onValueChange = { price = it },
                    label = { Text("Harga", color = if (isLight) MinimalTextLight else MinimalTextDark) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    colors = TextFieldDefaults.outlinedTextFieldColors(
                        backgroundColor = if (isDarkMode) MinimalBackgroundDark else MinimalBackgroundLight,
                        focusedBorderColor = MinimalPrimary,
                        unfocusedBorderColor = MinimalSecondary,
                        cursorColor = MinimalPrimary,
                        textColor = if (isDarkMode) MinimalTextDark else MinimalTextLight,
                    )
                )
            }

            CompositionLocalProvider(LocalTextSelectionColors provides customTextSelectionColors) {
                // Stok Produk
                OutlinedTextField(
                    value = stock,
                    onValueChange = { stock = it },
                    label = { Text("Stok", color = if (isLight) MinimalTextLight else MinimalTextDark) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    colors = TextFieldDefaults.outlinedTextFieldColors(
                        backgroundColor = if (isDarkMode) MinimalBackgroundDark else MinimalBackgroundLight,
                        focusedBorderColor = MinimalPrimary,
                        unfocusedBorderColor = MinimalSecondary,
                        cursorColor = MinimalPrimary,
                        textColor = if (isDarkMode) MinimalTextDark else MinimalTextLight,
                    )
                )
            }

            CompositionLocalProvider(LocalTextSelectionColors provides customTextSelectionColors) {
                // Kategori Produk
                OutlinedTextField(
                    value = category,
                    onValueChange = { category = it },
                    label = { Text("Kategori", color = if (isLight) MinimalTextLight else MinimalTextDark) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    singleLine = true,
                    colors = TextFieldDefaults.outlinedTextFieldColors(
                        backgroundColor = if (isDarkMode) MinimalBackgroundDark else MinimalBackgroundLight,
                        focusedBorderColor = MinimalPrimary,
                        unfocusedBorderColor = MinimalSecondary,
                        cursorColor = MinimalPrimary,
                        textColor = if (isDarkMode) MinimalTextDark else MinimalTextLight,
                    )
                )
            }

            // Dropdown untuk memilih Brand
            BrandDropdown(
                brands = brandsList,
                selectedBrand = selectedBrand,
                onBrandSelected = { selectedBrand = it }
            )

            if (brandsList.isNotEmpty()) {
                Text(
                    text = "Jumlah brand tersedia: ${brandsList.size}",
                    color = Color.Green,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            } else if (message == "Gagal mengambil daftar brand.") {
                Text(
                    text = message ?: "",
                    color = Color.Red,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Thumbnail Produk
            Text(
                text = "Thumbnail",
                color = if (isLight) MinimalTextLight else MinimalTextDark,
                fontSize = 16.sp,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            if (thumbnailUri != null) {
                Box(
                    modifier = Modifier
                        .size(150.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color.LightGray)
                ) {
                    Image(
                        painter = rememberImagePainter(thumbnailUri),
                        contentDescription = "Thumbnail",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                    IconButton(
                        onClick = { thumbnailUri = null },
                        modifier = Modifier.align(Alignment.TopEnd)
                    ) {
                        Icon(Icons.Default.Delete, contentDescription = "Hapus Thumbnail", tint = Color.Red)
                    }
                }
            } else {
                Box(
                    modifier = Modifier
                        .size(150.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color.Gray)
                        .clickable { thumbnailPickerLauncher.launch("image/*") },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.Image,
                        contentDescription = "Pilih Thumbnail",
                        tint = Color.White,
                        modifier = Modifier.size(40.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Gambar Opsional (1-5)
            Text(
                text = "Gambar Opsional",
                color = if (isLight) MinimalTextLight else MinimalTextDark,
                fontSize = 16.sp,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            Column {
                optionalImages.forEachIndexed { index, uri ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                    ) {
                        if (uri != null) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(vertical = 8.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(80.dp)
                                        .clip(RoundedCornerShape(8.dp)) // Ganti CircleShape menjadi RoundedCornerShape
                                        .background(Color.LightGray)
                                ) {
                                    Image(
                                        painter = rememberImagePainter(uri),
                                        contentDescription = "Gambar Opsional ${index + 1}",
                                        contentScale = ContentScale.Crop,
                                        modifier = Modifier.fillMaxSize()
                                    )
                                }

                                Spacer(modifier = Modifier.width(8.dp))

                                IconButton(
                                    onClick = {
                                        optionalImages = optionalImages.toMutableList().apply { set(index, null) }
                                    },
                                    modifier = Modifier
                                        .size(40.dp)
                                ) {
                                    Icon(
                                        Icons.Default.Delete,
                                        contentDescription = "Hapus Gambar",
                                        tint = Color.Red,
                                        modifier = Modifier.size(24.dp)
                                    )
                                }
                            }
                        } else {
                            Box(
                                modifier = Modifier
                                    .size(80.dp)
                                    .clip(RoundedCornerShape(8.dp)) // Menggunakan RoundedCornerShape juga untuk state kosong
                                    .background(if (isSystemInDarkTheme()) ButtonDarkColor else ButtonLightColor)
                                    .clickable { imagePickerLaunchers[index].launch("image/*") },
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    Icons.Default.Add,
                                    contentDescription = "Tambah Gambar",
                                    tint = Color.White,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Tombol Tambah Produk
            Button(
                onClick = {
                    if (validateProductInput(title, description, price, stock, category, selectedBrand, thumbnailUri)) {
                        coroutineScope.launch {
                            isLoading = true
                            message = null
                            try {
                                // Upload Thumbnail
                                val thumbnailUrl = uploadImage(storage, thumbnailUri!!, "thumbnails")

                                // Upload Gambar Opsional
                                val optionalUrls = mutableListOf<String>()
                                for (uri in optionalImages) {
                                    if (uri != null) {
                                        val url = uploadImage(storage, uri, "products/images")
                                        optionalUrls.add(url)
                                    }
                                }

                                // Konversi price dan stock
                                val priceValue = price.toDouble()
                                val stockValue = stock.toInt()

                                // Simpan Data Produk ke Firestore
                                val productData = mapOf(
                                    "title" to title,
                                    "description" to description,
                                    "price" to priceValue,
                                    "stock" to stockValue,
                                    "category" to category, // Sertakan kategori
                                    "brandId" to selectedBrand,
                                    "thumbnailUrl" to thumbnailUrl,
                                    "imageUrls" to optionalUrls,
                                    "createdAt" to System.currentTimeMillis()
                                )

                                firestore.collection("products").add(productData).await()

                                // Reset Form
                                title = ""
                                description = ""
                                price = ""
                                stock = ""
                                category = "" // Reset kategori
                                selectedBrand = null
                                thumbnailUri = null
                                optionalImages = listOf(null, null, null, null, null)
                                message = "Produk berhasil ditambahkan!"
                            } catch (e: Exception) {
                                e.printStackTrace()
                                message = "Terjadi kesalahan: ${e.message}"
                            } finally {
                                isLoading = false
                            }
                        }
                    } else {
                        message = "Mohon lengkapi semua field yang diperlukan."
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp), // Ukuran tombol sebelumnya
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isDarkMode) ButtonDarkColor else ButtonLightColor,
                    contentColor = Color.White
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
                    Text("Tambah Produk", fontSize = 16.sp)
                }
            }

            // Pesan
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
        }
    }
}

suspend fun uploadImage(storage: FirebaseStorage, uri: Uri, folder: String): String {
    val storageRef = storage.reference.child("$folder/${System.currentTimeMillis()}_${uri.lastPathSegment}")
    storageRef.putFile(uri).await()
    return storageRef.downloadUrl.await().toString()
}
