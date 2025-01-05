package com.callcenter.smartclass.ui.home.pesanan.data

import com.google.gson.annotations.SerializedName

data class TransactionStatusResponse(
    @SerializedName("status_code") val statusCode: String,
    @SerializedName("status_message") val statusMessage: String,
    @SerializedName("transaction_id") val transactionId: String,
    @SerializedName("order_id") val orderId: String,
    @SerializedName("payment_type") val paymentType: String,
    @SerializedName("transaction_time") val transactionTime: String,
    @SerializedName("transaction_status") val transactionStatus: String,
    @SerializedName("fraud_status") val fraudStatus: String
)