package com.fpalud.wecker;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;

import java.io.File;
import java.util.ArrayList;
import java.util.Calendar;

public class Alarm
{
    int hours;
    int minutes;

    ArrayList<Boolean> days = new ArrayList<>();
    ArrayList<String> idSongsList = new ArrayList<>();

    boolean active;
    boolean vibration;
    boolean emergencyAlarm;
    boolean randomPlaylist;
    boolean randomSong;

    File selectedSong = null;
    String selectedSongId = null;

    int id;

    Alarm()
    {
        setActive(true);
        id = (int) System.currentTimeMillis();
    }

    Alarm(int hours, int minutes, ArrayList<Boolean> days, ArrayList<String> idSongsList,
          boolean vibration, boolean emergencyAlarm, boolean randomPlaylist, boolean randomSong)
    {
        setHours(hours);
        setMinutes(minutes);
        setDays(days);

        setIdSongsList(idSongsList);

        setActive(true);
        setVibration(vibration);
        setEmergencyAlarm(emergencyAlarm);
        setRandomPlaylist(randomPlaylist);
        setRandomSong(randomSong);

        id = (int) System.currentTimeMillis();
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

    public boolean isRandomPlaylist() {
        return randomPlaylist;
    }
    public void setRandomPlaylist(boolean randomPlaylist) {
        this.randomPlaylist = randomPlaylist;
    }

    public boolean isRandomSong() {
        return randomSong;
    }
    public void setRandomSong(boolean randomSong) {
        this.randomSong = randomSong;
    }

    public File getSelectedSong() {
        return selectedSong;
    }
    public void setSelectedSong(File selectedSong) {
        this.selectedSong = selectedSong;
    }

    public String getSelectedSongId() {
        return selectedSongId;
    }
    public void setSelectedSongId(String selectedSongId) {
        this.selectedSongId = selectedSongId;
    }

    public int getId()
    {
        return id;
    }
}
