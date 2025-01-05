package com.callcenter.smartclass.ui.home.pesanan

import android.app.Activity
import android.content.Context
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
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
import androidx.navigation.NavController
import com.callcenter.smartclass.ui.home.pesanan.viewmodel.PaymentsViewModel
import com.callcenter.smartclass.ui.theme.*
import com.midtrans.sdk.uikit.api.model.CustomColorTheme
import com.midtrans.sdk.uikit.api.model.TransactionResult
import com.midtrans.sdk.uikit.external.UiKitApi
import com.midtrans.sdk.uikit.internal.util.UiKitConstants
import kotlinx.coroutines.delay

private const val TAG = "PaymentsMidtrans"

@OptIn(ExperimentalMaterial3Api::class, ExperimentalAnimationApi::class)
@Composable
fun PaymentsMidtrans(navController: NavController, orderId: String) {
    Log.d(TAG, "PaymentsMidtrans composable invoked with orderId: $orderId")
    val context = LocalContext.current
    val activity = context as? Activity
    val isDarkMode = smartclassTheme.colors.isDark
    val isLight = !smartclassTheme.colors.isDark

    val viewModel: PaymentsViewModel = viewModel()

    val orderState by viewModel.order.collectAsState()
    val order = orderState
    Log.d(TAG, "Order State: $order")

    val transactionStatusState by viewModel.transactionStatus.collectAsState()
    val transactionStatus = transactionStatusState
    Log.d(TAG, "Transaction Status State: $transactionStatus")

    LaunchedEffect(orderId) {
        Log.d(TAG, "LaunchedEffect triggered for orderId: $orderId")
        viewModel.getOrder(orderId)
    }

    val paymentLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        Log.d(TAG, "Payment activity result received with resultCode: ${result.resultCode}")
        if (result.resultCode == Activity.RESULT_OK) {
            val transactionResult =
                result.data?.getParcelableExtra<TransactionResult>(UiKitConstants.KEY_TRANSACTION_RESULT)
            Log.d(TAG, "TransactionResult: $transactionResult")
            transactionResult?.let {
                when (it.status) {
                    "success" -> {
                        Toast.makeText(
                            context,
                            "Transaksi berhasil. ID: ${it.transactionId}",
                            Toast.LENGTH_LONG
                        ).show()
                        Log.d(TAG, "Transaction successful: ${it.transactionId}")
                        viewModel.updatePaymentStatus(orderId, "lunas")
                        viewModel.fetchTransactionStatus(orderId, "lunas")
                    }
                    "pending" -> {
                        Toast.makeText(
                            context,
                            "Transaksi tertunda. ID: ${it.transactionId}",
                            Toast.LENGTH_LONG
                        ).show()
                        Log.d(TAG, "Transaction pending: ${it.transactionId}")
                        viewModel.updatePaymentStatus(orderId, "Pending")
                        viewModel.fetchTransactionStatus(orderId, "Pending")
                    }
                    "failed" -> {
                        Toast.makeText(
                            context,
                            "Transaksi gagal. ID: ${it.transactionId}",
                            Toast.LENGTH_LONG
                        ).show()
                        Log.d(TAG, "Transaction failed: ${it.transactionId}")
                        viewModel.updatePaymentStatus(orderId, "Expired")
                        viewModel.fetchTransactionStatus(orderId, it.transactionId)
                    }
                    "canceled" -> {
                        Toast.makeText(
                            context,
                            "Transaksi dibatalkan",
                            Toast.LENGTH_LONG
                        ).show()
                        Log.d(TAG, "Transaction canceled")
                        viewModel.updatePaymentStatus(orderId, "dibatalkan")
                        viewModel.fetchTransactionStatus(orderId)
                    }
                    "invalid" -> {
                        Toast.makeText(
                            context,
                            "Transaksi tidak valid. ID: ${it.transactionId}",
                            Toast.LENGTH_LONG
                        ).show()
                        Log.d(TAG, "Transaction invalid: ${it.transactionId}")
                        viewModel.fetchTransactionStatus(orderId, it.transactionId)
                    }
                    else -> {
                        Toast.makeText(
                            context,
                            "ID Transaksi: ${it.transactionId}. Pesan: ${it.status}",
                            Toast.LENGTH_LONG
                        ).show()
                        Log.d(TAG, "Transaction status unknown: ${it.transactionId}, Status: ${it.status}")
                        viewModel.fetchTransactionStatus(orderId, it.transactionId)
                    }
                }
            } ?: run {
                Toast.makeText(context, "Transaksi tidak valid", Toast.LENGTH_LONG).show()
                Log.e(TAG, "TransactionResult is null")
            }
        } else {
            Log.d(TAG, "Payment activity result not OK")
        }
    }

    // Tambahkan state untuk melacak apakah pembayaran otomatis sudah dijalankan
    var paymentInitiated by remember { mutableStateOf(false) }

    LaunchedEffect(order) {
        order?.let {
            if (!paymentInitiated) { // Cek apakah pembayaran otomatis sudah dijalankan
                Log.d(TAG, "Fetching Snap Token for order: ${it.orderId}")
                val snapToken = viewModel.fetchSnapToken(it)
                Log.d(TAG, "Fetched Snap Token: $snapToken")
                if (!snapToken.isNullOrEmpty() && activity != null) {
                    Log.d(TAG, "Starting payment UI flow")
                    UiKitApi.getDefaultInstance().startPaymentUiFlow(
                        activity,
                        paymentLauncher,
                        snapToken
                    )
                    paymentInitiated = true // Set flag menjadi true setelah pembayaran otomatis dijalankan
                } else {
                    Toast.makeText(context, "Gagal mendapatkan Snap Token", Toast.LENGTH_LONG).show()
                    Log.e(TAG, "Snap Token is null atau kosong atau activity adalah null")
                }
            }
        }
    }

    LaunchedEffect(Unit) {
        try {
            Log.d(TAG, "Initializing UiKitApi")
            UiKitApi.Builder()
                .withMerchantClientKey("SB-Mid-client-bKs9hY7uAp9mKb-h")
                .withContext(context)
                .withMerchantUrl("https://api-mub6zdhcna-uc.a.run.app/")
                .enableLog(true)
                .withColorTheme(CustomColorTheme("#FFE51255", "#B61548", "#FFE51255"))
                .build()
            Log.d(TAG, "UiKitApi initialized dengan merchant URL dan client key")
            setLocaleNew(context, "id")
            Log.d(TAG, "Locale diatur ke Indonesian")
        } catch (e: Exception) {
            Log.e(TAG, "Gagal menginisialisasi UiKitApi: ${e.message}")
            Toast.makeText(context, "Gagal menginisialisasi pembayaran", Toast.LENGTH_LONG).show()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Pembayaran", fontSize = 20.sp, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = {
                        Log.d(TAG, "Back button clicked")
                        navController.popBackStack()
                    }) {
                        Icon(
                            Icons.Filled.ArrowBack,
                            contentDescription = "Kembali",
                            tint = if (isLight) DarkBlue else LightBlue
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = if (isDarkMode) MinimalBackgroundDark else MinimalBackgroundLight,
                    titleContentColor = if (isDarkMode) MinimalTextDark else MinimalTextLight,
                    navigationIconContentColor = if (isDarkMode) DarkText else WhiteColor,
                    actionIconContentColor = if (isDarkMode) DarkText else WhiteColor
                )
            )
        },
        content = { paddingValues ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(smartclassTheme.colors.uiBackground)
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                AnimatedContent(
                    targetState = transactionStatus ?: order,
                    transitionSpec = {
                        fadeIn(animationSpec = tween(300)) with fadeOut(animationSpec = tween(300))
                    }
                ) { state ->
                    when (state) {
                        is TransactionStatusResponse -> {
                            PaymentStatusView(
                                status = state.transactionStatus,
                                orderId = state.orderId,
                                navController = navController,
                                viewModel = viewModel
                            )
                        }
                        is Order -> {
                            PaymentStatusView(
                                status = state.paymentStatus,
                                orderId = state.orderId,
                                navController = navController,
                                viewModel = viewModel
                            )
                        }
                        else -> {
                            LoadingView(message = "Memuat data pesanan...")
                        }
                    }
                }
            }
        }
    )
}

