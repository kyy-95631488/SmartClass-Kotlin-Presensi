package com.callcenter.smartclass.ui.home.childprofile.diarymenu.data

data class Recipe(
    val id: String = "",
    val title: String = "",
    val content: String? = null,
    val thumbnailUrl: String? = null,
    val age: String? = null,
    val cookingTime: String? = null,
    val servings: String? = null,
    val servingContents: String? = null,
    val funFacts: String? = null,
    val category: String? = null
)