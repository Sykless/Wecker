package com.fpalud.wecker;

import android.app.Application;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import com.spotify.android.appremote.api.SpotifyAppRemote;

import java.io.File;
import java.util.ArrayList;

public class WeckerParameters  extends Application
{
    String spotifyToken;
    SpotifyAppRemote spotifyConnect;
    String musicFolderPath;

    File defaultTrack;
    ArrayList<Alarm> alarmList;

    boolean deezerChecked;
    boolean spotifyChecked;
    boolean folderChecked;
    boolean vibrationChillMode;

    int alarmVolume;

    @Override
    public void onCreate()
    {
        super.onCreate();

        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this);
        spotifyToken = sharedPrefs.getString("spotifyToken","");
        musicFolderPath = sharedPrefs.getString("musicFolderPath","");
        alarmVolume = sharedPrefs.getInt("alarmVolume",5);
        defaultTrack = new File(sharedPrefs.getString("defaultTrack",""));
        deezerChecked = sharedPrefs.getBoolean("deezerChecked", false);
        spotifyChecked = sharedPrefs.getBoolean("spotifyChecked", false);
        folderChecked = sharedPrefs.getBoolean("folderChecked", false);
        vibrationChillMode = sharedPrefs.getBoolean("vibrationChillMode", true);
        alarmList = new Gson().fromJson(sharedPrefs.getString("alarmList", null), new TypeToken<ArrayList<Alarm>>() {}.getType());

        if (alarmList == null)
        {
            alarmList = new ArrayList<>();
        }
    }

    public File getDefaultTrack()
    {
        return defaultTrack;
    }

    public void setDefaultTrack(File defaultTrack)
    {
        this.defaultTrack = defaultTrack;

        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor editor = sharedPrefs.edit();
        editor.putString("defaultTrack",defaultTrack.getAbsolutePath());
        editor.apply();
    }

    public SpotifyAppRemote getSpotifyConnect()
    {
        return spotifyConnect;
    }

    public void setSpotifyConnect(SpotifyAppRemote spotifyConnect)
    {
        this.spotifyConnect = spotifyConnect;
    }

    public String getSpotifyToken()
    {
        return spotifyToken;
    }

    public String getMusicFolderPath()
    {
        return musicFolderPath;
    }

    public void setMusicFolderPath(String musicFolderPath)
    {
        this.musicFolderPath = musicFolderPath;

        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor editor = sharedPrefs.edit();
        editor.putString("musicFolderPath",musicFolderPath);
        editor.apply();
    }

    public void setSpotifyToken(String spotifyToken)
    {
        this.spotifyToken = spotifyToken;

        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor editor = sharedPrefs.edit();
        editor.putString("spotifyToken",spotifyToken);
        editor.apply();
    }

    public ArrayList<Alarm> getAlarmList()
    {
        return alarmList;
    }

    public void setAlarmList(ArrayList<Alarm> alarmList)
    {
        this.alarmList = alarmList;

        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor editor = sharedPrefs.edit();
        editor.putString("alarmList", new Gson().toJson(alarmList));
        editor.apply();
    }

    public void addAlarm(Alarm alarm)
    {
        alarmList.add(alarm);

        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor editor = sharedPrefs.edit();
        editor.putString("alarmList", new Gson().toJson(alarmList));
        editor.apply();
    }

    public boolean isDeezerChecked()
    {
        return deezerChecked;
    }

    public void setDeezerChecked(boolean deezerChecked)
    {
        this.deezerChecked = deezerChecked;

        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor editor = sharedPrefs.edit();
        editor.putBoolean("deezerChecked", deezerChecked);
        editor.apply();
    }

    public boolean isSpotifyChecked()
    {
        return spotifyChecked;
    }

    public void setSpotifyChecked(boolean spotifyChecked)
    {
        this.spotifyChecked = spotifyChecked;

        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor editor = sharedPrefs.edit();
        editor.putBoolean("spotifyChecked", spotifyChecked);
        editor.apply();
    }

    public boolean isFolderChecked()
    {
        return folderChecked;
    }

    public void setFolderChecked(boolean folderChecked)
    {
        this.folderChecked = folderChecked;

        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor editor = sharedPrefs.edit();
        editor.putBoolean("folderChecked", folderChecked);
        editor.apply();
    }

    public int getAlarmVolume()
    {
        return alarmVolume;
    }

    public void setAlarmVolume(int alarmVolume)
    {
        this.alarmVolume = alarmVolume;

        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor editor = sharedPrefs.edit();
        editor.putInt("alarmVolume", alarmVolume);
        editor.apply();
    }

    public boolean isVibrationChillMode()
    {
        return vibrationChillMode;
    }

    public void setVibrationChillMode(boolean vibrationChillMode)
    {
        this.vibrationChillMode = vibrationChillMode;

        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor editor = sharedPrefs.edit();
        editor.putBoolean("vibrationChillMode", vibrationChillMode);
        editor.apply();
    }
}
