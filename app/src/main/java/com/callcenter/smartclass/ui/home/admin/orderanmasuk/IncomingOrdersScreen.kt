package com.callcenter.smartclass.ui.home.admin.orderanmasuk

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import com.callcenter.smartclass.ui.home.admin.orderanmasuk.data.OrderAdmin
import com.callcenter.smartclass.ui.home.admin.orderanmasuk.viewmodel.IncomingOrdersViewModel
import com.callcenter.smartclass.ui.theme.*
import java.text.SimpleDateFormat
import java.util.Locale

@Composable
fun IncomingOrdersScreen(
    navController: NavController,
    viewModel: IncomingOrdersViewModel = viewModel()
) {
    val orders by viewModel.orders.collectAsState()
    val allUsernames by viewModel.usernames.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState() // Tambahkan ini

    var isFilterDialogOpen by remember { mutableStateOf(false) }
    var selectedUsername by remember { mutableStateOf<String?>(null) }

    val filteredOrders = if (selectedUsername != null) {
        orders.filter { it.username == selectedUsername }
    } else {
        orders
    }

    smartclassTheme {
        Scaffold(
            topBar = {
                AppTopBar(navController)
            },
            floatingActionButton = {
                FloatingActionButton(
                    onClick = { isFilterDialogOpen = true },
                    containerColor = if (!smartclassTheme.colors.isDark) DarkBlue else LightBlue,
                    contentColor = Color.White,
                    modifier = Modifier
                        .padding(16.dp)
                ) {
                    Icon(Icons.Filled.FilterList, contentDescription = "Filter Orders")
                }
            },
            content = { paddingValues ->
                OrderContent(
                    orders = filteredOrders,
                    navController = navController,
                    paddingValues = paddingValues,
                    selectedUsername = selectedUsername,
                    onClearFilter = { selectedUsername = null },
                    isLoading = isLoading // Tambahkan ini
                )
            }
        )

        if (isFilterDialogOpen) {
            FilterDialog(
                usernames = allUsernames,
                onDismiss = { isFilterDialogOpen = false },
                onSelect = { username ->
                    selectedUsername = username
                    isFilterDialogOpen = false
                },
                onClearFilter = {
                    selectedUsername = null
                    isFilterDialogOpen = false
                }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppTopBar(navController: NavController) {
    TopAppBar(
        title = {
            Text(
                "Orderan Masuk",
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
fun OrderContent(
    orders: List<OrderAdmin>,
    navController: NavController,
    paddingValues: PaddingValues,
    selectedUsername: String?,
    onClearFilter: () -> Unit,
    isLoading: Boolean
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues)
            .background(if (!smartclassTheme.colors.isDark) BgColor else MinimalBackgroundDark)
            .padding(16.dp)
    ) {
        Column {
            if (selectedUsername != null) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color.LightGray.copy(alpha = 0.2f))
                        .padding(8.dp)
                ) {
                    Text(
                        text = "Filter: $selectedUsername",
                        style = MaterialTheme.typography.bodyMedium,
                        color = if (!smartclassTheme.colors.isDark) DarkText else WhiteColor
                    )
                    Spacer(modifier = Modifier.weight(1f))
                    IconButton(onClick = onClearFilter) {
                        Icon(
                            imageVector = Icons.Filled.Close,
                            contentDescription = "Hapus Filter",
                            tint = Color.Red
                        )
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
            }

            if (isLoading) {
                ShimmerEffect(modifier = Modifier.fillMaxSize())
            } else {
                if (orders.isEmpty()) {
                    EmptyOrdersView()
                } else {
                    OrdersList(orders = orders, navController = navController)
                }
            }
        }
    }
}

@Composable
fun EmptyOrdersView() {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = Icons.Filled.Inbox,
            contentDescription = "No Orders",
            tint = Color.Gray,
            modifier = Modifier.size(120.dp)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "Tidak ada orderan masuk.",
            color = if (!smartclassTheme.colors.isDark) MinimalTextLight else MinimalTextDark,
            style = MaterialTheme.typography.titleMedium
        )
    }
}

@Composable
fun FilterDialog(
    usernames: List<String>,
    onDismiss: () -> Unit,
    onSelect: (String) -> Unit,
    onClearFilter: () -> Unit
) {

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(text = "Filter Berdasarkan Username")
        },
        text = {
            if (usernames.isNotEmpty()) {
                Column {
                    usernames.forEach { username ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onSelect(username) }
                                .padding(vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Person,
                                contentDescription = null,
                                tint = if (!smartclassTheme.colors.isDark) DarkBlue else LightBlue,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = username,
                                style = MaterialTheme.typography.bodyMedium,
                                color = if (!smartclassTheme.colors.isDark) DarkText else WhiteColor
                            )
                        }
                    }
                }
            } else {
                Text(
                    text = "Tidak ada username untuk difilter.",
                    color = if (!smartclassTheme.colors.isDark) MinimalTextDark else MinimalTextDark
                )
            }
        },
        confirmButton = {
            TextButton(onClick = onClearFilter) {
                Text(
                    "Hapus Filter",
                    color = Color.Red
                )
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(
                    "Batal",
                    color = if (!smartclassTheme.colors.isDark) DarkBlue else LightBlue
                )
            }
        },
        containerColor = if (isSystemInDarkTheme()) Neutral4.copy(alpha = 0.85f) else Neutral4.copy(alpha = 0.85f),
        titleContentColor = if (isSystemInDarkTheme()) WhiteColor else WhiteColor,
        textContentColor = if (isSystemInDarkTheme()) WhiteColor else WhiteColor
    )
}

