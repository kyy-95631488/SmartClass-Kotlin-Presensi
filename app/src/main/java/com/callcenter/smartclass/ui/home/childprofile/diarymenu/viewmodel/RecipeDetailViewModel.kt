package com.callcenter.smartclass.ui.ui.home.childprofile.diarymenu.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.callcenter.smartclass.ui.home.childprofile.diarymenu.data.Recipe
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class RecipeDetailViewModel : ViewModel() {
    private val db = FirebaseFirestore.getInstance()

    private val _recipeDetail = MutableStateFlow<Recipe?>(null)
    val recipeDetail: StateFlow<Recipe?> = _recipeDetail

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage

    private val _userHasFavorited = MutableStateFlow(false)
    val userHasFavorited: StateFlow<Boolean> = _userHasFavorited

    private val _isBookmarked = MutableStateFlow(false)
    val isBookmarked: StateFlow<Boolean> = _isBookmarked

    private val auth = FirebaseAuth.getInstance()
    private val userId: String
        get() = auth.currentUser?.uid ?: ""

    fun fetchRecipeDetail(recipeId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val recipeDoc = db.collection("resep_menu")
                    .document(recipeId)
                    .get()
                    .await()

                if (recipeDoc.exists()) {
                    val title = recipeDoc.getString("title") ?: "Tidak Ada Judul"
                    val content = recipeDoc.getString("content")
                    val thumbnailUrl = recipeDoc.getString("thumbnailUrl")
                    val age = recipeDoc.getString("usia")
                    val cookingTime = recipeDoc.getString("waktuMasak")
                    val servings = recipeDoc.getString("porsi")
                    val servingContents = recipeDoc.getString("kandunganPorsi")
                    val funFacts = recipeDoc.getString("funFacts")
                    val category = recipeDoc.getString("category")

                    val recipe = Recipe(
                        id = recipeId,
                        title = title,
                        content = content,
                        thumbnailUrl = thumbnailUrl,
                        age = age,
                        cookingTime = cookingTime,
                        servings = servings,
                        servingContents = servingContents,
                        funFacts = funFacts,
                        category = category
                    )
                    _recipeDetail.value = recipe

                    // Fetch user-specific states
                    fetchUserFavoriteStatus(recipeId)
                    fetchUserBookmarkStatus(recipeId)
                } else {
                    _recipeDetail.value = null
                    _errorMessage.value = "Resep tidak ditemukan."
                }
            } catch (e: Exception) {
                e.printStackTrace()
                _errorMessage.value = "Terjadi kesalahan saat memuat resep."
                _recipeDetail.value = null
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun loadRecipeDetail(recipeId: String) {
        if (_recipeDetail.value == null) {
            fetchRecipeDetail(recipeId)
        }
    }

    private suspend fun fetchUserFavoriteStatus(recipeId: String) {
        if (userId.isEmpty()) {
            _userHasFavorited.value = false
            return
        }
        val favoriteDoc = db.collection("users")
            .document(userId)
            .collection("favorites")
            .document(recipeId)
            .get()
            .await()
        _userHasFavorited.value = favoriteDoc.exists()
    }

    private suspend fun fetchUserBookmarkStatus(recipeId: String) {
        if (userId.isEmpty()) {
            _isBookmarked.value = false
            return
        }
        val bookmarkDoc = db.collection("users")
            .document(userId)
            .collection("bookmarks")
            .document(recipeId)
            .get()
            .await()
        _isBookmarked.value = bookmarkDoc.exists()
    }

    // Toggle Favorite
//    fun toggleFavorite(recipeId: String) {
//        if (userId.isEmpty()) {
//            _errorMessage.value = "Anda harus login untuk memberi favorit."
//            return
//        }
//
//        viewModelScope.launch {
//            try {
//                val favoriteRef = db.collection("users")
//                    .document(userId)
//                    .collection("favorites")
//                    .document(recipeId)
//
//                val doc = favoriteRef.get().await()
//                if (doc.exists()) {
//                    // Remove favorite
//                    favoriteRef.delete().await()
//                    _userHasFavorited.value = false
//                    // Optionally, decrement favorite count in recipe
//                    db.collection("resep_menu")
//                        .document(recipeId)
//                        .update("favoriteCount", FieldValue.increment(-1))
//                } else {
//                    // Add favorite
//                    favoriteRef.set(mapOf("favoritedAt" to FieldValue.serverTimestamp())).await()
//                    _userHasFavorited.value = true
//                    // Optionally, increment favorite count in recipe
//                    db.collection("resep_menu")
//                        .document(recipeId)
//                        .update("favoriteCount", FieldValue.increment(1))
//                }
//            } catch (e: Exception) {
//                e.printStackTrace()
//                _errorMessage.value = "Gagal memperbarui favorit."
//            }
//        }
//    }

    // Toggle Bookmark
//    fun toggleBookmark(recipeId: String) {
//        if (userId.isEmpty()) {
//            _errorMessage.value = "Anda harus login untuk menandai bookmark."
//            return
//        }
//
//        viewModelScope.launch {
//            try {
//                val bookmarkRef = db.collection("users")
//                    .document(userId)
//                    .collection("bookmarks")
//                    .document(recipeId)
//
//                val doc = bookmarkRef.get().await()
//                if (doc.exists()) {
//                    // Remove bookmark
//                    bookmarkRef.delete().await()
//                    _isBookmarked.value = false
//                } else {
//                    // Add bookmark
//                    bookmarkRef.set(mapOf("bookmarkedAt" to FieldValue.serverTimestamp())).await()
//                    _isBookmarked.value = true
//                }
//            } catch (e: Exception) {
//                e.printStackTrace()
//                _errorMessage.value = "Gagal memperbarui bookmark."
//            }
//        }
//    }
}
