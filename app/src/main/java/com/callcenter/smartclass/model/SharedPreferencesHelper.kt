package com.callcenter.smartclass.model

import android.content.Context
import android.content.SharedPreferences
import android.util.Log

object SharedPreferencesHelper {
    private const val PREF_NAME = "AppPrefs"
    private const val TOKEN_KEY = "auth_token"
    private const val TOKEN_EXPIRATION_KEY = "token_expiration_time"

    private fun getSharedPreferences(context: Context): SharedPreferences {
        return context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
    }

    fun saveToken(context: Context, token: String, expirationTime: Long) {
        val sharedPreferences = getSharedPreferences(context)
        val editor = sharedPreferences.edit()
        editor.putString(TOKEN_KEY, token)
        editor.putLong(TOKEN_EXPIRATION_KEY, expirationTime)
        editor.apply()
        Log.d("TokenInfo", "Token disimpan dengan waktu kedaluwarsa: $expirationTime")
    }

    fun getToken(context: Context): String? {
        return getSharedPreferences(context).getString(TOKEN_KEY, null)
    }

    fun getTokenExpirationTime(context: Context): Long {
        return getSharedPreferences(context).getLong(TOKEN_EXPIRATION_KEY, 0L)
    }

    fun isTokenAvailable(context: Context): Boolean {
        return getToken(context) != null
    }

    fun isTokenExpired(context: Context): Boolean {
        val expirationTime = getTokenExpirationTime(context)
        return expirationTime <= System.currentTimeMillis()
    }

    fun clearToken(context: Context) {
        val sharedPreferences = getSharedPreferences(context)
        with(sharedPreferences.edit()) {
            remove(TOKEN_KEY)
            remove(TOKEN_EXPIRATION_KEY)
            apply()
            Log.d("TokenInfo", "Token dan waktu kedaluwarsa telah dihapus.")
        }
    }
}