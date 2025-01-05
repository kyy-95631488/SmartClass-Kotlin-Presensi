package com.callcenter.smartclass.ui.home.childprofile

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.callcenter.smartclass.ui.theme.*
import java.util.*
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddActivityScreen(navController: NavController, childId: String) {
    val context = LocalContext.current
    val isLight = !smartclassTheme.colors.isDark
    var activityName by remember { mutableStateOf("") }
    var startTime by remember { mutableStateOf("") }
    var endTime by remember { mutableStateOf("") }
    var selectedTags = remember { mutableStateListOf<String>() }
    var isUploading by remember { mutableStateOf(false) }

    // Capture the detail activity input
    var activityDetail by remember { mutableStateOf("") }

    fun getCurrentDate(): String {
        val calendar = Calendar.getInstance()
        val day = calendar.get(Calendar.DAY_OF_MONTH)
        val month = calendar.get(Calendar.MONTH) + 1
        val year = calendar.get(Calendar.YEAR)
        return "$day/$month/$year"
    }

    var selectedDate by remember { mutableStateOf(getCurrentDate()) }

    val tags = listOf("Konsultasi", "Kursus", "Sekolah", "Aktivitas Harian", "Imunisasi")

    val isDarkMode = isSystemInDarkTheme()

    val openDatePickerDialog = {
        val calendar = Calendar.getInstance()
        val datePickerDialog = DatePickerDialog(
            context,
            { _, year, monthOfYear, dayOfMonth ->
                selectedDate = "$dayOfMonth/${monthOfYear + 1}/$year"
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        )
        datePickerDialog.show()
    }

    val openTimePickerDialog = { onTimeSelected: (String) -> Unit ->
        val calendar = Calendar.getInstance()
        val timePickerDialog = TimePickerDialog(
            context,
            { _, hourOfDay, minute ->
                val formattedTime = String.format("%02d:%02d", hourOfDay, minute)
                onTimeSelected(formattedTime)
            },
            calendar.get(Calendar.HOUR_OF_DAY),
            calendar.get(Calendar.MINUTE),
            true
        )
        timePickerDialog.show()
    }

    val scaffoldState = rememberScaffoldState()
    val auth = FirebaseAuth.getInstance()
    val firestore = FirebaseFirestore.getInstance()
    val coroutineScope = rememberCoroutineScope()

    Scaffold(
        scaffoldState = scaffoldState,
        topBar = {
            TopAppBar(
                title = { Text("Tambah Aktivitas", color = if (isLight) MinimalTextLight else MinimalTextDark) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            Icons.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = if (isLight) DarkBlue else LightBlue
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = if (isLight) MinimalBackgroundLight else MinimalBackgroundDark
                )
            )
        },
        backgroundColor = smartclassTheme.colors.uiBackground
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                OutlinedTextField(
                    value = activityName,
                    onValueChange = { activityName = it },
                    label = { Text("Nama Aktivitas", color = if (isLight) MinimalTextLight else MinimalTextDark) },
                    modifier = Modifier.fillMaxWidth(),
                    colors = TextFieldDefaults.outlinedTextFieldColors(
                        backgroundColor = if (isDarkMode) MinimalBackgroundDark else MinimalBackgroundLight,
                        focusedBorderColor = MinimalPrimary,
                        unfocusedBorderColor = MinimalSecondary,
                        cursorColor = MinimalPrimary,
                        textColor = if (isDarkMode) MinimalTextDark else MinimalTextLight,
                    )
                )
            }

            item {
                OutlinedTextField(
                    value = selectedDate,
                    onValueChange = { },
                    label = { Text("Tanggal Aktivitas", color = if (isLight) MinimalTextLight else MinimalTextDark) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { openDatePickerDialog() },
                    readOnly = true,
                    trailingIcon = {
                        Icon(
                            Icons.Filled.CalendarToday,
                            contentDescription = "Pick Date",
                            tint = if (isLight) MinimalTextLight else MinimalTextDark,
                            modifier = Modifier.clickable { openDatePickerDialog() }
                        )
                    },
                    colors = TextFieldDefaults.outlinedTextFieldColors(
                        backgroundColor = if (isDarkMode) MinimalBackgroundDark else MinimalBackgroundLight,
                        focusedBorderColor = MinimalPrimary,
                        unfocusedBorderColor = MinimalSecondary,
                        cursorColor = MinimalPrimary,
                        textColor = if (isDarkMode) MinimalTextDark else MinimalTextLight,
                    )
                )
            }

            item {
                Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = startTime,
                        onValueChange = { },
                        label = { Text("Mulai", color = if (isLight) MinimalTextLight else MinimalTextDark) },
                        modifier = Modifier
                            .weight(1f)
                            .clickable {
                                openTimePickerDialog { selectedTime ->
                                    startTime = selectedTime
                                }
                            },
                        readOnly = true,
                        trailingIcon = {
                            Icon(
                                imageVector = Icons.Filled.AccessTime,
                                contentDescription = "Pilih Waktu Mulai",
                                tint = if (isLight) MinimalTextLight else MinimalTextDark,
                                modifier = Modifier.clickable {
                                    openTimePickerDialog { selectedTime ->
                                        startTime = selectedTime
                                    }
                                }
                            )
                        },
                        colors = TextFieldDefaults.outlinedTextFieldColors(
                            backgroundColor = if (isDarkMode) MinimalBackgroundDark else MinimalBackgroundLight,
                            focusedBorderColor = MinimalPrimary,
                            unfocusedBorderColor = MinimalSecondary,
                            cursorColor = MinimalPrimary,
                            textColor = if (isDarkMode) MinimalTextDark else MinimalTextLight,
                        )
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    OutlinedTextField(
                        value = endTime,
                        onValueChange = { },
                        label = { Text("Selesai", color = if (isLight) MinimalTextLight else MinimalTextDark) },
                        modifier = Modifier
                            .weight(1f)
                            .clickable {
                                openTimePickerDialog { selectedTime ->
                                    endTime = selectedTime
                                }
                            },
                        readOnly = true,
                        trailingIcon = {
                            Icon(
                                imageVector = Icons.Filled.AccessTime,
                                contentDescription = "Pilih Waktu Selesai",
                                tint = if (isLight) MinimalTextLight else MinimalTextDark,
                                modifier = Modifier.clickable {
                                    openTimePickerDialog { selectedTime ->
                                        endTime = selectedTime
                                    }
                                }
                            )
                        },
                        colors = TextFieldDefaults.outlinedTextFieldColors(
                            backgroundColor = if (isDarkMode) MinimalBackgroundDark else MinimalBackgroundLight,
                            focusedBorderColor = MinimalPrimary,
                            unfocusedBorderColor = MinimalSecondary,
                            cursorColor = MinimalPrimary,
                            textColor = if (isDarkMode) MinimalTextDark else MinimalTextLight,
                        )
                    )
                }
            }

            item {
                Text("Tambah Tag", color = if (isLight) MinimalTextLight else MinimalTextDark, style = MaterialTheme.typography.subtitle1)
            }

            item {
                LazyRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(tags.size) { index ->
                        val tag = tags[index]
                        val isSelected = selectedTags.contains(tag)

                        Row(
                            modifier = Modifier
                                .clickable {
                                    if (isSelected) selectedTags.remove(tag)
                                    else selectedTags.add(tag)
                                }
                                .padding(8.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isSelected) MinimalPrimary else MaterialTheme.colors.surface)
                                .padding(horizontal = 12.dp, vertical = 8.dp),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = if (isSelected) Icons.Default.Check else Icons.Default.Add,
                                contentDescription = if (isSelected) "Selected" else "Not Selected",
                                tint = if (isSelected) MaterialTheme.colors.onPrimary else MaterialTheme.colors.onSurface
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = tag,
                                style = TextStyle(fontSize = 14.sp),
                                color = if (isSelected) MaterialTheme.colors.onPrimary else MaterialTheme.colors.onSurface
                            )
                        }
                    }
                }
            }

            item {
                OutlinedTextField(
                    value = activityDetail,
                    onValueChange = { activityDetail = it },
                    label = { Text("Detail Aktivitas (opsional)", color = if (isLight) MinimalTextLight else MinimalTextDark) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(150.dp),
                    colors = TextFieldDefaults.outlinedTextFieldColors(
                        backgroundColor = if (isDarkMode) MinimalBackgroundDark else MinimalBackgroundLight,
                        focusedBorderColor = MinimalPrimary,
                        unfocusedBorderColor = MinimalSecondary,
                        cursorColor = MinimalPrimary,
                        textColor = if (isDarkMode) MinimalTextDark else MinimalTextLight,
                    )
                )
            }

            item {
                Button(
                    onClick = {
                        if (activityName.isEmpty()) {
                            coroutineScope.launch {
                                scaffoldState.snackbarHostState.showSnackbar("Nama aktivitas tidak boleh kosong")
                            }
                        } else {
                            isUploading = true
                            val currentUser = auth.currentUser
                            if (currentUser != null) {
                                val userId = currentUser.uid
                                val activityData = hashMapOf(
                                    "activityName" to activityName,
                                    "selectedDate" to selectedDate,
                                    "startTime" to startTime,
                                    "endTime" to endTime,
                                    "selectedTags" to selectedTags.toList(),
                                    "detail" to activityDetail,
                                    "timestamp" to System.currentTimeMillis()
                                )

                                firestore.collection("users")
                                    .document(userId)
                                    .collection("children")
                                    .document(childId)
                                    .collection("activities")
                                    .add(activityData)
                                    .addOnSuccessListener {
                                        isUploading = false
                                        navController.popBackStack()
                                    }
                                    .addOnFailureListener { e ->
                                        isUploading = false
                                        coroutineScope.launch {
                                            scaffoldState.snackbarHostState.showSnackbar("Gagal menyimpan aktivitas: ${e.message}")
                                        }
                                    }
                            } else {
                                isUploading = false
                                coroutineScope.launch {
                                    scaffoldState.snackbarHostState.showSnackbar("Pengguna tidak masuk")
                                }
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        backgroundColor = if (isDarkMode) ButtonDarkColor else ButtonLightColor,
                        contentColor = WhiteColor
                    ),
                    elevation = ButtonDefaults.elevation(8.dp),
                    enabled = !isUploading
                ) {
                    Text("Simpan")
                }
            }
        }
    }
}
