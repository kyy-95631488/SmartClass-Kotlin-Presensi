package com.callcenter.smartclass.ui.home.admin.orderanmasuk.data

data class OrderDetail(
    val order: OrderAdmin,
    val username: String,
    val productTitle: String,
    val thumbnailUrl: String,
    val country: String = "",
    val phoneNumber: String = "",
    val city: String = "",
    val postalCode: String = "",
    val province: String = "",
    val street: String = ""
)