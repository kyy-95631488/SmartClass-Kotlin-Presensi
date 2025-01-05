package com.callcenter.smartclass.ui.home.admin.tambahproduk.produk.data

import android.net.Uri

fun validateProductInput(
    title: String,
    description: String,
    price: String,
    stock: String,
    category: String,
    selectedBrand: String?,
    thumbnailUri: Uri?
): Boolean {
    if (title.isBlank()) return false
    if (description.isBlank()) return false
    if (price.isBlank()) return false
    if (stock.isBlank()) return false
    if (category.isBlank()) return false
    if (price.toDoubleOrNull() == null) return false
    if (stock.toIntOrNull() == null) return false
    if (selectedBrand == null) return false
    if (thumbnailUri == null) return false
    return true
}
