package com.callcenter.smartclass.ui.home.mainfeaturesgrid.predictv2.network

import com.callcenter.smartclass.ui.home.mainfeaturesgrid.predictv2.data.Child

data class ChildPrediction(
    val child: Child,
    val prediction: PredictResponse?
)