package com.callcenter.smartclass.ui.funcauth.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.callcenter.smartclass.ui.funcauth.FunLoginEmail

class FunLoginEmailFactory(private val context: Context) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(FunLoginEmail::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return FunLoginEmail() as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}