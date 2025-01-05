package com.callcenter.smartclass.ui.home.article.viewmodel.list

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.callcenter.smartclass.data.Artikel
import com.callcenter.smartclass.ui.funcauth.repo.ArticleRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class ArticleViewModel : ViewModel() {
    private val repository = ArticleRepository()

    private val _articles = MutableStateFlow<List<Artikel>>(emptyList())
    val articles: StateFlow<List<Artikel>> = _articles

    private val _filteredArticles = MutableStateFlow<List<Artikel>>(emptyList())
    val filteredArticles: StateFlow<List<Artikel>> = _filteredArticles

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery

    private val _selectedCategory = MutableStateFlow("")
    val selectedCategory: StateFlow<String> = _selectedCategory

    init {
        fetchArticles()
    }

    private fun fetchArticles() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val fetchedArticles = repository.getArticles()
                _articles.value = fetchedArticles
                _filteredArticles.value = fetchedArticles
                _errorMessage.value = null
            } catch (e: Exception) {
                _errorMessage.value = "Gagal memuat artikel: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
        filterArticles()
    }

    fun selectCategory(category: String) {
        _selectedCategory.value = category
        filterArticles()
    }

    private fun filterArticles() {
        val query = _searchQuery.value.lowercase()
        val selectedCategory = _selectedCategory.value

        _filteredArticles.value = _articles.value.filter {
            (query.isEmpty() || it.title.lowercase().contains(query) || it.topic.lowercase().contains(query)) &&
                    (selectedCategory.isEmpty() || it.category == selectedCategory)
        }
    }

    fun incrementViewCount(uuid: String) {
        viewModelScope.launch {
            try {
                repository.incrementViewCount(uuid)
                // Optimistically update the local state
                val currentArticles = _articles.value.toMutableList()
                val index = currentArticles.indexOfFirst { it.uuid == uuid }
                if (index != -1) {
                    val updatedArticle = currentArticles[index].copy(viewCount = currentArticles[index].viewCount + 1)
                    currentArticles[index] = updatedArticle
                    _articles.value = currentArticles
                    filterArticles() // Update filtered articles as well
                }
            } catch (e: Exception) {
                _errorMessage.value = "Gagal menambah jumlah lihat: ${e.message}"
            }
        }
    }
}
