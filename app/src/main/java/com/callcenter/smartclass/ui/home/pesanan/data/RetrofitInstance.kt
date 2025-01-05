package com.callcenter.smartclass.ui.home.pesanan.data

import android.util.Log
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Response as OkHttpResponse
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

private const val TAG = "RetrofitInstance"

val loggingInterceptor = Interceptor { chain ->
    val request = chain.request()
    Log.d(TAG, "Sending request: ${request.url} on ${chain.connection()}")
    val response: OkHttpResponse = chain.proceed(request)
    Log.d(TAG, "Received response for ${response.request.url} with status ${response.code}")
    response
}

val client = OkHttpClient.Builder()
    .addInterceptor(loggingInterceptor)
    .build()

object RetrofitInstance {
    private val retrofit by lazy {
        Log.d(TAG, "Building Retrofit instance")
        Retrofit.Builder()
            .baseUrl("https://api-mub6zdhcna-uc.a.run.app/")
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    val api: ApiService by lazy {
        Log.d(TAG, "Creating ApiService instance")
        retrofit.create(ApiService::class.java)
    }
}
