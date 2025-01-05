package com.callcenter.smartclass.ui.home.childprofile.diaryaktivitas

import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.callcenter.smartclass.ui.home.childprofile.data.Activity
import com.callcenter.smartclass.ui.home.childprofile.viewmodel.DailyActivityViewModel
import com.callcenter.smartclass.ui.theme.DarkBlue
import com.callcenter.smartclass.ui.theme.smartclassTheme
import com.callcenter.smartclass.ui.theme.LightBlue
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException
import java.util.Date
import java.util.Locale

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun DailyActivityCard(
    modifier: Modifier = Modifier,
    navController: NavController,
    childId: String,
    viewModel: DailyActivityViewModel = androidx.lifecycle.viewmodel.compose.viewModel(
        factory = DailyActivityViewModel.Factory(childId)
    )
) {
    val TAG = "DailyActivityCard"

    val isLight = !smartclassTheme.colors.isDark
    val isDarkMode = isSystemInDarkTheme()

    val activities by viewModel.activities.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()

    Log.d(TAG, "Composing DailyActivityCard")

    Card(
        shape = RoundedCornerShape(16.dp),
        backgroundColor = if (isDarkMode) Color(0xFF2C2C2C) else Color(0xFFF0F0F0),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = "Aktivitas Hari Ini",
                style = MaterialTheme.typography.h6.copy(fontSize = 18.sp, fontWeight = FontWeight.Bold),
                color = if (isDarkMode) Color(0xFFC8E6C9) else Color(0xFF388E3C)
            )
            Spacer(modifier = Modifier.height(16.dp))

            when {
                isLoading -> {
                    Log.d(TAG, "Loading activities...")
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(100.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = if (isLight) DarkBlue else LightBlue)
                    }
                }
                !error.isNullOrEmpty() -> {
                    Log.e(TAG, "Error: $error")
                    Text(
                        text = error!!,
                        color = MaterialTheme.colors.error,
                        modifier = Modifier.padding(8.dp)
                    )
                }
                activities.isEmpty() -> {
                    Log.d(TAG, "No activities today")
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
                                text = "Belum ada aktivitas untuk hari ini.",
                                style = MaterialTheme.typography.body1.copy(fontWeight = FontWeight.Medium),
                                color = if (isDarkMode) Color(0xFFB0BEC5) else Color(0xFF00695C),
                                modifier = Modifier.fillMaxWidth(),
                                maxLines = 2
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            OutlinedButton(
                                onClick = {
                                    Log.d(TAG, "Add New Activity button clicked")
                                    navController.navigate("add_activity/$childId")
                                },
                                shape = RoundedCornerShape(16.dp),
                                border = BorderStroke(1.dp, if (isDarkMode) Color(0xFF81D4FA).copy(alpha = 0.5f) else Color(0xFF4FC3F7).copy(alpha = 0.5f)),
                                colors = ButtonDefaults.outlinedButtonColors(
                                    backgroundColor = Color.Transparent,
                                    contentColor = if (isDarkMode) Color(0xFF81D4FA) else Color(0xFF0288D1)
                                ),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(text = "+ Aktivitas Baru")
                            }
                        }
                    }
                }
                else -> {
                    Log.d(TAG, "Displaying activities list")
                    LazyColumn(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(items = activities, key = { it.timestamp }) { activity ->
                            ActivityItem(
                                activity = activity,
                                isDarkMode = isDarkMode,
                                onClick = {
                                    Log.d(TAG, "Activity clicked: ${activity.timestamp}")
                                    navController.navigate("edit_activity/${childId}/${activity.timestamp}")
                                }
                            )
                        }
                        item {
                            Spacer(modifier = Modifier.height(16.dp))
                            OutlinedButton(
                                onClick = {
                                    Log.d(TAG, "Add New Activity button clicked")
                                    navController.navigate("add_activity/$childId")
                                },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(16.dp),
                                border = BorderStroke(
                                    1.dp,
                                    if (isDarkMode) Color(0xFF81D4FA).copy(alpha = 0.5f) else Color(0xFF4FC3F7).copy(alpha = 0.5f)
                                ),
                                colors = ButtonDefaults.outlinedButtonColors(
                                    backgroundColor = Color.Transparent,
                                    contentColor = if (isLight) Color(0xFF0288D1) else Color(0xFF81D4FA)
                                )
                            ) {
                                Text(text = "+ Aktivitas Baru")
                            }
                        }
                    }
                }
            }
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun ActivityItem(
    activity: Activity,
    isDarkMode: Boolean,
    onClick: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(12.dp),
        backgroundColor = if (isDarkMode) Color(0xFF37474F) else Color(0xFFE0F7FA),
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .clickable { onClick() }
    ) {
        Row(
            modifier = Modifier
                .padding(12.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = activity.activityName,
                    style = MaterialTheme.typography.subtitle1.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    ),
                    color = if (isDarkMode) Color(0xFFCFD8DC) else Color(0xFF004D40)
                )

                val formattedDate = remember(activity.selectedDate) {
                    try {
                        val inputFormatter = DateTimeFormatter.ofPattern("d/M/yyyy", Locale("id", "ID"))
                        val date = LocalDate.parse(activity.selectedDate, inputFormatter)
                        val outputFormatter = DateTimeFormatter.ofPattern("EEEE, dd MMMM yyyy", Locale("id", "ID"))
                        date.format(outputFormatter).replaceFirstChar { it.uppercaseChar() }
                    } catch (e: DateTimeParseException) {
                        Log.e("ActivityItem", "Error parsing selectedDate: ${activity.selectedDate}", e)
                        activity.selectedDate
                    }
                }

                Text(
                    text = formattedDate,
                    style = MaterialTheme.typography.body2.copy(
                        fontWeight = FontWeight.Medium,
                        fontSize = 14.sp
                    ),
                    color = if (isDarkMode) Color(0xFFCFD8DC) else Color(0xFF00695C)
                )

                Spacer(modifier = Modifier.height(4.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Card(
                        shape = RoundedCornerShape(8.dp),
                        backgroundColor = if (isDarkMode) Color(0xFF546E7A) else Color(0xFF4FC3F7),
                        modifier = Modifier
                            .padding(end = 8.dp)
                    ) {
                        Text(
                            text = activity.startTime,
                            style = MaterialTheme.typography.body2.copy(
                                color = Color.White.copy(alpha = 0.9f)
                            ),
                            modifier = Modifier
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }
            }
            Icon(
                imageVector = Icons.Default.ArrowForward,
                contentDescription = "Navigate",
                tint = if (isDarkMode) Color(0xFF81D4FA) else Color(0xFF0288D1),
                modifier = Modifier.size(20.dp)
            )
        }
    }
}
