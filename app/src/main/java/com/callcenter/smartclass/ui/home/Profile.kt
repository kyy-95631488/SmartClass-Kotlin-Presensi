package com.callcenter.smartclass.ui.home

import android.content.Context
import android.content.Intent
import androidx.activity.ComponentActivity
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import coil.compose.SubcomposeAsyncImage
import coil.compose.SubcomposeAsyncImageContent
import coil.request.ImageRequest
import coil.transform.CircleCropTransformation
import com.callcenter.smartclass.R
import com.callcenter.smartclass.data.LocaleManager
import com.callcenter.smartclass.model.SharedPreferencesHelper
import com.callcenter.smartclass.ui.components.smartclassCard
import com.callcenter.smartclass.ui.funcauth.FunLoginGoogle
import com.callcenter.smartclass.ui.theme.*
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore

@Composable
fun Profile(navController: NavHostController, modifier: Modifier = Modifier) {
    val context = LocalContext.current

    if (FirebaseApp.getApps(context).isEmpty()) {
        FirebaseApp.initializeApp(context)
    }

    val auth = FirebaseAuth.getInstance()
    val currentUser = auth.currentUser

    var displayName by remember { mutableStateOf("Unknown") }
    var email by remember { mutableStateOf("No Email") }
    var photoUrl by remember { mutableStateOf<String?>(null) }

    currentUser?.let { user: FirebaseUser ->
        displayName = user.displayName ?: "No Name"
        email = user.email ?: "No Email"
        photoUrl = user.photoUrl?.toString()
    }

    var isAccountSettingsExpanded by remember { mutableStateOf(false) }

    var isLanguageSettingsExpanded by remember { mutableStateOf(false) }

    var errorMessage by remember { mutableStateOf<String?>(null) }

    var showDeleteDialog by remember { mutableStateOf(false) }

    var selectedLanguage by remember { mutableStateOf(LocaleManager.getLanguage(context)) }

    smartclassTheme {
        Column(
            modifier = modifier
                .fillMaxSize()
                .background(smartclassTheme.colors.uiBackground)
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        brush = Brush.horizontalGradient(
                            colors = listOf(Color(0xFF0B5C6A), Color(0xff00a1c7))
                        ),
                        shape = RoundedCornerShape(16.dp)
                    )
                    .padding(20.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                ProfileImage(
                    photoUrl = photoUrl,
                    modifier = Modifier
                        .size(80.dp)
                )
                Spacer(modifier = Modifier.width(20.dp))
                Column {
                    Text(
                        text = displayName,
                        style = MaterialTheme.typography.headlineSmall.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp
                        ),
                        color = Color(0xFFE5E5E5)
                    )
                    Text(
                        text = email,
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFFB0BEC5)
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                ProfileItem(
                    title = stringResource(id = R.string.account),
                    icon = Icons.Default.AccountCircle,
                    color = Color(0xFF1976D2),
                    onClick = {
                        navController.navigate("account")
                    }
                )

                ProfileItem(
                    title = stringResource(id = R.string.mainadmin),
                    icon = Icons.Default.AdminPanelSettings,
                    color = Color(0xFF1976D2),
                    onClick = {
                        navController.navigate("mainadmin")
                    }
                )

                ProfileItem(
                    title = stringResource(id = R.string.language),
                    icon = Icons.Default.Language,
                    color = Color(0xFF1976D2),
                    isExpandable = true,
                    isExpanded = isLanguageSettingsExpanded,
                    onExpandToggle = { isLanguageSettingsExpanded = !isLanguageSettingsExpanded }
                )

                AnimatedVisibility(visible = isLanguageSettingsExpanded) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(start = 24.dp)
                    ) {
                        LanguageOption(
                            language = stringResource(id = R.string.system_default),
                            isSelected = selectedLanguage == "system",
                            onSelect = {
                                selectedLanguage = "system"
                                LocaleManager.persistLanguage(context, "system")
                                LocaleManager.setLocale(context)
                                restartActivity(context)
                            }
                        )
                        LanguageOption(
                            language = stringResource(id = R.string.english),
                            isSelected = selectedLanguage == "en",
                            onSelect = {
                                selectedLanguage = "en"
                                LocaleManager.persistLanguage(context, "en")
                                LocaleManager.setLocale(context)
                                restartActivity(context)
                            }
                        )
                        LanguageOption(
                            language = stringResource(id = R.string.indonesian),
                            isSelected = selectedLanguage == "id",
                            onSelect = {
                                selectedLanguage = "id"
                                LocaleManager.persistLanguage(context, "id")
                                LocaleManager.setLocale(context)
                                restartActivity(context)
                            }
                        )
                    }
                }

                // Pindahkan Pengaturan Akun di bawah Pengaturan Bahasa
                ProfileItem(
                    title = stringResource(id = R.string.account_settings),
                    icon = Icons.Default.Settings,
                    color = Color(0xFF1976D2),
                    isExpandable = true,
                    isExpanded = isAccountSettingsExpanded,
                    onExpandToggle = { isAccountSettingsExpanded = !isAccountSettingsExpanded }
                )

                // Opsi yang muncul saat Pengaturan Akun diekspansi
                AnimatedVisibility(visible = isAccountSettingsExpanded) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(start = 24.dp, top = 4.dp, bottom = 58.dp)
                    ) {
                        ProfileItem(
                            title = stringResource(id = R.string.delete_account),
                            icon = Icons.Default.Delete,
                            color = Color(0xFFD32F2F),
                            onClick = {
                                showDeleteDialog = true
                            },
                            isSubItem = true
                        )
                        @Suppress("DEPRECATION")
                        ProfileItem(
                            title = stringResource(id = R.string.logout),
                            icon = Icons.Default.ExitToApp,
                            color = Color(0xFF1976D2),
                            onClick = {
                                handleLogoutWithError(context) { error ->
                                    errorMessage = error
                                }
                            },
                            isSubItem = true
                        )
                    }
                }

                // Tambahkan ProfileItem lainnya jika diperlukan
            }

            if (errorMessage != null) {
                ErrorAlertDialog(
                    message = errorMessage!!,
                    onDismiss = { errorMessage = null }
                )
            }

            if (showDeleteDialog) {
                DeleteConfirmationDialog(
                    onConfirm = {
                        showDeleteDialog = false
                        handleDeleteAccountWithError(context) { error ->
                            errorMessage = error
                        }
                    },
                    onDismiss = {
                        showDeleteDialog = false
                    }
                )
            }
        }
    }
}

