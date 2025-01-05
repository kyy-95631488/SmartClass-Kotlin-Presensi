package com.callcenter.smartclass.ui.home.account

import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.Uri
import android.os.Build
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.selection.LocalTextSelectionColors
import androidx.compose.foundation.text.selection.TextSelectionColors
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.filled.Warning
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import coil.compose.rememberAsyncImagePainter
import com.callcenter.smartclass.ui.theme.*
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.storage.storage

@SuppressLint("ComposableNaming")
@Composable
fun myaccount() {
    val firebaseAuth = FirebaseAuth.getInstance()
    val currentUser = firebaseAuth.currentUser

    val context = LocalContext.current
    val isConnected by remember { mutableStateOf(checkInternetConnection(context)) }

    if (currentUser == null || !isConnected) {
        EmptyState()
    } else {
        AccountContent(firebaseAuth, currentUser)
    }
}

@Composable
fun EmptyState() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Filled.Warning,
            contentDescription = "Empty State",
            modifier = Modifier.size(100.dp),
            tint = Color.Gray
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "Tidak ada sesi yang aktif atau koneksi internet.",
            style = MaterialTheme.typography.h6,
            color = Color.Gray
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Silakan periksa koneksi internet Anda atau login kembali.",
            style = MaterialTheme.typography.body2,
            color = Color.Gray
        )
    }
}

