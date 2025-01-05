package com.callcenter.smartclass.ui.home.admin.orderanmasuk.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.callcenter.smartclass.ui.home.admin.orderanmasuk.data.OrderAdmin
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class IncomingOrdersViewModel : ViewModel() {

    private val _orders = MutableStateFlow<List<OrderAdmin>>(emptyList())
    val orders: StateFlow<List<OrderAdmin>> = _orders

    private val _usernames = MutableStateFlow<List<String>>(emptyList())
    val usernames: StateFlow<List<String>> = _usernames

    private val firestore = FirebaseFirestore.getInstance()

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading

    init {
        fetchOrders()
    }

    private fun fetchOrders() {
        firestore.collection("orders")
            .addSnapshotListener { querySnapshot, exception ->
                if (exception != null) {
                    println("Error fetching orders: ${exception.message}")
                    _isLoading.value = false
                    return@addSnapshotListener
                }

                val ordersList = mutableListOf<OrderAdmin>()
                val orders = querySnapshot?.documents ?: run {
                    _isLoading.value = false
                    return@addSnapshotListener
                }
                viewModelScope.launch {
                    val usernameSet = mutableSetOf<String>()
                    for (orderDoc in orders) {
                        val orderId = orderDoc.getString("orderId") ?: ""
                        val userId = orderDoc.getString("userId") ?: ""
                        val productId = orderDoc.getString("productId") ?: ""

                        val createdAtTimestamp = orderDoc.getTimestamp("createdAt") ?: Timestamp.now()
                        val paymentStatus = orderDoc.getString("paymentStatus") ?: ""
                        val shippingStatus = orderDoc.getString("shippingStatus") ?: ""

                        val userDoc = firestore.collection("users").document(userId).get().await()
                        val username = userDoc.getString("username") ?: "Unknown"
                        usernameSet.add(username)

                        val productDoc = firestore.collection("products").document(productId).get().await()
                        val productTitle = productDoc.getString("title") ?: "Unknown Product"
                        val thumbnailUrl = productDoc.getString("thumbnailUrl") ?: ""

                        val order = OrderAdmin(
                            orderId = orderId,
                            userId = userId,
                            productId = productId,
                            username = username,
                            productTitle = productTitle,
                            thumbnailUrl = thumbnailUrl,
                            createdAt = createdAtTimestamp,
                            paymentStatus = paymentStatus,
                            shippingStatus = shippingStatus
                        )

                        ordersList.add(order)
                    }

                    ordersList.sortByDescending { it.createdAt.toDate() }

                    _orders.value = ordersList
                    _usernames.value = usernameSet.toList().sorted()
                    _isLoading.value = false
                }
            }
    }
}
