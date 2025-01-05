package com.callcenter.smartclass.ui.funcauth.viewmodel

import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class UserViewModel : ViewModel() {

    private val _isAdmin = MutableStateFlow(false)
    val isAdmin: StateFlow<Boolean> = _isAdmin

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()

    init {
        fetchUserRole()
    }

    private fun fetchUserRole() {
        val user = auth.currentUser
        if (user != null) {
            firestore.collection("users").document(user.uid).get()
                .addOnSuccessListener { document ->
                    if (document != null) {
                        val role = document.getString("role")
                        _isAdmin.value = role == "admin"
                    } else {
                        _isAdmin.value = false
                    }
                    _isLoading.value = false
                }
                .addOnFailureListener { exception ->
                    _isAdmin.value = false
                    _isLoading.value = false
                }
        } else {
            _isAdmin.value = false
            _isLoading.value = false
        }
    }
}
