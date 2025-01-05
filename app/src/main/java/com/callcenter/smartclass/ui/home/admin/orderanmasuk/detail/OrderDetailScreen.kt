package com.callcenter.smartclass.ui.home.admin.orderanmasuk.detail

import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.selection.LocalTextSelectionColors
import androidx.compose.foundation.text.selection.TextSelectionColors
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material.ContentAlpha
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.callcenter.smartclass.ui.home.admin.orderanmasuk.viewmodel.OrderDetailViewModel
import com.callcenter.smartclass.ui.home.admin.orderanmasuk.viewmodel.OrderDetailViewModelFactory
import com.callcenter.smartclass.ui.theme.*
import kotlinx.coroutines.launch
import java.text.NumberFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OrderDetailScreen(orderId: String, navController: NavController) {
    val viewModel: OrderDetailViewModel = viewModel(
        factory = OrderDetailViewModelFactory(orderId)
    )
    val orderDetail by viewModel.orderDetail.collectAsState()

    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()

    var receiptUrl by remember { mutableStateOf("") }
    var shippingReceiptUrl by remember { mutableStateOf("") }
    var shippingStatus by remember { mutableStateOf("") }
    var paymentStatus by remember { mutableStateOf("") }

    var courierType by remember { mutableStateOf("") }

    val isDarkMode = isSystemInDarkTheme()

    val scrollState = rememberScrollState()

    val rupiahFormat = remember {
        NumberFormat.getCurrencyInstance(Locale("id", "ID")).apply {
            maximumFractionDigits = 0
            minimumFractionDigits = 0
        }
    }

    smartclassTheme {
        Scaffold(
            snackbarHost = {
                SnackbarHost(
                    snackbarHostState,
                    snackbar = { data ->
                    Snackbar(
                        snackbarData = data,
                        containerColor = if (isDarkMode) Color.Black else Color.White,
                        contentColor = if (isDarkMode) Color.White else Color.Black,
                        actionColor = if (isDarkMode) Color.Blue else Color.Green
                    )
                })
            },
            topBar = {
                AppTopBar01(navController)
            },
            content = { paddingValues ->
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                        .background(if (!isSystemInDarkTheme()) BgColor else MinimalBackgroundDark)
                        .padding(16.dp)
                ) {
                    Column(modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(scrollState)
                    ) {
                        orderDetail?.let { detail ->
                            LaunchedEffect(detail) {
                                receiptUrl = detail.order.receiptUrl ?: ""
                                shippingReceiptUrl = detail.order.shippingReceiptUrl ?: ""
                                shippingStatus = detail.order.shippingStatus ?: ""
                                paymentStatus = detail.order.paymentStatus ?: ""
                            }

                            Text(
                                text = "Order ID: ${detail.order.orderId}",
                                style = MaterialTheme.typography.titleMedium,
                                color = if (!isSystemInDarkTheme()) MinimalTextLight else MinimalTextDark
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Pelanggan: ${detail.username}",
                                style = MaterialTheme.typography.bodyLarge,
                                color = if (!isSystemInDarkTheme()) MinimalTextLight else MinimalTextDark
                            )

                            // Tampilkan informasi kontak dan alamat
                            Text(
                                text = "No. Telepon: ${detail.phoneNumber}",
                                style = MaterialTheme.typography.bodyLarge,
                                color = if (!isSystemInDarkTheme()) MinimalTextLight else MinimalTextDark
                            )
                            Text(
                                text = "Alamat: ${detail.street}, ${detail.city}, ${detail.province}, ${detail.postalCode}, ${detail.country}",
                                style = MaterialTheme.typography.bodyLarge,
                                color = if (!isSystemInDarkTheme()) MinimalTextLight else MinimalTextDark
                            )

                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = "Produk: ${detail.productTitle}",
                                style = MaterialTheme.typography.bodyLarge,
                                color = if (!isSystemInDarkTheme()) MinimalTextLight else MinimalTextDark
                            )
                            Text(
                                text = "Jumlah: ${detail.order.quantity}",
                                style = MaterialTheme.typography.bodyLarge,
                                color = if (!isSystemInDarkTheme()) MinimalTextLight else MinimalTextDark
                            )
                            Text(
                                text = "Harga: ${rupiahFormat.format(detail.order.totalPrice)}",
                                style = MaterialTheme.typography.bodyLarge,
                                color = if (!isSystemInDarkTheme()) MinimalTextLight else MinimalTextDark
                            )
                            Text(
                                text = "Tanggal: ${detail.order.createdAt.toDate()}",
                                style = MaterialTheme.typography.bodyLarge,
                                color = if (!isSystemInDarkTheme()) MinimalTextLight else MinimalTextDark
                            )

                            Spacer(modifier = Modifier.height(16.dp))

                            if (detail.thumbnailUrl.isNotEmpty()) {
                                AsyncImage(
                                    model = detail.thumbnailUrl,
                                    contentDescription = "Product Image",
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(16.dp))
                                        .fillMaxWidth()
                                        .height(200.dp)
                                        .padding(vertical = 8.dp)
                                )
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            InputField(
                                label = "Receipt URL",
                                value = receiptUrl,
                                onValueChange = { receiptUrl = it },
                                textColor = if (!isSystemInDarkTheme()) MinimalTextLight else MinimalTextDark
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            InputField(
                                label = "Shipping Receipt URL [ No. Resi ]",
                                value = shippingReceiptUrl,
                                onValueChange = { shippingReceiptUrl = it },
                                textColor = if (!isSystemInDarkTheme()) MinimalTextLight else MinimalTextDark
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            var expandedCourier by remember { mutableStateOf(false) }
                            val courierOptions = listOf("JNE", "TIKI", "J&T", "POS Indonesia", "SiCepat", "Ninja Xpress", "Gosend", "GrabExpress")

                            ExposedDropdownMenuBox(
                                expanded = expandedCourier,
                                onExpandedChange = { expandedCourier = !expandedCourier }
                            ) {
                                OutlinedTextField(
                                    value = courierType,
                                    onValueChange = {},
                                    readOnly = true,
                                    label = { Text("Jenis Kurir", color = if (!isSystemInDarkTheme()) MinimalTextLight else MinimalTextDark) },
                                    trailingIcon = {
                                        CompositionLocalProvider(
                                            LocalContentColor provides if (!isSystemInDarkTheme()) MinimalTextLight else MinimalTextDark
                                        ) {
                                            ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedCourier)
                                        }
                                    },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .menuAnchor(),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedContainerColor = if (isSystemInDarkTheme()) MinimalBackgroundDark else MinimalBackgroundLight,
                                        unfocusedContainerColor = if (isSystemInDarkTheme()) MinimalBackgroundDark else MinimalBackgroundLight,
                                        focusedBorderColor = MinimalPrimary,
                                        unfocusedBorderColor = MinimalSecondary,
                                        cursorColor = MinimalPrimary,
                                        focusedTextColor = if (!isSystemInDarkTheme()) MinimalTextLight else MinimalTextDark,
                                        unfocusedTextColor = if (!isSystemInDarkTheme()) MinimalTextLight else MinimalTextDark,
                                        disabledTextColor = if (!isSystemInDarkTheme()) MinimalTextLight.copy(alpha = ContentAlpha.disabled) else MinimalTextDark.copy(alpha = ContentAlpha.disabled)
                                    )
                                )
                                ExposedDropdownMenu(
                                    expanded = expandedCourier,
                                    onDismissRequest = { expandedCourier = false },
                                    modifier = Modifier
                                        .background(if (isSystemInDarkTheme()) MinimalBackgroundDark else MinimalBackgroundLight)
                                ) {
                                    courierOptions.forEach { selectionOption ->
                                        DropdownMenuItem(
                                            text = {
                                                Text(
                                                    text = selectionOption,
                                                    color = if (!isSystemInDarkTheme()) MinimalTextLight else MinimalTextDark
                                                )
                                            },
                                            onClick = {
                                                courierType = selectionOption
                                                expandedCourier = false
                                            },
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .background(
                                                    if (isSystemInDarkTheme()) DarkBlue.copy(alpha = 0.12f) else LightBlue.copy(alpha = 0.12f)
                                                )
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            var expandedShipping by remember { mutableStateOf(false) }
                            val shippingOptions = listOf("Pending", "DiKirim", "Selesai", "Cancelled")

                            ExposedDropdownMenuBox(
                                expanded = expandedShipping,
                                onExpandedChange = { expandedShipping = !expandedShipping }
                            ) {
                                OutlinedTextField(
                                    value = shippingStatus,
                                    onValueChange = {},
                                    readOnly = true,
                                    label = { Text("Shipping Status", color = if (!isSystemInDarkTheme()) MinimalTextLight else MinimalTextDark) },
                                    trailingIcon = {
                                        CompositionLocalProvider(
                                            LocalContentColor provides if (!isSystemInDarkTheme()) MinimalTextLight else MinimalTextDark
                                        ) {
                                            ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedShipping)
                                        }
                                    },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .menuAnchor(),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedContainerColor = if (isSystemInDarkTheme()) MinimalBackgroundDark else MinimalBackgroundLight,
                                        unfocusedContainerColor = if (isSystemInDarkTheme()) MinimalBackgroundDark else MinimalBackgroundLight,
                                        focusedBorderColor = MinimalPrimary,
                                        unfocusedBorderColor = MinimalSecondary,
                                        cursorColor = MinimalPrimary,
                                        focusedTextColor = if (!isSystemInDarkTheme()) MinimalTextLight else MinimalTextDark,
                                        unfocusedTextColor = if (!isSystemInDarkTheme()) MinimalTextLight else MinimalTextDark,
                                        disabledTextColor = if (!isSystemInDarkTheme()) MinimalTextLight.copy(alpha = ContentAlpha.disabled) else MinimalTextDark.copy(alpha = ContentAlpha.disabled)
                                    )
                                )
                                ExposedDropdownMenu(
                                    expanded = expandedShipping,
                                    onDismissRequest = { expandedShipping = false },
                                    modifier = Modifier
                                        .background(if (isSystemInDarkTheme()) MinimalBackgroundDark else MinimalBackgroundLight)
                                ) {
                                    shippingOptions.forEach { selectionOption ->
                                        DropdownMenuItem(
                                            text = {
                                                Text(
                                                    text = selectionOption,
                                                    color = if (!isSystemInDarkTheme()) MinimalTextLight else MinimalTextDark
                                                )
                                            },
                                            onClick = {
                                                shippingStatus = selectionOption
                                                expandedShipping = false
                                            },
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .background(
                                                    if (isSystemInDarkTheme()) DarkBlue.copy(alpha = 0.12f) else LightBlue.copy(alpha = 0.12f)
                                                )
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            var expandedPayment by remember { mutableStateOf(false) }
                            val paymentOptions = listOf("Pending", "Settlement", "Invalid", "Refunded")

                            ExposedDropdownMenuBox(
                                expanded = expandedPayment,
                                onExpandedChange = { expandedPayment = !expandedPayment }
                            ) {
                                OutlinedTextField(
                                    value = paymentStatus,
                                    onValueChange = {},
                                    readOnly = true,
                                    label = { Text("Payment Status", color = if (!isSystemInDarkTheme()) MinimalTextLight else MinimalTextDark) },
                                    trailingIcon = {
                                        CompositionLocalProvider(
                                            LocalContentColor provides if (!isSystemInDarkTheme()) MinimalTextLight else MinimalTextDark
                                        ) {
                                            ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedPayment)
                                        }
                                    },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .menuAnchor(),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedContainerColor = if (isSystemInDarkTheme()) MinimalBackgroundDark else MinimalBackgroundLight,
                                        unfocusedContainerColor = if (isSystemInDarkTheme()) MinimalBackgroundDark else MinimalBackgroundLight,
                                        focusedBorderColor = MinimalPrimary,
                                        unfocusedBorderColor = MinimalSecondary,
                                        cursorColor = MinimalPrimary,
                                        focusedTextColor = if (!isSystemInDarkTheme()) MinimalTextLight else MinimalTextDark,
                                        unfocusedTextColor = if (!isSystemInDarkTheme()) MinimalTextLight else MinimalTextDark,
                                        disabledTextColor = if (!isSystemInDarkTheme()) MinimalTextLight.copy(alpha = ContentAlpha.disabled) else MinimalTextDark.copy(alpha = ContentAlpha.disabled)
                                    )
                                )
                                ExposedDropdownMenu(
                                    expanded = expandedPayment,
                                    onDismissRequest = { expandedPayment = false },
                                    modifier = Modifier
                                        .background(if (isSystemInDarkTheme()) MinimalBackgroundDark else MinimalBackgroundLight)
                                ) {
                                    paymentOptions.forEach { selectionOption ->
                                        DropdownMenuItem(
                                            text = {
                                                Text(
                                                    text = selectionOption,
                                                    color = if (!isSystemInDarkTheme()) MinimalTextLight else MinimalTextDark
                                                )
                                            },
                                            onClick = {
                                                paymentStatus = selectionOption
                                                expandedPayment = false
                                            },
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .background(
                                                    if (isSystemInDarkTheme()) DarkBlue.copy(alpha = 0.12f) else LightBlue.copy(alpha = 0.12f)
                                                )
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            Button(
                                onClick = {
                                    viewModel.updateOrder(
                                        receiptUrl = receiptUrl,
                                        shippingReceiptUrl = shippingReceiptUrl,
                                        shippingStatus = shippingStatus,
                                        paymentStatus = paymentStatus,
                                        courierType = courierType,
                                        onSuccess = {
                                            coroutineScope.launch {
                                                snackbarHostState.showSnackbar("Order berhasil diperbarui")
                                                navController.popBackStack()
                                            }
                                        },
                                        onFailure = { exception ->
                                            coroutineScope.launch {
                                                snackbarHostState.showSnackbar("Gagal memperbarui order: ${exception.message}")
                                            }
                                        }
                                    )
                                },
                                modifier = Modifier.fillMaxWidth(),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (isDarkMode) ButtonDarkColor else ButtonLightColor,
                                    contentColor = if (isDarkMode) WhiteColor else DarkText
                                )
                            ) {
                                Text("Simpan Perubahan")
                            }
                        }
                    } ?: run {
                        CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                    }
                }
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppTopBar01(navController: NavController) {
    TopAppBar(
        title = {
            Text(
                "Orderan Detail",
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
            )
        },
        navigationIcon = {
            IconButton(onClick = { navController.popBackStack() }) {
                Icon(
                    Icons.Filled.ArrowBack,
                    contentDescription = "Kembali",
                    tint = if (!smartclassTheme.colors.isDark) DarkBlue else LightBlue
                )
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = if (!smartclassTheme.colors.isDark) MinimalBackgroundLight else MinimalBackgroundDark,
            titleContentColor = if (!smartclassTheme.colors.isDark) MinimalTextLight else MinimalTextDark,
            navigationIconContentColor = if (!smartclassTheme.colors.isDark) DarkText else WhiteColor
        )
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InputField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    textColor: Color,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    readOnly: Boolean = false
) {

    val isDarkMode = isSystemInDarkTheme()

    val customTextSelectionColors = TextSelectionColors(
        handleColor = if (isDarkMode) MinimalPrimary else MinimalPrimary,
        backgroundColor = if (isDarkMode) MinimalPrimary else MinimalPrimary.copy(alpha = 0.4f)
    )

    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            label,
            style = MaterialTheme.typography.bodySmall,
            color = if (isSystemInDarkTheme()) MinimalTextDark else MinimalTextLight
        )
        Spacer(modifier = Modifier.height(4.dp))

        CompositionLocalProvider(LocalTextSelectionColors provides customTextSelectionColors) {
            OutlinedTextField(
                value = value,
                onValueChange = { if (!readOnly) onValueChange(it) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                textStyle = MaterialTheme.typography.bodySmall.copy(color = textColor),
                keyboardOptions = keyboardOptions,
                enabled = !readOnly,
                readOnly = readOnly,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = textColor,
                    unfocusedTextColor = textColor,
                    cursorColor = MinimalPrimary,
                    focusedBorderColor = MinimalPrimary,
                    unfocusedBorderColor = MinimalSecondary,
                    focusedContainerColor = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent
                )
            )
        }
    }
}
