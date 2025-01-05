package com.callcenter.smartclass.ui.home.mainfeaturesgrid.infoproduk.data

data class Product_Data(
    val id: String = "",
    val title: String = "",
    val thumbnailUrl: String = "",
    val imageUrls: List<String> = emptyList(),
    val createdAt: Long = 0L,
    val stock: Int = 0,
    val price: Double = 0.0,
    val description: String = "",
    val brandId: String = "",
    val category: String = ""
)