@Composable
fun PaymentStatusView(
    status: String,
    orderId: String,
    navController: NavController,
    viewModel: PaymentsViewModel = viewModel()
) {
    Log.d(TAG, "PaymentStatusView called with status: $status")
    when (status.lowercase()) {
        "settlement", "capture", "lunas" -> SuccessView(orderId = orderId, navController = navController)
        "pending" -> PendingView(orderId = orderId)
        "deny", "expired", "cancel", "failed", "dibatalkan" -> FailureView(
            orderId = orderId,
            navController = navController,
            viewModel = viewModel
        )
        else -> ErrorView(orderId = orderId, status = status, navController = navController)
    }
}

@Composable
fun FailureView(orderId: String, navController: NavController, viewModel: PaymentsViewModel) {
    val context = LocalContext.current
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier
            .padding(16.dp)
    ) {
        Icon(
            imageVector = Icons.Filled.Error,
            contentDescription = "Gagal",
            tint = Color(0xFFF44336), // Merah
            modifier = Modifier.size(80.dp)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "Pembayaran gagal atau dibatalkan untuk Order ID: $orderId",
            color = Color(0xFFF44336),
            fontSize = 18.sp,
            fontWeight = FontWeight.Medium,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(24.dp))
        Row {
            Button(onClick = {
                // Coba lagi pembayaran tanpa generate Snap Token baru
                viewModel.getOrder(orderId) // Pastikan order terbaru diambil
                Toast.makeText(context, "Memulai ulang pembayaran...", Toast.LENGTH_SHORT).show()
            }) {
                Text("Coba Lagi")
            }
            Spacer(modifier = Modifier.width(16.dp))
            Button(onClick = {
                navController.navigate("support")
            }) {
                Text("Bantuan")
            }
        }
    }
}

