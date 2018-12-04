package com.fpalud.wecker;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

import com.deezer.sdk.model.Permissions;
import com.deezer.sdk.network.connect.DeezerConnect;
import com.deezer.sdk.network.connect.SessionStore;
import com.deezer.sdk.network.connect.event.DialogListener;

import java.util.Calendar;

public class Home extends AppCompatActivity
{
    DeezerConnect deezerConnect;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.home_layout);
    }

    public void goToDeezer()
    {
        Intent intent = new Intent(this, Deezer.class);
        this.startActivity(intent);
    }

    public void launchDeezer(View view)
    {
        deezerConnect = new DeezerConnect(this, "315304");

        // restore any saved session
        SessionStore sessionStore = new SessionStore();

        if (sessionStore.restore(deezerConnect, this))
        {
            goToDeezer();
        }
        else
        {
            String[] permissions = new String[] {
                    Permissions.BASIC_ACCESS,
                    Permissions.MANAGE_LIBRARY,
                    Permissions.LISTENING_HISTORY };

            // The listener for authentication events
            DialogListener listener = new DialogListener() {
                public void onComplete(Bundle values)
                {
                    SessionStore sessionStore = new SessionStore();
                    sessionStore.save(deezerConnect, getApplicationContext());

                    goToDeezer();
                }

                public void onCancel() {System.out.println("Canceled !");}
                public void onException(Exception e) {System.out.println("Exception !");}
            };

            // Launches the authentication process
            deezerConnect.authorize(this, permissions, listener);
        }
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
}
