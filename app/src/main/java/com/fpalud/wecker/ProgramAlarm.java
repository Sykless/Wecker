package com.fpalud.wecker;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.util.ArrayList;

public class ProgramAlarm extends BroadcastReceiver
{
    @Override
    public void onReceive(Context context, Intent i)
    {
        System.out.println("Boot started");

        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(context);
        ArrayList<Alarm> alarmList = new Gson().fromJson(sharedPrefs.getString("alarmList", null), new TypeToken<ArrayList<Alarm>>() {}.getType());

        System.out.println("Boot loaded");

        if (alarmList != null)
        {
            System.out.println("Boot alarm setup");

            for (Alarm alarm : alarmList)
            {
                if (alarm.isActive())
                {
                    SetupAlarm.setupAlarm(alarm, context);
                }
            }
        }

        System.out.println("Boot completed");
    }
}