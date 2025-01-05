package com.callcenter.smartclass.ui.home.mainfeaturesgrid.predictv2

import android.annotation.SuppressLint
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Save
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.callcenter.smartclass.R
import com.callcenter.smartclass.ui.home.mainfeaturesgrid.predictv2.data.AppDatabase
import com.callcenter.smartclass.ui.home.mainfeaturesgrid.predictv2.network.ChildPrediction
import com.callcenter.smartclass.ui.home.mainfeaturesgrid.predictv2.viewmodel.PredictUiState
import com.callcenter.smartclass.ui.home.mainfeaturesgrid.predictv2.viewmodel.PredictViewModel
import com.callcenter.smartclass.ui.home.mainfeaturesgrid.predictv2.viewmodel.PredictViewModelFactory
import com.callcenter.smartclass.ui.theme.*
import com.google.firebase.auth.FirebaseAuth
import com.patrykandpatrick.vico.compose.axis.horizontal.bottomAxis
import com.patrykandpatrick.vico.compose.chart.Chart
import com.patrykandpatrick.vico.compose.chart.line.lineChart
import com.patrykandpatrick.vico.core.axis.formatter.AxisValueFormatter
import com.patrykandpatrick.vico.core.entry.ChartEntryModelProducer
import com.patrykandpatrick.vico.core.entry.FloatEntry

@SuppressLint("UnusedMaterialScaffoldPaddingParameter", "UnusedBoxWithConstraintsScope")
@Composable
fun PredictOnline(navController: NavController) {
    val context = LocalContext.current
    val childDao = AppDatabase.getDatabase(context).childDao()
    val factory = PredictViewModelFactory(childDao)
    val viewModel: PredictViewModel = viewModel(factory = factory)

    val scaffoldState = rememberScaffoldState()
    val isLight = !smartclassTheme.colors.isDark
    val isDarkMode = isSystemInDarkTheme()

    val auth = FirebaseAuth.getInstance()
    val currentUser = auth.currentUser

    LaunchedEffect(key1 = currentUser?.uid) {
        currentUser?.uid?.let { uid ->
            viewModel.fetchPredictionsForAllChildren(userId = uid)
        }
    }

    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        scaffoldState = scaffoldState,
        topBar = {
            TopAppBar(
                title = { Text("Prediksi Online", color = if (isDarkMode) Color.White else Color.Black) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            Icons.Filled.ArrowBack,
                            contentDescription = "Kembali",
                            tint = if (isLight) DarkBlue else LightBlue
                        )
                    }
                },
                backgroundColor = if (isLight) MinimalBackgroundLight else MinimalBackgroundDark,
                contentColor = if (isLight) MinimalTextLight else MinimalTextDark
            )
        },
        backgroundColor = smartclassTheme.colors.uiBackground,
        content = { innerPadding ->
            BoxWithConstraints(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(16.dp)
            ) {
                val maxWidth = maxWidth
                val isWideScreen = maxWidth > 600.dp // Contoh batas lebar layar

                Column(
                    modifier = Modifier
                        .fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        elevation = 8.dp,
                        backgroundColor = smartclassTheme.colors.uiFloated
                    ) {
                        Column(
                            modifier = Modifier
                                .padding(16.dp)
                        ) {
                            Text(
                                text = "Jika Anda ingin bisa memprediksi apakah anak stunting atau tidak secara offline, Anda harus klik tombol simpan terlebih dahulu (membutuhkan akses internet). Setelah itu, Anda baru bisa mengakses halaman prediksi offline tanpa internet.",
                                style = MaterialTheme.typography.body2,
                                modifier = Modifier.padding(bottom = 16.dp),
                                color = if (isDarkMode) Color.White else Color.Black
                            )
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.End,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Button(
                                    onClick = {
                                        viewModel.saveDataToLocal()
                                    },
                                    colors = ButtonDefaults.buttonColors(
                                        backgroundColor = if (isLight) DarkBlue else LightBlue,
                                        contentColor = Color.White
                                    ),
                                    modifier = Modifier
                                        .padding(8.dp)
                                        .height(48.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Save,
                                        contentDescription = "Simpan Data",
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Simpan Data")
                                }
                            }
                        }
                    }

                    when (uiState) {
                        PredictUiState.Loading -> {
                            Box(
                                modifier = Modifier.fillMaxWidth(),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxSize(),
                                    verticalArrangement = Arrangement.Center,
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    CircularProgressIndicator(
                                        color = if (isDarkMode) Color(0xFF81D4FA) else Color(0xFF66BB6A),
                                        strokeWidth = 6.dp
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        text = stringResource(id = R.string.Loading),
                                        style = MaterialTheme.typography.body1.copy(color = if (isDarkMode) Color.White else Color.Black)
                                    )
                                }
                            }
                        }
                        is PredictUiState.Success -> {
                            val predictions = (uiState as PredictUiState.Success).predictions
                            if (predictions.isNotEmpty()) {
                                if (predictions.size > 1) {
                                    // Gunakan LazyVerticalGrid untuk layout grid yang responsif
                                    LazyVerticalGrid(
                                        columns = GridCells.Adaptive(minSize = 300.dp),
                                        verticalArrangement = Arrangement.spacedBy(16.dp),
                                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        items(predictions) { childPrediction ->
                                            childPrediction.prediction?.let { prediction ->
                                                PredictionCard(childPrediction = childPrediction)
                                            } ?: run {
                                                Card(
                                                    modifier = Modifier
                                                        .fillMaxWidth()
                                                        .height(250.dp),
                                                    elevation = 8.dp,
                                                    backgroundColor = smartclassTheme.colors.uiFloated
                                                ) {
                                                    Column(
                                                        modifier = Modifier
                                                            .padding(16.dp),
                                                        verticalArrangement = Arrangement.Center,
                                                        horizontalAlignment = Alignment.CenterHorizontally
                                                    ) {
                                                        Text(
                                                            text = "Prediksi gagal untuk ${childPrediction.child.name}",
                                                            color = Color.Red
                                                        )
                                                    }
                                                }
                                            }
                                        }
                                    }
                                } else {
                                    predictions.firstOrNull()?.prediction?.let { prediction ->
                                        PredictionCard(childPrediction = predictions.first())
                                    }
                                }
                            } else {
                                Text(
                                    "Tidak ada data prediksi tersedia.",
                                    style = MaterialTheme.typography.body1,
                                    color = if (isDarkMode) Color.White else Color.Black
                                )
                            }
                        }
                        is PredictUiState.Error -> {
                            // Error sudah ditangani dengan Snackbar
                        }
                        else -> {}
                    }
                }
            }
        }
    )

    LaunchedEffect(Unit) {
        viewModel.saveResult.collect { result ->
            result.onSuccess { message ->
                scaffoldState.snackbarHostState.showSnackbar(message)
            }.onFailure { exception ->
                scaffoldState.snackbarHostState.showSnackbar("Gagal menyimpan data: ${exception.message}")
            }
        }
    }
}