@Composable
fun ProfileItem(
    title: String,
    icon: ImageVector,
    color: Color = smartclassTheme.colors.textInteractive,
    onClick: (() -> Unit)? = null,
    isExpandable: Boolean = false,
    isExpanded: Boolean = false,
    onExpandToggle: (() -> Unit)? = null,
    isSubItem: Boolean = false
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.95f else 1f,
        animationSpec = tween(durationMillis = 100)
    )

    val backgroundColor by animateColorAsState(
        targetValue = if (isPressed) color.copy(alpha = 0.1f) else Color.Transparent,
        animationSpec = tween(durationMillis = 100)
    )

    smartclassCard(
        modifier = Modifier
            .fillMaxWidth()
            .padding(
                start = if (isSubItem) 16.dp else 8.dp,
                end = if (isSubItem) 16.dp else 8.dp,
                top = if (isSubItem) 4.dp else 4.dp,
                bottom = if (isSubItem) 4.dp else 4.dp
            )
            .clickable(
                onClick = {
                    if (isExpandable) {
                        onExpandToggle?.invoke()
                    } else {
                        onClick?.invoke()
                    }
                },
                interactionSource = interactionSource,
                indication = null
            )
            .background(backgroundColor, RoundedCornerShape(12.dp))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .scale(scale)
                .animateContentSize(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                tint = color,
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(color.copy(alpha = 0.05f))
                    .padding(8.dp)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Medium),
                color = if (isSystemInDarkTheme()) WhiteColor else DarkText
            )
            Spacer(modifier = Modifier.weight(1f))
            if (isExpandable) {
                Icon(
                    imageVector = if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                    contentDescription = if (isExpanded) stringResource(id = R.string.collapse) else stringResource(id = R.string.expand),
                    tint = smartclassTheme.colors.textPrimary
                )
            }
        }
    }
}

