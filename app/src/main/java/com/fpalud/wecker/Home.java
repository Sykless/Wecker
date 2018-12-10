package com.fpalud.wecker;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import java.util.Calendar;

public class Home extends BaseActivity
{
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.home_layout);

        Intent intent = getIntent();
        System.out.println(intent.getStringArrayListExtra("idList"));
    }

    public void goToCreate(View view)
    {
        Intent intent = new Intent(this, MusicOrigin.class);
        startActivity(intent);
    }

    public void setAlarm()
    {
        AlarmManager am = (AlarmManager) this.getSystemService(Context.ALARM_SERVICE);

        Calendar futureDate = Calendar.getInstance();
        futureDate.set(Calendar.MINUTE,futureDate.get(Calendar.MINUTE) + 1);

        Intent intent = new Intent(this, LaunchAlarm.class);
        PendingIntent sender = PendingIntent.getBroadcast(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);

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

    @Override
    protected void onLeaveThisActivity()
    {
        // Don't use an exit animation when leaving the main activity!
    }
}
