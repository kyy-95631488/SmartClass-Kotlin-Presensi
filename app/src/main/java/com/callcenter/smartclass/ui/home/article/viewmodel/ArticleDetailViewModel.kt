package com.callcenter.smartclass.ui.home.article.viewmodel

import androidx.lifecycle.ViewModel
import com.callcenter.smartclass.data.Artikel
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class ArticleDetailViewModel : ViewModel() {

    private val _article = MutableStateFlow<Artikel?>(null)
    val article: StateFlow<Artikel?> = _article

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage

    private val firestore = FirebaseFirestore.getInstance()

    fun fetchArticleByUuid(uuid: String) {
        _isLoading.value = true
        firestore.collection("articles").document(uuid).get()
            .addOnSuccessListener { document ->
                if (document != null && document.exists()) {
                    val artikel = document.toObject(Artikel::class.java)
                    _article.value = artikel
                } else {
                    _errorMessage.value = "Artikel tidak ditemukan."
                }
                _isLoading.value = false
            }
            .addOnFailureListener { exception ->
                _errorMessage.value = exception.message
                _isLoading.value = false
            }
    }

    // Update love count
    fun updateLoveCount(uuid: String, userId: String) {
        val artikel = _article.value ?: return

        // Cek apakah pengguna sudah memberi love
        if (artikel.lovedBy.contains(userId)) {
            _errorMessage.value = "Anda sudah memberi love pada artikel ini."
            return
        }

        // Tambah loveCount
        val newLoveCount = artikel.loveCount + 1

        // Update Firestore
        firestore.collection("articles").document(uuid)
            .update("loveCount", newLoveCount, "lovedBy", FieldValue.arrayUnion(userId))
            .addOnSuccessListener {
                firestore.collection("articles").document(uuid).get()
                    .addOnSuccessListener { document ->
                        if (document != null && document.exists()) {
                            val updatedArtikel = document.toObject(Artikel::class.java)
                            _article.value = updatedArtikel
                        } else {
                            _errorMessage.value = "Artikel tidak ditemukan setelah update."
                        }
                    }
                    .addOnFailureListener { exception ->
                        _errorMessage.value = exception.message
                    }
            }
            .addOnFailureListener { exception ->
                _errorMessage.value = exception.message
            }
    }
}
