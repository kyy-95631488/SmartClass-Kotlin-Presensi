package com.callcenter.smartclass.ui.home.account

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions

fun updateAccount(
    firebaseAuth: FirebaseAuth,
    newUsername: String,
    npm: String,
    onResult: (Boolean, String) -> Unit
) {
    val currentUser = firebaseAuth.currentUser
    val firestore = FirebaseFirestore.getInstance()

    currentUser?.let { user ->
        val profileUpdates = UserProfileChangeRequest.Builder()
            .setDisplayName(newUsername)
            .build()

        user.updateProfile(profileUpdates).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val userData = mapOf("npm" to npm)
                firestore.collection("users").document(user.uid)
                    .set(userData, SetOptions.merge())
                    .addOnCompleteListener { firestoreTask ->
                        if (firestoreTask.isSuccessful) {
                            onResult(true, "Username dan NPM berhasil diperbarui.")
                        } else {
                            onResult(false, "Gagal menyimpan NPM: ${firestoreTask.exception?.message}")
                        }
                    }
            } else {
                onResult(false, "Gagal memperbarui username: ${task.exception?.message}")
            }
        }
    } ?: run {
        onResult(false, "User tidak terautentikasi.")
    }
}
