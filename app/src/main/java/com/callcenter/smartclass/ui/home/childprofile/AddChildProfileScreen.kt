package com.callcenter.smartclass.ui.home.childprofile

//noinspection UsingMaterialAndMaterial3Libraries
import android.app.DatePickerDialog
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.result.contract.ActivityResultContracts.RequestPermission
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.selection.LocalTextSelectionColors
import androidx.compose.foundation.text.selection.TextSelectionColors
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import com.callcenter.smartclass.ui.components.smartclassCard
import com.callcenter.smartclass.ui.theme.*
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.UploadTask
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddChildProfileScreen(navController: NavController) {
    var childName by remember { mutableStateOf("") }
    var birthDate by remember { mutableStateOf("") }
    var gender by remember { mutableStateOf("Laki-laki") }
    var height by remember { mutableStateOf("") }
    var weight by remember { mutableStateOf("") }
    var headCircumference by remember { mutableStateOf("") }
    var showDialog by remember { mutableStateOf(false) }
    var imageUri by remember { mutableStateOf<Uri?>(null) }
    var isUploading by remember { mutableStateOf(false) }
    var uploadProgress by remember { mutableStateOf(0f) }
    var showImageSourceDialog by remember { mutableStateOf(false) }
    var showPermissionDeniedDialog by remember { mutableStateOf(false) }

    val dateFormatter = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
    val context = LocalContext.current
    val isDarkMode = isSystemInDarkTheme()

    val db = FirebaseFirestore.getInstance()
    val auth = FirebaseAuth.getInstance()
    val storage = FirebaseStorage.getInstance()

    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent(),
        onResult = { uri: Uri? ->
            uri?.let { imageUri = it }
        }
    )

    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicturePreview(),
        onResult = { bitmap: Bitmap? ->
            bitmap?.let {
                val uri = getImageUriFromBitmap(context, it)
                imageUri = uri
            }
        }
    )

    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        contract = RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            cameraLauncher.launch(null)
        } else {
            showPermissionDeniedDialog = true
        }
    }

    val enterTransitionVisibility = remember { fadeIn(animationSpec = tween(1000)) }
    val exitTransitionVisibility = remember { fadeOut(animationSpec = tween(500)) }

    val customTextSelectionColors = TextSelectionColors(
        handleColor = if (isDarkMode) MinimalPrimary else MinimalPrimary,
        backgroundColor = if (isDarkMode) MinimalPrimary else MinimalPrimary.copy(alpha = 0.4f)
    )

    val isLight = !smartclassTheme.colors.isDark

    val scaffoldState = rememberScaffoldState()

    Scaffold(
        scaffoldState = scaffoldState,
        topBar = {
            TopAppBar(
                title = { Text("Tambah Profile Anak", color = if (isLight) MinimalTextLight else MinimalTextDark) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            Icons.Filled.ArrowBack,
                            contentDescription = "Kembali",
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
        content = { innerPadding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(smartclassTheme.colors.uiBackground)
                    .verticalScroll(rememberScrollState())
                    .padding(innerPadding)
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.height(24.dp))

                AnimatedVisibility(
                    visible = true,
                    enter = enterTransitionVisibility,
                    exit = exitTransitionVisibility
                ) {
                    smartclassCard(
                        modifier = Modifier.fillMaxWidth(),
                        elevation = 6.dp
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(120.dp)
                                    .clip(MaterialTheme.shapes.medium)
                                    .clickable(
                                        onClick = { showImageSourceDialog = true }
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                if (imageUri != null) {
                                    Image(
                                        painter = rememberAsyncImagePainter(imageUri),
                                        contentDescription = "Foto Profil Anak",
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .clip(MaterialTheme.shapes.medium)
                                    )
                                } else {
                                    Icon(
                                        imageVector = Icons.Filled.AccountCircle,
                                        contentDescription = "Foto Profil Anak",
                                        modifier = Modifier
                                            .size(100.dp)
                                            .padding(16.dp),
                                        tint = if (isDarkMode) Gray else Color.Gray
                                    )
                                }

                                Icon(
                                    imageVector = Icons.Filled.PhotoLibrary,
                                    contentDescription = "Upload Gambar",
                                    modifier = Modifier
                                        .size(28.dp)
                                        .align(Alignment.BottomEnd)
                                        .padding(4.dp),
                                    tint = if (isDarkMode) MinimalPrimary else MinimalPrimary
                                )
                            }

                            if (showPermissionDeniedDialog) {
                                AlertDialog(
                                    onDismissRequest = { showPermissionDeniedDialog = false },
                                    title = { Text("Izin Kamera Diperlukan") },
                                    text = { Text("Aplikasi ini memerlukan akses ke kamera untuk mengambil foto. Silakan izinkan akses kamera di pengaturan.") },
                                    confirmButton = {
                                        Button(
                                            onClick = { showPermissionDeniedDialog = false },
                                            colors = ButtonDefaults.buttonColors(
                                                backgroundColor = if (isDarkMode) ButtonDarkColor else ButtonLightColor,
                                                contentColor = if (isDarkMode) TextDarkColor else TextLightColor
                                            )
                                        ) {
                                            Text("Tutup")
                                        }
                                    },
                                    backgroundColor = if (isDarkMode) Color(0xFF212121) else MinimalBackgroundLight,
                                    contentColor = if (isDarkMode) Color.White else Color.Black,
                                    shape = MaterialTheme.shapes.medium,
                                    modifier = Modifier.padding(16.dp)
                                )
                            }

                            if (showImageSourceDialog) {
                                Dialog(onDismissRequest = { showImageSourceDialog = false }) {
                                    Surface(
                                        shape = MaterialTheme.shapes.medium,
                                        color = if (isDarkMode) Color(0xFF212121) else MinimalBackgroundLight,
                                        modifier = Modifier.padding(16.dp),
                                        elevation = 8.dp
                                    ) {
                                        Column(
                                            modifier = Modifier.padding(24.dp),
                                            horizontalAlignment = Alignment.CenterHorizontally
                                        ) {
                                            Text(
                                                "Pilih Sumber Gambar",
                                                style = MaterialTheme.typography.h6.copy(
                                                    fontSize = 20.sp,
                                                    color = if (isDarkMode) TextDarkColor else TextLightColor
                                                )
                                            )
                                            Spacer(modifier = Modifier.height(16.dp))
                                            Divider(color = if (isDarkMode) MinimalSecondary else MinimalSecondary)
                                            Spacer(modifier = Modifier.height(16.dp))
                                            Button(
                                                onClick = {
                                                    galleryLauncher.launch("image/*")
                                                    showImageSourceDialog = false
                                                },
                                                modifier = Modifier.fillMaxWidth(),
                                                colors = ButtonDefaults.buttonColors(
                                                    backgroundColor = if (isDarkMode) ButtonDarkColor else ButtonLightColor,
                                                    contentColor = if (isDarkMode) TextDarkColor else TextLightColor
                                                ),
                                                elevation = ButtonDefaults.elevation(4.dp)
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Filled.PhotoLibrary,
                                                    contentDescription = null,
                                                    modifier = Modifier.size(24.dp)
                                                )
                                                Spacer(modifier = Modifier.width(8.dp))
                                                Text("Dari Galeri")
                                            }
                                            Spacer(modifier = Modifier.height(8.dp))
                                            Button(
                                                onClick = {
                                                    if (ContextCompat.checkSelfPermission(context, android.Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
                                                        cameraLauncher.launch(null)
                                                        showImageSourceDialog = false
                                                    } else {
                                                        cameraPermissionLauncher.launch(android.Manifest.permission.CAMERA)
                                                        showImageSourceDialog = false
                                                    }
                                                },
                                                modifier = Modifier.fillMaxWidth(),
                                                colors = ButtonDefaults.buttonColors(
                                                    backgroundColor = if (isDarkMode) ButtonDarkColor else ButtonLightColor,
                                                    contentColor = if (isDarkMode) TextDarkColor else TextLightColor
                                                ),
                                                elevation = ButtonDefaults.elevation(4.dp)
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Filled.CameraAlt,
                                                    contentDescription = null,
                                                    modifier = Modifier.size(24.dp)
                                                )
                                                Spacer(modifier = Modifier.width(8.dp))
                                                Text("Dari Kamera")
                                            }
                                        }
                                    }
                                }
                            }

                            if (isUploading) {
                                Spacer(modifier = Modifier.height(8.dp))
                                LinearProgressIndicator(
                                    progress = uploadProgress,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(8.dp)
                                        .clip(MaterialTheme.shapes.small),
                                    color = if (isDarkMode) MinimalPrimary else MinimalPrimary
                                )
                                Spacer(modifier = Modifier.height(16.dp))
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            CompositionLocalProvider(LocalTextSelectionColors provides customTextSelectionColors) {
                                OutlinedTextField(
                                    value = childName,
                                    onValueChange = { childName = it },
                                    label = { Text("Nama Lengkap", color = MinimalPrimary) },
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = TextFieldDefaults.outlinedTextFieldColors(
                                        backgroundColor = if (isDarkMode) MinimalBackgroundDark else MinimalBackgroundLight,
                                        focusedBorderColor = MinimalPrimary,
                                        unfocusedBorderColor = MinimalSecondary,
                                        cursorColor = MinimalPrimary,
                                        textColor = if (isDarkMode) MinimalTextDark else MinimalTextLight
                                    )
                                )
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            // Menampilkan DatePickerDialog langsung di onClick
                            OutlinedTextField(
                                value = birthDate,
                                onValueChange = { birthDate = it },
                                label = { Text("Tanggal Lahir", color = MinimalPrimary) },
                                modifier = Modifier.fillMaxWidth(),
                                readOnly = true,
                                trailingIcon = {
                                    IconButton(onClick = {
                                        val calendar = Calendar.getInstance()
                                        val year = calendar.get(Calendar.YEAR)
                                        val month = calendar.get(Calendar.MONTH)
                                        val day = calendar.get(Calendar.DAY_OF_MONTH)

                                        DatePickerDialog(
                                            context,
                                            { _, selectedYear, selectedMonth, selectedDay ->
                                                val selectedDate = Calendar.getInstance()
                                                selectedDate.set(selectedYear, selectedMonth, selectedDay)
                                                birthDate = dateFormatter.format(selectedDate.time)
                                            },
                                            year, month, day
                                        ).show()
                                    }) {
                                        Icon(
                                            imageVector = Icons.Filled.CalendarToday,
                                            contentDescription = "Pilih Tanggal",
                                            tint = MinimalPrimary
                                        )
                                    }
                                },
                                colors = TextFieldDefaults.outlinedTextFieldColors(
                                    backgroundColor = if (isDarkMode) MinimalBackgroundDark else MinimalBackgroundLight,
                                    focusedBorderColor = MinimalPrimary,
                                    unfocusedBorderColor = MinimalSecondary,
                                    cursorColor = MinimalPrimary,
                                    textColor = if (isDarkMode) MinimalTextDark else MinimalTextLight
                                )
                            )

                            Spacer(modifier = Modifier.height(16.dp))

                            CompositionLocalProvider(LocalTextSelectionColors provides customTextSelectionColors) {
                                OutlinedTextField(
                                    value = height,
                                    onValueChange = {
                                        // Ganti koma dengan titik
                                        val newValue = it.replace(',', '.')
                                        // Opsional: Tambahkan validasi untuk hanya menerima angka dan satu titik desimal
                                        if (newValue.matches(Regex("^\\d*\\.?\\d*$"))) {
                                            height = newValue
                                        }
                                    },
                                    label = { Text("Tinggi Badan (cm)", color = MinimalPrimary) },
                                    modifier = Modifier.fillMaxWidth(),
                                    keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Decimal),
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

                            // Added OutlinedTextField for Berat Badan
                            CompositionLocalProvider(LocalTextSelectionColors provides customTextSelectionColors) {
                                OutlinedTextField(
                                    value = weight,
                                    onValueChange = {
                                        val newValue = it.replace(',', '.')
                                        if (newValue.matches(Regex("^\\d*\\.?\\d*$"))) {
                                            weight = newValue
                                        }
                                    },
                                    label = { Text("Berat Badan (kg)", color = MinimalPrimary) },
                                    modifier = Modifier.fillMaxWidth(),
                                    keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Decimal),
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

                            // Added OutlinedTextField for Lingkar Kepala
                            CompositionLocalProvider(LocalTextSelectionColors provides customTextSelectionColors) {
                                OutlinedTextField(
                                    value = headCircumference,
                                    onValueChange = {
                                        val newValue = it.replace(',', '.')
                                        if (newValue.matches(Regex("^\\d*\\.?\\d*$"))) {
                                            headCircumference = newValue
                                        }
                                    },
                                    label = { Text("Lingkar Kepala (cm)", color = MinimalPrimary) },
                                    modifier = Modifier.fillMaxWidth(),
                                    keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Decimal),
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

                            Text(
                                "Jenis Kelamin",
                                color = if (isDarkMode) DarkTextColor else LightTextColor
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceEvenly
                            ) {
                                // Tombol Laki-laki
                                Button(
                                    onClick = { gender = "Laki-laki" },
                                    colors = ButtonDefaults.buttonColors(
                                        backgroundColor = if (gender == "Laki-laki") MinimalPrimary else if (isDarkMode) MinimalBackgroundDark else MinimalBackgroundLight,
                                        contentColor = if (gender == "Laki-laki") Color.White else if (isDarkMode) MinimalTextDark else MinimalTextLight
                                    ),
                                    modifier = Modifier.size(64.dp),
                                    shape = MaterialTheme.shapes.small,
                                    elevation = ButtonDefaults.elevation(4.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.Male,
                                        contentDescription = "Laki-laki",
                                        tint = if (gender == "Laki-laki") Color.White else if (isDarkMode) MinimalTextDark else MinimalTextLight
                                    )
                                }
                                Button(
                                    onClick = { gender = "Perempuan" },
                                    colors = ButtonDefaults.buttonColors(
                                        backgroundColor = if (gender == "Perempuan") MinimalPrimary else if (isDarkMode) MinimalBackgroundDark else MinimalBackgroundLight,
                                        contentColor = if (gender == "Perempuan") Color.White else if (isDarkMode) MinimalTextDark else MinimalTextLight
                                    ),
                                    modifier = Modifier.size(64.dp),
                                    shape = MaterialTheme.shapes.small,
                                    elevation = ButtonDefaults.elevation(4.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.Female,
                                        contentDescription = "Perempuan",
                                        tint = if (gender == "Perempuan") Color.White else if (isDarkMode) MinimalTextDark else MinimalTextLight
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(14.dp))

                            Button(
                                onClick = {
                                    if (childName.isBlank() || birthDate.isBlank() || height.isBlank() || weight.isBlank() || headCircumference.isBlank() || imageUri == null) {
                                        showDialog = true
                                    } else {
                                        isUploading = true
                                        uploadProgress = 0f
                                        val userId = auth.currentUser?.uid
                                        if (userId != null) {
                                            val childData = hashMapOf(
                                                "name" to childName,
                                                "birthDate" to birthDate,
                                                "gender" to gender,
                                                "height" to height,
                                                "weight" to weight, // Added Berat Badan
                                                "headCircumference" to headCircumference // Added Lingkar Kepala
                                            )

                                            val childrenCollection = db.collection("users").document(userId).collection("children")
                                            childrenCollection.add(childData)
                                                .addOnSuccessListener { documentReference ->
                                                    val childId = documentReference.id
                                                    val storageRef = storage.reference.child("users/$userId/children/$childId/profile.jpg")
                                                    val uploadTask: UploadTask = storageRef.putFile(imageUri!!)

                                                    uploadTask.addOnProgressListener { taskSnapshot ->
                                                        val progress = (100.0 * taskSnapshot.bytesTransferred) / taskSnapshot.totalByteCount
                                                        uploadProgress = (progress / 100).toFloat()
                                                    }.addOnSuccessListener {
                                                        storageRef.downloadUrl.addOnSuccessListener { uri ->
                                                            childrenCollection.document(childId)
                                                                .update("profileImageUrl", uri.toString())
                                                                .addOnSuccessListener {
                                                                    Log.d("Firestore", "Child added with ID: $childId")
                                                                    isUploading = false
                                                                    navController.popBackStack()
                                                                }
                                                                .addOnFailureListener { e ->
                                                                    Log.e("Firestore", "Error updating child with image URL", e)
                                                                    isUploading = false
                                                                }
                                                        }.addOnFailureListener { e ->
                                                            Log.e("Storage", "Error getting download URL", e)
                                                            isUploading = false
                                                        }
                                                    }
                                                        .addOnFailureListener { e ->
                                                            Log.e("Storage", "Error uploading image", e)
                                                            isUploading = false
                                                        }
                                                }
                                                .addOnFailureListener { e ->
                                                    Log.e("Firestore", "Error adding child", e)
                                                    isUploading = false
                                                }
                                        }
                                    }
                                },
                                modifier = Modifier.fillMaxWidth(),
                                colors = ButtonDefaults.buttonColors(
                                    backgroundColor = if (isDarkMode) ButtonDarkColor else ButtonLightColor,
                                    contentColor = if (isDarkMode) WhiteColor else WhiteColor
                                ),
                                elevation = ButtonDefaults.elevation(8.dp),
                                enabled = !isUploading
                            ) {
                                Text("Simpan Profil")
                            }
                        }
                    }
                }

                if (showDialog) {
                    AlertDialog(
                        onDismissRequest = { showDialog = false },
                        title = { Text("Peringatan") },
                        text = { Text("Harap lengkapi semua data terlebih dahulu.") },
                        confirmButton = {
                            Button(
                                onClick = { showDialog = false },
                                colors = ButtonDefaults.buttonColors(
                                    backgroundColor = if (isDarkMode) ButtonDarkColor else ButtonLightColor,
                                    contentColor = if (isDarkMode) TextDarkColor else TextLightColor
                                )
                            ) {
                                Text("Tutup")
                            }
                        },
                        backgroundColor = if (isDarkMode) Color(0xFF212121) else MinimalBackgroundLight,
                        contentColor = if (isDarkMode) Color.White else Color.Black,
                        shape = MaterialTheme.shapes.medium,
                        modifier = Modifier.padding(16.dp)
                    )
                }
            }
        }
    )
}

fun getImageUriFromBitmap(context: Context, bitmap: Bitmap): Uri {
    val tempFile = File(context.cacheDir, "${UUID.randomUUID()}.jpg")
    tempFile.outputStream().use { outStream ->
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outStream)
        outStream.flush()
    }
    return FileProvider.getUriForFile(
        context,
        "${context.packageName}.fileprovider",
        tempFile
    )
}