/**
 * Composable untuk menampilkan opsi bahasa dengan RadioButton
 */
@Composable
fun LanguageOption(language: String, isSelected: Boolean, onSelect: () -> Unit,
                   isSubItem: Boolean = false) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.95f else 1f,
        animationSpec = tween(durationMillis = 100)
    )

    val backgroundColor by animateColorAsState(
        targetValue = if (isPressed) Color(0xFF1976D2).copy(alpha = 0.1f) else Color.Transparent,
        animationSpec = tween(durationMillis = 100)
    )

    smartclassCard(
        modifier = Modifier
            .fillMaxWidth()
            .padding(
                start = if (isSubItem) 16.dp else 8.dp,
                end = if (isSubItem) 16.dp else 8.dp,
                top = if (isSubItem) 4.dp else 8.dp,
                bottom = if (isSubItem) 4.dp else 8.dp
            )
            .clickable(
                onClick = onSelect,
                interactionSource = interactionSource,
                indication = null
            )
            .background(backgroundColor, RoundedCornerShape(12.dp))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
                .scale(scale)
                .animateContentSize(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            RadioButton(
                selected = isSelected,
                onClick = onSelect,
                colors = RadioButtonDefaults.colors(
                    unselectedColor = WhiteColor,
                    selectedColor = Color(0xFF1976D2)
                )
            )
            Spacer(modifier = Modifier.width(16.dp))
            Text(
                text = language,
                style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Medium),
                color = if (isSystemInDarkTheme()) WhiteColor else DarkText
            )
        }
    }
}

/**
 * Fungsi untuk merestart aktivitas agar perubahan bahasa diterapkan
 */
private fun restartActivity(context: Context) {
    if (context is ComponentActivity) {
        context.recreate()
    }
}

/**
 * Composable untuk AlertDialog kesalahan
 */
@Composable
fun ErrorAlertDialog(message: String, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(text = stringResource(id = R.string.error)) },
        text = { Text(text = message) },
        confirmButton = {
            TextButton(
                onClick = onDismiss,
                colors = ButtonDefaults.textButtonColors(contentColor = Ocean8)
            ) {
                Text(stringResource(id = R.string.ok), color = Ocean8)
            }
        },
        icon = {
            Icon(
                imageVector = Icons.Default.Error,
                contentDescription = stringResource(id = R.string.error),
                tint = FunctionalRed
            )
        },
        containerColor = if (isSystemInDarkTheme()) Neutral4.copy(alpha = 0.85f) else Neutral4.copy(alpha = 0.85f),
        titleContentColor = if (isSystemInDarkTheme()) WhiteColor else WhiteColor,
        textContentColor = if (isSystemInDarkTheme()) WhiteColor else WhiteColor
    )
}

/**
 * Composable untuk Dialog Konfirmasi Hapus Akun
 */
@Composable
fun DeleteConfirmationDialog(onConfirm: () -> Unit, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(text = stringResource(id = R.string.delete_confirmation_title)) },
        text = { Text(text = stringResource(id = R.string.delete_confirmation_message)) },
        confirmButton = {
            TextButton(
                onClick = onConfirm,
                colors = ButtonDefaults.textButtonColors(contentColor = Ocean8)
            ) {
                Text(stringResource(id = R.string.delete), color = FunctionalRed)
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                colors = ButtonDefaults.textButtonColors(contentColor = Ocean8)
            ) {
                Text(stringResource(id = R.string.cancel), color = Ocean8)
            }
        },
        icon = {
            Icon(
                imageVector = Icons.Default.Warning,
                contentDescription = "Warning",
                tint = FunctionalRed
            )
        },
        containerColor = if (isSystemInDarkTheme()) Neutral4.copy(alpha = 0.85f) else Neutral4.copy(alpha = 0.85f),
        titleContentColor = if (isSystemInDarkTheme()) WhiteColor else WhiteColor,
        textContentColor = if (isSystemInDarkTheme()) WhiteColor else WhiteColor
    )
}

