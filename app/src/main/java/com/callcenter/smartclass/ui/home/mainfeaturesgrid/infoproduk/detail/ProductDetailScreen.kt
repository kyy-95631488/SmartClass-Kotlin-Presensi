package com.callcenter.smartclass.ui.home.mainfeaturesgrid.infoproduk.detail

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.rememberImagePainter
import com.callcenter.smartclass.ui.home.mainfeaturesgrid.infoproduk.data.Address
import com.callcenter.smartclass.ui.home.mainfeaturesgrid.infoproduk.data.Product_Data
import com.callcenter.smartclass.ui.home.mainfeaturesgrid.infoproduk.viewmodel.ProductDetailViewModel
import com.callcenter.smartclass.ui.theme.*
import kotlinx.coroutines.launch
import java.text.NumberFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductDetailScreen(
    productId: String,
    navController: NavController,
    productDetailViewModel: ProductDetailViewModel = viewModel()
) {
    // Gunakan remember untuk menyimpan aliran data produk
    val productFlow = remember(productId) { productDetailViewModel.getProductById(productId) }
    val product by productFlow.collectAsState(initial = null)
    val isLoading by productDetailViewModel.isLoading.collectAsState()
    val errorMessage by productDetailViewModel.errorMessage.collectAsState()

    val address by productDetailViewModel.address.collectAsState()
    val isAddressLoading by productDetailViewModel.isAddressLoading.collectAsState()

    val currentProduct = product

    val isDarkMode = isSystemInDarkTheme()
    val isLight = !smartclassTheme.colors.isDark

    val rupiahFormat = remember {
        NumberFormat.getCurrencyInstance(Locale("id", "ID")).apply {
            maximumFractionDigits = 0
            minimumFractionDigits = 0
        }
    }

    val coroutineScope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    val relatedProducts by productDetailViewModel.relatedProducts.collectAsState()

    LaunchedEffect(currentProduct) {
        currentProduct?.brandId?.let { brandId ->
            productDetailViewModel.fetchRelatedProducts(brandId)
        }
    }

    LaunchedEffect(Unit) {
        productDetailViewModel.fetchAddress()
    }

    fun isAddressIncomplete(address: Address?): Boolean {
        return address == null ||
                address.street.isBlank() ||
                address.city.isBlank() ||
                address.province.isBlank() ||
                address.postalCode.isBlank()
    }

    var quantity by rememberSaveable { mutableStateOf(1) }
    val isQuantityValid = quantity in 1..(currentProduct?.stock ?: 1)

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = currentProduct?.title ?: "Detail Produk",
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            Icons.Filled.ArrowBack,
                            contentDescription = "Kembali",
                            tint = if (!isSystemInDarkTheme()) DarkBlue else LightBlue
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = if (!isSystemInDarkTheme()) MinimalBackgroundLight else MinimalBackgroundDark,
                    titleContentColor = if (!isSystemInDarkTheme()) MinimalTextLight else MinimalTextDark,
                    navigationIconContentColor = if (!isSystemInDarkTheme()) DarkText else WhiteColor,
                )
            )
        },
        snackbarHost = {
            SnackbarHost(hostState = snackbarHostState) { data ->
                val snackbarType = data.visuals?.message?.let {
                    if (it.contains("berhasil", ignoreCase = true)) "success" else "error"
                } ?: "default"

                val backgroundColor = when (snackbarType) {
                    "success" -> SnackbarSuccessBackground
                    "error" -> SnackbarErrorBackground
                    else -> MaterialTheme.colorScheme.surfaceVariant
                }

                Snackbar(
                    snackbarData = data,
                    containerColor = backgroundColor,
                    contentColor = SnackbarContentColor
                )
            }
        },
        containerColor = smartclassTheme.colors.uiBackground
    ) { paddingValues ->
        when {
            isLoading -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = if (isDarkMode) Color(0xFF81D4FA) else Color(0xFF66BB6A))
                }
            }

            errorMessage != null -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = errorMessage ?: "Terjadi kesalahan yang tidak diketahui.",
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            }

            currentProduct == null -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Produk tidak ditemukan.",
                        style = MaterialTheme.typography.bodyLarge,
                        color = Color.Red
                    )
                }
            }

            else -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                        .verticalScroll(rememberScrollState())
                ) {
                    // Kartu Detail Produk
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .wrapContentHeight()
                            .padding(16.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (!smartclassTheme.colors.isDark) WhiteColor else Neutral8
                        )
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp)
                        ) {
                            if (currentProduct.imageUrls.isNotEmpty()) {
                                if (currentProduct.imageUrls.size == 1) {
                                    Image(
                                        painter = rememberImagePainter(data = currentProduct.imageUrls[0]),
                                        contentDescription = currentProduct.title,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(250.dp)
                                            .clip(RoundedCornerShape(12.dp)),
                                        contentScale = ContentScale.Crop
                                    )
                                } else {
                                    LazyRow(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(250.dp),
                                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                                    ) {
                                        items(currentProduct.imageUrls) { imageUrl ->
                                            Image(
                                                painter = rememberImagePainter(data = imageUrl),
                                                contentDescription = currentProduct.title,
                                                modifier = Modifier
                                                    .width(250.dp)
                                                    .height(250.dp)
                                                    .clip(RoundedCornerShape(12.dp)),
                                                contentScale = ContentScale.Crop
                                            )
                                        }
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(20.dp))

                            Text(
                                text = currentProduct.title,
                                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis
                            )

                            Spacer(modifier = Modifier.height(10.dp))

                            Text(
                                text = "Harga: ${rupiahFormat.format(currentProduct.price)}",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = if (isLight) DarkBlue else LightBlue
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            Text(
                                text = "Stok: ${currentProduct.stock}",
                                style = MaterialTheme.typography.bodyLarge,
                                color = if (currentProduct.stock > 0) Color(0xFF5EFF65) else Color(0xFFF44336)
                            )

                            Spacer(modifier = Modifier.height(20.dp))

                            Divider(
                                color = if (isDarkMode) Ocean4 else Ocean7,
                                thickness = 1.dp
                            )

                            Spacer(modifier = Modifier.height(20.dp))

                            Text(
                                text = "Deskripsi",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            Text(
                                text = currentProduct.description,
                                style = MaterialTheme.typography.bodyMedium
                            )

                            Spacer(modifier = Modifier.height(24.dp))

                            if (isAddressLoading) {
                                CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally), color = if (isDarkMode) Color(0xFF81D4FA) else Color(0xFF66BB6A))
                            } else if (isAddressIncomplete(address)) {
                                AddressForm(
                                    existingAddress = address,
                                    onAddressSubmit = { newAddress ->
                                        productDetailViewModel.saveAddress(newAddress) { success ->
                                            if (success) {
                                                coroutineScope.launch {
                                                    snackbarHostState.showSnackbar(
                                                        message = "Alamat berhasil disimpan.",
                                                        duration = SnackbarDuration.Short
                                                    )
                                                }
                                            } else {
                                                coroutineScope.launch {
                                                    snackbarHostState.showSnackbar(
                                                        message = "Gagal menyimpan alamat.",
                                                        duration = SnackbarDuration.Short
                                                    )
                                                }
                                            }
                                        }
                                    },
                                    snackbarHostState = snackbarHostState
                                )
                            }

                            Spacer(modifier = Modifier.height(24.dp))

                            // Row untuk Stepper dan Tombol Checkout
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(16.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                // Komponen Stepper
                                QuantityStepper(
                                    quantity = quantity,
                                    onQuantityChange = { newQty ->
                                        quantity = newQty
                                    },
                                    maxQuantity = currentProduct.stock
                                )

                                // Tombol Checkout dengan ikon
                                Button(
                                    onClick = {
                                        if (!isAddressIncomplete(address)) {
                                            if (isQuantityValid) {
                                                currentProduct?.let { product ->
                                                    productDetailViewModel.purchaseProduct(
                                                        product.id,
                                                        quantity
                                                    ) { success, message ->
                                                        coroutineScope.launch {
                                                            if (success) {
                                                                snackbarHostState.showSnackbar(
                                                                    message = "Berhasil membeli $quantity ${product.title}.",
                                                                    duration = SnackbarDuration.Short
                                                                )
                                                            } else {
                                                                snackbarHostState.showSnackbar(
                                                                    message = message
                                                                        ?: "Gagal membeli produk.",
                                                                    duration = SnackbarDuration.Short
                                                                )
                                                            }
                                                        }
                                                    }
                                                }
                                            } else {
                                                coroutineScope.launch {
                                                    snackbarHostState.showSnackbar(
                                                        message = "Jumlah pembelian tidak valid.",
                                                        duration = SnackbarDuration.Short
                                                    )
                                                }
                                            }
                                        } else {
                                            coroutineScope.launch {
                                                snackbarHostState.showSnackbar(
                                                    message = "Silakan isi alamat terlebih dahulu.",
                                                    duration = SnackbarDuration.Short
                                                )
                                            }
                                        }
                                    },
                                    enabled = isQuantityValid,
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = if (isDarkMode) Ocean4 else Ocean7,
                                        contentColor = Color.White
                                    ),
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(56.dp),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.ShoppingCart,
                                        contentDescription = "Checkout",
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(text = "Checkout", style = MaterialTheme.typography.bodyMedium)
                                }
                            }

                            // Tampilkan pesan error jika jumlah tidak valid
                            if (!isQuantityValid) {
                                Text(
                                    text = "Jumlah harus antara 1 dan ${currentProduct.stock}",
                                    color = MaterialTheme.colorScheme.error,
                                    style = MaterialTheme.typography.bodySmall,
                                    modifier = Modifier.padding(start = 16.dp, top = 4.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Bagian Produk Terkait di luar Card
                    if (relatedProducts.isNotEmpty()) {
                        Text(
                            text = "Produk Terkait",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            modifier = Modifier.padding(horizontal = 16.dp)
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        LazyRow(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp),
                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            items(relatedProducts) { product ->
                                RelatedProductItem(
                                    product = product,
                                    onProductClick = {
                                        navController.navigate("productDetail/${product.id}")
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun RelatedProductItem(
    product: Product_Data,
    onProductClick: (Product_Data) -> Unit
) {
    Card(
        modifier = Modifier
            .width(150.dp)
            .clickable { onProductClick(product) },
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (!smartclassTheme.colors.isDark) WhiteColor else Neutral8
        )
    ) {
        Column(
            modifier = Modifier.padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Image(
                painter = rememberImagePainter(data = product.thumbnailUrl),
                contentDescription = product.title,
                modifier = Modifier
                    .size(100.dp)
                    .clip(RoundedCornerShape(8.dp)),
                contentScale = ContentScale.Crop
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = product.title,
                style = MaterialTheme.typography.bodyMedium,
                maxLines = 999999999,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(4.dp))

            val rupiahFormat = NumberFormat.getCurrencyInstance(Locale("id", "ID")).apply {
                maximumFractionDigits = 0
                minimumFractionDigits = 0
            }

            Text(
                text = rupiahFormat.format(product.price),
                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                color = if (!isSystemInDarkTheme()) DarkBlue else LightBlue
            )
        }
    }
    Spacer(modifier = Modifier.height(8.dp))
}
