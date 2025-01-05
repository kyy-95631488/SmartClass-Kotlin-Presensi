package com.callcenter.smartclass.ui.home.pesanan.data

import com.google.firebase.Timestamp

data class Order(
    val orderId: String = "",
    val userId: String = "",
    val productId: String = "",
    val quantity: Int = 0,
    val totalPrice: Double = 0.0,
    val paymentStatus: String = "Pending",
    val shippingStatus: String = "Pending",
    val receiptUrl: String? = null,
    val shippingReceiptUrl: String? = null,
    val createdAt: Timestamp = Timestamp.now(),
    var snapToken: String? = null,
    val courierType: String = ""
)