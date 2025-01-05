package com.callcenter.smartclass.ui.home.childprofile

import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTransformGestures
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.*
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import com.callcenter.smartclass.R
import com.callcenter.smartclass.data.ChildProfile
import com.callcenter.smartclass.data.helper.getGrowthAnalysis
import com.callcenter.smartclass.ui.components.smartclassCard
import com.callcenter.smartclass.ui.theme.MinimalTextDark
import com.callcenter.smartclass.ui.theme.MinimalTextLight
import com.callcenter.smartclass.ui.theme.Neutral4
import com.callcenter.smartclass.ui.theme.TextDarkColor
import com.callcenter.smartclass.ui.theme.TextLightColor
import com.callcenter.smartclass.ui.theme.WhiteColor
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.Period
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException
import java.util.Locale

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun ChildDataTabContent(childId: String, navController: NavController) {
    val db = FirebaseFirestore.getInstance()
    val auth = FirebaseAuth.getInstance()
    var childProfile by remember { mutableStateOf<ChildProfile?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    var isDeleteDialogVisible by remember { mutableStateOf(false) }
    var isUpdateDialogVisible by remember { mutableStateOf(false) }
    var aiAnalysis by remember { mutableStateOf<String?>(null) }
    var isAiLoading by remember { mutableStateOf(false) }

    // State for focused image
    var focusedImageUrl by remember { mutableStateOf<String?>(null) }

    val userId = auth.currentUser?.uid

    LaunchedEffect(childId, userId) {
        if (userId != null) {
            try {
                val document = db.collection("users")
                    .document(userId)
                    .collection("children")
                    .document(childId)
                    .get()
                    .await()
                childProfile = document.toObject(ChildProfile::class.java)
                isLoading = false
                Log.d("ChildDataTabContent", "Data profil anak berhasil diambil: $childProfile")

                childProfile?.let {
                    isAiLoading = true
                    aiAnalysis = getGrowthAnalysis(it)
                    isAiLoading = false
                    Log.d("ChildDataTabContent", "Analisis AI: $aiAnalysis")
                }
            } catch (e: Exception) {
                isLoading = false
                Log.e("ChildDataTabContent", "Gagal mengambil data profil anak: ${e.message}")
            }
        }
    }

    val isDarkMode = isSystemInDarkTheme()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        when {
            isLoading -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize(),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    CircularProgressIndicator(
                        color = if (isDarkMode) Color(0xFF81D4FA) else Color(0xFF039BE5),
                        strokeWidth = 6.dp
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = stringResource(id = R.string.loading_child_profile),
                        style = MaterialTheme.typography.bodyLarge.copy(color = if (isDarkMode) Color.White else Color.Black)
                    )
                }
            }
            else -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(24.dp)
                ) {
                    ProfileInfoCard(
                        childProfile = childProfile,
                        isDarkMode = isDarkMode,
                        onDelete = { isDeleteDialogVisible = true },
                        onUpdate = { isUpdateDialogVisible = true },
                        onImageClick = { imageUrl ->
                            focusedImageUrl = imageUrl
                        }
                    )

                    if (isAiLoading) {
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            CircularProgressIndicator(
                                color = if (isDarkMode) Color(0xFF81D4FA) else Color(0xFF66BB6A),
                                strokeWidth = 6.dp
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = stringResource(id = R.string.generating_analysis),
                                style = MaterialTheme.typography.bodyLarge.copy(color = if (isDarkMode) Color.White else Color.Black)
                            )
                        }
                    } else {
                        aiAnalysis?.let { analysis ->
                            GrowthAnalysisCard(
                                analysis = analysis,
                                isDarkMode = isDarkMode
                            )
                        }
                    }

                    if (isDeleteDialogVisible) {
                        DeleteConfirmationDialog(
                            isDarkMode = isDarkMode,
                            onDismiss = { isDeleteDialogVisible = false },
                            onConfirm = {
                                deleteChildProfile(db, userId, childId, navController)
                                isDeleteDialogVisible = false
                            }
                        )
                    }

                    if (isUpdateDialogVisible) {
                        navController.navigate("editChildProfile/$childId") {
                            popUpTo("currentScreen") { inclusive = true }
                        }
                        isUpdateDialogVisible = false
                    }
                }
            }
        }

        // Display focused image with pinch-to-zoom if available
        focusedImageUrl?.let { url ->
            FullscreenImageViewer(url) {
                focusedImageUrl = null
            }
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun ProfileInfoCard(
    childProfile: ChildProfile?,
    isDarkMode: Boolean,
    onDelete: () -> Unit,
    onUpdate: () -> Unit,
    onImageClick: (String) -> Unit
) {
    val backgroundColor = if (isDarkMode) Color(0xFF424242) else Color(0xFFF5F5F5)
    val accentColor = if (isDarkMode) Color(0xFF81D4FA) else Color(0xFF039BE5)

    val ageYearsLabel = stringResource(id = R.string.age_years)
    val ageMonthsLabel = stringResource(id = R.string.age_months)

    val formattedAge = formatBirthDate(
        birthDate = childProfile?.birthDate ?: "-",
        ageYearsLabel = ageYearsLabel,
        ageMonthsLabel = ageMonthsLabel
    )

    val genderIcon: ImageVector = when (childProfile?.gender?.lowercase(Locale.getDefault())) {
        "perempuan", "female" -> Icons.Filled.Female
        "laki-laki", "male" -> Icons.Filled.Male
        else -> Icons.Default.Person
    }

    Card(
        modifier = Modifier
            .fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 12.dp),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = backgroundColor
        )
    ) {
        Column(modifier = Modifier.padding(24.dp)) {
            childProfile?.profileImageUrl?.let { imageUrl ->
                Image(
                    painter = rememberAsyncImagePainter(imageUrl),
                    contentDescription = stringResource(id = R.string.profile_child_01),
                    modifier = Modifier
                        .size(120.dp)
                        .align(Alignment.CenterHorizontally)
                        .clip(CircleShape)
                        .border(4.dp, accentColor, CircleShape)
                        .clickable { onImageClick(imageUrl) } // Image click to show full screen
                )
                Spacer(modifier = Modifier.height(16.dp))
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(id = R.string.profile_child_01),
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold,
                        color = accentColor
                    ),
                    modifier = Modifier.weight(1f)
                )
                IconButton(
                    onClick = onUpdate,
                    modifier = Modifier
                        .size(32.dp)
                        .background(
                            color = accentColor.copy(alpha = 0.2f),
                            shape = CircleShape
                        )
                ) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = stringResource(id = R.string.update),
                        tint = accentColor
                    )
                }
                Spacer(modifier = Modifier.width(26.dp))
                IconButton(
                    onClick = onDelete,
                    modifier = Modifier
                        .size(32.dp)
                        .background(
                            color = Color(0xFFFFCDD2),
                            shape = CircleShape
                        )
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = stringResource(id = R.string.delete_01),
                        tint = Color(0xFFD32F2F)
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                GrowthResultCard(
                    label = stringResource(id = R.string.label_height),
                    value = childProfile?.height?.takeIf { it.isNotBlank() }?.let { "$it ${stringResource(id = R.string.height_unit)}" } ?: "- ${stringResource(id = R.string.height_unit)}",
                    icon = Icons.Default.Height,
                    modifier = Modifier.weight(1f),
                    isDarkMode = isDarkMode,
                    accentColor = accentColor
                )
                GrowthResultCard(
                    label = stringResource(id = R.string.label_age),
                    value = formattedAge,
                    icon = Icons.Default.Cake,
                    modifier = Modifier.weight(1f),
                    isDarkMode = isDarkMode,
                    accentColor = accentColor
                )
                GrowthResultCard(
                    label = stringResource(id = R.string.label_gender),
                    value = childProfile?.gender ?: "-",
                    icon = genderIcon,
                    modifier = Modifier.weight(1f),
                    isDarkMode = isDarkMode,
                    accentColor = accentColor
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            childProfile?.lastUpdated?.toDate()?.let { date ->
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = stringResource(id = R.string.last_updated, SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(date)),
                    style = MaterialTheme.typography.bodySmall,
                    color = if (isDarkMode) MinimalTextDark else MinimalTextLight,
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                )
            }
        }
    }
}

