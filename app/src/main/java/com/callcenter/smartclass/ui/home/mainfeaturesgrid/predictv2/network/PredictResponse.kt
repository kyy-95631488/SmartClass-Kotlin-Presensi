package com.callcenter.smartclass.ui.home.mainfeaturesgrid.predictv2.network

data class PredictResponse(
    val nama_anak: String,
    val probabilitas_stunting: String,
    val probabilitas_tidak_stunting: String,
    val usia: Int
)