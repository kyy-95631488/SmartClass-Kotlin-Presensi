package com.callcenter.smartclass.ui.home.admin.orderanmasuk.viewmodel

import androidx.lifecycle.ViewModel
import com.callcenter.smartclass.ui.home.admin.orderanmasuk.data.OrderAdmin
import com.callcenter.smartclass.ui.home.admin.orderanmasuk.data.OrderDetail
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class OrderDetailViewModel(orderId: String) : ViewModel() {

    private val _orderDetail = MutableStateFlow<OrderDetail?>(null)
    val orderDetail: StateFlow<OrderDetail?> = _orderDetail

    private val firestore = FirebaseFirestore.getInstance()

    init {
        fetchOrderDetail(orderId)
    }

    private fun fetchOrderDetail(orderId: String) {
        kotlinx.coroutines.CoroutineScope(kotlinx.coroutines.Dispatchers.IO).launch {
            try {
                val orderDoc = firestore.collection("orders").document(orderId).get().await()
                if (orderDoc != null && orderDoc.exists()) {
                    val orderData = orderDoc.toObject(OrderAdmin::class.java)
                    if (orderData != null) {
                        val userDoc = firestore.collection("users").document(orderData.userId).get().await()
                        val username = userDoc.getString("username") ?: "Unknown"

                        // Ambil detail pengiriman dan kontak user
                        val country = userDoc.getString("country") ?: ""
                        val phoneNumber = userDoc.getString("phoneNumber") ?: ""
                        val city = userDoc.getString("city") ?: ""
                        val postalCode = userDoc.getString("postalCode") ?: ""
                        val province = userDoc.getString("province") ?: ""
                        val street = userDoc.getString("street") ?: ""

                        val productDoc = firestore.collection("products").document(orderData.productId).get().await()
                        val productTitle = productDoc.getString("title") ?: "Unknown Product"
                        val thumbnailUrl = productDoc.getString("thumbnailUrl") ?: ""

                        val combinedOrderDetail = OrderDetail(
                            order = orderData,
                            username = username,
                            productTitle = productTitle,
                            thumbnailUrl = thumbnailUrl,
                            country = country,
                            phoneNumber = phoneNumber,
                            city = city,
                            postalCode = postalCode,
                            province = province,
                            street = street
                        )

                        _orderDetail.value = combinedOrderDetail
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun updateOrder(
        receiptUrl: String,
        shippingReceiptUrl: String,
        shippingStatus: String,
        paymentStatus: String,
        courierType: String,
        onSuccess: () -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        val updates = mapOf(
            "receiptUrl" to receiptUrl,
            "shippingReceiptUrl" to shippingReceiptUrl,
            "shippingStatus" to shippingStatus,
            "paymentStatus" to paymentStatus,
            "courierType" to courierType
        )

        firestore.collection("orders")
            .document(_orderDetail.value?.order?.orderId ?: "")
            .update(updates)
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { exception -> onFailure(exception) }
    }
}