@Composable
fun SuccessView(orderId: String, navController: NavController) {
    Log.d(TAG, "SuccessView invoked for orderId: $orderId")
    // Navigasi otomatis setelah 2 detik
    LaunchedEffect(Unit) {
        delay(2000L) // Jeda selama 2 detik
        navController.popBackStack()
    }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier
            .padding(16.dp)
    ) {
        Icon(
            imageVector = Icons.Filled.CheckCircle,
            contentDescription = "Berhasil",
            tint = Color(0xFF4CAF50), // Hijau
            modifier = Modifier.size(80.dp)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "Pembayaran berhasil untuk Order ID: $orderId",
            color = Color(0xFF4CAF50),
            fontSize = 18.sp,
            fontWeight = FontWeight.Medium,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(24.dp))
        // Opsional: Tombol navigasi manual jika pengguna ingin kembali lebih cepat
        Button(onClick = {
            navController.popBackStack()
        }) {
            Text("Kembali ke Beranda")
        }
    }
}

@Composable
fun PendingView(orderId: String) {
    Log.d(TAG, "PendingView invoked for orderId: $orderId")
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier
            .padding(16.dp)
    ) {
        Icon(
            imageVector = Icons.Filled.Pending,
            contentDescription = "Pending",
            tint = Color(0xFFFFC107), // Kuning
            modifier = Modifier.size(80.dp)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "Pembayaran sedang diproses untuk Order ID: $orderId",
            color = Color(0xFFFFC107),
            fontSize = 18.sp,
            fontWeight = FontWeight.Medium,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(24.dp))
        Button(onClick = {
            // Implementasikan aksi cek status pembayaran jika diperlukan
            // Misalnya, panggil kembali viewModel untuk memperbarui status
            // Contoh:
            // viewModel.fetchTransactionStatus(orderId)
        }) {
            Text("Cek Status Pembayaran")
        }
    }
}

@Composable
fun ErrorView(orderId: String, status: String, navController: NavController) {
    Log.d(TAG, "ErrorView invoked for orderId: $orderId with status: $status")
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier
            .padding(16.dp)
    ) {
        Icon(
            imageVector = Icons.Filled.Warning,
            contentDescription = "Error",
            tint = Color(0xFFFF9800), // Oranye
            modifier = Modifier.size(80.dp)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "Terjadi kesalahan: $status untuk Order ID: $orderId",
            color = Color(0xFFFF9800),
            fontSize = 18.sp,
            fontWeight = FontWeight.Medium,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(24.dp))
        Button(onClick = {
            // Aksi untuk mengulang atau melaporkan error
            // Misalnya, memicu kembali proses pembayaran atau navigasi
            navController.popBackStack()
        }) {
            Text("Coba Lagi")
        }
    }
}

@Composable
fun LoadingView(message: String) {
    Log.d(TAG, "LoadingView invoked with message: $message")
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        CircularProgressIndicator(
            color = smartclassTheme.colors.textPrimary,
            strokeWidth = 4.dp,
            modifier = Modifier.size(48.dp)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = message,
            color = smartclassTheme.colors.textPrimary,
            fontSize = 16.sp,
            fontWeight = FontWeight.Normal
        )
    }
}

private fun setLocaleNew(context: Context, languageCode: String?) {
    Log.d("setLocaleNew", "Setting locale to: $languageCode")
    val sharedPref = context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
    sharedPref.edit().putString("language", languageCode).apply()
    Log.d("setLocaleNew", "Locale set successfully")
}

// Tambahkan data class untuk membedakan status transaksi dan order
sealed class UiState
data class TransactionStatusResponse(val transactionStatus: String, val orderId: String) : UiState()
data class Order(val orderId: String, val paymentStatus: String) : UiState()
