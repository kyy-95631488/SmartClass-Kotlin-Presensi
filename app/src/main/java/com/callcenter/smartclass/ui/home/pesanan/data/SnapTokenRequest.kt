package com.callcenter.smartclass.ui.home.pesanan.data

data class SnapTokenRequest(
    val order_id: String,
    val gross_amount: Double,
    val item_details: List<ItemDetail>,
    val customer_details: CustomerDetails
)