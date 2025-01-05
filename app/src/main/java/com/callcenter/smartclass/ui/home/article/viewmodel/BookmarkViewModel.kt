package com.callcenter.smartclass.ui.home.article.viewmodel

import androidx.lifecycle.ViewModel
import com.callcenter.smartclass.data.Artikel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class BookmarkViewModel : ViewModel() {
    private val _bookmarkedArticles = MutableStateFlow<List<Artikel>>(emptyList())
    val bookmarkedArticles: StateFlow<List<Artikel>> = _bookmarkedArticles

    private val firestore = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private val userId = auth.currentUser?.uid ?: ""

    init {
        if (userId.isNotEmpty()) {
            fetchBookmarks()
        }
    }

    private fun fetchBookmarks() {
        firestore.collection("users")
            .document(userId)
            .collection("bookmarks")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    // Tangani error jika diperlukan
                    return@addSnapshotListener
                }
                if (snapshot != null) {
                    val bookmarks = snapshot.toObjects(Artikel::class.java)
                    _bookmarkedArticles.value = bookmarks
                }
            }
    }

    fun addBookmark(article: Artikel) {
        if (!_bookmarkedArticles.value.any { it.uuid == article.uuid }) {
            firestore.collection("users")
                .document(userId)
                .collection("bookmarks")
                .document(article.uuid)
                .set(article)
                .addOnSuccessListener {
                    _bookmarkedArticles.value = _bookmarkedArticles.value + article
                }
                .addOnFailureListener {
                    // Tangani kegagalan jika diperlukan
                }
        }
    }

    fun removeBookmark(article: Artikel) {
        firestore.collection("users")
            .document(userId)
            .collection("bookmarks")
            .document(article.uuid)
            .delete()
            .addOnSuccessListener {
                _bookmarkedArticles.value = _bookmarkedArticles.value.filterNot { it.uuid == article.uuid }
            }
            .addOnFailureListener {
                // Tangani kegagalan jika diperlukan
            }
    }

    fun toggleBookmark(article: Artikel) {
        if (_bookmarkedArticles.value.any { it.uuid == article.uuid }) {
            removeBookmark(article)
        } else {
            addBookmark(article)
        }
    }
}
