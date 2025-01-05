package com.callcenter.smartclass.ui.home.pesanan

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.callcenter.smartclass.ui.home.pesanan.dataListOrder.OrderListData
import com.callcenter.smartclass.ui.home.pesanan.dataListOrder.ProductListData
import com.callcenter.smartclass.ui.home.pesanan.fetchList.fetchOrders
import com.callcenter.smartclass.ui.home.pesanan.fetchList.fetchProduct
import com.callcenter.smartclass.ui.theme.DarkBlue
import com.callcenter.smartclass.ui.theme.smartclassTheme
import com.callcenter.smartclass.ui.theme.LightBlue
import com.callcenter.smartclass.ui.theme.MinimalTextDark
import com.callcenter.smartclass.ui.theme.MinimalTextLight
import com.callcenter.smartclass.ui.theme.Neutral8
import com.callcenter.smartclass.ui.theme.WhiteColor
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch
import java.text.NumberFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OrderListScreen() {
    val coroutineScope = rememberCoroutineScope()
    var orders by remember { mutableStateOf<List<Pair<OrderListData, ProductListData?>>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val isDarkMode = isSystemInDarkTheme()
    val isLight = !smartclassTheme.colors.isDark

    val currentUser = FirebaseAuth.getInstance().currentUser
    val uid = currentUser?.uid

    LaunchedEffect(Unit) {
        if (uid != null) {
            coroutineScope.launch {
                try {
                    val fetchedOrders = fetchOrders(uid)
                        .sortedByDescending { it.createdAt }
                    orders = fetchedOrders.map { order ->
                        val product = fetchProduct(order.productId)
                        order to product
                    }
                } catch (e: Exception) {
                    errorMessage = "Gagal memuat pesanan."
                } finally {
                    isLoading = false
                }
            }
        } else {
            errorMessage = "Pengguna tidak terautentikasi."
            isLoading = false
        }
    }

    Scaffold { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(smartclassTheme.colors.uiBackground)
                .padding(paddingValues)
        ) {
            when {
                isLoading -> {
                    LazyColumn(
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        items(3) {
                            ShimmerOrderCard()
                        }
                    }
                }
                errorMessage != null -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.ErrorOutline,
                            contentDescription = "Error",
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(64.dp)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = errorMessage!!,
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodyLarge,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                    }
                }
                orders.isEmpty() -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.Inbox,
                            contentDescription = "Tidak Ada Pesanan",
                            tint = if (isLight) LightBlue else MinimalTextDark,
                            modifier = Modifier.size(64.dp)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "Belum ada pesanan.",
                            style = MaterialTheme.typography.bodyLarge,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                            color = if (isLight) MinimalTextLight else MinimalTextDark
                        )
                    }
                }
                else -> {
                    LazyColumn(
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        items(orders.size) { index ->
                            val (order, product) = orders[index]
                            CustomOrderCard(
                                order = order,
                                product = product,
                                backgroundColor = if (!smartclassTheme.colors.isDark) WhiteColor else Neutral8,
                                contentColor = if (isDarkMode) MinimalTextDark else MinimalTextLight
                            )
                        }
                        item {
                            Spacer(modifier = Modifier.height(32.dp))
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun CustomOrderCard(
    order: OrderListData,
    product: ProductListData?,
    backgroundColor: Color = if (!smartclassTheme.colors.isDark) WhiteColor else Neutral8,
    contentColor: Color = MaterialTheme.colorScheme.onSurface
) {

    val rupiahFormat = remember {
        NumberFormat.getCurrencyInstance(Locale("id", "ID")).apply {
            maximumFractionDigits = 0
            minimumFractionDigits = 0
        }
    }

    val isLight = !smartclassTheme.colors.isDark

    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = backgroundColor,
            contentColor = contentColor
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
        modifier = Modifier
            .fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
        ) {
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(product?.thumbnailUrl)
                    .crossfade(true)
                    .build(),
                contentDescription = "Product Thumbnail",
                placeholder = rememberVectorPainter(image = Icons.Default.Image),
                error = rememberVectorPainter(image = Icons.Default.BrokenImage),
                modifier = Modifier
                    .size(80.dp)
                    .clip(RoundedCornerShape(8.dp))
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = product?.title ?: "Produk Tidak Dikenal",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = if (isLight) DarkBlue else LightBlue
                )
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.ShoppingCart,
                        contentDescription = "Jumlah",
                        tint = if (isLight) DarkBlue else LightBlue,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Jumlah: ${order.quantity}x"
                    )
                }
                StatusIndicator(
                    status = order.shippingStatus,
                    label = "Status"
                )
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.AttachMoney,
                        contentDescription = "Total Harga",
                        tint = if (isLight) DarkBlue else LightBlue,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Total Harga: ${rupiahFormat.format(order.totalPrice)}"
                    )
                }
            }
        }
    }
}

@Composable
fun StatusIndicator(status: String, label: String) {
    val (icon, color) = when (status.lowercase()) {
        "berhasil", "selesai", "completed", "settlement", "paid" -> Pair(Icons.Filled.CheckCircle, Color(0xFF4CAF50))
        "batal", "cancelled", "failed", "invalid" -> Pair(Icons.Filled.Cancel, Color(0xFFF44336))
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
            text = "$label: ${status.replaceFirstChar { it.uppercase() }}",
            style = MaterialTheme.typography.bodySmall,
            color = color
        )
    }
}

@Composable
fun ShimmerOrderCard(modifier: Modifier = Modifier) {
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.LightGray.copy(alpha = 0.6f),
            contentColor = Color.Transparent
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
        modifier = modifier
            .fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
        ) {
            ShimmerEffect(
                modifier = Modifier
                    .size(80.dp)
                    .clip(RoundedCornerShape(8.dp))
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                ShimmerEffect(modifier = Modifier
                    .height(20.dp)
                    .fillMaxWidth(0.6f))
                ShimmerEffect(modifier = Modifier
                    .height(20.dp)
                    .fillMaxWidth(0.4f))
                ShimmerEffect(modifier = Modifier
                    .height(20.dp)
                    .fillMaxWidth(0.5f))
                ShimmerEffect(modifier = Modifier
                    .height(20.dp)
                    .fillMaxWidth(0.3f))
            }
        }
    }
}

@Composable
fun ShimmerEffect(
    modifier: Modifier = Modifier,
    shimmerColor: Color = Color.LightGray.copy(alpha = 0.6f)
) {
    val transition = rememberInfiniteTransition()
    val translateAnim by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1000f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1200, easing = LinearEasing)
        )
    )

    Box(
        modifier = modifier
            .background(
                Brush.linearGradient(
                    colors = listOf(
                        shimmerColor.copy(alpha = 0.6f),
                        shimmerColor.copy(alpha = 0.2f),
                        shimmerColor.copy(alpha = 0.6f)
                    ),
                    start = Offset(translateAnim - 1000f, translateAnim - 1000f),
                    end = Offset(translateAnim, translateAnim)
                )
            )
    )
}
