package com.callcenter.smartclass.ui.home.mainfeaturesgrid.predictv2.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "children")
data class Child(
    @PrimaryKey var id: String = "",
    val name: String = "",
    val birthDate: String = "",
    val gender: String = "",
    val height: String = "",
    val weight: String = "",
    val headCircumference: String = "",
)