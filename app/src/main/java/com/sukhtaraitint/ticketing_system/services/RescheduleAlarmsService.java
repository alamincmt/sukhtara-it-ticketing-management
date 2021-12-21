package com.sukhtaraitint.ticketing_system.services;
import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import androidx.lifecycle.LifecycleService;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ProcessLifecycleOwner;

import com.technobd.alquran_v4.R;
import com.technobd.alquran_v4.common.model.PrayerTime;
import com.technobd.alquran_v4.common.receivers.AlarmBroadcastReceiver;
import com.technobd.alquran_v4.common.utils.AlQuranApplication;
import com.technobd.alquran_v4.common.utils.Constants;
import com.technobd.alquran_v4.common.utils.TextUtils;
import com.technobd.alquran_v4.common.utils.Variables;
import com.technobd.alquran_v4.db.AlQuranDatabase;
import com.technobd.alquran_v4.db.dao.PrayerTimeDao;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class  RescheduleAlarmsService extends LifecycleService {

    public static final String ALARM_CHANNEL_ID = "ALARM_SERVICE_CHANNEL_NEW";
    int i = 0;
    private AlQuranApplication alQuranApplication;

    @Override
    public void onCreate() {
        super.onCreate();
        alQuranApplication = (AlQuranApplication) getApplication();
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
                    .setSmallIcon(R.drawable.ic_alarm_black_24dp)
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

        PrayerTimeDao prayerTimeDao = AlQuranDatabase.getInstance(getApplicationContext()).prayerTimeDao();

        prayerTimeDao.getAllPrayerTimeByAlarmStatus(true).observe(ProcessLifecycleOwner.get(), new Observer<List<PrayerTime>>() {
            @Override
            public void onChanged(List<PrayerTime> prayerTimes) {

                for (PrayerTime prayerTime : prayerTimes) {

                    if (prayerTime.isActiveStatus()) {
//                        Log.d("AlarmBroadcastReceiver"," 1111........prayerTime.getPrayerName(): "+prayerTime.getPrayerName());
                        setRebootAlarm(prayerTime);
//                        prayerTime.schedule(getApplicationContext(), preferenceData.getStringValue(prayerTime.getPrayerName()));
                    }
                }


            }
        });
    }

    private void setRebootAlarm(PrayerTime prayerTimeObj){
                    String alarmType = "adhan_alarm";
                    String prayerName = prayerTimeObj.getPrayerName();
                    String prayerTime = prayerTimeObj.getPrayerTime();
                    String alarmUUID = prayerTimeObj.getAlarmUUID();
                    Date alarmTime = prayerTimeObj.getAlarmTime();
                    String date = prayerTimeObj.getDate();
                    String districtId = prayerTimeObj.getDistrictId();
                    boolean activeStatus = prayerTimeObj.isActiveStatus();

                    if(prayerName.equals("ফজর")||prayerName.equals("যোহর")||prayerName.equals("আসর")||prayerName.equals("মাগরিব")||prayerName.equals("এশা")){
                        alarmType = "adhan_alarm";
                        if(alQuranApplication.appConfiguration.getPrayerTimeStringData(prayerName)!=null && !alQuranApplication.appConfiguration.getPrayerTimeStringData(prayerName).equals("")){
                            alarmType = alQuranApplication.appConfiguration.getPrayerTimeStringData(prayerName);
                        }
                    }else if(prayerName.equals("সূর্যোদয়")||prayerName.equals("সূর্যাস্ত")||prayerName.equals("ইমসাক")||prayerName.equals("মধ্যরাত")){
                        alarmType = "notification";
                    }
                    alQuranApplication.appConfig().set(Constants.PRAYER_TIME_PARENT_KEY, prayerTimeObj.getPrayerName(), alarmType);

                    String[] splitStr ;
                    String timestamp;
                    long millis;

//                    if(i== 0 && prayerName.equals("ফজর")){
//                        timestamp  = "16/03/2021"+ " " + "15:40";
//                        i = i+1;
//                    } else {
//                        splitStr = prayerTime.split("\\s+");
//                        timestamp  = TextUtils.banglaToEnglish(date)+ " " + splitStr[0];
//                    }

                    splitStr = prayerTime.split("\\s+");
                    timestamp  = TextUtils.banglaToEnglish(date)+ " " + splitStr[0];


                    SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm");
                    Date dateP = null;
                    try {
                        dateP = sdf.parse(timestamp);
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }
                    millis = dateP.getTime();

                    long current_time = System.currentTimeMillis();


//                   Log.d("AlarmBroadcastReceiver", " diff: "+(millis-current_time));

                    if(millis > current_time){
//                        Log.d("AlarmBroadcastReceiver", prayerName+" - Type : "+alarmType + " prayerTimeArrayList: ");


//                        if(!prayerTimeObj.isActiveStatus()){
//                            Log.d("AlarmBroadcastReceiver", " millis: "+ millis);
                            // Alarm set
                            AlarmManager alarmManager = (AlarmManager) getApplicationContext().getSystemService(ALARM_SERVICE);

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
                            }

                            //Update Room database
                            prayerTimeObj.setAlarmUUID(String.valueOf(millis));
                            prayerTimeObj.setActiveStatus(true);

//                            prayerTimeViewModel.update(prayerTimeObj);

//                            Log.d("AlarmBroadcastReceiver","showTimeChooser ::: prayerName: "+prayerName + " prayerTime: "+prayerTime
//                                    +" alarmUUID: "+ alarmUUID+ " alarmTime: "+ alarmTime
//                                    +" date: "+ date+" districtId: "+districtId+" activeStatus: "+activeStatus +" totalTime: ");


//                    }


                }

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