@Composable
fun GrowthResultCard(
    label: String,
    value: String,
    icon: ImageVector,
    modifier: Modifier = Modifier,
    isDarkMode: Boolean,
    accentColor: Color
) {
    val backgroundColor = if (isDarkMode) Color(0xFF616161) else Color(0xFFE3F2FD)

    Card(
        modifier = modifier
            .fillMaxWidth()
            .wrapContentHeight(),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = backgroundColor
        )
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth()
                .wrapContentHeight(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                modifier = Modifier
                    .size(28.dp)
                    .align(Alignment.CenterHorizontally),
                tint = accentColor
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.titleSmall.copy(
                    fontWeight = FontWeight.SemiBold,
                    color = if (isDarkMode) TextDarkColor else TextLightColor
                ),
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )
            Text(
                text = value,
                fontWeight = FontWeight.Bold,
                color = accentColor,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )
        }
    }
}

@Composable
fun GrowthAnalysisCard(
    analysis: String,
    isDarkMode: Boolean
) {
    val backgroundColor = if (isDarkMode) Color(0xFF424242) else Color(0xFFF1F8E9)
    val accentColor = if (isDarkMode) Color(0xFF81D4FA) else Color(0xFF66BB6A)

    Card(
        modifier = Modifier
            .fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 12.dp),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = backgroundColor
        )
    ) {
        Column(modifier = Modifier.padding(24.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Assessment,
                    contentDescription = stringResource(id = R.string.growth_analysis),
                    modifier = Modifier.size(40.dp),
                    tint = accentColor
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = stringResource(id = R.string.growth_analysis),
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold,
                        color = accentColor
                    )
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = parseSimpleMarkdown(analysis),
                style = MaterialTheme.typography.bodyLarge.copy(color = if (isDarkMode) TextDarkColor else TextLightColor)
            )
        }
    }
}

