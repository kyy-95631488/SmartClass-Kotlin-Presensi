package com.callcenter.smartclass.ui.home.mainfeaturesgrid.predictv2.network

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface ApiService {
    @POST("predict")
    suspend fun getPrediction(@Body request: PredictRequest): Response<PredictResponse>
}