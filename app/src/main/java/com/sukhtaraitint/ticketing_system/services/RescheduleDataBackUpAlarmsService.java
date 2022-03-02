package com.sukhtaraitint.ticketing_system.services;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import androidx.lifecycle.LifecycleService;

import com.sukhtaraitint.ticketing_system.R;
import com.sukhtaraitint.ticketing_system.utils.Variables;

public class RescheduleDataBackUpAlarmsService extends LifecycleService {

    public static final String ALARM_CHANNEL_ID = "ALARM_SERVICE_CHANNEL_NEW";

    @Override
    public void onCreate() {
        super.onCreate();
        if (Build.VERSION.SDK_INT >= 26) {
            String CHANNEL_ID = "my_channel_01";
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID,
                    "Channel human readable title",
                    NotificationManager.IMPORTANCE_DEFAULT);

            ((NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE)).createNotificationChannel(channel);

            Notification notification = new NotificationCompat.Builder(this, CHANNEL_ID)
                    .setContentTitle("")
                    .setContentText("").build();

            startForeground(1, notification);
        }

        notificationAndAlarmReschedule();
//        Log.d("AlarmBroadcastReceiver"," 1111........RescheduleAlarmsService");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);

        try{
            notificationAndAlarmReschedule();
        }catch (Exception e){
            e.printStackTrace();
        }

        return START_STICKY;
    }

    private void notificationAndAlarmReschedule() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            Notification notification = null;
            notification = new NotificationCompat.Builder(this, ALARM_CHANNEL_ID)
                    .setContentTitle("Prayer name")
                    .setContentText("Prayer content text")
                    .setSmallIcon(R.drawable.ic_launcher_foreground)
                    .setAutoCancel(true)
                    .setOngoing(false)
                    .setGroup(Variables.NOTIFICATION_GROUP_ID)
                    .setSound(null)
                    .build();

//            createAlarmNotificationChannnelWithoutSound();

            startForeground(0, notification);
            NotificationManager mNotificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
            mNotificationManager.cancel(0);
        }
    }

    private void setRebootAlarm(){
        /*AlarmManager alarmManager = (AlarmManager) getApplicationContext().getSystemService(ALARM_SERVICE);

        Intent intent = new Intent(getApplicationContext(), AlarmBroadcastReceiver.class);
        intent.putExtra("TITLE", "Prayer Title ...");
        intent.putExtra("requestCode", millis);
        intent.putExtra("PrayerName", prayerName);
        intent.putExtra("AlarmType", alarmType);

        PendingIntent alarmPendingIntent = PendingIntent.getBroadcast(getApplicationContext(), (int) millis, intent, PendingIntent.FLAG_IMMUTABLE);
//                            alarmManager.set(AlarmManager.RTC_WAKEUP, millis, alarmPendingIntent);

        //Show Alarm in Exact Time
        alarmManager.setWindow(AlarmManager.RTC_WAKEUP, millis,(100), alarmPendingIntent);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, millis, alarmPendingIntent);
        }else {
            alarmManager.setExact(AlarmManager.RTC_WAKEUP, millis, alarmPendingIntent);
        }*/
    }




    private void createAlarmNotificationChannnel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel serviceChannel = new NotificationChannel(ALARM_CHANNEL_ID, "Alarm Service Channel", NotificationManager.IMPORTANCE_LOW);
//            serviceChannel.setSound(null, null);
            NotificationManager manager = getSystemService(NotificationManager.class);
            manager.createNotificationChannel(serviceChannel);
        }
    }

    private void createAlarmNotificationChannnelWithoutSound() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel serviceChannel = new NotificationChannel(ALARM_CHANNEL_ID, "Alarm Service Channel", NotificationManager.IMPORTANCE_LOW);
            serviceChannel.setSound(null, null);
            NotificationManager manager = getSystemService(NotificationManager.class);
            manager.createNotificationChannel(serviceChannel);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        super.onBind(intent);
        return null;
    }
}
