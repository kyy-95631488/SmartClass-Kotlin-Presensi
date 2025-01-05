package com.callcenter.smartclass.ui.home.pesanan

import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.HelpOutline
import androidx.compose.material.icons.filled.HourglassEmpty
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material.icons.filled.Payment
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.callcenter.smartclass.ui.home.pesanan.data.Order
import com.callcenter.smartclass.ui.home.pesanan.viewmodel.OrderViewModel
import com.callcenter.smartclass.ui.theme.smartclassTheme
import com.callcenter.smartclass.ui.theme.MinimalTextDark
import com.callcenter.smartclass.ui.theme.MinimalTextLight
import com.callcenter.smartclass.ui.theme.Neutral8
import com.callcenter.smartclass.ui.theme.Ocean8
import com.callcenter.smartclass.ui.theme.WhiteColor
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OrderStatusScreen(
    orderViewModel: OrderViewModel = viewModel(),
    navController: NavController
) {
    val orders by orderViewModel.orders.collectAsState()
    val isLoading by orderViewModel.isLoading.collectAsState()
    val errorMessage by orderViewModel.errorMessage.collectAsState()

    val isLight = !smartclassTheme.colors.isDark

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(smartclassTheme.colors.uiBackground)
    ) {
        when {
            isLoading -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }

            errorMessage != null -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = errorMessage ?: "Terjadi kesalahan yang tidak diketahui.",
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            }

            orders.isEmpty() -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Anda belum memiliki pesanan.",
                        style = MaterialTheme.typography.bodyLarge,
                        color = if (isLight) MinimalTextLight else MinimalTextDark
                    )
                }
            }

            else -> {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(8.dp),
                    contentPadding = PaddingValues(bottom = 50.dp)
                ) {
                    items(orders) { order ->
                        OrderItem(order = order, navController = navController)
                        Divider(
                            color = if (smartclassTheme.colors.isDark) Color.Gray else Color.LightGray,
                            thickness = 1.dp
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun OrderItem(order: Order, navController: NavController) {
    val rupiahFormat = remember {
        NumberFormat.getCurrencyInstance(Locale("id", "ID")).apply {
            maximumFractionDigits = 0
            minimumFractionDigits = 0
        }
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .shadow(4.dp, shape = MaterialTheme.shapes.medium),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(containerColor = if (!smartclassTheme.colors.isDark) WhiteColor else Neutral8)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    imageVector = Icons.Default.ShoppingCart,
                    contentDescription = "Order Icon",
                    tint = smartclassTheme.colors.textPrimary,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Order ID: ${order.orderId}",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = smartclassTheme.colors.textPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Tanggal dan Resi
            Column(modifier = Modifier.fillMaxWidth()) {
                order.createdAt.let { timestamp ->
                    val date = remember(timestamp) { timestamp.toDate() }
                    val formattedDate = remember(date) {
                        SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault()).format(date)
                    }
                    Text(
                        text = "Dibuat Pada: $formattedDate",
                        style = MaterialTheme.typography.bodySmall,
                        color = smartclassTheme.colors.textSecondary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Resi: ${order.receiptUrl ?: "-"}",
                    style = MaterialTheme.typography.bodySmall,
                    color = smartclassTheme.colors.textSecondary,
                    maxLines = 999,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Resi Pengiriman: ${order.shippingReceiptUrl ?: "-"}",
                    style = MaterialTheme.typography.bodySmall,
                    color = smartclassTheme.colors.textSecondary,
                    maxLines = 999,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Kurir: ${order.courierType ?: "-"}",
                    style = MaterialTheme.typography.bodySmall,
                    color = smartclassTheme.colors.textSecondary,
                    maxLines = 999,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Column(modifier = Modifier.fillMaxWidth()) {
                DetailRow(label = "Produk ID", value = order.productId)
                DetailRow(label = "Jumlah", value = order.quantity.toString())
                DetailRow(label = "Total Harga", value = "${rupiahFormat.format(order.totalPrice)}")
            }

            Spacer(modifier = Modifier.height(8.dp))

            Column(modifier = Modifier.fillMaxWidth()) {
                StatusIndicator01(status = order.paymentStatus, label = "Status Pembayaran")
                Spacer(modifier = Modifier.height(8.dp))
                StatusIndicator01(status = order.shippingStatus, label = "Status Pengiriman")
            }

            if (order.paymentStatus.lowercase(Locale.getDefault()) in listOf("pending", "menunggu", "pending payment")) {
                Spacer(modifier = Modifier.height(12.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        modifier = Modifier
                            .clickable(
                                onClick = { navController.navigate("paymentsMidtrans/${order.orderId}") },
                                interactionSource = remember { MutableInteractionSource() },
                                indication = LocalIndication.current
                            )
                            .background(color = Ocean8, shape = MaterialTheme.shapes.small)
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Payment,
                            contentDescription = "Bayar",
                            tint = Color.White,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Bayar",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.White
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun DetailRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = "$label:",
            style = MaterialTheme.typography.bodyMedium,
            color = smartclassTheme.colors.textPrimary
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            color = smartclassTheme.colors.textPrimary,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
fun StatusIndicator01(status: String, label: String) {
    val (icon, color) = when (status.lowercase(Locale.getDefault())) {
        "berhasil", "selesai", "completed", "settlement", "paid" -> Pair(Icons.Filled.CheckCircle, Color(0xFF4CAF50))
        "batal", "dibatalkan", "cancelled", "failed", "invalid" -> Pair(Icons.Filled.Cancel, Color(0xFFF44336))
        "refunded", "dikembalikan" -> Pair(Icons.Filled.Refresh, Color(0xFF00BCD4))
        "pending", "menunggu", "pending payment" -> Pair(Icons.Filled.HourglassEmpty, Color(0xFFFFC107))
        "sedang proses kirim", "dikirim", "sending", "in transit" -> Pair(Icons.Filled.LocalShipping, Color(0xFF2196F3))
        else -> Pair(Icons.Filled.HelpOutline, Color(0xFF9E9E9E))
    }

    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
            imageVector = icon,
            contentDescription = "$label Status",
            tint = color,
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text = "$label: ${status.replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() }}",
            style = MaterialTheme.typography.bodySmall,
            color = color
        )
    }
}
