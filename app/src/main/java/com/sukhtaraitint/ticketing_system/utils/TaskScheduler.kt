package com.sukhtaraitint.ticketing_system.utils

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import com.sukhtaraitint.ticketing_system.receivers.DataBackUpAlarmBroadcastReceiver

class TaskScheduler {

    lateinit var context: Context

    constructor(context: Context) {
        this.context = context
    }

    fun schedule(context: Context, alarmType: String?) {
        /*val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, AlarmBroadcastReceiver::class.java)
        intent.putExtra("TITLE", "Prayer Title ...")
        intent.putExtra("requestCode", id)
        intent.putExtra("PrayerName", prayerName)
        intent.putExtra("AlarmType", alarmType)
        val alarmPendingIntent =
            PendingIntent.getBroadcast(context, id, intent, PendingIntent.FLAG_IMMUTABLE)
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = System.currentTimeMillis()
        calendar[Calendar.HOUR_OF_DAY] = alarmTime.getHours()
        calendar[Calendar.MINUTE] = alarmTime.getMinutes()
        calendar[Calendar.SECOND] = 0
        calendar[Calendar.MILLISECOND] = 0

        // if alarm time has already passed, increment day by 1
        if (calendar.timeInMillis <= System.currentTimeMillis()) {
            calendar[Calendar.DAY_OF_MONTH] = calendar[Calendar.DAY_OF_MONTH] + 1
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            alarmManager.setExact(
                AlarmManager.RTC_WAKEUP,
                calendar.timeInMillis,
                alarmPendingIntent
            )
            //            alarmManager.setWindow();
        }*/
    }

    fun scheduleTask(){
        // Alarm set
        var alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        var millis: Long = 20000
        var prayerName = "Task name here."
        var alarmType = "data synchronization"
        //                    Log.d("Alarm_Type007", " : "+ getAlarmType(prayerTimeObj)+ " ::: "+ alarmType);
//                    alarmType = getAlarmType(prayerTimeObj);
        val intent = Intent(context, DataBackUpAlarmBroadcastReceiver::class.java)
        intent.putExtra("TITLE", "Prayer Title ...")
        intent.putExtra("requestCode", millis)
        intent.putExtra("PrayerName", prayerName)
        intent.putExtra("AlarmType", alarmType)

        val alarmPendingIntent =
            PendingIntent.getBroadcast(context, millis as Int, intent, PendingIntent.FLAG_IMMUTABLE)

        //Show alarm in exact Time....

        //Show alarm in exact Time....
        alarmManager.setWindow(AlarmManager.RTC_WAKEUP, millis, 100.toLong(), alarmPendingIntent)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            alarmManager.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                millis,
                alarmPendingIntent
            )
        } else {
            alarmManager.setExact(AlarmManager.RTC_WAKEUP, millis, alarmPendingIntent)
        }

    }


}