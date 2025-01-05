package com.callcenter.smartclass.ui.home.mainfeaturesgrid.resepmpasi.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.callcenter.smartclass.ui.home.admin.tambahresepmpasi.data.ResepMpasi
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class ResepMpasiViewModel : ViewModel() {
    private val firestore = FirebaseFirestore.getInstance()

    private val _recipes = MutableStateFlow<List<ResepMpasi>>(emptyList())
    val recipes: StateFlow<List<ResepMpasi>> = _recipes

    private val _filteredRecipes = MutableStateFlow<List<ResepMpasi>>(emptyList())
    val filteredRecipes: StateFlow<List<ResepMpasi>> = _filteredRecipes

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery

    private val _selectedCategory = MutableStateFlow("")
    val selectedCategory: StateFlow<String> = _selectedCategory

    init {
        fetchRecipes()
    }

    private fun fetchRecipes() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val snapshot = firestore.collection("resep_mpasi").get().await()
                val fetchedRecipes = snapshot.toObjects(ResepMpasi::class.java)
                _recipes.value = fetchedRecipes
                _filteredRecipes.value = fetchedRecipes
                _errorMessage.value = null
            } catch (e: Exception) {
                _errorMessage.value = "Gagal memuat resep: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
        filterRecipes()
    }

    fun selectCategory(category: String) {
        _selectedCategory.value = category
        filterRecipes()
    }

    private fun filterRecipes() {
        val query = _searchQuery.value.lowercase()
        val selectedCategory = _selectedCategory.value

        _filteredRecipes.value = _recipes.value.filter {
            (query.isEmpty() || it.title.lowercase().contains(query) || it.topic.lowercase().contains(query)) &&
                    (selectedCategory.isEmpty() || it.category == selectedCategory)
        }
    }

    fun incrementViewCount(uuid: String) {
        viewModelScope.launch {
            try {
                val recipeRef = firestore.collection("resep_mpasi").document(uuid)
                firestore.runTransaction { transaction ->
                    val snapshot = transaction.get(recipeRef)
                    val newViewCount = (snapshot.getLong("viewCount") ?: 0L) + 1
                    transaction.update(recipeRef, "viewCount", newViewCount)
                }.await()
                val currentRecipes = _recipes.value.toMutableList()
                val index = currentRecipes.indexOfFirst { it.uuid == uuid }
                if (index != -1) {
                    val updatedRecipe = currentRecipes[index].copy(viewCount = currentRecipes[index].viewCount + 1)
                    currentRecipes[index] = updatedRecipe
                    _recipes.value = currentRecipes
                    filterRecipes()
                }
            } catch (e: Exception) {
                _errorMessage.value = "Gagal menambah jumlah lihat: ${e.message}"
            }
        }
    }

    fun toggleLove(recipe: ResepMpasi) {
        viewModelScope.launch {
            try {
                val recipeRef = firestore.collection("resep_mpasi").document(recipe.uuid)
                val userId = getCurrentUserId()

                if (recipe.lovedBy.contains(userId)) {
                    firestore.runTransaction { transaction ->
                        val snapshot = transaction.get(recipeRef)
                        val newLovedBy = snapshot.get("lovedBy") as? List<String> ?: emptyList()
                        val updatedLovedBy = newLovedBy.filter { it != userId }
                        val newLoveCount = (snapshot.getLong("loveCount") ?: 0L) - 1
                        transaction.update(recipeRef, "lovedBy", updatedLovedBy, "loveCount", newLoveCount)
                    }.await()
                } else {
                    firestore.runTransaction { transaction ->
                        val snapshot = transaction.get(recipeRef)
                        val newLovedBy = snapshot.get("lovedBy") as? List<String> ?: emptyList()
                        val updatedLovedBy = newLovedBy + userId
                        val newLoveCount = (snapshot.getLong("loveCount") ?: 0L) + 1
                        transaction.update(recipeRef, "lovedBy", updatedLovedBy, "loveCount", newLoveCount)
                    }.await()
                }

                val updatedRecipes = _recipes.value.map {
                    if (it.uuid == recipe.uuid) {
                        if (it.lovedBy.contains(userId)) {
                            it.copy(
                                lovedBy = it.lovedBy.filter { id -> id != userId },
                                loveCount = it.loveCount - 1
                            )
                        } else {
                            it.copy(
                                lovedBy = it.lovedBy + userId,
                                loveCount = it.loveCount + 1
                            )
                        }
                    } else {
                        it
                    }
                }
                _recipes.value = updatedRecipes
                filterRecipes()
            } catch (e: Exception) {
                _errorMessage.value = "Gagal memperbarui love: ${e.message}"
            }
        }
    }

    private fun getCurrentUserId(): String {
        return FirebaseAuth.getInstance().currentUser?.uid ?: ""
    }
}