fun parseSimpleMarkdown(text: String): AnnotatedString {
    return buildAnnotatedString {
        var i = 0
        while (i < text.length) {
            when {
                text.startsWith("## ", i) -> {
                    val end = text.indexOf('\n', i)
                    val heading = if (end != -1) {
                        text.substring(i + 3, end)
                    } else {
                        text.substring(i + 3)
                    }
                    append(heading)
                    addStyle(
                        style = SpanStyle(
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold
                        ),
                        start = length - heading.length,
                        end = length
                    )
                    i += 3 + heading.length
                }
                text.startsWith("**", i) -> {
                    val end = text.indexOf("**", i + 2)
                    if (end != -1) {
                        val content = text.substring(i + 2, end)
                        append(content)
                        addStyle(
                            style = SpanStyle(
                                fontWeight = FontWeight.Bold
                            ),
                            start = length - content.length,
                            end = length
                        )
                        i = end + 2
                    } else {
                        append(text[i])
                        i++
                    }
                }
                text.startsWith("*-", i) -> {
                    val end = text.indexOf('*', i + 1)
                    if (end != -1) {
                        val content = text.substring(i + 1, end)
                        append(content)
                        addStyle(
                            style = SpanStyle(
                                fontWeight = FontWeight.Light
                            ),
                            start = length - content.length,
                            end = length
                        )
                        i = end + 1
                    } else {
                        append(text[i])
                        i++
                    }
                }
                else -> {
                    append(text[i])
                    i++
                }
            }
        }
    }
}

