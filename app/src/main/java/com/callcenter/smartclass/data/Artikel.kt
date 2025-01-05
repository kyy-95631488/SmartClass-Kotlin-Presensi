package com.callcenter.smartclass.data

import com.google.firebase.firestore.DocumentId

data class Artikel(
    @DocumentId
    val uuid: String = "",
    val title: String = "",
    val topic: String = "",
    val content: String = "",
    val thumbnailUrl: String = "",
    val category: String = "",
    val timestamp: Long = System.currentTimeMillis(),
    val viewCount: Long = 0L,
    val loveCount: Long = 0L,
    val lovedBy: List<String> = emptyList(),
)
