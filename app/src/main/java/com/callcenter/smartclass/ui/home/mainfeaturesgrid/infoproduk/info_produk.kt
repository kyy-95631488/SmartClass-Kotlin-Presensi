package com.callcenter.smartclass.ui.home.mainfeaturesgrid.infoproduk

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.selection.TextSelectionColors
import androidx.compose.material.ContentAlpha
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.callcenter.smartclass.ui.home.mainfeaturesgrid.infoproduk.brand.BrandItem
import com.callcenter.smartclass.ui.home.mainfeaturesgrid.infoproduk.produk.CategoryItem
import com.callcenter.smartclass.ui.home.mainfeaturesgrid.infoproduk.produk.ProductItem
import com.callcenter.smartclass.ui.home.mainfeaturesgrid.infoproduk.viewmodel.BrandViewModel
import com.callcenter.smartclass.ui.home.mainfeaturesgrid.infoproduk.viewmodel.ProductViewModel
import com.callcenter.smartclass.ui.theme.*
import androidx.compose.foundation.lazy.grid.items as lazyGridItems
import androidx.compose.foundation.lazy.items as lazyRowItems
import androidx.compose.ui.platform.LocalConfiguration

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun info_produk(
    navController: NavController,
    brandViewModel: BrandViewModel = viewModel(),
    productViewModel: ProductViewModel = viewModel()
) {
    val brands by brandViewModel.brands.collectAsState()
    val products by productViewModel.products.collectAsState()
    val categories by productViewModel.categories.collectAsState()
    val isLoading by productViewModel.isLoading.collectAsState()
    val errorMessage by productViewModel.errorMessage.collectAsState()

    val uiBackground = smartclassTheme.colors.uiBackground

    val brandIdToBrandMap = brands.associateBy { it.uid }

    val isLight = !smartclassTheme.colors.isDark

    var searchQuery by remember { mutableStateOf("") }
    var selectedBrandId by remember { mutableStateOf<String?>(null) }
    var selectedCategory by remember { mutableStateOf<String?>(null) }

    val filteredProducts = products.filter { product ->
        val matchesSearch = product.title.contains(searchQuery, ignoreCase = true)
        val matchesBrand = selectedBrandId?.let { product.brandId == it } ?: true
        val matchesCategory = selectedCategory?.let { product.category == it } ?: true
        matchesSearch && matchesBrand && matchesCategory
    }

    val configuration = LocalConfiguration.current
    val screenWidthDp = configuration.screenWidthDp

    // Menentukan jumlah kolom berdasarkan lebar layar
    val numberOfColumns = when {
        screenWidthDp < 600 -> 2
        screenWidthDp < 840 -> 3
        else -> 4
    }

    val isDarkMode = isSystemInDarkTheme()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Info Produk") },
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
                    containerColor = if (isLight) MinimalBackgroundLight else MinimalBackgroundDark,
                    titleContentColor = if (isLight) MinimalTextLight else MinimalTextDark,
                    navigationIconContentColor = if (isLight) DarkText else WhiteColor,
                    actionIconContentColor = if (isLight) DarkText else WhiteColor
                )
            )
        },
        containerColor = uiBackground
    ) { paddingValues ->
        when {
            isLoading -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                        .background(uiBackground),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = if (isDarkMode) Color(0xFF81D4FA) else Color(0xFF66BB6A))
                }
            }

            errorMessage != null -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                        .background(uiBackground),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = errorMessage ?: "Terjadi kesalahan yang tidak diketahui.",
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            }

            products.isEmpty() -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                        .background(uiBackground),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Tidak ada produk tersedia.",
                        style = MaterialTheme.typography.bodyLarge,
                        color = if (isSystemInDarkTheme()) WhiteColor else DarkText
                    )
                }
            }

            filteredProducts.isEmpty() -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                        .background(uiBackground),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = when {
                            searchQuery.isNotEmpty() && selectedBrandId != null && selectedCategory != null ->
                                "Produk tidak ditemukan untuk \"$searchQuery\" di brand dan kategori yang dipilih."

                            searchQuery.isNotEmpty() && selectedBrandId != null ->
                                "Produk tidak ditemukan untuk \"$searchQuery\" di brand yang dipilih."

                            searchQuery.isNotEmpty() && selectedCategory != null ->
                                "Produk tidak ditemukan untuk \"$searchQuery\" di kategori yang dipilih."

                            selectedBrandId != null && selectedCategory != null ->
                                "Tidak ada produk untuk brand dan kategori yang dipilih."

                            selectedBrandId != null ->
                                "Tidak ada produk untuk brand yang dipilih."

                            selectedCategory != null ->
                                "Tidak ada produk untuk kategori yang dipilih."

                            else ->
                                "Produk tidak ditemukan."
                        },
                        style = MaterialTheme.typography.bodyLarge,
                        color = if (isSystemInDarkTheme()) WhiteColor else DarkText
                    )
                }
            }

            else -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                        .background(uiBackground)
                ) {
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { query -> searchQuery = query },
                        placeholder = {
                            Text(
                                text = "Cari Produk...",
                                style = MaterialTheme.typography.bodyMedium,
                                color = if (isSystemInDarkTheme()) WhiteColor else DarkText.copy(alpha = 0.5f)
                            )
                        },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Search,
                                contentDescription = "Search Icon",
                                tint = if (isSystemInDarkTheme()) WhiteColor else DarkText.copy(alpha = 0.5f)
                            )
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp)
                            .clip(RoundedCornerShape(24.dp))
                            .background(if (isSystemInDarkTheme()) DarkText else WhiteColor),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = if (isSystemInDarkTheme()) WhiteColor else DarkText,
                            unfocusedTextColor = if (isSystemInDarkTheme()) WhiteColor else DarkText,
                            disabledTextColor = if (isSystemInDarkTheme()) WhiteColor else DarkText.copy(alpha = ContentAlpha.disabled),
                            errorTextColor = MaterialTheme.colorScheme.error,
                            cursorColor = if (isSystemInDarkTheme()) WhiteColor else DarkText,
                            selectionColors = TextSelectionColors(
                                handleColor = if (isSystemInDarkTheme()) WhiteColor else DarkText,
                                backgroundColor = if (isSystemInDarkTheme()) WhiteColor else DarkText.copy(alpha = 0.4f)
                            ),
                            focusedBorderColor = Color.Transparent,
                            unfocusedBorderColor = Color.Transparent,
                            disabledBorderColor = Color.Transparent,
                            errorBorderColor = Color.Transparent,
                            focusedLabelColor = if (isSystemInDarkTheme()) WhiteColor else DarkText,
                            unfocusedLabelColor = if (isSystemInDarkTheme()) WhiteColor else DarkText.copy(alpha = 0.5f),
                            disabledLabelColor = if (isSystemInDarkTheme()) WhiteColor else DarkText.copy(alpha = ContentAlpha.disabled),
                            errorLabelColor = MaterialTheme.colorScheme.error,
                            focusedContainerColor = Color.Transparent,
                            unfocusedContainerColor = Color.Transparent,
                            disabledContainerColor = Color.Transparent,
                            errorContainerColor = Color.Transparent
                        )
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Brand Pilihan",
                            style = MaterialTheme.typography.titleMedium,
                            color = if (isSystemInDarkTheme()) WhiteColor else DarkText
                        )
