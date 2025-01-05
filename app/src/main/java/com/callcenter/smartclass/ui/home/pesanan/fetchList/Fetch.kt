package com.callcenter.smartclass.ui.home.pesanan.fetchList

import com.callcenter.smartclass.ui.home.pesanan.dataListOrder.OrderListData
import com.callcenter.smartclass.ui.home.pesanan.dataListOrder.ProductListData
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.tasks.await

suspend fun fetchOrders(uid: String): List<OrderListData> {
    val firestore = FirebaseFirestore.getInstance()
    val orderSnapshot = firestore.collection("orders")
        .whereEqualTo("userId", uid)
        .orderBy("createdAt", Query.Direction.DESCENDING)
        .get()
        .await()
    return orderSnapshot.documents.mapNotNull { doc ->
        doc.toObject(OrderListData::class.java)
    }
}

suspend fun fetchProduct(productId: String): ProductListData? {
    val firestore = FirebaseFirestore.getInstance()
    val productSnapshot = firestore.collection("products").document(productId).get().await()
    return productSnapshot.toObject(ProductListData::class.java)
}