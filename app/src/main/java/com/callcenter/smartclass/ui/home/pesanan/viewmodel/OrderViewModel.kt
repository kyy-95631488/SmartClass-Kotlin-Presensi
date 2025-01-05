package com.callcenter.smartclass.ui.home.pesanan.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import com.callcenter.smartclass.ui.home.pesanan.data.Order
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class OrderViewModel : ViewModel() {

    private val _orders = MutableStateFlow<List<Order>>(emptyList())
    val orders: StateFlow<List<Order>> = _orders

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage

    private val firestore = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    init {
        fetchOrders()
    }

    fun fetchOrders() {
        val user = auth.currentUser
        if (user == null) {
            _errorMessage.value = "Pengguna tidak terautentikasi."
            return
        }

        _isLoading.value = true
        firestore.collection("orders")
            .whereEqualTo("userId", user.uid)
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .addSnapshotListener { querySnapshot, exception ->
                if (exception != null) {
                    Log.e("OrderViewModel", "Error fetching orders: ${exception.message}")
                    _errorMessage.value = exception.message
                    _isLoading.value = false
                    return@addSnapshotListener
                }

                if (querySnapshot != null) {
                    val orderList = querySnapshot.documents.mapNotNull { document ->
                        val order = document.toObject(Order::class.java)
                        if (order != null) {
                            order.copy(orderId = document.id)
                        } else {
                            Log.e("OrderViewModel", "Gagal memetakan dokumen: ${document.id}")
                            null
                        }
                    }
                    _orders.value = orderList
                    _isLoading.value = false
                }
            }
    }
}
