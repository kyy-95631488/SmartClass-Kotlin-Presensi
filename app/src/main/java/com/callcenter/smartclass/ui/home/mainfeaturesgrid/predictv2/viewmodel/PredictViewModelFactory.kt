package com.callcenter.smartclass.ui.home.mainfeaturesgrid.predictv2.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.callcenter.smartclass.ui.home.mainfeaturesgrid.predictv2.data.ChildDao
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class PredictViewModelFactory(
    private val childDao: ChildDao,
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance(),
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(PredictViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return PredictViewModel(firestore, auth, childDao) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}