package com.callcenter.smartclass.ui.home.childprofile.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.callcenter.smartclass.ui.home.childprofile.data.Activity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class DailyActivityViewModel(private val childId: String) : ViewModel() {

    private val TAG = "DailyActivityViewModel"

    private val _activities = MutableStateFlow<List<Activity>>(emptyList())
    val activities: StateFlow<List<Activity>> = _activities

    private val _isLoading = MutableStateFlow<Boolean>(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()

    private var activitiesListener: ListenerRegistration? = null

    init {
        Log.d(TAG, "ViewModel initialized with childId: $childId")
        listenToDailyActivities()
    }

    private fun listenToDailyActivities() {
        Log.d(TAG, "Listening to daily activities for childId: $childId")
        val currentUser = auth.currentUser
        if (currentUser == null) {
            Log.e(TAG, "User not logged in")
            _error.value = "User not logged in."
            return
        }

        val userId = currentUser.uid
        Log.d(TAG, "User ID: $userId")

        _isLoading.value = true

        activitiesListener = firestore.collection("users")
            .document(userId)
            .collection("children")
            .document(childId)
            .collection("activities")
            .orderBy("timestamp")
            .addSnapshotListener { querySnapshot, exception ->
                if (exception != null) {
                    Log.e(TAG, "Listen failed: ${exception.message}", exception)
                    _error.value = "Failed to listen to activities: ${exception.message}"
                    _isLoading.value = false
                    return@addSnapshotListener
                }

                if (querySnapshot != null) {
                    Log.d(TAG, "Successfully listened to activities, count: ${querySnapshot.size()}")
                    val activityList = querySnapshot.documents.mapNotNull { it.toObject(Activity::class.java) }
                    _activities.value = activityList
                    _isLoading.value = false
                } else {
                    Log.w(TAG, "Current data: null")
                    _error.value = "No data available."
                    _isLoading.value = false
                }
            }
    }

    override fun onCleared() {
        super.onCleared()
        activitiesListener?.remove()
        Log.d(TAG, "ViewModel cleared and listener removed")
    }

    class Factory(private val childId: String) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(DailyActivityViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return DailyActivityViewModel(childId) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}
