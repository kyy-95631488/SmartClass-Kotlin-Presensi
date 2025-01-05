package com.callcenter.smartclass.ui.home.childprofile

import android.app.DatePickerDialog
import android.net.Uri
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.selection.LocalTextSelectionColors
import androidx.compose.foundation.text.selection.TextSelectionColors
import androidx.compose.foundation.verticalScroll
//noinspection UsingMaterialAndMaterial3Libraries
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Person
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import com.callcenter.smartclass.data.ChildProfile
import com.callcenter.smartclass.ui.components.smartclassCard
import com.callcenter.smartclass.ui.home.admin.InputField
import com.callcenter.smartclass.ui.theme.*
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun EditChildProfile(childId: String, navController: NavController) {
    val db = FirebaseFirestore.getInstance()
    val storage = FirebaseStorage.getInstance()
    val auth = FirebaseAuth.getInstance()

    var childProfile by remember { mutableStateOf<ChildProfile?>(null) }
    var name by remember { mutableStateOf("") }
    var birthDate by remember { mutableStateOf("") }
    var gender by remember { mutableStateOf("") }
    var height by remember { mutableStateOf("") }
    var weight by remember { mutableStateOf("") } // Added
    var headCircumference by remember { mutableStateOf("") } // Added
    var isSaving by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var successMessage by remember { mutableStateOf<String?>(null) }
    var imageUri by remember { mutableStateOf<Uri?>(null) }
    var isUploadingImage by remember { mutableStateOf(false) }

    val context = LocalContext.current
    val calendar = Calendar.getInstance()
    val isDarkMode = isSystemInDarkTheme()

    val customTextSelectionColors = TextSelectionColors(
        handleColor = if (isDarkMode) MinimalPrimary else MinimalPrimary,
        backgroundColor = if (isDarkMode) MinimalPrimary else MinimalPrimary.copy(alpha = 0.4f)
    )

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent(),
        onResult = { uri: Uri? ->
            uri?.let {
                imageUri = it
            }
        }
    )

    LaunchedEffect(childId) {
        try {
            val document = db.collection("users")
                .document(auth.currentUser?.uid ?: "")
                .collection("children")
                .document(childId)
                .get()
                .await()
            childProfile = document.toObject(ChildProfile::class.java)
            name = childProfile?.name ?: ""
            birthDate = childProfile?.birthDate ?: ""
            gender = childProfile?.gender ?: ""
            height = childProfile?.height ?: ""
            weight = childProfile?.weight ?: ""
            headCircumference = childProfile?.headCircumference ?: ""
        } catch (e: Exception) {
            Log.e("EditChildProfile", "Error fetching profile: ${e.message}")
            errorMessage = "Gagal memuat profil."
        }
    }

    fun uploadImageAndUpdateProfile(childId: String, onComplete: (Boolean) -> Unit) {
        val userId = auth.currentUser?.uid ?: return
        val storageRef = storage.reference.child("users/$userId/children/$childId/profile.jpg")

        isUploadingImage = true

        storageRef.putFile(imageUri!!)
            .addOnSuccessListener { taskSnapshot ->
                storageRef.downloadUrl.addOnSuccessListener { uri ->
                    val updateData = mapOf(
                        "profileImageUrl" to uri.toString(),
                        "lastUpdated" to com.google.firebase.firestore.FieldValue.serverTimestamp()
                    )

                    db.collection("users")
                        .document(userId)
                        .collection("children")
                        .document(childId)
                        .update(updateData)
                        .addOnSuccessListener {
                            isUploadingImage = false
                            onComplete(true)
                        }
                        .addOnFailureListener { e ->
                            Log.e("EditChildProfile", "Error updating image URL: ${e.message}")
                            isUploadingImage = false
                            errorMessage = "Gagal memperbarui gambar profil."
                            onComplete(false)
                        }
                }.addOnFailureListener { e ->
                    Log.e("EditChildProfile", "Error getting download URL: ${e.message}")
                    isUploadingImage = false
                    errorMessage = "Gagal mendapatkan URL gambar."
                    onComplete(false)
                }
            }
            .addOnFailureListener { e ->
                Log.e("EditChildProfile", "Error uploading image: ${e.message}")
                isUploadingImage = false
                errorMessage = "Gagal mengunggah gambar."
                onComplete(false)
            }
    }

    val enterTransition = remember { fadeIn(animationSpec = tween(1000)) }
    val exitTransition = remember { fadeOut(animationSpec = tween(500)) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .background(if (isDarkMode) DarkBackgroundColor else LightBackgroundColor)
            .verticalScroll(rememberScrollState())
    ) {
        AnimatedVisibility(
            visible = true,
            enter = enterTransition,
            exit = exitTransition
        ) {
            Text(
                "Edit Profil Anak",
                style = MaterialTheme.typography.h5,
                color = if (isDarkMode) DarkTextColor else LightTextColor
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        Box(
            modifier = Modifier
                .size(120.dp)
                .align(Alignment.CenterHorizontally)
                .border(4.dp, Color.Gray, CircleShape)
                .background(Color.Gray.copy(alpha = 0.2f), CircleShape)
                .clickable {
                    launcher.launch("image/*")
                }
                .padding(2.dp),
            contentAlignment = Alignment.Center
        ) {
            if (imageUri != null) {
                Image(
                    painter = rememberAsyncImagePainter(imageUri),
                    contentDescription = "Foto Profil Anak",
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(CircleShape)
                )
            } else if (!childProfile?.profileImageUrl.isNullOrEmpty()) {
                Image(
                    painter = rememberAsyncImagePainter(childProfile?.profileImageUrl),
                    contentDescription = "Foto Profil Anak",
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(CircleShape)
                )
            } else {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = "Profile Placeholder",
                    modifier = Modifier
                        .size(60.dp),
                    tint = Color.Gray
                )
            }
        }

        AnimatedVisibility(
            visible = isUploadingImage,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            LinearProgressIndicator(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                color = if (isDarkMode) MinimalPrimary else MinimalPrimary,
                backgroundColor = if (isDarkMode) MinimalBackgroundDark else MinimalBackgroundLight
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        smartclassCard(
            modifier = Modifier.fillMaxWidth(),
            elevation = 6.dp
        ) {
            Column(modifier = Modifier.padding(16.dp)) {

                CompositionLocalProvider(LocalTextSelectionColors provides customTextSelectionColors) {
                    AnimatedVisibility(
                        visible = true,
                        enter = slideInVertically(initialOffsetY = { -40 }) + fadeIn(),
                        exit = slideOutVertically(targetOffsetY = { 40 }) + fadeOut()
                    ) {
                        InputField(
                            label = "Nama Anak",
                            value = name,
                            onValueChange = { name = it },
                            textColor = if (isDarkMode) MinimalTextDark else MinimalTextLight
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                CompositionLocalProvider(LocalTextSelectionColors provides customTextSelectionColors) {
                    AnimatedVisibility(
                        visible = true,
                        enter = slideInVertically(initialOffsetY = { -40 }) + fadeIn(),
                        exit = slideOutVertically(targetOffsetY = { 40 }) + fadeOut()
                    ) {
                        DatePickerField(
                            label = "Tanggal Lahir",
                            date = birthDate,
                            onDateSelected = { selectedDate ->
                                birthDate = selectedDate
                            },
                            textColor = if (isDarkMode) MinimalTextDark else MinimalTextLight,
                            calendar = calendar,
                            context = context
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                AnimatedVisibility(
                    visible = true,
                    enter = slideInVertically(initialOffsetY = { -40 }) + fadeIn(),
                    exit = slideOutVertically(targetOffsetY = { 40 }) + fadeOut()
                ) {
                    GenderDropdown(
                        selectedGender = gender,
                        onGenderSelected = { gender = it },
                        textColor = if (isDarkMode) MinimalTextDark else MinimalTextLight
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                CompositionLocalProvider(LocalTextSelectionColors provides customTextSelectionColors) {
                    // Height Input Field
                    AnimatedVisibility(
                        visible = true,
                        enter = slideInVertically(initialOffsetY = { -40 }) + fadeIn(),
                        exit = slideOutVertically(targetOffsetY = { 40 }) + fadeOut()
                    ) {
                        InputField(
                            label = "Tinggi Badan (cm)",
                            value = height,
                            onValueChange = { height = it },
                            textColor = if (isDarkMode) MinimalTextDark else MinimalTextLight,
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Number,
                                imeAction = ImeAction.Next
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                CompositionLocalProvider(LocalTextSelectionColors provides customTextSelectionColors) {
                    AnimatedVisibility(
                        visible = true,
                        enter = slideInVertically(initialOffsetY = { -40 }) + fadeIn(),
                        exit = slideOutVertically(targetOffsetY = { 40 }) + fadeOut()
                    ) {
                        InputField(
                            label = "Berat Badan (kg)",
                            value = weight,
                            onValueChange = { weight = it },
                            textColor = if (isDarkMode) MinimalTextDark else MinimalTextLight,
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Decimal,
                                imeAction = ImeAction.Next
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                CompositionLocalProvider(LocalTextSelectionColors provides customTextSelectionColors) {
                    AnimatedVisibility(
                        visible = true,
                        enter = slideInVertically(initialOffsetY = { -40 }) + fadeIn(),
                        exit = slideOutVertically(targetOffsetY = { 40 }) + fadeOut()
                    ) {
                        InputField(
                            label = "Lingkar Kepala (cm)",
                            value = headCircumference,
                            onValueChange = { headCircumference = it },
                            textColor = if (isDarkMode) MinimalTextDark else MinimalTextLight,
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Decimal,
                                imeAction = ImeAction.Done
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                AnimatedVisibility(
                    visible = true,
                    enter = scaleIn(initialScale = 0.8f) + fadeIn(),
                    exit = scaleOut(targetScale = 0.8f) + fadeOut()
                ) {
                    Button(
                        onClick = {
                            if (name.isBlank() || birthDate.isBlank() || gender.isBlank() || height.isBlank()
                                || weight.isBlank() || headCircumference.isBlank()
                            ) {
                                errorMessage = "Semua bidang harus diisi."
                                return@Button
                            }

                            isSaving = true

                            if (imageUri != null) {
                                uploadImageAndUpdateProfile(childId) { success ->
                                    if (success) {
                                        updateProfileFields(db, auth, childId, name, birthDate, gender, height, weight, headCircumference) { updateSuccess ->
                                            if (updateSuccess) {
                                                isSaving = false
                                                successMessage = "Profil anak berhasil diperbarui."
                                            } else {
                                                isSaving = false
                                            }
                                        }
                                    } else {
                                        isSaving = false
                                    }
                                }
                            } else {
                                updateProfileFields(db, auth, childId, name, birthDate, gender, height, weight, headCircumference) { updateSuccess ->
                                    if (updateSuccess) {
                                        isSaving = false
                                        successMessage = "Profil anak berhasil diperbarui."
                                    } else {
                                        isSaving = false
                                    }
                                }
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = 48.dp),
                        colors = ButtonDefaults.buttonColors(
                            backgroundColor = if (isDarkMode) ButtonDarkColor else ButtonLightColor,
                            contentColor = if (isDarkMode) WhiteColor else DarkText
                        ),
                        enabled = !isSaving && !isUploadingImage
                    ) {
                        if (isSaving || isUploadingImage) {
                            CircularProgressIndicator(
                                color = Color.White,
                                modifier = Modifier
                                    .size(20.dp)
                                    .padding(end = 8.dp),
                                strokeWidth = 2.dp
                            )
                        }
                        Text("Simpan")
                    }
                }
            }
        }

        if (errorMessage != null) {
            AlertDialog(
                onDismissRequest = { errorMessage = null },
                title = {
                    Text(
                        text = "Terjadi Kesalahan",
                        color = if (isDarkMode) MinimalTextDark else MinimalTextLight
                    )
                },
                text = {
                    Text(
                        text = errorMessage!!,
                        color = if (isDarkMode) MinimalTextDark else MinimalTextLight
                    )
                },
                confirmButton = {
                    TextButton(onClick = { errorMessage = null }) {
                        Text("OK")
                    }
                },
                backgroundColor = if (isDarkMode) DarkBackgroundColor else LightBackgroundColor,
                contentColor = if (isDarkMode) MinimalTextDark else MinimalTextLight
            )
        }

        if (successMessage != null) {
            AlertDialog(
                onDismissRequest = { /* Do nothing to force user to press OK */ },
                title = {
                    Text(
                        text = "Sukses",
                        color = if (isDarkMode) MinimalTextDark else MinimalTextLight
                    )
                },
                text = {
                    Text(
                        text = successMessage!!,
                        color = if (isDarkMode) MinimalTextDark else MinimalTextLight
                    )
                },
                confirmButton = {
                    TextButton(onClick = {
                        successMessage = null
                        navController.popBackStack()
                    }) {
                        Text("OK")
                    }
                },
                backgroundColor = if (isDarkMode) DarkBackgroundColor else LightBackgroundColor,
                contentColor = if (isDarkMode) MinimalTextDark else MinimalTextLight
            )
        }
    }
}

@Composable
fun InputField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    textColor: Color,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    readOnly: Boolean = false
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            label,
            style = MaterialTheme.typography.body2,
            color = if (isSystemInDarkTheme()) MinimalTextDark else MinimalTextLight
        )
        Spacer(modifier = Modifier.height(4.dp))

        OutlinedTextField(
            value = value,
            onValueChange = { if (!readOnly) onValueChange(it) },
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp),
            textStyle = MaterialTheme.typography.body1.copy(color = textColor),
            keyboardOptions = keyboardOptions,
            enabled = !readOnly,
            colors = TextFieldDefaults.outlinedTextFieldColors(
                backgroundColor = if (isSystemInDarkTheme()) MinimalBackgroundDark else MinimalBackgroundLight,
                focusedBorderColor = MinimalPrimary,
                unfocusedBorderColor = MinimalSecondary,
                cursorColor = MinimalPrimary,
                textColor = textColor
            )
        )
    }
}

private fun updateProfileFields(
    db: FirebaseFirestore,
    auth: FirebaseAuth,
    childId: String,
    name: String,
    birthDate: String,
    gender: String,
    height: String,
    weight: String,
    headCircumference: String,
    onComplete: (Boolean) -> Unit
) {
    val userId = auth.currentUser?.uid ?: return

    val updateData = mapOf(
        "name" to name,
        "birthDate" to birthDate,
        "gender" to gender,
        "height" to height,
        "weight" to weight,
        "headCircumference" to headCircumference,
        "lastUpdated" to com.google.firebase.firestore.FieldValue.serverTimestamp()
    )

    db.collection("users")
        .document(userId)
        .collection("children")
        .document(childId)
        .update(updateData)
        .addOnSuccessListener {
            onComplete(true)
        }
        .addOnFailureListener { e ->
            Log.e("EditChildProfile", "Error updating profile fields: ${e.message}")
            onComplete(false)
        }
}

@Composable
fun DatePickerField(
    label: String,
    date: String,
    onDateSelected: (String) -> Unit,
    textColor: Color,
    calendar: Calendar,
    context: android.content.Context
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            label,
            style = MaterialTheme.typography.body2,
            color = if (isSystemInDarkTheme()) MinimalTextDark else MinimalTextLight
        )
        Spacer(modifier = Modifier.height(4.dp))

        OutlinedTextField(
            value = date,
            onValueChange = { /* Read-only */ },
            modifier = Modifier
                .fillMaxWidth()
                .clickable {
                    val datePicker = DatePickerDialog(
                        context,
                        { _, year, month, dayOfMonth ->
                            val selectedDate = Calendar.getInstance().apply {
                                set(year, month, dayOfMonth)
                            }.time
                            val formatter = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                            onDateSelected(formatter.format(selectedDate))
                        },
                        calendar.get(Calendar.YEAR),
                        calendar.get(Calendar.MONTH),
                        calendar.get(Calendar.DAY_OF_MONTH)
                    )
                    datePicker.show()
                },
            readOnly = true,
            textStyle = MaterialTheme.typography.body1.copy(color = textColor),
            trailingIcon = {
                IconButton(onClick = {
                    val datePicker = DatePickerDialog(
                        context,
                        { _, year, month, dayOfMonth ->
                            val selectedDate = Calendar.getInstance().apply {
                                set(year, month, dayOfMonth)
                            }.time
                            val formatter = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                            onDateSelected(formatter.format(selectedDate))
                        },
                        calendar.get(Calendar.YEAR),
                        calendar.get(Calendar.MONTH),
                        calendar.get(Calendar.DAY_OF_MONTH)
                    )
                    datePicker.show()
                }) {
                    Icon(
                        imageVector = Icons.Default.CalendarToday,
                        contentDescription = "Pilih Tanggal Lahir",
                        tint = if (isSystemInDarkTheme()) MinimalTextDark else MinimalTextLight
                    )
                }
            },
            colors = TextFieldDefaults.outlinedTextFieldColors(
                backgroundColor = if (isSystemInDarkTheme()) MinimalBackgroundDark else MinimalBackgroundLight,
                focusedBorderColor = MinimalPrimary,
                unfocusedBorderColor = MinimalSecondary,
                cursorColor = MinimalPrimary,
                textColor = textColor
            )
        )
    }
}

@Composable
fun GenderDropdown(
    selectedGender: String,
    onGenderSelected: (String) -> Unit,
    textColor: Color
) {
    val genders = listOf("Laki-laki", "Perempuan", "Lainnya")
    var expanded by remember { mutableStateOf(false) }

    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            "Jenis Kelamin",
            style = MaterialTheme.typography.body2,
            color = if (isSystemInDarkTheme()) MinimalTextDark else MinimalTextLight
        )
        Spacer(modifier = Modifier.height(4.dp))

        OutlinedTextField(
            value = selectedGender,
            onValueChange = { /* Read-only */ },
            modifier = Modifier
                .fillMaxWidth()
                .clickable { expanded = true },
            readOnly = true,
            textStyle = MaterialTheme.typography.body1.copy(color = textColor),
            trailingIcon = {
                IconButton(onClick = { expanded = !expanded }) {
                    Icon(
                        imageVector = Icons.Default.ArrowDropDown,
                        contentDescription = "Dropdown",
                        tint = if (isSystemInDarkTheme()) MinimalTextDark else MinimalTextLight
                    )
                }
            },
            colors = TextFieldDefaults.outlinedTextFieldColors(
                backgroundColor = if (isSystemInDarkTheme()) MinimalBackgroundDark else MinimalBackgroundLight,
                focusedBorderColor = MinimalPrimary,
                unfocusedBorderColor = MinimalSecondary,
                cursorColor = MinimalPrimary,
                textColor = textColor
            )
        )

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier.fillMaxWidth()
        ) {
            genders.forEach { genderOption ->
                DropdownMenuItem(onClick = {
                    onGenderSelected(genderOption)
                    expanded = false
                }) {
                    Text(text = genderOption)
                }
            }
        }
    }
}