@Composable
fun DeleteConfirmationDialog(
    isDarkMode: Boolean,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    val backgroundColor = if (isSystemInDarkTheme()) Neutral4.copy(alpha = 0.85f) else Neutral4.copy(alpha = 0.85f)
    val borderColor = if (isDarkMode) Color(0xFF81D4FA) else Color(0xFF81D4FA)

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(dismissOnClickOutside = true)
    ) {
        AnimatedVisibility(
            visible = true,
            enter = fadeIn(animationSpec = tween(durationMillis = 300)) + scaleIn(
                initialScale = 0.8f,
                animationSpec = tween(durationMillis = 300)
            ),
            exit = fadeOut(animationSpec = tween(durationMillis = 300)) + scaleOut(
                targetScale = 0.8f,
                animationSpec = tween(durationMillis = 300)
            )
        ) {
            smartclassCard(
                modifier = Modifier
                    .fillMaxWidth(0.9f)
                    .wrapContentHeight(),
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(2.dp, borderColor),
                color = backgroundColor
            ) {
                Column(
                    modifier = Modifier
                        .padding(24.dp)
                        .fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Default.Warning,
                        contentDescription = stringResource(id = R.string.delete_child_profile),
                        tint = Color(0xFFD32F2F),
                        modifier = Modifier.size(48.dp)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = stringResource(id = R.string.delete_child_profile),
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold, color = Color(0xFFD32F2F))
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = stringResource(id = R.string.delete_confirmation),
                        style = MaterialTheme.typography.bodyMedium.copy(color =  if (isSystemInDarkTheme()) WhiteColor else WhiteColor),
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(24.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        TextButton(
                            onClick = onDismiss
                        ) {
                            Text(
                                text = stringResource(id = R.string.cancel_01),
                                color = Color(0xFFD32F2F)
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        TextButton(
                            onClick = onConfirm
                        ) {
                            Text(
                                text = stringResource(id = R.string.confirm_delete),
                                color = Color(0xFF81D4FA)
                            )
                        }
                    }
                }
            }
        }
    }
}

fun deleteChildProfile(
    db: FirebaseFirestore,
    userId: String?,
    childId: String,
    navController: NavController
) {
    userId?.let {
        db.collection("users")
            .document(it)
            .collection("children")
            .document(childId)
            .delete()
            .addOnSuccessListener {
                navController.navigate("home/feed") {
                    popUpTo("currentScreen") { inclusive = true }
                }
            }
            .addOnFailureListener { e ->
                Log.e("DeleteChildProfile", "Error deleting profile: ${e.message}")
            }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun formatBirthDate(
    birthDate: String,
    ageYearsLabel: String,
    ageMonthsLabel: String
): String {
    return try {
        val inputFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy", Locale.ENGLISH)
        val birthDateLocal = LocalDate.parse(birthDate, inputFormatter)

        val currentDate = LocalDate.now()
        val period = Period.between(birthDateLocal, currentDate)

        val years = period.years
        val months = period.months
        val yearsText = if (years > 0) "$years $ageYearsLabel" else ""
        val monthsText = if (months > 0) "$months $ageMonthsLabel" else ""

        when {
            years > 0 && months > 0 -> "$yearsText / $monthsText"
            years > 0 -> yearsText
            months > 0 -> monthsText
            else -> "-"
        }
    } catch (e: DateTimeParseException) {
        Log.e("formatBirthDate", "Error parsing birth date: ${e.message}")
        "-"
    }
}

@Composable
fun FullscreenImageViewer(
    url: String,
    onClose: () -> Unit
) {
    var scale by remember { mutableFloatStateOf(1f) }
    var offset by remember { mutableStateOf(Offset.Zero) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.5f))
            .pointerInput(Unit) {}
    ) {
        Image(
            painter = rememberAsyncImagePainter(url),
            contentDescription = "Focused Image",
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight()
                .align(Alignment.Center)
                .padding(32.dp)
                .clip(RoundedCornerShape(16.dp))
                .graphicsLayer(
                    scaleX = scale,
                    scaleY = scale,
                    translationX = offset.x,
                    translationY = offset.y
                )
                .pointerInput(Unit) {
                    detectTransformGestures { _, pan, zoom, _ ->
                        scale = (scale * zoom).coerceIn(1f, 5f)
                        offset += pan
                    }
                }
        )

        IconButton(
            onClick = onClose,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(32.dp)
                .background(
                    color = Color.Black.copy(alpha = 0.5f),
                    shape = CircleShape
                )
        ) {
            Icon(
                imageVector = Icons.Default.ArrowBack,
                contentDescription = "Close",
                tint = Color.White
            )
        }
    }
}
