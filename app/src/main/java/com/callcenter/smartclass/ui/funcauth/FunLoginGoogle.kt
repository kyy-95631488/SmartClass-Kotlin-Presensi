@file:Suppress("DEPRECATION")

package com.callcenter.smartclass.ui.funcauth

import android.app.AlertDialog
import android.content.res.Configuration
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.compose.runtime.*
import androidx.compose.ui.tooling.preview.Preview
import com.callcenter.smartclass.R
import com.callcenter.smartclass.data.LocaleManager
import com.callcenter.smartclass.model.SharedPreferencesHelper
import com.callcenter.smartclass.ui.smartclassApp
import com.callcenter.smartclass.ui.theme.smartclassTheme
import com.callcenter.smartclass.ui.uionly.GoogleLoginScreen
import com.callcenter.smartclass.ui.uionly.UiLoginViaEmail
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions

class FunLoginGoogle : ComponentActivity() {

    private lateinit var googleSignInClient: GoogleSignInClient
    private lateinit var auth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore

    @RequiresApi(Build.VERSION_CODES.R)
    private val signInLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            @Suppress("DEPRECATION") val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
            if (task.isSuccessful) {
                @Suppress("DEPRECATION") val account: GoogleSignInAccount? = task.result
                account?.let {
                    firebaseAuthWithGoogle(it.idToken!!)
                }
            } else {
                showSignInFailureDialog(task.exception?.message)
            }
        }

    @RequiresApi(Build.VERSION_CODES.R)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        auth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()

        val selectedLanguage = LocaleManager.getLanguage(this)

        if (SharedPreferencesHelper.isTokenAvailable(this)) {
            if (SharedPreferencesHelper.isTokenExpired(this)) {
                auth.signOut()
            } else {
                setContent { smartclassApp() }
                return
            }
        }

        setupGoogleSignInClient()

        setContent {
            smartclassTheme {
                GoogleLoginScreen(
                    onClick = { checkInternetAndSignIn() },
                    onEmailLoginClick = { navigateToEmailLogin() },
                    selectedLanguage = selectedLanguage
                )
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.R)
    private fun setupGoogleSignInClient() {
        @Suppress("DEPRECATION")
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()
        googleSignInClient = GoogleSignIn.getClient(this, gso)
    }

    @RequiresApi(Build.VERSION_CODES.R)
    private fun checkInternetAndSignIn() {
        if (isInternetAvailable()) {
            if (!this::googleSignInClient.isInitialized) {
                setupGoogleSignInClient()
            }
            signIn()
        } else {
            showNoInternetDialog()
        }
    }

    private fun isInternetAvailable(): Boolean {
        val connectivityManager = getSystemService(CONNECTIVITY_SERVICE) as ConnectivityManager
        val network = connectivityManager.activeNetwork ?: return false
        val activeNetwork = connectivityManager.getNetworkCapabilities(network) ?: return false
        return activeNetwork.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
    }

    @RequiresApi(Build.VERSION_CODES.R)
    private fun showNoInternetDialog() {
        runOnUiThread {
            val selectedLanguage = LocaleManager.getLanguage(this) // Ambil bahasa yang dipilih

            AlertDialog.Builder(this)
                .setTitle("No Internet Connection")
                .setMessage("Please check your internet connection and try again.")
                .setPositiveButton("OK") { dialog, _ ->
                    dialog.dismiss()
                    setContent {
                        smartclassTheme {
                            GoogleLoginScreen(
                                onClick = { checkInternetAndSignIn() },
                                onEmailLoginClick = { navigateToEmailLogin() },
                                selectedLanguage = selectedLanguage // Tambahkan parameter ini
                            )
                        }
                    }
                }
                .setCancelable(false)
                .show()
        }
    }

    @RequiresApi(Build.VERSION_CODES.R)
    private fun signIn() {
        val signInIntent = googleSignInClient.signInIntent
        signInLauncher.launch(signInIntent)
    }

    @RequiresApi(Build.VERSION_CODES.R)
    private fun firebaseAuthWithGoogle(idToken: String) {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        auth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    val user = auth.currentUser
                    user?.let {
                        assignUserRole(it)

                        // Mendapatkan ID Token (JWT)
                        it.getIdToken(true)
                            .addOnCompleteListener { tokenTask ->
                                if (tokenTask.isSuccessful) {
                                    val idToken = tokenTask.result?.token
                                    val expirationTime = tokenTask.result?.expirationTimestamp

                                    // Pastikan expirationTime dalam milidetik
                                    if (idToken != null && expirationTime != null) {
                                        SharedPreferencesHelper.saveToken(this, idToken, expirationTime * 1000) // Konversi ke milidetik
                                        Log.d("FunLoginGoogle", "Token disimpan dengan sukses")
                                    }

                                    Log.d("FunLoginGoogle", "ID Token: $idToken")

                                    // Pindahkan setContent ke sini setelah token disimpan
                                    setContent { smartclassApp() }
                                } else {
                                    Log.e("FunLoginGoogle", "Gagal mendapatkan ID Token", tokenTask.exception)
                                    // Anda bisa menampilkan dialog kesalahan di sini jika diperlukan
                                }
                            }
                    }
                } else {
                    showSignInFailureDialog(task.exception?.message)
                }
            }
    }

    private fun assignUserRole(user: FirebaseUser) {
        val adminEmails = listOf(
            "akunstoragex@gmail.com",
            "cerberus404x@gmail.com",
            "smartclassx@gmail.com",
            "kennyjosiahresa@gmail.com"
        )

        val userEmail = user.email
        val userProfilePicUrl = user.photoUrl?.toString()
        val username = user.displayName
        val userUuid = user.uid

        if (userEmail in adminEmails) {

            val userData = mapOf(
                "uuid" to userUuid,
                "email" to userEmail,
                "role" to "admin",
                "username" to username,
                "profilePic" to userProfilePicUrl,
                "timestamp" to FieldValue.serverTimestamp()
            )

            firestore.collection("users")
                .document(user.uid)
                .set(userData, SetOptions.merge())
                .addOnSuccessListener {
                    Log.d("FunLoginGoogle", "Admin role assigned to $userEmail")
                }
                .addOnFailureListener { e ->
                    Log.e("FunLoginGoogle", "Error assigning admin role", e)
                }
        } else {
            val userData = mapOf(
                "uuid" to userUuid,
                "email" to userEmail,
                "role" to "user",
                "username" to username,
                "profilePic" to userProfilePicUrl,
                "timestamp" to FieldValue.serverTimestamp()
            )

            firestore.collection("users")
                .document(user.uid)
                .set(userData, SetOptions.merge())
                .addOnSuccessListener {
                    Log.d("FunLoginGoogle", "User role assigned to $userEmail")
                }
                .addOnFailureListener { e ->
                    Log.e("FunLoginGoogle", "Error assigning user role", e)
                }
        }
    }

    @RequiresApi(Build.VERSION_CODES.R)
    private fun showSignInFailureDialog(errorMessage: String?) {
        runOnUiThread {
            AlertDialog.Builder(this)
                .setTitle("Sign-In Failed")
                .setMessage(errorMessage ?: "Unknown error occurred.")
                .setPositiveButton("Retry") { dialog, _ ->
                    dialog.dismiss()
                    checkInternetAndSignIn() // Jangan reset tampilan di sini
                }
                .setNegativeButton("Cancel") { dialog, _ ->
                    dialog.dismiss()
                }
                .setCancelable(false)
                .show()
        }
    }

    @RequiresApi(Build.VERSION_CODES.R)
    private fun navigateToEmailLogin() {
        setContent {
            smartclassTheme {
                UiLoginViaEmail()
            }
        }
    }
}

@Preview("default")
@Preview("dark theme", uiMode = Configuration.UI_MODE_NIGHT_YES)
@Preview(showBackground = true)
@Composable
fun PreviewGoogleLoginScreen() {
    smartclassTheme {
        GoogleLoginScreen(
            onClick = {},
            onEmailLoginClick = {},
            selectedLanguage = "en" // Tambahkan parameter ini
        )
    }
}