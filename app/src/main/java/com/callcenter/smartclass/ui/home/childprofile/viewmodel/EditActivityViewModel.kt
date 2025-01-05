package com.callcenter.smartclass.ui.home.childprofile.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.callcenter.smartclass.ui.home.childprofile.data.Activity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class EditActivityViewModel(private val childId: String, private val activityTimestamp: Long) : ViewModel() {

    private val TAG = "EditActivityViewModel"

    private val _activity = MutableStateFlow<Activity?>(null)
    val activity: StateFlow<Activity?> = _activity

    private val _isLoading = MutableStateFlow<Boolean>(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()

    private val _isUpdateSuccessful = MutableStateFlow<Boolean?>(null)
    val isUpdateSuccessful: StateFlow<Boolean?> = _isUpdateSuccessful

    init {
        fetchActivity()
    }

    private fun fetchActivity() {
        Log.d(TAG, "Fetching activity with timestamp: $activityTimestamp")
        _isLoading.value = true

        firestore.collection("users")
            .document(FirebaseAuth.getInstance().currentUser?.uid ?: "")
            .collection("children")
            .document(childId)
            .collection("activities")
            .whereEqualTo("timestamp", activityTimestamp)
            .get()
            .addOnSuccessListener { querySnapshot ->
                if (querySnapshot.isEmpty) {
                    _error.value = "Aktivitas tidak ditemukan."
                } else {
                    val fetchedActivity = querySnapshot.documents.first().toObject(Activity::class.java)
                    if (fetchedActivity != null) {
                        _activity.value = fetchedActivity.copy(id = querySnapshot.documents.first().id)
                    } else {
                        _error.value = "Data aktivitas tidak valid."
                    }
                }
                _isLoading.value = false
            }
            .addOnFailureListener { exception ->
                Log.e(TAG, "Error fetching activity: ${exception.message}", exception)
                _error.value = "Gagal mengambil data aktivitas."
                _isLoading.value = false
            }
    }

    fun updateActivityName(newName: String) {
        val currentActivity = _activity.value
        if (currentActivity != null) {
            _activity.value = currentActivity.copy(activityName = newName)
        }
    }

    fun updateSelectedDate(newDate: String) {
        val currentActivity = _activity.value
        if (currentActivity != null) {
            _activity.value = currentActivity.copy(selectedDate = newDate)
        }
    }

    fun updateStartTime(newStartTime: String) {
        val currentActivity = _activity.value
        if (currentActivity != null) {
            _activity.value = currentActivity.copy(startTime = newStartTime)
        }
    }

    fun updateEndTime(newEndTime: String) {
        val currentActivity = _activity.value
        if (currentActivity != null) {
            _activity.value = currentActivity.copy(endTime = newEndTime)
        }
    }

    fun updateSelectedTags(newTags: List<String>) {
        val currentActivity = _activity.value
        if (currentActivity != null) {
            _activity.value = currentActivity.copy(selectedTags = newTags)
        }
    }

    fun updateDetail(newDetail: String) {
        val currentActivity = _activity.value
        if (currentActivity != null) {
            _activity.value = currentActivity.copy(detail = newDetail)
        }
    }

    fun resetUpdateSuccess() {
        _isUpdateSuccessful.value = null
    }

    fun updateActivity() {
        val updatedActivity = _activity.value
        if (updatedActivity != null && updatedActivity.id.isNotEmpty()) {
            _isLoading.value = true
            firestore.collection("users")
                .document(FirebaseAuth.getInstance().currentUser?.uid ?: "")
                .collection("children")
                .document(childId)
                .collection("activities")
                .document(updatedActivity.id)
                .set(updatedActivity)
                .addOnSuccessListener {
                    Log.d(TAG, "Activity updated successfully.")
                    _isUpdateSuccessful.value = true
                    _isLoading.value = false
                }
                .addOnFailureListener { e ->
                    Log.e(TAG, "Failed to update activity: ${e.message}", e)
                    _error.value = "Gagal memperbarui aktivitas."
                    _isUpdateSuccessful.value = false
                    _isLoading.value = false
                }
        } else {
            _error.value = "Aktivitas tidak valid."
            _isUpdateSuccessful.value = false
        }
    }

    class Factory(private val childId: String, private val activityTimestamp: Long) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(EditActivityViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return EditActivityViewModel(childId, activityTimestamp) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}
