package com.callcenter.smartclass.ui.funcauth.repo

import com.callcenter.smartclass.data.Artikel
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class ArticleRepository {

    private val firestore = FirebaseFirestore.getInstance()
    private val articlesCollection = firestore.collection("articles")

    /**
     * Fetches a list of articles from Firestore.
     */
    suspend fun getArticles(): List<Artikel> {
        return try {
            val snapshot = articlesCollection.get().await()
            snapshot.documents.mapNotNull { it.toObject(Artikel::class.java) }
        } catch (e: Exception) {
            throw e
        }
    }

    /**
     * Increments the view count for a specific article.
     */
    suspend fun incrementViewCount(uuid: String) {
        try {
            val articleRef = firestore.collection("articles").document(uuid)
            articleRef.update("viewCount", FieldValue.increment(1)).await()
        } catch (e: Exception) {
            throw e
        }
    }
}
