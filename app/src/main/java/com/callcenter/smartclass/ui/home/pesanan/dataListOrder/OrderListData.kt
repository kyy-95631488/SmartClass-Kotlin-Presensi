package com.callcenter.smartclass.ui.home.pesanan.dataListOrder

import com.google.firebase.Timestamp

data class OrderListData(
    val productId: String = "",
    val quantity: Int = 0,
    val shippingStatus: String = "",
    val totalPrice: Double = 0.0,
    val createdAt: Timestamp = Timestamp.now()
)
