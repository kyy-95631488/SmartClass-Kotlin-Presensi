package com.callcenter.smartclass.ui.home.childprofile.diaryaktivitas.edit

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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.callcenter.smartclass.ui.home.childprofile.viewmodel.EditActivityViewModel
import com.callcenter.smartclass.ui.theme.*
import kotlinx.coroutines.launch
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditActivityScreen(
    navController: NavController,
    childId: String,
    activityTimestamp: Long,
    viewModel: EditActivityViewModel = androidx.lifecycle.viewmodel.compose.viewModel(
        factory = EditActivityViewModel.Factory(childId, activityTimestamp)
    )
) {
    val activity by viewModel.activity.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()
    val isUpdateSuccessful by viewModel.isUpdateSuccessful.collectAsState()

    val context = LocalContext.current
    val isLight = !smartclassTheme.colors.isDark
    val isDarkMode = isSystemInDarkTheme()
    val scaffoldState = rememberScaffoldState()
    val coroutineScope = rememberCoroutineScope()

    var activityName by remember { mutableStateOf("") }
    var selectedDate by remember { mutableStateOf("") }
    var startTime by remember { mutableStateOf("") }
    var endTime by remember { mutableStateOf("") }
    var selectedTags = remember { mutableStateListOf<String>() }
    var activityDetail by remember { mutableStateOf("") }
    val tags = listOf("Konsultasi", "Kursus", "Sekolah", "Aktivitas Harian", "Imunisasi")

    LaunchedEffect(activity) {
        activity?.let {
            activityName = it.activityName
            selectedDate = it.selectedDate
            startTime = it.startTime
            endTime = it.endTime
            selectedTags.clear()
            selectedTags.addAll(it.selectedTags)
            activityDetail = it.detail
        }
    }

    LaunchedEffect(isUpdateSuccessful) {
        isUpdateSuccessful?.let { success ->
            if (success) {
                navController.popBackStack()
            } else {
                coroutineScope.launch {
                    scaffoldState.snackbarHostState.showSnackbar("Gagal menyimpan aktivitas")
                }
            }
            viewModel.resetUpdateSuccess()
        }
    }

    // Perbaikan: Ubah fungsi openTimePickerDialog untuk menerima lambda
    val openTimePickerDialog: ( (String) -> Unit ) -> Unit = { setTime ->
        val calendar = Calendar.getInstance()
        TimePickerDialog(
            context,
            { _, hourOfDay, minute ->
                setTime(String.format("%02d:%02d", hourOfDay, minute))
            },
            calendar.get(Calendar.HOUR_OF_DAY),
            calendar.get(Calendar.MINUTE),
            true
        ).show()
    }

    val openDatePickerDialog = {
        val calendar = Calendar.getInstance().apply {
            val parts = selectedDate.split("/").mapNotNull { it.toIntOrNull() }
            if (parts.size == 3) {
                set(Calendar.DAY_OF_MONTH, parts[0])
                set(Calendar.MONTH, parts[1] - 1)
                set(Calendar.YEAR, parts[2])
            }
        }
        DatePickerDialog(
            context,
            { _, year, monthOfYear, dayOfMonth ->
                selectedDate = "$dayOfMonth/${monthOfYear + 1}/$year"
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        ).show()
    }

    Scaffold(
        scaffoldState = scaffoldState,
        topBar = {
            TopAppBar(
                title = { Text("Edit Aktivitas", color = if (isLight) MinimalTextLight else MinimalTextDark) },
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
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            when {
                isLoading -> {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center),
                        color = if (isLight) DarkBlue else LightBlue
                    )
                }
                !error.isNullOrEmpty() -> {
                    LaunchedEffect(error) {
                        coroutineScope.launch {
                            scaffoldState.snackbarHostState.showSnackbar(error ?: "Terjadi kesalahan")
                        }
                    }
                }
                activity != null -> {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        // Activity Name
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

                        // Selected Date
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

                        // Start and End Time
                        item {
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(16.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                OutlinedTextField(
                                    value = startTime,
                                    onValueChange = { },
                                    label = { Text("Mulai", color = if (isLight) MinimalTextLight else MinimalTextDark) },
                                    modifier = Modifier
                                        .weight(1f)
                                        .clickable { openTimePickerDialog { newTime -> startTime = newTime } },
                                    readOnly = true,
                                    trailingIcon = {
                                        Icon(
                                            imageVector = Icons.Filled.AccessTime,
                                            contentDescription = "Pilih Waktu Mulai",
                                            tint = if (isLight) MinimalTextLight else MinimalTextDark,
                                            modifier = Modifier.clickable { openTimePickerDialog { newTime -> startTime = newTime } }
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
                                OutlinedTextField(
                                    value = endTime,
                                    onValueChange = { },
                                    label = { Text("Selesai", color = if (isLight) MinimalTextLight else MinimalTextDark) },
                                    modifier = Modifier
                                        .weight(1f)
                                        .clickable { openTimePickerDialog { newTime -> endTime = newTime } },
                                    readOnly = true,
                                    trailingIcon = {
                                        Icon(
                                            imageVector = Icons.Filled.AccessTime,
                                            contentDescription = "Pilih Waktu Selesai",
                                            tint = if (isLight) MinimalTextLight else MinimalTextDark,
                                            modifier = Modifier.clickable { openTimePickerDialog { newTime -> endTime = newTime } }
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

                        // Tags Section
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

                        // Activity Detail
                        item {
                            OutlinedTextField(
                                value = activityDetail,
                                onValueChange = { activityDetail = it },
                                label = { Text("Detail Aktivitas", color = if (isLight) MinimalTextLight else MinimalTextDark) },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(150.dp),
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

                        // Save Button
                        item {
                            Button(
                                onClick = {
                                    if (activityName.isEmpty()) {
                                        coroutineScope.launch {
                                            scaffoldState.snackbarHostState.showSnackbar("Nama aktivitas tidak boleh kosong")
                                        }
                                    } else {
                                        // Update each field in the ViewModel
                                        viewModel.updateActivityName(activityName)
                                        viewModel.updateSelectedDate(selectedDate)
                                        viewModel.updateStartTime(startTime)
                                        viewModel.updateEndTime(endTime)
                                        viewModel.updateSelectedTags(selectedTags.toList())
                                        viewModel.updateDetail(activityDetail)

                                        // Then call updateActivity without parameters
                                        viewModel.updateActivity()
                                    }
                                },
                                modifier = Modifier.fillMaxWidth(),
                                colors = ButtonDefaults.buttonColors(
                                    backgroundColor = if (isDarkMode) ButtonDarkColor else ButtonLightColor,
                                    contentColor = WhiteColor
                                ),
                                elevation = ButtonDefaults.elevation(8.dp),
                                enabled = !isLoading
                            ) {
                                Text("Simpan")
                            }
                        }
                    }
                }
            }
        }
    }
}
