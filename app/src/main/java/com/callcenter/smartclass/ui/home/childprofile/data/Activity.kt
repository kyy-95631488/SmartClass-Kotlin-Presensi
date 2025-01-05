package com.callcenter.smartclass.ui.home.childprofile.data

data class Activity(
    val id: String = "",
    val activityName: String = "",
    val selectedDate: String = "",
    val startTime: String = "",
    val endTime: String = "",
    val selectedTags: List<String> = listOf(),
    val detail: String = "",
    val timestamp: Long = System.currentTimeMillis()
)