@Composable
fun OrdersList(orders: List<OrderAdmin>, navController: NavController) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        if (orders.isEmpty()) {
            items(5) {
                ShimmerOrderCard()
            }
        } else {
            items(orders) { order ->
                OrderCard(order = order, onClick = {
                    navController.navigate("order_detail/${order.orderId}")
                })
            }
        }
    }
}

@Composable
fun ShimmerOrderCard() {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
            .clip(RoundedCornerShape(12.dp)),
        colors = CardDefaults.cardColors(
            containerColor = Color.Transparent
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            ShimmerEffect(
                modifier = Modifier
                    .size(80.dp)
                    .clip(RoundedCornerShape(12.dp))
            )

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.fillMaxWidth()) {
                ShimmerEffect(
                    modifier = Modifier
                        .height(12.dp)
                        .fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                ShimmerEffect(
                    modifier = Modifier
                        .height(12.dp)
                        .fillMaxWidth(0.6f)
                )
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

@Composable
fun OrderCard(order: OrderAdmin, onClick: () -> Unit) {
    val formattedDate = remember(order.createdAt) {
        val sdf = SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault())
        sdf.format(order.createdAt.toDate())
    }

    val interactionSource = remember { MutableInteractionSource() }
    val rippleColor = if (!smartclassTheme.colors.isDark) Color.LightGray else Gray

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(
                interactionSource = interactionSource,
                indication = ripple(color = rippleColor, bounded = true),
                onClick = { onClick() }
            ),
        colors = CardDefaults.cardColors(
            containerColor = if (!smartclassTheme.colors.isDark) WhiteColor else Neutral8
        ),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.width(80.dp)
            ) {
                ProductImage(thumbnailUrl = order.thumbnailUrl)
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = formattedDate,
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
            Spacer(modifier = Modifier.width(16.dp))

            OrderDetails(order = order)
        }
    }
}

@Composable
fun ProductImage(thumbnailUrl: String) {
    Image(
        painter = rememberAsyncImagePainter(thumbnailUrl),
        contentDescription = "Thumbnail Produk",
        modifier = Modifier
            .size(80.dp)
            .clip(RoundedCornerShape(12.dp)),
        contentScale = ContentScale.Crop
    )
}

@Composable
fun OrderDetails(order: OrderAdmin) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
    ) {
        Text(
            text = "Order ID: ${order.orderId}",
            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
            color = if (!smartclassTheme.colors.isDark) DarkText else WhiteColor
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = "Pelanggan: ${order.username}",
            style = MaterialTheme.typography.bodySmall,
            color = if (!smartclassTheme.colors.isDark) MinimalTextLight else MinimalTextDark
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = "Produk: ${order.productTitle}",
            style = MaterialTheme.typography.bodySmall,
            color = if (!smartclassTheme.colors.isDark) MinimalTextLight else MinimalTextDark
        )

        Spacer(modifier = Modifier.height(4.dp))
        val receiptText = order.shippingReceiptUrl?.takeIf { it.isNotBlank() } ?: "-"
        Text(
            text = "No Resi: $receiptText",
            style = MaterialTheme.typography.bodySmall,
            color = if (!smartclassTheme.colors.isDark) DarkText else WhiteColor
        )

        Spacer(modifier = Modifier.height(8.dp))
        StatusIndicators(paymentStatus = order.paymentStatus, shippingStatus = order.shippingStatus)
    }
}

@Composable
fun StatusIndicators(paymentStatus: String, shippingStatus: String) {
    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        StatusIndicator(status = paymentStatus, label = "Pembayaran")
        StatusIndicator(status = shippingStatus, label = "Pengiriman")
    }
}

@Composable
fun StatusIndicator(status: String, label: String) {
    val (icon, color) = when (status.lowercase()) {
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
            text = "$label: ${status.replaceFirstChar { it.uppercase() }}",
            style = MaterialTheme.typography.bodySmall,
            color = color
        )
    }
}
