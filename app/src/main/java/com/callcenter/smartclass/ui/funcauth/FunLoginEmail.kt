package com.callcenter.smartclass.ui.funcauth

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Properties
import javax.mail.*
import javax.mail.internet.InternetAddress
import javax.mail.internet.MimeMessage
import kotlin.random.Random

class FunLoginEmail() : ViewModel() {

    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()

    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState: StateFlow<LoginUiState> = _uiState

    // Ubah menjadi kode 4 digit
    private fun generateRandomCode(): String {
        return Random.nextInt(1000, 9999).toString()
    }

    private fun sendEmail(receiverEmail: String, verificationCode: String) {
        viewModelScope.launch {
            try {
                val senderEmail = "smartclassx@gmail.com"
                val senderPassword = "mmgvsuyxhqsoxxtc" // Pastikan kredensial disimpan dengan aman
                val host = "smtp.gmail.com"

                val properties: Properties = System.getProperties().apply {
                    put("mail.transport.protocol", "smtp")
                    put("mail.smtp.host", host)
                    put("mail.smtp.port", "465")
                    put("mail.smtp.socketFactory.fallback", "false")
                    put("mail.smtp.quitwait", "false")
                    put("mail.smtp.socketFactory.port", "465")
                    put("mail.smtp.starttls.enable", "true")
                    put("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory")
                    put("mail.smtp.ssl.enable", "true")
                    put("mail.smtp.auth", "true")
                }

                val session: Session = Session.getInstance(properties, object : Authenticator() {
                    override fun getPasswordAuthentication(): PasswordAuthentication {
                        return PasswordAuthentication(senderEmail, senderPassword)
                    }
                })

                val mimeMessage = MimeMessage(session).apply {
                    addRecipient(Message.RecipientType.TO, InternetAddress(receiverEmail))
                    subject = "Your Verification Code"
                    setContent(
                        """
        <!DOCTYPE html>
        <html lang="en">
        <head>
          <meta charset="UTF-8" />
          <meta name="viewport" content="width=device-width, initial-scale=1.0"/>
          <title>Verification Code</title>
          <style>
            body {
              background-color: #f4f4f4;
              font-family: Arial, sans-serif;
              margin: 0;
              padding: 20px;
            }
            .container {
              background-color: #ffffff;
              border-radius: 5px;
              padding: 20px;
              max-width: 600px;
              margin: auto;
            }
            h1 {
              color: #333333;
              font-size: 22px;
              margin-bottom: 20px;
            }
            p {
              color: #555555;
              font-size: 16px;
              line-height: 1.6;
              margin: 10px 0;
            }
            .code {
              display: inline-block;
              font-size: 24px;
              font-weight: bold;
              color: #007BFF;
              background: #f0f8ff;
              padding: 10px 15px;
              border-radius: 4px;
              margin: 20px 0;
              text-decoration: none;
            }
            .footer {
              font-size: 14px;
              color: #999999;
              margin-top: 30px;
              border-top: 1px solid #eeeeee;
              padding-top: 15px;
              text-align: center;
            }
          </style>
        </head>
        <body>
          <div class="container">
            <h1>Your Verification Code</h1>
            <p>Hi,</p>
            <p>Thank you for signing up! Please use the code below to verify your account:</p>
            <p class="code">$verificationCode</p>
            <p>If you did not request this, you can safely ignore this email.</p>
            <p>Best Regards,<br><strong>smartclass</strong></p>
            <div class="footer">
              <p>If you have any questions, feel free to <a href="mailto:smartclassx@gmail.com">contact us</a>.</p>
            </div>
          </div>
        </body>
        </html>
        """.trimIndent(),
                        "text/html; charset=utf-8"
                    )
                }

                withContext(Dispatchers.IO) {
                    Transport.send(mimeMessage)
                }
            } catch (e: Exception) {
                e.printStackTrace()
                _uiState.value = _uiState.value.copy(
                    dialogMessage = "Failed to send email: ${e.message}",
                    dialogVisible = true
                )
            }
        }
    }