/**
 * Fungsi untuk menangani logout dengan penanganan error
 */
fun handleLogoutWithError(context: Context, onError: (String) -> Unit) {

    lateinit var auth: FirebaseAuth

    try {
        auth = FirebaseAuth.getInstance()
        FirebaseAuth.getInstance().signOut()
        auth.signOut()
        SharedPreferencesHelper.clearToken(context)

        if (context is ComponentActivity) {
            val intent = Intent(context, FunLoginGoogle::class.java)
            context.startActivity(intent)
            context.finish()
        }
    } catch (e: Exception) {
        onError(e.message ?: "An error occurred during logout.")
    }
}

/**
 * Fungsi untuk menangani penghapusan akun dengan penanganan error
 */
fun handleDeleteAccountWithError(context: Context, onError: (String) -> Unit) {
    val auth = FirebaseAuth.getInstance()
    val user = auth.currentUser
    val firestore = FirebaseFirestore.getInstance()

    user?.let { currentUser ->
        val uid = currentUser.uid

        // Menghapus data pengguna dari Firestore
        firestore.collection("users").document(uid).delete()
            .addOnSuccessListener {
                // Setelah berhasil menghapus dari Firestore, hapus akun dari Auth
                currentUser.delete()
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            // Logout dan navigasi ke layar login
                            auth.signOut()
                            if (context is ComponentActivity) {
                                val intent = Intent(context, FunLoginGoogle::class.java)
                                context.startActivity(intent)
                                context.finish()
                            }
                        } else {
                            val error = task.exception?.localizedMessage ?: "Terjadi kesalahan saat menghapus akun."
                            onError(error)
                        }
                    }
            }
            .addOnFailureListener { e ->
                // Jika gagal menghapus data dari Firestore
                onError(e.localizedMessage ?: "Gagal menghapus data pengguna di Firestore.")
            }
    } ?: run {
        onError("Pengguna tidak ditemukan.")
    }
}

/**
 * Composable untuk menampilkan gambar profil dengan efek shimmer saat loading
 */
@Composable
fun ProfileImage(photoUrl: String?, modifier: Modifier = Modifier) {
    val context = LocalContext.current

    SubcomposeAsyncImage(
        model = ImageRequest.Builder(context)
            .data(photoUrl)
            .crossfade(true)
            .transformations(CircleCropTransformation())
            .build(),
        contentDescription = stringResource(id = R.string.profile),
        contentScale = ContentScale.Fit,
        modifier = modifier
            .clip(CircleShape)
    ) {
        val state = painter.state
        if (state is coil.compose.AsyncImagePainter.State.Loading || state is coil.compose.AsyncImagePainter.State.Empty) {
            ShimmerEffect(
                modifier = Modifier
                    .fillMaxSize()
                    .clip(CircleShape)
            )
        } else {
            SubcomposeAsyncImageContent()
        }
    }
}

/**
 * Composable untuk efek shimmer
 */
@Composable
fun ShimmerEffect(
    modifier: Modifier = Modifier,
    shimmerColor: Color = Color.LightGray.copy(alpha = 0.6f)
) {
    val transition = rememberInfiniteTransition()
    val translateAnim by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1000f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1200, easing = LinearEasing)
        )
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

@Preview("default")
@Preview("large font", fontScale = 2f)
@Composable
fun ProfilePreview() {
    smartclassTheme {
        val navController = rememberNavController()
        Profile(navController)
    }
}
