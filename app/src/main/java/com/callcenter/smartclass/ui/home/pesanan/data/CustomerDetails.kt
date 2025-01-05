package com.callcenter.smartclass.ui.home.pesanan.data

data class CustomerDetails(
    val first_name: String,
    val last_name: String,
    val email: String,
    val phone_number: String? = null,
    val address: String? = null
)