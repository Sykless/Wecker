package com.fpalud.wecker;

import java.util.ArrayList;

public class Alarm
{
    int hours;
    int minutes;
    ArrayList<Boolean> days = new ArrayList<>();
    ArrayList<String> idSongsList = new ArrayList<>();
    boolean vibration;
    boolean emergencyAlarm;
    boolean active;
    int id;

    Alarm()
    {
        setActive(true);
        id = (int) System.currentTimeMillis();
    }

    Alarm(int hours, int minutes, ArrayList<Boolean> days, ArrayList<String> idSongsList, boolean vibration, boolean emergencyAlarm)
    {
        setHours(hours);
        setMinutes(minutes);
        setDays(days);
        setIdSongsList(idSongsList);
        setVibration(vibration);
        setEmergencyAlarm(emergencyAlarm);
        setActive(true);
        id = (int) System.currentTimeMillis();

        /*AlarmManager am = (AlarmManager) this.getSystemService(Context.ALARM_SERVICE);

        Calendar futureDate = Calendar.getInstance();
        futureDate.set(Calendar.SECOND,futureDate.get(Calendar.SECOND) + 10);

        Intent intent = new Intent(this, LaunchAlarm.class);
        PendingIntent sender = PendingIntent.getBroadcast(this, id, intent, PendingIntent.FLAG_UPDATE_CURRENT);

        if (android.os.Build.VERSION.SDK_INT >= 19)
        {
            System.out.println("Launch api >= 19");
            am.setExact(AlarmManager.RTC_WAKEUP, futureDate.getTimeInMillis(), sender);
        }
        else {
            System.out.println("Launch api < 19");
            am.set(AlarmManager.RTC_WAKEUP, futureDate.getTimeInMillis(), sender);
        }*/
    }

    public int getHours()
    {
        return hours;
    }
    public void setHours(int hours)
    {
        this.hours = hours;
    }

    public int getMinutes()
    {
        return minutes;
    }
    public void setMinutes(int minutes)
    {
        this.minutes = minutes;
    }

    public ArrayList<Boolean> getDays()
    {
        return days;
    }
    public void setDays(ArrayList<Boolean> days)
    {
        this.days = days;
    }

    public ArrayList<String> getIdSongsList()
    {
        return idSongsList;
    }
    public void setIdSongsList(ArrayList<String> idSongsList)
    {
        this.idSongsList = idSongsList;
    }

    public boolean isVibration()
    {
        return vibration;
    }
    public void setVibration(boolean vibration)
    {
        this.vibration = vibration;
    }

    public boolean isActive()
    {
        return active;
    }
    public void setActive(boolean active)
    {
        this.active = active;
    }

    public boolean isEmergencyAlarm()
    {
        return emergencyAlarm;
    }
    public void setEmergencyAlarm(boolean emergencyAlarm)
    {
        this.emergencyAlarm = emergencyAlarm;
    }

    public int getId()
    {
        return id;
    }
}