    private fun saveVerificationCode(code: String, email: String) {
        val timestamp = System.currentTimeMillis()
        val codeData = hashMapOf(
            "verificationCode" to code,
            "email" to email,
            "timestamp" to timestamp
        )

        firestore.collection("verificationCodes")
            .document(email)
            .set(codeData)
            .addOnSuccessListener {
                sendEmail(email, code)
                _uiState.value = _uiState.value.copy(
                    dialogMessage = "Verification code has been sent to your email.",
                    dialogVisible = true
                )
            }
            .addOnFailureListener { e ->
                _uiState.value = _uiState.value.copy(
                    dialogMessage = "Error saving verification code: ${e.message}",
                    dialogVisible = true
                )
            }
    }

    fun validateVerificationCode(inputCode: String, email: String, password: String) {
        _uiState.value = _uiState.value.copy(loadingVerification = true)

        firestore.collection("verificationCodes")
            .document(email)
            .get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    val savedCode = document.getString("verificationCode")
                    val savedTimestamp = document.getLong("timestamp") ?: 0
                    val currentTime = System.currentTimeMillis()
                    val expirationTime = 5 * 60 * 1000 // 5 menit

                    if (inputCode == savedCode && (currentTime - savedTimestamp) < expirationTime) {
                        firestore.collection("verificationCodes").document(email).delete()
                        auth.signInWithEmailAndPassword(email, password)
                            .addOnCompleteListener { task ->
                                if (task.isSuccessful) {
                                    val user = auth.currentUser
                                    user?.let {
                                        assignUserRole(it)
                                    }
                                    _uiState.value = _uiState.value.copy(
                                        navigateToHome = true,
                                        loadingVerification = false
                                    )
                                } else {
                                    _uiState.value = _uiState.value.copy(
                                        verificationError = "Login failed: ${task.exception?.message}",
                                        loadingVerification = false
                                    )
                                }
                            }
                    } else {
                        _uiState.value = _uiState.value.copy(
                            verificationError = "Invalid or expired verification code.",
                            loadingVerification = false
                        )
                    }
                } else {
                    _uiState.value = _uiState.value.copy(
                        verificationError = "Verification code not found.",
                        loadingVerification = false
                    )
                }
            }
            .addOnFailureListener {
                _uiState.value = _uiState.value.copy(
                    verificationError = "Failed to validate verification code.",
                    loadingVerification = false
                )
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
        val username = user.displayName ?: userEmail?.substringBefore("@") ?: "Unknown"
        val userUuid = user.uid

        val role = if (userEmail in adminEmails) "admin" else "user"

        val userData = mapOf(
            "uuid" to userUuid,
            "email" to userEmail,
            "role" to role,
            "username" to username,
            "profilePic" to userProfilePicUrl,
            "timestamp" to FieldValue.serverTimestamp()
        )

        firestore.collection("users")
            .document(user.uid)
            .set(userData, SetOptions.merge())
            .addOnSuccessListener {
                Log.d("FunLoginEmail", "$role role assigned to $userEmail")
            }
            .addOnFailureListener { e ->
                Log.e("FunLoginEmail", "Error assigning $role role", e)
            }
    }

    fun onLoginClick(email: String, password: String) {
        if (password.length >= 8) {
            _uiState.value = _uiState.value.copy(loading = true)
            val verificationCode = generateRandomCode()
            saveVerificationCode(verificationCode, email)
            _uiState.value = _uiState.value.copy(
                loading = false,
                verificationDialogVisible = true
            )
        } else {
            _uiState.value = _uiState.value.copy(
                dialogMessage = "Please enter a valid password.",
                dialogVisible = true,
                loading = false
            )
        }
    }

    fun dismissDialog() {
        _uiState.value = _uiState.value.copy(dialogVisible = false)
    }

    fun dismissVerificationDialog() {
        _uiState.value = _uiState.value.copy(verificationDialogVisible = false)
    }

    data class LoginUiState(
        val email: String = "",
        val password: String = "",
        val verificationCode: String = "",
        val showPassword: Boolean = false,
        val passwordError: String = "",
        val loading: Boolean = false,
        val verificationDialogVisible: Boolean = false,
        val verificationError: String = "",
        val loadingVerification: Boolean = false,
        val dialogVisible: Boolean = false,
        val dialogMessage: String = "",
        val navigateToHome: Boolean = false
    )
}
