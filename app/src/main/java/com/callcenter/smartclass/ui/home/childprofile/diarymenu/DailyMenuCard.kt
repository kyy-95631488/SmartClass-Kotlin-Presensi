package com.callcenter.smartclass.ui.home.childprofile.diarymenu.diarymenu

import android.app.Application
import android.content.Intent
import android.content.pm.PackageManager
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.Card
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.DropdownMenu
import androidx.compose.material.DropdownMenuItem
import androidx.compose.material.MaterialTheme
import androidx.compose.material.OutlinedButton
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Icon
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import com.callcenter.smartclass.ui.home.childprofile.diarymenu.data.DailyMenu
import com.callcenter.smartclass.ui.home.childprofile.diarymenu.viewmodel.DailyMenuViewModel
import android.Manifest
import android.app.AlarmManager
import android.content.Context
import android.os.Build
import androidx.annotation.RequiresApi

@RequiresApi(Build.VERSION_CODES.S)
@Composable
fun DailyMenuCard(
    modifier: Modifier = Modifier,
    childId: String,
    navController: NavController,
    viewModel: DailyMenuViewModel = viewModel(factory = ViewModelProvider.AndroidViewModelFactory.getInstance(LocalContext.current.applicationContext as Application))
) {
    val context = LocalContext.current
    val isDarkMode = isSystemInDarkTheme()

    // State untuk izin notifikasi
    var isNotificationGranted by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        )
    }

    val notificationLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { granted ->
            isNotificationGranted = granted
        }
    )

    val alarmIntent = remember {
        Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM)
    }

    LaunchedEffect(Unit) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

        if (ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            if (!alarmManager.canScheduleExactAlarms()) {
                context.startActivity(alarmIntent)
            }
        } else {
            notificationLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }

    val dailyMenus by viewModel.dailyMenus.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()

    val daysOfWeek = listOf("Senin", "Selasa", "Rabu", "Kamis", "Jumat", "Sabtu", "Minggu")

    var selectedDay by remember { mutableStateOf(
        java.util.Calendar.getInstance().getDisplayName(
            java.util.Calendar.DAY_OF_WEEK,
            java.util.Calendar.LONG,
            java.util.Locale("id", "ID")
        ) ?: "Senin"
    ) }

    LaunchedEffect(key1 = childId, key2 = selectedDay) {
        viewModel.fetchDailyMenus(childId, selectedDay)
    }

    Card(
        shape = RoundedCornerShape(16.dp),
        backgroundColor = if (isDarkMode) Color(0xFF2C2C2C) else Color(0xFFF0F0F0),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Selector Hari
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Menu Hari ${selectedDay}",
                    style = MaterialTheme.typography.h6.copy(
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    ),
                    color = if (isDarkMode) Color(0xFFC8E6C9) else Color(0xFF388E3C)
                )
                // Dropdown untuk pemilihan hari
                var expanded by remember { mutableStateOf(false) }
                Box(modifier = Modifier.wrapContentSize(Alignment.TopStart)) {
                    OutlinedButton(
                        onClick = { expanded = true },
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.outlinedButtonColors(
                            backgroundColor = if (isDarkMode) Color(0xFF37474F) else Color(0xFFE0F7FA),
                            contentColor = if (isDarkMode) Color(0xFF81D4FA) else Color(0xFF0288D1)
                        )
                    ) {
                        Text(text = "Pilih Hari")
                        Icon(
                            imageVector = Icons.Default.ArrowDropDown,
                            contentDescription = "Pilih Hari",
                            tint = if (isDarkMode) Color(0xFF81D4FA) else Color(0xFF0288D1)
                        )
                    }
                    DropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false }
                    ) {
                        daysOfWeek.forEach { day ->
                            DropdownMenuItem(
                                onClick = {
                                    selectedDay = day
                                    expanded = false
                                }
                            ) {
                                Text(text = day)
                            }
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(16.dp))

            when {
                isLoading -> {
                    CircularProgressIndicator(
                        color = if (isDarkMode) Color(0xFF81D4FA) else Color(0xFF0288D1)
                    )
                }
                errorMessage != null -> {
                    Text(
                        text = errorMessage ?: "Terjadi kesalahan.",
                        style = MaterialTheme.typography.body1.copy(fontWeight = FontWeight.Medium),
                        color = Color.Red,
                        modifier = Modifier.fillMaxWidth(),
                        maxLines = 2
                    )
                }
                dailyMenus.isEmpty() -> {
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        backgroundColor = if (isDarkMode) Color(0xFF37474F) else Color(0xFFE0F7FA),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 8.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .padding(16.dp)
                                .fillMaxWidth(),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "Belum ada menu yang ditambahkan untuk hari ini",
                                style = MaterialTheme.typography.body1.copy(fontWeight = FontWeight.Medium),
                                color = if (isDarkMode) Color(0xFFB0BEC5) else Color(0xFF00695C),
                                modifier = Modifier.fillMaxWidth(),
                                maxLines = 2
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            OutlinedButton(
                                onClick = {
                                    navController.navigate("add_menu/$childId")
                                },
                                shape = RoundedCornerShape(16.dp),
                                border = BorderStroke(
                                    1.dp,
                                    if (isDarkMode) Color(0xFF81D4FA).copy(alpha = 0.5f) else Color(0xFF4FC3F7).copy(alpha = 0.5f)
                                ),
                                colors = ButtonDefaults.outlinedButtonColors(
                                    backgroundColor = Color.Transparent,
                                    contentColor = if (isDarkMode) Color(0xFF81D4FA) else Color(0xFF0288D1)
                                ),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(text = "+ Menu Baru")
                            }
                        }
                    }
                }
                else -> {
                    val sortedDailyMenus = dailyMenus.sortedBy { it.time }

                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 180.dp)
                            .padding(horizontal = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(sortedDailyMenus) { menu ->
                            MenuItem(
                                menu = menu,
                                isDarkMode = isDarkMode,
                                onItemClick = {
                                    navController.navigate("recipe_detail/${menu.recipeId}")
                                }
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    OutlinedButton(
                        onClick = {
                            navController.navigate("add_menu/$childId")
                        },
                        shape = RoundedCornerShape(16.dp),
                        border = BorderStroke(
                            1.dp,
                            if (isDarkMode) Color(0xFF81D4FA).copy(alpha = 0.5f) else Color(0xFF4FC3F7).copy(alpha = 0.5f)
                        ),
                        colors = ButtonDefaults.outlinedButtonColors(
                            backgroundColor = Color.Transparent,
                            contentColor = if (isDarkMode) Color(0xFF81D4FA) else Color(0xFF0288D1)
                        ),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(text = "+ Menu Baru")
                    }
                }
            }
        }
    }
}

