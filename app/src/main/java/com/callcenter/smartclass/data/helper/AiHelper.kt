package com.callcenter.smartclass.data.helper

import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import com.callcenter.smartclass.BuildConfig
import com.callcenter.smartclass.data.ChildProfile
import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.content
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale

@RequiresApi(Build.VERSION_CODES.O)
suspend fun getGrowthAnalysis(childProfile: ChildProfile?): String {
    Log.d("getGrowthAnalysis", "Memulai proses analisis pertumbuhan.")

    return withContext(Dispatchers.IO) {
        if (childProfile == null) {
            Log.e("getGrowthAnalysis", "ChildProfile adalah null.")
            return@withContext "Data profil anak tidak tersedia."
        }

        val currentDateTime = LocalDateTime.now().format(
            DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss", Locale.getDefault())
        )

        val requestPayload = """
            Analisis Pertumbuhan Anak dan Pencegahan Stunting:
            - Tinggi Badan: ${childProfile.height} cm
            - Berat Badan: ${childProfile.weight} kg
            - Lingkar Kepala: ${childProfile.headCircumference} cm
            - Tanggal Lahir: ${childProfile.birthDate} 
            - Jenis Kelamin: ${childProfile.gender}
            - Tanggal Analisis: $currentDateTime

            Berdasarkan data di atas, lakukan analisis pertumbuhan anak ini dan berikan rekomendasi serta langkah-langkah yang dapat diambil untuk mencegah stunting. Sertakan saran mengenai nutrisi, aktivitas fisik, dan faktor lingkungan yang dapat mendukung pertumbuhan optimal anak.
        """.trimIndent()

        Log.d("getGrowthAnalysis", "Payload untuk AI: $requestPayload")

        try {
            val generativeModel = GenerativeModel(
                modelName = "gemini-1.5-flash",
                apiKey = BuildConfig.GEMINI_API_KEY
            )
            Log.d("getGrowthAnalysis", "GenerativeModel berhasil dibuat.")

            val inputContent = content {
                text(requestPayload)
            }

            Log.d("getGrowthAnalysis", "Mengirim permintaan ke model AI.")
            val responseFlow = generativeModel.generateContentStream(inputContent)

            val fullResponse = StringBuilder()
            responseFlow.collect { chunk ->
                Log.d("getGrowthAnalysis", "Menerima chunk: ${chunk.text}")
                fullResponse.append(chunk.text ?: "")
            }

            if (fullResponse.isNotEmpty()) {
                Log.d("getGrowthAnalysis", "Analisis AI berhasil diperoleh.")
                fullResponse.toString()
            } else {
                Log.w("getGrowthAnalysis", "Tidak ada analisis yang diterima dari AI.")
                "Tidak ada analisis tersedia."
            }
        } catch (e: Exception) {
            Log.e("getGrowthAnalysis", "Error saat mendapatkan analisis: ${e.message}", e)
            "Gagal mendapatkan analisis: ${e.message}"
        }
    }
}
