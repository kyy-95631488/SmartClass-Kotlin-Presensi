package com.callcenter.smartclass.ui.home.mainfeaturesgrid.resepmpasi.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.callcenter.smartclass.ui.home.admin.tambahresepmpasi.data.ResepMpasi
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class BookmarkResepViewModel : ViewModel() {
    private val _bookmarkedRecipes = MutableStateFlow<List<ResepMpasi>>(emptyList())
    val bookmarkedRecipes: StateFlow<List<ResepMpasi>> = _bookmarkedRecipes

    private val firestore = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private val userId = auth.currentUser?.uid

    init {
        if (!userId.isNullOrEmpty()) {
            fetchBookmarks()
        } else {
            Log.e("BookmarkResepViewModel", "User not authenticated.")
        }
    }

    private fun fetchBookmarks() {
        firestore.collection("users")
            .document(userId!!)
            .collection("bookmark_resep_mpasi")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e("BookmarkResepViewModel", "Error fetching bookmarks: ${error.message}")
                    return@addSnapshotListener
                }
                if (snapshot != null) {
                    val bookmarks = snapshot.toObjects(ResepMpasi::class.java)
                    _bookmarkedRecipes.value = bookmarks
                    Log.d("BookmarkResepViewModel", "Bookmarks fetched: ${bookmarks.size}")
                }
            }
    }

    suspend fun addBookmark(recipe: ResepMpasi) {
        if (!userId.isNullOrEmpty() && recipe.uuid.isNotEmpty() &&
            !_bookmarkedRecipes.value.any { it.uuid == recipe.uuid }) {
            try {
                firestore.collection("users")
                    .document(userId)
                    .collection("bookmark_resep_mpasi")
                    .document(recipe.uuid)
                    .set(recipe)
                    .await()
                _bookmarkedRecipes.value = _bookmarkedRecipes.value + recipe
                Log.d("BookmarkResepViewModel", "Bookmark added for: ${recipe.uuid}")
            } catch (e: Exception) {
                Log.e("BookmarkResepViewModel", "Error adding bookmark: ${e.message}")
            }
        }
    }

    suspend fun removeBookmark(recipe: ResepMpasi) {
        if (!userId.isNullOrEmpty() && recipe.uuid.isNotEmpty()) {
            try {
                firestore.collection("users")
                    .document(userId)
                    .collection("bookmark_resep_mpasi")
                    .document(recipe.uuid)
                    .delete()
                    .await()
                _bookmarkedRecipes.value = _bookmarkedRecipes.value.filterNot { it.uuid == recipe.uuid }
                Log.d("BookmarkResepViewModel", "Bookmark removed for: ${recipe.uuid}")
            } catch (e: Exception) {
                Log.e("BookmarkResepViewModel", "Error removing bookmark: ${e.message}")
            }
        }
    }

    fun toggleBookmark(recipe: ResepMpasi) {
        viewModelScope.launch {
            if (_bookmarkedRecipes.value.any { it.uuid == recipe.uuid }) {
                Log.d("BookmarkResepViewModel", "Toggling off bookmark for: ${recipe.uuid}")
                removeBookmark(recipe)
            } else {
                Log.d("BookmarkResepViewModel", "Toggling on bookmark for: ${recipe.uuid}")
                addBookmark(recipe)
            }
        }
    }
}
