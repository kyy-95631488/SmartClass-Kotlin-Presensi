package com.callcenter.smartclass.ui

import com.auth0.jwt.JWT
import com.auth0.jwt.exceptions.JWTDecodeException

object TokenValidator {

    fun validateToken(token: String): Boolean {
        return try {
            val decodedJWT = JWT.decode(token)

            val issuedAt = decodedJWT.issuedAt?.time ?: 0L
            val expiration = decodedJWT.expiresAt?.time ?: 0L
            val currentTime = System.currentTimeMillis()

            if (issuedAt > currentTime) {
                return false
            }

            if (expiration < currentTime) {
                return false
            }

            true
        } catch (_: JWTDecodeException) {
            false
        }
    }
}