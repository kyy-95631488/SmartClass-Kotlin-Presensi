package com.callcenter.smartclass.ui.home.mainfeaturesgrid.infoproduk.repo

import com.callcenter.smartclass.ui.home.mainfeaturesgrid.infoproduk.data.Brand_Data
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class BrandRepository {
    private val db = FirebaseFirestore.getInstance()

    suspend fun getBrands(): List<Brand_Data> {
        val brandsSnapshot = db.collection("brands").get().await()
        return brandsSnapshot.documents.map { doc ->
            Brand_Data(
                uid = doc.id,
                name = doc.getString("name") ?: "",
                imageUrl = doc.getString("imageUrl") ?: ""
            )
        }
    }

    suspend fun getProductCountByBrand(brandId: String): Int {
        val productsSnapshot = db.collection("products")
            .whereEqualTo("brandId", brandId)
            .get()
            .await()
        return productsSnapshot.size()
    }
}