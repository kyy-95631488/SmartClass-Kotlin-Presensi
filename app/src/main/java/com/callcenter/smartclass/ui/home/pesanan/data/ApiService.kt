package com.callcenter.smartclass.ui.home.pesanan.data

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface ApiService {
    @POST("generate-snap-token")
    suspend fun generateSnapToken(@Body order: SnapTokenRequest): Response<SnapTokenResponse>

    @GET("get-transaction-status/{orderId}")
    suspend fun getTransactionStatus(
        @Path("orderId") orderId: String,
        @Query("transaction_id") transactionId: String? = null
    ): Response<TransactionStatusResponse>
}

data class SnapTokenResponse(
    val snapToken: String
)