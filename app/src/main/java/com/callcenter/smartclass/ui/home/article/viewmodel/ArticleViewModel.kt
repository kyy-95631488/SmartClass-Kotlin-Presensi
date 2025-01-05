package com.callcenter.smartclass.ui.home.article.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.lifecycle.ViewModel
import com.callcenter.smartclass.data.Artikel
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query

class ArticleViewModel : ViewModel() {
    private val _articles = mutableStateListOf<Artikel>()
    val articles: SnapshotStateList<Artikel> get() = _articles

    var isLoading by mutableStateOf(true)
        private set

    var errorMessage by mutableStateOf<String?>(null)
        private set

    private val db = FirebaseFirestore.getInstance()

    init {
        fetchArticles()
    }

    private fun fetchArticles() {
        isLoading = true
        db.collection("articles")
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .limit(5)
            .get()
            .addOnSuccessListener { result ->
                val articlesList = result.map { document ->
                    document.toObject(Artikel::class.java)
                }
                _articles.clear()
                _articles.addAll(articlesList)
                isLoading = false

                errorMessage = null
            }
            .addOnFailureListener { exception ->
                errorMessage = "Tidak dapat mengambil artikel. Periksa koneksi internet Anda."
                isLoading = false
            }
    }

    /**
     * Fungsi untuk menambah jumlah tampilan artikel secara atomik.
     * @param uuid UUID dari artikel yang akan ditambah view count-nya.
     */
    fun incrementViewCount(uuid: String) {
        val articleRef = db.collection("articles").document(uuid)
        articleRef.update("viewCount", FieldValue.increment(1))
            .addOnSuccessListener {
                val index = _articles.indexOfFirst { it.uuid == uuid }
                if (index != -1) {
                    val updatedArticle = _articles[index].copy(viewCount = _articles[index].viewCount + 1)
                    _articles[index] = updatedArticle
                }
            }
            .addOnFailureListener { exception ->
                // Handle kegagalan, misalnya log error
                // Anda bisa menampilkan pesan error jika diperlukan
            }
    }
}
