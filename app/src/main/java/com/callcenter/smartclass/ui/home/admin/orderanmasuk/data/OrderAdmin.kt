package com.callcenter.smartclass.ui.home.admin.orderanmasuk.data

import com.google.firebase.Timestamp

data class OrderAdmin(
    val orderId: String = "",
    val userId: String = "",
    val productId: String = "",
    val username: String = "",
    val productTitle: String = "",
    val thumbnailUrl: String = "",
    val createdAt: Timestamp = Timestamp.now(),
    val paymentStatus: String = "",
    val shippingStatus: String = "",
    val receiptUrl: String? = "",
    val shippingReceiptUrl: String? = "",
    val quantity: Int = 0,
    val totalPrice: Double = 0.0,
    val courierType: String = ""
)