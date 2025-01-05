package com.callcenter.smartclass.ui.home.childprofile.diarymenu.add

import android.app.AlarmManager
import android.content.Context
import android.content.Intent
import android.os.Build
import android.provider.Settings
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.callcenter.smartclass.ui.home.childprofile.diarymenu.RecipeSelectionDialog
import com.callcenter.smartclass.ui.home.childprofile.diarymenu.data.DailyMenu
import com.callcenter.smartclass.ui.home.childprofile.diarymenu.data.Recipe
import com.callcenter.smartclass.ui.home.childprofile.diarymenu.viewmodel.DailyMenuViewModel
import com.callcenter.smartclass.ui.theme.*
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

@RequiresApi(Build.VERSION_CODES.S)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddMenuScreen(childId: String, navController: NavHostController) {

    val context = LocalContext.current

    val snackbarHostState = remember { SnackbarHostState() }
    val isLight = !smartclassTheme.colors.isDark

    val alarmIntent = remember {
        Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM)
    }

    LaunchedEffect(Unit) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        if (!alarmManager.canScheduleExactAlarms()) {
            context.startActivity(alarmIntent)
        }
    }

    val currentUser = remember { FirebaseAuth.getInstance().currentUser }
    val userId = currentUser?.uid ?: ""

    var childName by remember { mutableStateOf<String?>(null) }
    var isLoading by remember { mutableStateOf(true) }

    suspend fun getChildName(userId: String, childId: String): String? {
        return try {
            val doc = FirebaseFirestore.getInstance()
                .collection("users")
                .document(userId)
                .collection("children")
                .document(childId)
                .get()
                .await()
            if (doc.exists()) {
                doc.getString("name")
            } else {
                null
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    LaunchedEffect(key1 = childId) {
        if (userId.isNotEmpty()) {
            childName = getChildName(userId, childId)
        }
        isLoading = false
    }

    Scaffold(
        containerColor = smartclassTheme.colors.uiBackground,
        snackbarHost = {
            SnackbarHost(snackbarHostState) { data ->
                Snackbar(
                    snackbarData = data,
                    containerColor = if (isLight) FunctionalRed else Rose5.copy(alpha = 0.85f),
                    contentColor = if (isLight) WhiteColor else MinimalBackgroundLight,
                    actionColor = if (isLight) WhiteColor else Neutral8
                )
            }
        },
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        "Tambah Menu Baru",
                        color = if (isLight) MinimalTextLight else MinimalTextDark,
                        style = MaterialTheme.typography.titleMedium
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = if (isLight) DarkBlue else LightBlue
                        )
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = if (isLight) MinimalBackgroundLight else MinimalBackgroundDark,
                    titleContentColor = if (isLight) MinimalTextLight else MinimalTextDark,
                    navigationIconContentColor = if (isLight) DarkText else WhiteColor,
                    actionIconContentColor = if (isLight) DarkText else WhiteColor
                )
            )
        }
    ) { paddingValues ->
        if (isLoading) {
            Box(
                modifier = Modifier
                    .padding(paddingValues)
                    .fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else {
            childName?.let { name ->
                ScheduleScreen(
                    modifier = Modifier
                        .padding(paddingValues)
                        .fillMaxSize()
                        .padding(16.dp),
                    childId = childId,
                    childName = name,
                    navController = navController,
                    snackbarHostState = snackbarHostState
                )
            } ?: run {
                Box(
                    modifier = Modifier
                        .padding(paddingValues)
                        .fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text("Nama anak tidak ditemukan.")
                }
            }
        }
    }
}

@Composable
fun ScheduleScreen(
    modifier: Modifier = Modifier,
    childId: String,
    childName: String,
    navController: NavHostController,
    snackbarHostState: SnackbarHostState,
    viewModel: DailyMenuViewModel = viewModel()
) {
    val scheduleItems = listOf(
        "06:00" to "ASI",
        "08:00" to "Makan Pagi",
        "10:00" to "Camilan",
        "12:00" to "Makan Siang",
        "14:00" to "ASI",
        "16:00" to "Camilan",
        "18:00" to "Makan Malam",
        "21:00" to "ASI",
        "24:00" to "ASI",
        "03:00" to "ASI"
    )

    val daysOfWeek = listOf(
        "Senin", "Selasa", "Rabu",
        "Kamis", "Jumat", "Sabtu", "Minggu"
    )

    val selectedDays = remember { mutableStateListOf<String>() }
    val selectedRecipes = remember { mutableStateMapOf<String, Recipe>() }

    val isLight = !smartclassTheme.colors.isDark
    val isDarkMode = isSystemInDarkTheme()

    val coroutineScope = rememberCoroutineScope()

    fun getUserId(): String? {
        return FirebaseAuth.getInstance().currentUser?.uid
    }

    suspend fun saveSchedule(selectedDays: List<String>): Boolean {
        val userId = getUserId() ?: return false
        val db = FirebaseFirestore.getInstance()

        return try {
            val existingScheduleSnapshot = db.collection("users")
                .document(userId)
                .collection("children")
                .document(childId)
                .collection("schedule")
                .get()
                .await()

            val existingDays = existingScheduleSnapshot.documents.map { it.id }

            val daysToDelete = existingDays.filter { it !in selectedDays }
            for (day in daysToDelete) {
                db.collection("users")
                    .document(userId)
                    .collection("children")
                    .document(childId)
                    .collection("schedule")
                    .document(day)
                    .delete()
                    .await()
            }

            // Save or update the selected schedule
            for (day in selectedDays) {
                val scheduleData = scheduleItems.map { (time, label) ->
                    val recipe = selectedRecipes[time]
                    mapOf(
                        "time" to time,
                        "label" to label,
                        "recipeId" to (if (label.uppercase() != "ASI") recipe?.id else null),
                        "recipeTitle" to (if (label.uppercase() != "ASI") recipe?.title else null)
                    )
                }

                db.collection("users")
                    .document(userId)
                    .collection("children")
                    .document(childId)
                    .collection("schedule")
                    .document(day)
                    .set(mapOf("items" to scheduleData))
                    .await()
            }

            // After saving, schedule notifications
            val dailyMenus = selectedDays.flatMap { day ->
                scheduleItems.mapNotNull { (time, label) ->
                    if (label.uppercase() != "ASI") {
                        val recipe = selectedRecipes[time]
                        recipe?.let {
                            DailyMenu(
                                time = "$day $time",
                                title = it.title,
                                thumbnailUrl = it.thumbnailUrl,
                                recipeId = it.id,
                                childName = childName
                            )
                        }
                    } else {
                        null
                    }
                }
            }

            // Cancel old notifications before scheduling new ones
            viewModel.cancelNotifications(dailyMenus = viewModel.dailyMenus.value)

            // Schedule new notifications
            viewModel.scheduleNotifications(dailyMenus = dailyMenus)

            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    suspend fun getExistingSchedule(userId: String, childId: String): Map<String, List<Map<String, Any?>>> {
        val db = FirebaseFirestore.getInstance()
        val scheduleMap = mutableMapOf<String, List<Map<String, Any?>>>()
        try {
            val scheduleSnapshot = db.collection("users")
                .document(userId)
                .collection("children")
                .document(childId)
                .collection("schedule")
                .get()
                .await()

            for (doc in scheduleSnapshot.documents) {
                val items = doc.get("items") as? List<Map<String, Any?>> ?: emptyList()
                scheduleMap[doc.id] = items
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return scheduleMap
    }

    LaunchedEffect(key1 = childId) {
        val userId = getUserId()
        if (userId != null) {
            val existingSchedule = getExistingSchedule(userId, childId)
            if (existingSchedule.isNotEmpty()) {
                selectedDays.clear()
                selectedDays.addAll(existingSchedule.keys)
                existingSchedule.forEach { (day, items) ->
                    items.forEach { item ->
                        val time = item["time"] as? String ?: ""
                        val recipeTitle = item["recipeTitle"] as? String
                        val recipeId = item["recipeId"] as? String
                        if (recipeTitle != null && recipeId != null) {
                            selectedRecipes[time] = Recipe(id = recipeId, title = recipeTitle)
                        }
                    }
                }
            }
        }
    }

    Column(
        modifier = modifier
    ) {
        Text(
            text = "Pilih Hari:",
            style = MaterialTheme.typography.titleSmall.copy(
                fontWeight = FontWeight.SemiBold,
                fontSize = 18.sp
            ),
            modifier = Modifier.padding(bottom = 12.dp),
            color = if (isLight) MinimalTextLight else MinimalTextDark
        )
        Row(
            modifier = Modifier
                .horizontalScroll(rememberScrollState())
                .padding(bottom = 20.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            daysOfWeek.forEach { day ->
                val isSelected = selectedDays.contains(day)
                Box(
                    modifier = Modifier
                        .background(
                            color = if (isSelected) Color(0xD36BDEE4) else Color.Gray.copy(alpha = 0.2f),
                            shape = RoundedCornerShape(20.dp)
                        )
                        .clickable {
                            if (isSelected) {
                                selectedDays.remove(day)
                            } else {
                                selectedDays.add(day)
                            }
                        }
                        .padding(horizontal = 14.dp, vertical = 8.dp)
                ) {
                    Text(
                        text = day,
                        color = if (isSelected) MinimalTextLight else Ocean8,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }

        Text(
            text = "Jadwalkan menu $childName di bawah ini",
            style = MaterialTheme.typography.titleSmall.copy(
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp
            ),
            modifier = Modifier.padding(bottom = 20.dp),
            color = if (isLight) MinimalTextLight else MinimalTextDark
        )

        LazyColumn(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(scheduleItems) { (time, label) ->
                ScheduleItem(
                    time = time,
                    label = label,
                    selectedRecipe = selectedRecipes[time],
                    onRecipeSelected = { recipe ->
                        if (recipe != null) {
                            selectedRecipes[time] = recipe
                        } else {
                            selectedRecipes.remove(time)
                        }
                    }
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Button(
                onClick = {
                    selectedDays.clear()
                    selectedDays.addAll(daysOfWeek)
                    coroutineScope.launch {
                        val success = saveSchedule(selectedDays.toList())
                        if (success) {
                            navController.popBackStack()
                        } else {
                            snackbarHostState.showSnackbar("Gagal menyimpan jadwal.")
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isDarkMode) Color(0xD36BDEE4) else Color(0xFF6BDEE4),
                    contentColor = Color.White
                )
            ) {
                Text(text = "Jadwalkan Untuk Semua Hari")
            }

            Button(
                onClick = {
                    if (selectedDays.isNotEmpty()) {
                        coroutineScope.launch {
                            val success = saveSchedule(selectedDays.toList())
                            if (success) {
                                navController.popBackStack()
                            } else {
                                snackbarHostState.showSnackbar("Gagal menyimpan jadwal.")
                            }
                        }
                    } else {
                        coroutineScope.launch {
                            snackbarHostState.showSnackbar("Pilih setidaknya satu hari untuk menjadwalkan.")
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isDarkMode) Color(0xD36BDEE4) else Color(0xFF6BDEE4),
                    contentColor = Color.White
                )
            ) {
                Text(
                    text = when {
                        selectedDays.size == 1 -> "Jadwalkan Untuk Hari ${selectedDays[0]} Saja"
                        selectedDays.size > 1 -> "Jadwalkan Untuk Hari yang Dipilih"
                        else -> "Jadwalkan"
                    }
                )
            }
        }
    }

    LaunchedEffect(selectedDays) {
        if (selectedDays.isEmpty()) {
            snackbarHostState.showSnackbar("Pilih setidaknya satu hari untuk menjadwalkan.")
        }
    }
}

@Composable
fun ScheduleItem(
    time: String,
    label: String,
    selectedRecipe: Recipe? = null,
    onRecipeSelected: (Recipe?) -> Unit
) {
    val isLight = !smartclassTheme.colors.isDark
    var showDialog by remember { mutableStateOf(false) }

    if (showDialog) {
        RecipeSelectionDialog(
            onDismiss = { showDialog = false },
            onRecipeSelected = { recipe ->
                onRecipeSelected(recipe)
                showDialog = false
            }
        )
    }

    Card(
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
        modifier = Modifier
            .fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (isLight) Color(0xFFF0F0F0) else Color(0xFF303030)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = time,
                color = if (isLight) MinimalTextLight else MinimalTextDark,
                style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
                modifier = Modifier.weight(0.2f)
            )
            Text(
                text = label,
                color = if (isLight) MinimalTextLight else MinimalTextDark,
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.weight(0.5f)
            )
            if (label.uppercase() != "ASI") {
                Row(
                    modifier = Modifier
                        .weight(0.3f)
                        .clickable { showDialog = true },
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.End
                ) {
                    // Teks Resep yang Dipilih atau "Pilih Resep"
                    Text(
                        text = selectedRecipe?.title ?: "Pilih Resep",
                        color = if (selectedRecipe != null) Color(0xD36BDEE4) else if (isLight) MinimalTextLight else MinimalTextDark,
                        textAlign = TextAlign.End,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.weight(1f)
                    )
                    Spacer(modifier = Modifier.width(4.dp)) // Jarak antar elemen

                    // Ikon Check atau Arrow Drop Down
                    if (selectedRecipe != null) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = "Resep Dipilih",
                            tint = Color.Green,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp)) // Jarak antar ikon
                        // Tambahkan Ikon Silang untuk Menghapus Resep
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Hapus Resep",
                            tint = Color.Red,
                            modifier = Modifier
                                .size(16.dp)
                                .clickable {
                                    onRecipeSelected(null) // Hapus resep yang dipilih
                                }
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Default.ArrowDropDown,
                            contentDescription = "Pilih Resep",
                            tint = if (isLight) MinimalTextLight else MinimalTextDark
                        )
                    }
                }
            }
        }
    }
}