@Composable
fun PredictionCard(childPrediction: ChildPrediction) {
    val prediction = childPrediction.prediction
    val namaAnak = prediction?.nama_anak ?: childPrediction.child.name
    val usia = prediction?.usia ?: childPrediction.child.birthDate
    val probabilitasStunting = prediction?.probabilitas_stunting?.replace("%", "")?.toFloatOrNull() ?: 0f
    val probabilitasTidakStunting = prediction?.probabilitas_tidak_stunting?.replace("%", "")?.toFloatOrNull() ?: 0f
    val isDarkMode = isSystemInDarkTheme()

    Card(
        modifier = Modifier
            .fillMaxWidth(),
        elevation = 8.dp,
        backgroundColor = smartclassTheme.colors.uiFloated
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
        ) {
            Text(
                text = "Prediksi Stunting untuk $namaAnak (Usia: $usia bulan)",
                style = MaterialTheme.typography.subtitle1,
                color = if (isDarkMode) Color.White else Color.Black
            )
            Spacer(modifier = Modifier.height(16.dp))

            val chartEntries = listOf(
                FloatEntry(0f, probabilitasTidakStunting),
                FloatEntry(1f, probabilitasStunting)
            )
            val labels = listOf("Tidak Stunting", "Stunting")

            val chartEntryModelProducer = ChartEntryModelProducer(chartEntries)

            val lineChart = lineChart()

            val bottomAxisConfig = bottomAxis(
                valueFormatter = AxisValueFormatter { value, _ ->
                    labels.getOrNull(value.toInt()) ?: value.toString()
                }
            )

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
            ) {
                Chart(
                    chart = lineChart,
                    chartModelProducer = chartEntryModelProducer,
                    modifier = Modifier.fillMaxSize(),
                    startAxis = null,
                    bottomAxis = bottomAxisConfig
                )
            }

            Spacer(modifier = Modifier.height(8.dp))
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "${probabilitasTidakStunting}% Tidak Stunting",
                    color = Color.Green
                )
                Text(
                    text = "${probabilitasStunting}% Stunting",
                    color = Color.Red
                )
            }
        }
    }
}
