package com.callcenter.smartclass.ui.home.mainfeaturesgrid.infoproduk.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.callcenter.smartclass.ui.home.mainfeaturesgrid.infoproduk.data.Product_Data
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class ProductViewModel : ViewModel() {
    private val _products = MutableStateFlow<List<Product_Data>>(emptyList())
    val products: StateFlow<List<Product_Data>> = _products

    private val _categories = MutableStateFlow<List<String>>(emptyList())
    val categories: StateFlow<List<String>> = _categories

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage

    private val firestore = FirebaseFirestore.getInstance()

    init {
        fetchProducts()
    }

    private fun fetchProducts() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val querySnapshot = firestore.collection("products").get().await()
                val productDataList = querySnapshot.documents.mapNotNull { document ->
                    document.toObject(Product_Data::class.java)?.copy(id = document.id)
                }
                _products.value = productDataList

                val categoryList = productDataList.map { it.category }.distinct()
                _categories.value = categoryList

                _isLoading.value = false
            } catch (e: Exception) {
                _errorMessage.value = "Gagal memuat produk: ${e.message}"
                _isLoading.value = false
            }
        }
    }
}
