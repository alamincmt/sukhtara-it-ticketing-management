package com.sukhtaraitint.ticketing_system.services;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.RingtoneManager;
import android.os.Build;
import android.os.IBinder;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

public class AlarmService extends Service {
    private MediaPlayer mediaPlayer;
    //    private Vibrator vibrator;

    private SharedPrefData sharedPrefData;

    public static final String ALARM_CHANNEL_ID = "ALARM_SERVICE_CHANNEL_NEW";

    @Override
    public void onCreate() {
        sharedPrefData = new SharedPrefData(getApplicationContext());
        Variables.PRAYER_ALARM_ONGOING = true;
        try{
            if(SuraListFragment.getInstance() != null){
                if(SuraListFragment.getInstance().suraListHandler != null){
                    SuraListFragment.getInstance().suraListHandler.adhanAlarmUpdateUI();
                }
            }
        }catch (Exception exception){
            exception.printStackTrace();
        }
        super.onCreate();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        sharedPrefData = new SharedPrefData(getApplicationContext());
        Variables.PRAYER_ALARM_ONGOING = true;

        try{
            if(SuraListFragment.getInstance() != null){
                SuraListFragment.getInstance().suraListHandler.adhanAlarmUpdateUI();
            }

            Intent notificationIntent = new Intent(this, PrayerAlarmActivity.class);
            int alarmID = intent.getIntExtra("requestCode", 0);
            String PrayerName = intent.getStringExtra("PrayerName");
            String AlarmType = intent.getStringExtra("AlarmType");

            notificationIntent.putExtra("requestCode", intent.getIntExtra("requestCode", PendingIntent.FLAG_IMMUTABLE));
            notificationIntent.putExtra("AlarmType", intent.getStringExtra("AlarmType"));
            notificationIntent.putExtra("PrayerName", intent.getStringExtra("PrayerName"));

            PendingIntent pendingIntent = PendingIntent.getActivity(this, alarmID, notificationIntent, PendingIntent.FLAG_IMMUTABLE);

            String alarmTitle = intent.getStringExtra("PrayerName") + " এর সময় হয়েছে ";

            AudioManager am;
            if(AlarmType != null && PrayerName != null){
                try{
                    if(AlarmType.equals("notification")){
                        mediaPlayer = new MediaPlayer();
                        am = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
                        am.setStreamVolume(AudioManager.STREAM_RING, am.getStreamMaxVolume(AudioManager.STREAM_RING), 0);
                        mediaPlayer = MediaPlayer.create(getApplicationContext(), RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION));
                        mediaPlayer.start();
                    }else if(AlarmType.equals("adhan_alarm")){
                        if(PrayerName.trim().equals("ফজর")){
                            mediaPlayer = new MediaPlayer();
                            am = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
                            am.setStreamVolume(AudioManager.STREAM_RING, am.getStreamMaxVolume(AudioManager.STREAM_RING), 0);
                            mediaPlayer = MediaPlayer.create(getApplicationContext(), R.raw.adhan_fajr_trimmed);
                            mediaPlayer.start();
                        }else if(PrayerName.trim().equals("যোহর") ||PrayerName.trim().equals("আসর") ||PrayerName.trim().equals("মাগরিব") || PrayerName.trim().equals("এশা")){
                            mediaPlayer = new MediaPlayer();
                            am = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
                            am.setStreamVolume(AudioManager.STREAM_RING, am.getStreamMaxVolume(AudioManager.STREAM_RING), 0);
                            mediaPlayer = MediaPlayer.create(getApplicationContext(), R.raw.adhan_trimmed);
                            mediaPlayer.start();
                        }
                    }
                    String message ="Close_Service";

                    if(mediaPlayer != null){
                        mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                            @Override
                            public void onCompletion(MediaPlayer mp) {
                                Variables.PRAYER_ALARM_ONGOING = false;
                                if(SuraListFragment.getInstance() != null){
                                    SuraListFragment.getInstance().suraListHandler.adhanAlarmUpdateUI();
                                }

                                stopForeground(true);
                                if(mediaPlayer != null){
                                    mediaPlayer.stop();
                                }
                            }
                        });
                    }
                }catch (Exception exception){
                    exception.printStackTrace();
                }

                Notification notification = null;
                if(PrayerName.trim().equals("ফজর") ||
                        PrayerName.trim().equals("যোহর") ||
                        PrayerName.trim().equals("আসর") ||
                        PrayerName.trim().equals("মাগরিব") ||
                        PrayerName.trim().equals("এশা")){

                    notification = new NotificationCompat.Builder(this, ALARM_CHANNEL_ID)
                            .setContentTitle(PrayerName)
                            .setContentText(alarmTitle)
                            .setSmallIcon(R.drawable.ic_alarm_black_24dp)
                            .setContentIntent(pendingIntent)
                            .setColor(Color.RED)
                            .setAutoCancel(true)
                            .setOngoing(false)
                            .setGroup(Variables.NOTIFICATION_GROUP_ID)
                            .setSound(null)
                            .build();

                    createAlarmNotificationChannnelWithoutSound();

                }else{
                    createAlarmNotificationChannnel();

                    notification = new NotificationCompat.Builder(this, ALARM_CHANNEL_ID)
                            .setContentTitle(PrayerName)
                            .setContentText(alarmTitle)
                            .setSmallIcon(R.drawable.ic_alarm_black_24dp)
                            .setContentIntent(pendingIntent)
                            .setAutoCancel(true)
                            .setGroup(Variables.NOTIFICATION_GROUP_ID)
                            .setOngoing(false)
                            .build();

                }

                startForeground(1, notification);
            }else{
                stopForeground(true);
            }
        }catch (Exception exception){
            exception.printStackTrace();
        }
        return START_STICKY;
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
        stopForeground(true);
        if(mediaPlayer != null){
            mediaPlayer.stop();
        }
//        vibrator.cancel();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
