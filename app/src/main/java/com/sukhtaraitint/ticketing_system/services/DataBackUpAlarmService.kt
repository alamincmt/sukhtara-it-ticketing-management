package com.sukhtaraitint.ticketing_system.services

import android.app.*
import android.content.Intent
import android.media.AudioManager
import android.media.MediaPlayer
import android.media.RingtoneManager
import android.os.Build
import android.os.IBinder
import android.util.Log
import android.widget.Toast
import androidx.core.app.NotificationCompat
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.sukhtaraitint.ticketing_system.*
import com.sukhtaraitint.ticketing_system.models.TicketSold
import com.sukhtaraitint.ticketing_system.models.TotalTicketSoldReport
import com.sukhtaraitint.ticketing_system.receivers.DataBackUpAlarmBroadcastReceiver
import com.sukhtaraitint.ticketing_system.utils.ConstantValues
import com.sukhtaraitint.ticketing_system.utils.Variables
import java.lang.Exception
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.Executors


class DataBackUpAlarmService : Service() {
    private var mediaPlayer: MediaPlayer? = null
    var ticketCount: Int? = 0
    var counterWiseSellReport: HashMap<String, Int>? = null
    var user_type: String? = "superadmin"
    var ticketSoldListener : ValueEventListener? = null
    override fun onCreate() {
        Variables.PRAYER_ALARM_ONGOING = true
        super.onCreate()
    }

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        Variables.PRAYER_ALARM_ONGOING = true
        try {
            val notificationIntent = Intent(this, SuperAdminReportActivity::class.java)
            val alarmID = intent.getIntExtra("requestCode", 0)
            val PrayerName = intent.getStringExtra("PrayerName")
            val AlarmType = intent.getStringExtra("AlarmType")
            notificationIntent.putExtra(
                "requestCode",
                intent.getIntExtra("requestCode", PendingIntent.FLAG_IMMUTABLE)
            )
            notificationIntent.putExtra("AlarmType", intent.getStringExtra("AlarmType"))
            notificationIntent.putExtra("PrayerName", intent.getStringExtra("PrayerName"))
            val pendingIntent = PendingIntent.getActivity(
                this,
                alarmID,
                notificationIntent,
                PendingIntent.FLAG_IMMUTABLE
            )
            val alarmTitle = intent.getStringExtra("PrayerName") + " এর সময় হয়েছে "
            setAlarmForNextDay()

            // save data.
            saveTotalSoldTicketReport()

            val am: AudioManager
            if (AlarmType != null && PrayerName != null) {
                try {
                    mediaPlayer = MediaPlayer()
                    am = getSystemService(AUDIO_SERVICE) as AudioManager
                    am.setStreamVolume(
                        AudioManager.STREAM_RING,
                        am.getStreamMaxVolume(AudioManager.STREAM_RING),
                        0
                    )
                    mediaPlayer = MediaPlayer.create(
                        applicationContext,
                        RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
                    )
                    mediaPlayer!!.start()
                    val message = "Close_Service"
                    if (mediaPlayer != null) {
                        mediaPlayer!!.setOnCompletionListener {
                            Variables.PRAYER_ALARM_ONGOING = false
                            stopForeground(true)
                            if (mediaPlayer != null) {
                                mediaPlayer!!.stop()
                            }
                        }
                    }
                } catch (exception: Exception) {
                    exception.printStackTrace()
                }
                var notification: Notification? = null
                createAlarmNotificationChannnel()
                notification = NotificationCompat.Builder(this, ALARM_CHANNEL_ID)
                    .setContentTitle("Sukhtara IT Ticketing System")
                    .setContentText(alarmTitle)
                    .setSmallIcon(R.drawable.ic_launcher_foreground)
                    .setContentIntent(pendingIntent)
                    .setAutoCancel(true)
                    .setGroup(Variables.NOTIFICATION_GROUP_ID)
                    .setOngoing(false)
                    .build()
                startForeground(1, notification)
            } else {
                stopForeground(true)
            }
        } catch (exception: Exception) {
            exception.printStackTrace()
        }
        return START_STICKY
    }

    private fun createAlarmNotificationChannnel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val serviceChannel = NotificationChannel(
                ALARM_CHANNEL_ID,
                "Alarm Service Channel",
                NotificationManager.IMPORTANCE_LOW
            )
            //            serviceChannel.setSound(null, null);
            val manager = getSystemService(
                NotificationManager::class.java
            )
            manager.createNotificationChannel(serviceChannel)
        }
    }

    private fun createAlarmNotificationChannnelWithoutSound() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val serviceChannel = NotificationChannel(
                ALARM_CHANNEL_ID,
                "Alarm Service Channel",
                NotificationManager.IMPORTANCE_LOW
            )
            serviceChannel.setSound(null, null)
            val manager = getSystemService(
                NotificationManager::class.java
            )
            manager.createNotificationChannel(serviceChannel)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        stopForeground(true)
        if (mediaPlayer != null) {
            mediaPlayer!!.stop()
        }
        //        vibrator.cancel();
    }

    override fun onBind(intent: Intent): IBinder? {
        return null
    }

    fun setAlarmForNextDay() {
        val alarmManager = applicationContext.getSystemService(ALARM_SERVICE) as AlarmManager
        //                    Log.d("Alarm_Type007", " : "+ getAlarmType(prayerTimeObj)+ " ::: "+ alarmType);
//                    alarmType = getAlarmType(prayerTimeObj);
        val intent = Intent(applicationContext, DataBackUpAlarmBroadcastReceiver::class.java)
        intent.putExtra("TITLE", "Data Back up And Delete ...")
        intent.putExtra("requestCode", 100)
        val alarmPendingIntent = PendingIntent.getBroadcast(
            applicationContext,
            100, intent, PendingIntent.FLAG_UPDATE_CURRENT
        )
        val todayMidnight = Calendar.getInstance()
        todayMidnight.add(Calendar.DATE, 1)
        todayMidnight[Calendar.HOUR_OF_DAY] = 0
        todayMidnight[Calendar.MINUTE] = 0
        todayMidnight[Calendar.SECOND] = 0

        //Show alarm in exact Time....
        alarmManager.setWindow(
            AlarmManager.RTC_WAKEUP,
            todayMidnight.timeInMillis,
            100.toLong(),
            alarmPendingIntent
        )
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            alarmManager.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                todayMidnight.timeInMillis,
                alarmPendingIntent
            )
        } else {
            alarmManager.setExact(
                AlarmManager.RTC_WAKEUP,
                todayMidnight.timeInMillis,
                alarmPendingIntent
            )
        }
    }

    companion object {
        //    private Vibrator vibrator;
        const val ALARM_CHANNEL_ID = "ALARM_SERVICE_CHANNEL_NEW"
    }

    // Data back up and delete related code
    private fun saveTotalSoldTicketReport() {
        val database = Firebase.database(ConstantValues.DB_URL)
        val ticketSoldRef = database.getReference("ticket_sold")

        ticketCount = 0

        ticketSoldListener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                Log.d("DataSnapshot", snapshot.getValue().toString())
                var totalTicketSold : TicketSold? = null

                if (snapshot.getValue() != null){
                    val ticketSoldCounterSet = snapshot.getValue() as Map<String, *>
                    for ((key, value) in ticketSoldCounterSet) {
                        val ticketSoldMap: Map<String, *> = value as Map<String, *>
                        var counterSellCount = 0
                        for ((key1, value1) in ticketSoldMap) {
                            val ticketSoldSingleMap: Map<String, *> = value1 as Map<String, *>

                            totalTicketSold = TicketSold(ticketSoldSingleMap.get("id").toString().toInt(),
                                ticketSoldSingleMap.get("group_counter_id").toString().toInt(), ticketSoldSingleMap.get("from_counter_id").toString(),
                                ticketSoldSingleMap.get("to_counter_id").toString(), ticketSoldSingleMap.get("price_total").toString(),
                                ticketSoldSingleMap.get("total_tickets").toString().toInt(), ticketSoldSingleMap.get("date_time").toString().toLong(),
                                ticketSoldSingleMap.get("sold_by_counter_id").toString())
                            ticketCount = ticketCount!! + ticketSoldSingleMap.get("total_tickets").toString().toInt()
                        }

                    }

                    if(ticketCount!! > 0){
                        val totalTicketSoldReportObj = TotalTicketSoldReport(
                            counterWiseSellReport, ticketCount,
                            System.currentTimeMillis(),
                            user_type
                        )

                        // Write a message to the database
                        val database = Firebase.database(ConstantValues.DB_URL)
                        val ticketSoldReportReference = database.getReference("daily_sell_report")
                        ticketSoldReportReference.keepSynced(true)

                        ticketSoldReportReference.child(createTicketSoldReportID()!!).setValue(totalTicketSoldReportObj)
                            .addOnSuccessListener {
                                Toast.makeText(applicationContext, "Data Successfully Synced. ", Toast.LENGTH_LONG).show()
                                deleteTicketSoldReport()
                            }
                            .addOnFailureListener {
                                Toast.makeText(applicationContext, "Data Sync Failed\nPlease try again. ", Toast.LENGTH_LONG).show()
                            }
                    }
                }

                ticketSoldRef.removeEventListener(ticketSoldListener!!)

            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }

        }
        ticketSoldRef.addValueEventListener(ticketSoldListener!!)
    }

    private fun deleteTicketSoldReport() {
        val database = Firebase.database(ConstantValues.DB_URL)
        val ticketSoldRef = database.getReference("ticket_sold")
        ticketSoldRef.setValue(null)
    }

    @Throws(Exception::class)
    fun createTicketSoldReportID(): String? {
        return UUID.randomUUID().toString() + "_" + System.currentTimeMillis()
    }
}
