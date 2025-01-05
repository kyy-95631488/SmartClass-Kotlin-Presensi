package com.callcenter.smartclass.ui.notifications

import android.annotation.SuppressLint
import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import com.callcenter.smartclass.ui.home.childprofile.diarymenu.data.DailyMenu
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import java.util.Calendar

object NotificationScheduler {

    suspend fun rescheduleAllNotifications(context: Context) {
        val auth = FirebaseAuth.getInstance()
        val db = FirebaseFirestore.getInstance()
        val currentUser = auth.currentUser
        val userId = currentUser?.uid ?: return

        try {
            val childrenSnapshot = db.collection("users")
                .document(userId)
                .collection("children")
                .get()
                .await()

            val dailyMenus = mutableListOf<DailyMenu>()

            for (childDoc in childrenSnapshot.documents) {
                val childId = childDoc.id
                val childName = childDoc.getString("name") ?: "Anak"

                val scheduleSnapshot = db.collection("users")
                    .document(userId)
                    .collection("children")
                    .document(childId)
                    .collection("schedule")
                    .get()
                    .await()

                for (scheduleDoc in scheduleSnapshot.documents) {
                    val day = scheduleDoc.id
                    val items = scheduleDoc.get("items") as? List<Map<String, Any?>> ?: continue

                    for (item in items) {
                        val time = item["time"] as? String ?: continue
                        val recipeId = item["recipeId"] as? String ?: continue
                        val recipeTitle = item["recipeTitle"] as? String ?: "Menu"

                        dailyMenus.add(
                            DailyMenu(
                                time = "$day $time",
                                title = recipeTitle,
                                thumbnailUrl = null,
                                recipeId = recipeId,
                                childName = childName // Tambahkan nama anak
                            )
                        )
                    }
                }
            }

            scheduleNotifications(context, dailyMenus)

        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    @SuppressLint("ScheduleExactAlarm")
    fun scheduleNotifications(context: Context, dailyMenus: List<DailyMenu>) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

        dailyMenus.forEach { menu ->
            val (dayOfWeek, time) = parseTime(menu.time)
            val calendar = Calendar.getInstance().apply {
                set(Calendar.DAY_OF_WEEK, dayOfWeek)
                val (hour, minute) = time.split(":").map { it.toInt() }
                set(Calendar.HOUR_OF_DAY, hour)
                set(Calendar.MINUTE, minute)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
                if (before(Calendar.getInstance())) {
                    add(Calendar.WEEK_OF_YEAR, 1)
                }
            }

            val intent = Intent(context, NotificationReceiver::class.java).apply {
                putExtra("title", "Jadwal Menu Hari Ini ${menu.title}")
                putExtra("message", "Saatnya memberi makan anak '${menu.childName}' menu '${menu.title}' pada jam '${time}'")
                putExtra("notificationId", generateNotificationId(menu))
            }

            val pendingIntent = PendingIntent.getBroadcast(
                context,
                generateNotificationId(menu),
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    calendar.timeInMillis,
                    pendingIntent
                )
            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                alarmManager.setExact(
                    AlarmManager.RTC_WAKEUP,
                    calendar.timeInMillis,
                    pendingIntent
                )
            } else {
                alarmManager.set(
                    AlarmManager.RTC_WAKEUP,
                    calendar.timeInMillis,
                    pendingIntent
                )
            }
        }
    }

    fun cancelNotifications(context: Context, dailyMenus: List<DailyMenu>) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

        dailyMenus.forEach { menu ->
            val intent = Intent(context, NotificationReceiver::class.java)
            val pendingIntent = PendingIntent.getBroadcast(
                context,
                generateNotificationId(menu),
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            alarmManager.cancel(pendingIntent)
        }
    }

    private fun parseTime(timeString: String): Pair<Int, String> {
        val parts = timeString.split(" ")
        val dayOfWeek = when (parts[0].lowercase()) {
            "senin" -> Calendar.MONDAY
            "selasa" -> Calendar.TUESDAY
            "rabu" -> Calendar.WEDNESDAY
            "kamis" -> Calendar.THURSDAY
            "jumat" -> Calendar.FRIDAY
            "sabtu" -> Calendar.SATURDAY
            "minggu" -> Calendar.SUNDAY
            else -> Calendar.MONDAY
        }
        val time = parts[1]
        return Pair(dayOfWeek, time)
    }

    private fun generateNotificationId(menu: DailyMenu): Int {
        return "${menu.recipeId}_${menu.time}".hashCode()
    }
}