@Composable
fun MenuItem(
    menu: DailyMenu,
    isDarkMode: Boolean,
    onItemClick: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(12.dp),
        backgroundColor = if (isDarkMode) Color(0xFF37474F) else Color(0xFFE0F7FA),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onItemClick() }
    ) {
        Row(
            modifier = Modifier
                .padding(12.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (menu.thumbnailUrl != null) {
                Image(
                    painter = rememberAsyncImagePainter(menu.thumbnailUrl),
                    contentDescription = "Thumbnail",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .size(60.dp)
                        .clip(CircleShape)
                        .border(2.dp, Color.Gray, CircleShape)
                )
            } else {
                Box(
                    modifier = Modifier
                        .size(60.dp)
                        .clip(CircleShape)
                        .background(Color.Gray)
                        .border(2.dp, Color.Gray, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = menu.title.firstOrNull()?.toString() ?: "?",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp
                    )
                }
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = menu.title,
                    style = MaterialTheme.typography.subtitle1.copy(
                        fontWeight = FontWeight.Bold
                    ),
                    color = if (isDarkMode) Color(0xFFC8E6C9) else Color(0xFF388E3C)
                )
                Text(
                    text = "Waktu: ${menu.time}",
                    style = MaterialTheme.typography.body2.copy(
                        fontWeight = FontWeight.Medium
                    ),
                    color = if (isDarkMode) Color(0xFFB0BEC5) else Color(0xFF00695C)
                )
            }
            Icon(
                imageVector = Icons.Default.Check,
                contentDescription = "Detail",
                tint = if (isDarkMode) Color(0xFF81D4FA) else Color(0xFF0288D1),
                modifier = Modifier.size(24.dp)
            )
        }
    }
}