//                        Text(
//                            text = "Lihat Semua",
//                            style = MaterialTheme.typography.bodySmall,
//                            color = if (isLight) DarkBlue else LightBlue,
//                            modifier = Modifier.clickable {
//                                selectedBrandId = null
//                            }
//                        )
                    }

                    LazyRow(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        contentPadding = PaddingValues(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        lazyRowItems(brands) { brand ->
                            BrandItem(
                                brandData = brand,
                                isSelected = brand.uid == selectedBrandId,
                                onClick = {
                                    selectedBrandId = if (selectedBrandId == brand.uid) null else brand.uid
                                }
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "Produk Terbaru",
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                        color = if (isSystemInDarkTheme()) WhiteColor else DarkText
                    )

                    LazyRow(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        contentPadding = PaddingValues(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        lazyRowItems(categories) { category ->
                            CategoryItem(category = category, onClick = {
                                selectedCategory = if (selectedCategory == category) null else category
                            })
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    LazyVerticalGrid(
                        columns = GridCells.Fixed(numberOfColumns),
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        lazyGridItems(filteredProducts) { product ->
                            val brandName = brandIdToBrandMap[product.brandId]?.name ?: "Unknown Brand"
                            ProductItem(productData = product, brandName = brandName, onClick = {
                                navController.navigate("productDetail/${product.id}")
                            })
                        }
                    }
                }
            }
        }
    }
}