@Composable
fun AccountContent(firebaseAuth: FirebaseAuth, currentUser: FirebaseUser) {
    val username = remember { mutableStateOf(TextFieldValue(currentUser.displayName ?: "")) }
    val email = remember { mutableStateOf(TextFieldValue(currentUser.email ?: "")) }
    val password = remember { mutableStateOf(TextFieldValue()) }

    var showDialog by remember { mutableStateOf(false) }
    var dialogMessage by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }

    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }
    var uploading by remember { mutableStateOf(false) }

    val context = LocalContext.current

    // Mendefinisikan izin yang diperlukan
    val permission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        android.Manifest.permission.READ_MEDIA_IMAGES
    } else {
        android.Manifest.permission.READ_EXTERNAL_STORAGE
    }

    val permissionStatus = ContextCompat.checkSelfPermission(context, permission)
    val hasPermission = permissionStatus == PackageManager.PERMISSION_GRANTED

    // Dialog untuk menjelaskan mengapa aplikasi membutuhkan izin
    var showRationale by remember { mutableStateOf(false) }

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent(),
        onResult = { uri: Uri? ->
            uri?.let {
                selectedImageUri = it
                uploading = true
                uploadProfileImage(it, firebaseAuth) { success, message ->
                    uploading = false
                    showDialog = true
                    dialogMessage = message
                    if (success) {
                        firebaseAuth.currentUser?.reload()
                    }
                }
            }
        }
    )

    // Fungsi untuk memeriksa apakah harus menampilkan penjelasan
    fun shouldShowRequestPermissionRationale(context: Context, permission: String): Boolean {
        val activity = context.findActivity()
        return activity?.let {
            androidx.core.app.ActivityCompat.shouldShowRequestPermissionRationale(it, permission)
        } == true
    }

    // Launcher untuk meminta izin
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { isGranted ->
            if (isGranted) {
                launcher.launch("image/*")
            } else {
                val activity = context.findActivity()
                if (activity != null && shouldShowRequestPermissionRationale(activity, permission)) {
                    showRationale = true
                } else {
                    showDialog = true
                    dialogMessage = "Izin ditolak. Anda dapat mengubah izin di pengaturan aplikasi."
                }
            }
        }
    )

    LaunchedEffect(currentUser) {
        username.value = TextFieldValue(currentUser.displayName ?: "")
        email.value = TextFieldValue(currentUser.email ?: "")
    }

    if (showDialog) {
        ResponsiveDialog(
            message = dialogMessage,
            onDismiss = { showDialog = false }
        )
    }

    // Tampilkan dialog penjelasan jika diperlukan
    if (showRationale) {
        AlertDialog(
            onDismissRequest = { showRationale = false },
            title = { Text("Izin Diperlukan") },
            text = { Text("Aplikasi membutuhkan akses ke galeri Anda untuk memperbarui foto profil.") },
            confirmButton = {
                TextButton(onClick = {
                    showRationale = false
                    permissionLauncher.launch(permission)
                }) {
                    Text("OK")
                }
            },
            dismissButton = {
                TextButton(onClick = { showRationale = false }) {
                    Text("Batal")
                }
            }
        )
    }

    val customTextSelectionColors = TextSelectionColors(
        handleColor = if (isSystemInDarkTheme()) MinimalPrimary else MinimalPrimary,
        backgroundColor = if (isSystemInDarkTheme()) MinimalPrimary else MinimalPrimary.copy(alpha = 0.4f)
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Spacer(modifier = Modifier.height(16.dp))

        Box(contentAlignment = Alignment.BottomEnd) {
            if (currentUser.photoUrl != null) {
                val painter = rememberAsyncImagePainter(currentUser.photoUrl)
                Image(
                    painter = painter,
                    contentDescription = "Profile Picture",
                    modifier = Modifier
                        .size(100.dp)
                        .clip(CircleShape)
                        .clickable {
                            if (hasPermission) {
                                launcher.launch("image/*")
                            } else {
                                permissionLauncher.launch(permission)
                            }
                        },
                    contentScale = ContentScale.Crop
                )
            } else {
                // Placeholder atau inisial
                Box(
                    modifier = Modifier
                        .size(100.dp)
                        .clip(CircleShape)
                        .background(Color.Gray),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = currentUser.displayName?.firstOrNull()?.toString() ?: "?",
                        style = MaterialTheme.typography.h4,
                        color = Color.White
                    )
                }
            }

            // Ikon edit
            Icon(
                imageVector = Icons.Default.Edit,
                contentDescription = "Edit Profile Picture",
                modifier = Modifier
                    .size(24.dp)
                    .background(Color.White, CircleShape)
                    .padding(4.dp)
                    .clickable {
                        if (hasPermission) {
                            launcher.launch("image/*")
                        } else {
                            permissionLauncher.launch(permission)
                        }
                    },
                tint = MinimalPrimary
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Username TextField
        CompositionLocalProvider(LocalTextSelectionColors provides customTextSelectionColors) {
            OutlinedTextField(
                value = username.value,
                onValueChange = { username.value = it },
                label = { Text("Username", color = if (isSystemInDarkTheme()) MinimalTextDark else MinimalTextLight) },
                placeholder = { Text("Enter username", color = MinimalSecondary) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                colors = TextFieldDefaults.outlinedTextFieldColors(
                    backgroundColor = if (isSystemInDarkTheme()) MinimalBackgroundDark else MinimalBackgroundLight,
                    focusedBorderColor = MinimalPrimary,
                    unfocusedBorderColor = MinimalSecondary,
                    cursorColor = MinimalPrimary,
                    textColor = if (isSystemInDarkTheme()) MinimalTextDark else MinimalTextLight
                )
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Email TextField (Disabled)
        OutlinedTextField(
            value = email.value,
            onValueChange = { email.value = it },
            label = { Text("Email", color = if (isSystemInDarkTheme()) MinimalTextDark else MinimalTextLight) },
            placeholder = { Text("Enter email", color = MinimalSecondary) },
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            colors = TextFieldDefaults.outlinedTextFieldColors(
                backgroundColor = if (isSystemInDarkTheme()) MinimalBackgroundDark else MinimalBackgroundLight,
                focusedBorderColor = MinimalPrimary,
                unfocusedBorderColor = MinimalSecondary,
                cursorColor = MinimalPrimary,
                textColor = if (isSystemInDarkTheme()) MinimalTextDark else MinimalTextLight
            ),
            enabled = false
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Password TextField
        CompositionLocalProvider(LocalTextSelectionColors provides customTextSelectionColors) {
            OutlinedTextField(
                value = password.value,
                onValueChange = { password.value = it },
                label = { Text("Password", color = if (isSystemInDarkTheme()) MinimalTextDark else MinimalTextLight) },
                placeholder = { Text("Enter password", color = MinimalSecondary) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                colors = TextFieldDefaults.outlinedTextFieldColors(
                    backgroundColor = if (isSystemInDarkTheme()) MinimalBackgroundDark else MinimalBackgroundLight,
                    focusedBorderColor = MinimalPrimary,
                    unfocusedBorderColor = MinimalSecondary,
                    cursorColor = MinimalPrimary,
                    textColor = if (isSystemInDarkTheme()) MinimalTextDark else MinimalTextLight
                ),
                visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                trailingIcon = {
                    val image = if (passwordVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff
                    IconButton(onClick = { passwordVisible = !passwordVisible }) {
                        Icon(
                            imageVector = image,
                            contentDescription = if (passwordVisible) "Hide Password" else "Show Password",
                            tint = if (isSystemInDarkTheme()) MinimalTextDark else MinimalPrimary
                        )
                    }
                }
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = {
                updateAccount(
                    firebaseAuth,
                    username.value.text,
                    password.value.text
                ) { success, message ->
                    showDialog = true
                    dialogMessage = message
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp),
            colors = ButtonDefaults.buttonColors(
                backgroundColor = MinimalPrimary,
                contentColor = Color.White
            )
        ) {
            Text("Update Account", color = if (isSystemInDarkTheme()) MinimalTextDark else MinimalTextLight)
        }

        if (uploading) {
            Spacer(modifier = Modifier.height(16.dp))
            CircularProgressIndicator()
        }
    }
}

@Composable
fun ResponsiveDialog(message: String, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(text = "Status", style = MaterialTheme.typography.h6) },
        text = {
            Text(
                text = message,
                style = MaterialTheme.typography.body1,
                modifier = Modifier.fillMaxWidth(),
                maxLines = 3,
                overflow = TextOverflow.Ellipsis
            )
        },
        confirmButton = {
            Button(
                onClick = onDismiss,
                modifier = Modifier.fillMaxWidth(0.5f)
            ) {
                Text("OK", style = MaterialTheme.typography.button)
            }
        }
    )
}

fun updateAccount(
    firebaseAuth: FirebaseAuth,
    newUsername: String,
    newPassword: String,
    onResult: (Boolean, String) -> Unit
) {
    val currentUser = firebaseAuth.currentUser

    currentUser?.let { user ->
        val profileUpdates = UserProfileChangeRequest.Builder()
            .setDisplayName(newUsername)
            .build()

        user.updateProfile(profileUpdates).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                if (newPassword.isNotEmpty()) {
                    user.updatePassword(newPassword).addOnCompleteListener { passwordTask ->
                        if (passwordTask.isSuccessful) {
                            onResult(true, "Username dan password berhasil diperbarui.")
                        } else {
                            onResult(false, "Gagal memperbarui password: ${passwordTask.exception?.message}")
                        }
                    }
                } else {
                    onResult(true, "Username berhasil diperbarui.")
                }
            } else {
                onResult(false, "Gagal memperbarui username: ${task.exception?.message}")
            }
        }
    } ?: run {
        onResult(false, "User tidak terautentikasi.")
    }
}

fun uploadProfileImage(
    imageUri: Uri,
    firebaseAuth: FirebaseAuth,
    onResult: (Boolean, String) -> Unit
) {
    val user = firebaseAuth.currentUser
    if (user == null) {
        onResult(false, "User not authenticated.")
        return
    }

    val storageReference = Firebase.storage.reference
    val profileImagesRef = storageReference.child("profile_images/${user.uid}.jpg")

    profileImagesRef.putFile(imageUri)
        .addOnSuccessListener {
            profileImagesRef.downloadUrl.addOnSuccessListener { uri ->
                val profileUpdates = UserProfileChangeRequest.Builder()
                    .setPhotoUri(uri)
                    .build()

                user.updateProfile(profileUpdates)
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            onResult(true, "Profile picture updated successfully.")
                        } else {
                            onResult(false, "Failed to update profile picture.")
                        }
                    }
            }.addOnFailureListener { exception: Exception ->
                onResult(false, "Failed to retrieve download URL: ${exception.message}")
            }
        }
        .addOnFailureListener { exception: Exception ->
            onResult(false, "Failed to upload image: ${exception.message}")
        }
}

fun checkInternetConnection(context: Context): Boolean {
    val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    val network = connectivityManager.activeNetwork ?: return false
    val activeNetwork = connectivityManager.getNetworkCapabilities(network) ?: return false
    return activeNetwork.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
}

fun Context.findActivity(): ComponentActivity? = when (this) {
    is ComponentActivity -> this
    is android.content.ContextWrapper -> baseContext.findActivity()
    else -> null
}
