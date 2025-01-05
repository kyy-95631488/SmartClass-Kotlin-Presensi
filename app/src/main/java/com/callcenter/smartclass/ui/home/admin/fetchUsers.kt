package com.callcenter.smartclass.ui.home.admin

import com.callcenter.smartclass.data.UserFirebase
import com.google.firebase.firestore.FirebaseFirestore

fun fetchUsers(onResult: (List<UserFirebase>) -> Unit) {
    val db = FirebaseFirestore.getInstance()
    db.collection("users")
        .get()
        .addOnSuccessListener { result ->
            val usersList = mutableListOf<UserFirebase>()
            for (document in result) {
                val user = document.toObject(UserFirebase::class.java)
                usersList.add(user)
            }
            onResult(usersList)
        }
        .addOnFailureListener { exception ->
            println("Error getting documents: $exception")
        }
}
