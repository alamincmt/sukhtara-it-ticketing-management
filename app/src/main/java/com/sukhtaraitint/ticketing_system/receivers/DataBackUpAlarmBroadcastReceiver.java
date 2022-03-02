package com.sukhtaraitint.ticketing_system.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import com.sukhtaraitint.ticketing_system.services.DataBackUpAlarmService;
import com.sukhtaraitint.ticketing_system.services.RescheduleDataBackUpAlarmsService;

public class DataBackUpAlarmBroadcastReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        if (Intent.ACTION_BOOT_COMPLETED.equals(intent.getAction())) {
            startRescheduleAlarmsService(context);
        }else {
            startAlarmService(context, intent);
        }
    }

    private void startAlarmService(Context context, Intent intent) {
        Intent intentService = new Intent(context, DataBackUpAlarmService.class);
//        intentService.putExtra("TITLE", intent.getStringExtra(TITLE));
        intentService.putExtra("TITLE", "Prayer Title ...");
        intentService.putExtra("requestCode", intent.getIntExtra("requestCode", 0));
        intentService.putExtra("PrayerName",  intent.getStringExtra("PrayerName"));
        intentService.putExtra("AlarmType",   intent.getStringExtra("AlarmType"));
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context.startForegroundService(intentService);
        } else {
            context.startService(intentService);
        }
    }

    private void startRescheduleAlarmsService(Context context) {
        try{
            Intent intentService = new Intent(context, RescheduleDataBackUpAlarmsService.class);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intentService);
            } else {
                context.startService(intentService);
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }
}
