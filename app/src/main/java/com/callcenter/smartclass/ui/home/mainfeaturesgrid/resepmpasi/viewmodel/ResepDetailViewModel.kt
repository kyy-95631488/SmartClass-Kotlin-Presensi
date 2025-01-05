package com.callcenter.smartclass.ui.home.mainfeaturesgrid.resepmpasi.viewmodel

import androidx.lifecycle.ViewModel
import com.callcenter.smartclass.ui.home.admin.tambahresepmpasi.data.ResepMpasi
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class ResepDetailViewModel : ViewModel() {

    private val _recipe = MutableStateFlow<ResepMpasi?>(null)
    val recipe: StateFlow<ResepMpasi?> = _recipe

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage

    private val firestore = FirebaseFirestore.getInstance()

    fun fetchRecipeByUuid(uuid: String) {
        _isLoading.value = true
        firestore.collection("resep_mpasi").document(uuid).get()
            .addOnSuccessListener { document ->
                if (document != null && document.exists()) {
                    val resep = document.toObject(ResepMpasi::class.java)
                    _recipe.value = resep
                } else {
                    _errorMessage.value = "Resep tidak ditemukan."
                }
                _isLoading.value = false
            }
            .addOnFailureListener { exception ->
                _errorMessage.value = exception.message
                _isLoading.value = false
            }
    }

    fun updateLoveCount(uuid: String, userId: String) {
        val resep = _recipe.value ?: return

        if (resep.lovedBy.contains(userId)) {
            _errorMessage.value = "Anda sudah memberi love pada resep ini."
            return
        }

        val newLoveCount = resep.loveCount + 1

        firestore.collection("resep_mpasi").document(uuid)
            .update("loveCount", newLoveCount, "lovedBy", FieldValue.arrayUnion(userId))
            .addOnSuccessListener {
                firestore.collection("resep_mpasi").document(uuid).get()
                    .addOnSuccessListener { document ->
                        if (document != null && document.exists()) {
                            val updatedResep = document.toObject(ResepMpasi::class.java)
                            _recipe.value = updatedResep
                        } else {
                            _errorMessage.value = "Resep tidak ditemukan setelah update."
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