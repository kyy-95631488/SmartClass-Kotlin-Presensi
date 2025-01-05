package com.callcenter.smartclass.ui.home.childprofile.diarymenu.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.callcenter.smartclass.ui.home.childprofile.diarymenu.data.Recipe
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class RecipeViewModel : ViewModel() {

    private val db = FirebaseFirestore.getInstance()
    private val _recipes = MutableStateFlow<List<Recipe>>(emptyList())

    private val _availableFirstLetters = MutableStateFlow<List<String>>(emptyList())
    val availableFirstLetters: StateFlow<List<String>> = _availableFirstLetters

    private val _filteredRecipes = MutableStateFlow<List<Recipe>>(emptyList())
    val filteredRecipes: StateFlow<List<Recipe>> = _filteredRecipes

    init {
        fetchRecipes()
    }

    private fun fetchRecipes() {
        viewModelScope.launch {
            db.collection("resep_menu")
                .get()
                .addOnSuccessListener { result ->
                    val recipeList = result.documents.map { doc ->
                        Recipe(
                            id = doc.id,
                            title = doc.getString("title") ?: ""
                        )
                    }
                    _recipes.value = recipeList
                    _filteredRecipes.value = recipeList

                    val letters = recipeList
                        .mapNotNull { it.title.firstOrNull()?.uppercaseChar() }
                        .distinct()
                        .sorted()
                    _availableFirstLetters.value = letters.map { it.toString() }
                }
                .addOnFailureListener { exception ->
                    // Handle the error as needed
                }
        }
    }

    fun filterRecipes(query: String, filter: String?) {
        viewModelScope.launch {
            var filtered = _recipes.value
            if (!query.isBlank()) {
                filtered = filtered.filter {
                    it.title.contains(query, ignoreCase = true)
                }
            }
            if (!filter.isNullOrBlank()) {
                filtered = filtered.filter {
                    it.title.contains(filter, ignoreCase = true)
                }
            }
            _filteredRecipes.value = filtered
        }
    }
}