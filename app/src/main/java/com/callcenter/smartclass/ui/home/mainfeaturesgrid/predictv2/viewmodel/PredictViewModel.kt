package com.callcenter.smartclass.ui.home.mainfeaturesgrid.predictv2.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.callcenter.smartclass.ui.home.mainfeaturesgrid.predictv2.data.Child
import com.callcenter.smartclass.ui.home.mainfeaturesgrid.predictv2.data.ChildDao
import com.callcenter.smartclass.ui.home.mainfeaturesgrid.predictv2.network.ChildPrediction
import com.callcenter.smartclass.ui.home.mainfeaturesgrid.predictv2.network.PredictRequest
import com.callcenter.smartclass.ui.home.mainfeaturesgrid.predictv2.network.RetrofitInstance
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await


sealed class PredictUiState {
    object Idle : PredictUiState()
    object Loading : PredictUiState()
    data class Success(val predictions: List<ChildPrediction>) : PredictUiState()
    data class Error(val message: String) : PredictUiState()
}

class PredictViewModel(
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance(),
    private val auth: FirebaseAuth = FirebaseAuth.getInstance(),
    private val childDao: ChildDao,
) : ViewModel() {

    private val _saveResult = MutableSharedFlow<Result<String>>()
    val saveResult = _saveResult.asSharedFlow()

    private val _uiState = MutableStateFlow<PredictUiState>(PredictUiState.Idle)
    val uiState: StateFlow<PredictUiState> = _uiState

    fun saveDataToLocal() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val uid = auth.currentUser?.uid ?: throw Exception("User not authenticated")
                val childrenSnapshot = firestore.collection("users")
                    .document(uid)
                    .collection("children")
                    .get()
                    .await()

                val childrenList = childrenSnapshot.documents.mapNotNull { doc ->
                    try {
                        doc.toObject(Child::class.java)?.copy(id = doc.id)
                    } catch (e: Exception) {
                        Log.e("PredictViewModel", "Error parsing document ${doc.id}: ${e.message}", e)
                        null
                    }
                }

                Log.d("PredictViewModel", "Jumlah anak yang diambil dari Firestore: ${childrenList.size}")
                childDao.insertChildren(childrenList)
                Log.d("PredictViewModel", "Data berhasil disimpan ke Room Database.")
                _saveResult.emit(Result.success("Data berhasil disimpan."))
            } catch (e: Exception) {
                Log.e("PredictViewModel", "Gagal menyimpan data: ${e.message}", e)
                _saveResult.emit(Result.failure(e))
            }
        }
    }

    fun fetchPredictionsForAllChildren(userId: String) {
        viewModelScope.launch {
            _uiState.value = PredictUiState.Loading
            try {
                // Fetch all children from Firestore
                val childrenSnapshot = firestore.collection("users")
                    .document(userId)
                    .collection("children")
                    .get()
                    .await()

                val childrenList = childrenSnapshot.documents.mapNotNull { doc ->
                    try {
                        doc.toObject(Child::class.java)?.copy(id = doc.id)
                    } catch (e: Exception) {
                        Log.e("PredictViewModel", "Error parsing document ${doc.id}: ${e.message}", e)
                        null
                    }
                }

                // Fetch predictions for each child
                val predictions = childrenList.map { child ->
                    try {
                        val request = PredictRequest(userId = userId, anakId = child.id)
                        val response = RetrofitInstance.api.getPrediction(request)
                        if (response.isSuccessful) {
                            val responseBody = response.body()
                            ChildPrediction(child, responseBody)
                        } else {
                            Log.e("PredictViewModel", "Error fetching prediction for ${child.name}: ${response.code()} ${response.message()}")
                            ChildPrediction(child, null)
                        }
                    } catch (e: Exception) {
                        Log.e("PredictViewModel", "Network error for ${child.name}: ${e.message}", e)
                        ChildPrediction(child, null)
                    }
                }

                _uiState.value = PredictUiState.Success(predictions)
            } catch (e: Exception) {
                _uiState.value = PredictUiState.Error("Kegagalan jaringan: ${e.message}")
            }
        }
    }
}
