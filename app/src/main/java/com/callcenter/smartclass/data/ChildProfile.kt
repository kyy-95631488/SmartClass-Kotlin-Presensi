package com.callcenter.smartclass.data

import com.google.firebase.Timestamp
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

data class ChildProfile(
    var id: String = "",
    val name: String = "",
    val birthDate: String = "",
    val gender: String = "",
    val height: String = "",
    val weight: String = "",
    val headCircumference: String = "",
    val profileImageUrl: String? = null,
    val lastUpdated: Timestamp? = null
) {
    /**
     * Menghitung usia anak dalam bulan.
     * @return Usia dalam bulan, atau 0 jika parsing gagal.
     */
    fun getAgeInMonths(): Int {
        return try {
            val inputFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
            val birthDateParsed: Date = inputFormat.parse(birthDate) ?: return 0
            val birthCalendar = Calendar.getInstance().apply { time = birthDateParsed }
            val currentCalendar = Calendar.getInstance()

            var months = (currentCalendar.get(Calendar.YEAR) - birthCalendar.get(Calendar.YEAR)) * 12
            months += currentCalendar.get(Calendar.MONTH) - birthCalendar.get(Calendar.MONTH)

            if (currentCalendar.get(Calendar.DAY_OF_MONTH) < birthCalendar.get(Calendar.DAY_OF_MONTH)) {
                months--
            }

            if (months < 0) 0 else months
        } catch (e: Exception) {
            e.printStackTrace()
            0
        }
    }

    /**
     * Mendapatkan usia anak, jika lebih dari 60 bulan diubah menjadi tahun.
     * @return Usia dalam bulan atau tahun.
     */
    fun getAge(): Float {
        val ageInMonths = getAgeInMonths()

        return if (ageInMonths > 60) {
            ageInMonths / 12f
        } else {
            ageInMonths.toFloat()
        }
    }

}
