package com.fpalud.wecker;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import java.util.Calendar;

public class Home extends AppCompatActivity
{
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.home_layout);

        Context context = getApplicationContext();
        AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

        Calendar futureDate = Calendar.getInstance();

        System.out.println("Current date : " + futureDate.getTime());
        futureDate.set(Calendar.MINUTE,futureDate.get(Calendar.MINUTE) + 1);
        System.out.println("Future date : " + futureDate.getTime());

        Intent intent = new Intent(context, LaunchAlarm.class);
        PendingIntent sender = PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);

        if (android.os.Build.VERSION.SDK_INT >= 19)
        {
            System.out.println("Launch api >= 19");
            am.setExact(AlarmManager.RTC_WAKEUP, futureDate.getTimeInMillis(), sender);
        }
        else {
            System.out.println("Launch api < 19");
            am.set(AlarmManager.RTC_WAKEUP, futureDate.getTimeInMillis(), sender);
        }
    }
}
