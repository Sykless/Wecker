package com.fpalud.wecker;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class LaunchAlarm extends BroadcastReceiver
{
    @Override
    public void onReceive(Context context, Intent intent)
    {
        Intent alarmIntent = new Intent(context, AlarmScreen.class);
        alarmIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        alarmIntent.putExtra("alarmId",intent.getIntExtra("alarmId",-1));
        context.startActivity(alarmIntent);
    }
}