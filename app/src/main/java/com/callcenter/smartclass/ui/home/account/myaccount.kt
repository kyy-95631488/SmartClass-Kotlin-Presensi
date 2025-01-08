package com.callcenter.smartclass.ui.home.account

import android.annotation.SuppressLint
import android.content.Context
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
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Warning
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import coil.compose.rememberAsyncImagePainter
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage

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
            text = "No active session or internet connection.",
            style = MaterialTheme.typography.h6,
            color = Color.Gray
        )
    }
}

@Composable
fun AccountContent(firebaseAuth: FirebaseAuth, currentUser: FirebaseUser) {
    val username = remember { mutableStateOf(TextFieldValue(currentUser.displayName ?: "")) }
    val email = remember { mutableStateOf(TextFieldValue(currentUser.email ?: "")) }

    // Default NPM and Firestore fetch
    val defaultNpm = "Contoh : 202243500xxx"
    val npm = remember { mutableStateOf(TextFieldValue(defaultNpm)) }

    // Fetch data from Firestore
    val firestore = FirebaseFirestore.getInstance()
    val userDocRef = firestore.collection("users").document(currentUser.uid)
    var loading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf("") }

    // Fetch user data from Firestore
    LaunchedEffect(currentUser.uid) {
        userDocRef.get().addOnSuccessListener { documentSnapshot ->
            if (documentSnapshot.exists()) {
                val fetchedNpm = documentSnapshot.getString("npm")
                npm.value = TextFieldValue(fetchedNpm ?: defaultNpm)  // Set fetched data or default
            } else {
                npm.value = TextFieldValue(defaultNpm)  // Set default if no data exists
            }
            loading = false
        }.addOnFailureListener {
            errorMessage = "Error fetching data: ${it.message}"
            loading = false
        }
    }

    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }
    var uploading by remember { mutableStateOf(false) }

    val permission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        android.Manifest.permission.READ_MEDIA_IMAGES
    } else {
        android.Manifest.permission.READ_EXTERNAL_STORAGE
    }

    var showDialog by remember { mutableStateOf(false) }
    var dialogMessage by remember { mutableStateOf("") }

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent(),
        onResult = { uri: Uri? ->
            uri?.let {
                selectedImageUri = it
                uploading = true
                uploadProfileImage(it, firebaseAuth) { success, message ->
                    uploading = false
                }
            }
        }
    )

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { isGranted ->
            if (isGranted) launcher.launch("image/*")
        }
    )

    // Adding scrollable state
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
            .verticalScroll(scrollState),  // Enable scrolling
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)  // To make sure items don't overlap
    ) {
        Box(contentAlignment = Alignment.BottomEnd) {
            if (currentUser.photoUrl != null) {
                val painter = rememberAsyncImagePainter(currentUser.photoUrl)
                Image(
                    painter = painter,
                    contentDescription = "Profile Picture",
                    modifier = Modifier
                        .size(100.dp)
                        .clip(CircleShape)
                        .clickable { launcher.launch("image/*") },
                    contentScale = ContentScale.Crop
                )
            } else {
                // Placeholder or initial
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

            Icon(
                imageVector = Icons.Default.Edit,
                contentDescription = "Edit Profile Picture",
                modifier = Modifier
                    .size(24.dp)
                    .background(Color.White, CircleShape)
                    .padding(4.dp)
                    .clickable { launcher.launch("image/*") },
                tint = MaterialTheme.colors.primary
            )
        }

        // Username TextField
        OutlinedTextField(
            value = username.value,
            onValueChange = { username.value = it },
            label = { Text("Username") },
            modifier = Modifier
                .fillMaxWidth()
        )

        // Email TextField (Disabled)
        OutlinedTextField(
            value = email.value,
            onValueChange = { email.value = it },
            label = { Text("Email") },
            modifier = Modifier
                .fillMaxWidth(),
            enabled = false
        )

        // NPM TextField
        if (loading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),  // Ensure padding around spinner
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else if (errorMessage.isNotEmpty()) {
            Text(text = errorMessage, color = Color.Red)
        } else {
            OutlinedTextField(
                value = npm.value,
                onValueChange = { npm.value = it },
                label = { Text("NPM") },
                modifier = Modifier
                    .fillMaxWidth()
            )
        }

        Button(
            onClick = {
                updateAccount(
                    firebaseAuth,
                    username.value.text,
                    npm.value.text
                ) { success, message ->
                    showDialog = true
                    dialogMessage = message
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
        ) {
            Text("Update Account")
        }

        if (uploading) {
            Spacer(modifier = Modifier.height(16.dp))
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        }
    }
}

fun uploadProfileImage(imageUri: Uri, firebaseAuth: FirebaseAuth, onResult: (Boolean, String) -> Unit) {
    val user = firebaseAuth.currentUser
    val storageReference = FirebaseStorage.getInstance().reference
    val profileImagesRef = storageReference.child("profile_images/${user?.uid}.jpg")

    profileImagesRef.putFile(imageUri)
        .addOnSuccessListener {
            profileImagesRef.downloadUrl.addOnSuccessListener { uri ->
                val profileUpdates = UserProfileChangeRequest.Builder()
                    .setPhotoUri(uri)
                    .build()
                user?.updateProfile(profileUpdates)
                    ?.addOnCompleteListener { task ->
                        if (task.isSuccessful) onResult(true, "Profile picture updated successfully.")
                        else onResult(false, "Failed to update profile picture.")
                    }
            }
        }
        .addOnFailureListener {
            onResult(false, "Failed to upload image: ${it.message}")
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
