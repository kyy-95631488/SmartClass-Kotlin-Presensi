package com.callcenter.smartclass.ui.home.childprofile.diarymenu.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.callcenter.smartclass.ui.home.childprofile.diarymenu.data.DailyMenu
import com.callcenter.smartclass.ui.notifications.NotificationScheduler
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class DailyMenuViewModel(application: Application) : AndroidViewModel(application) {

    private val db = FirebaseFirestore.getInstance()
    private val context = getApplication<Application>().applicationContext

    private val _dailyMenus = MutableStateFlow<List<DailyMenu>>(emptyList())
    val dailyMenus: StateFlow<List<DailyMenu>> = _dailyMenus

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage

    fun fetchDailyMenus(childId: String, dayOfWeek: String) {
        viewModelScope.launch {
            val currentUser = FirebaseAuth.getInstance().currentUser
            val userId = currentUser?.uid

            if (userId != null) {
                try {
                    val scheduleDoc = db.collection("users")
                        .document(userId)
                        .collection("children")
                        .document(childId)
                        .collection("schedule")
                        .document(dayOfWeek)
                        .get()
                        .await()

                    val childDoc = db.collection("users")
                        .document(userId)
                        .collection("children")
                        .document(childId)
                        .get()
                        .await()

                    val childName = childDoc.getString("name") ?: "Anak"
                    val menus = mutableListOf<DailyMenu>()

                    if (scheduleDoc.exists()) {
                        val items = scheduleDoc.get("items") as? List<Map<String, Any?>> ?: emptyList()
                        for (item in items) {
                            val time = item["time"] as? String ?: continue
                            val recipeId = item["recipeId"] as? String ?: continue

                            val recipeDoc = db.collection("resep_menu")
                                .document(recipeId)
                                .get()
                                .await()

                            if (recipeDoc.exists()) {
                                val title = recipeDoc.getString("title") ?: "Tidak Ada Judul"
                                val thumbnailUrl = recipeDoc.getString("thumbnailUrl")
                                menus.add(DailyMenu(time, title, thumbnailUrl, recipeId, childName))
                            }
                        }
                    }

                    _dailyMenus.value = menus
                    _isLoading.value = false
                } catch (e: Exception) {
                    e.printStackTrace()
                    _errorMessage.value = "Gagal mengambil data jadwal."
                    _isLoading.value = false
                }
            } else {
                _errorMessage.value = "Pengguna tidak terautentikasi."
                _isLoading.value = false
            }
        }
    }

    fun scheduleNotifications(dailyMenus: List<DailyMenu>) {
        NotificationScheduler.scheduleNotifications(context, dailyMenus)
    }

    fun cancelNotifications(dailyMenus: List<DailyMenu>) {
        NotificationScheduler.cancelNotifications(context, dailyMenus)
    }
}
