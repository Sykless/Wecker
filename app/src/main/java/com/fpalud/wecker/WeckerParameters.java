package com.fpalud.wecker;

import android.app.Application;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

import com.spotify.android.appremote.api.ConnectionParams;
import com.spotify.android.appremote.api.Connector;
import com.spotify.android.appremote.api.SpotifyAppRemote;

public class WeckerParameters  extends Application
{
    String spotifyToken = "";
    SpotifyAppRemote spotifyConnect;

    String musicFolderPath = "";

    @Override
    public void onCreate()
    {
        super.onCreate();

        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this);
        spotifyToken = sharedPrefs.getString("spotifyToken","");
        musicFolderPath = sharedPrefs.getString("musicFolderPath","");

        if (spotifyToken.length() > 0)
        {
            ConnectionParams connectionParams =
                    new ConnectionParams.Builder("46065021347f4ef3bd007487a2497d2f")
                            .setRedirectUri("wecker://callback")
                            .showAuthView(true)
                            .build();

            SpotifyAppRemote.connect(this, connectionParams, new Connector.ConnectionListener()
            {
                @Override
                public void onConnected(SpotifyAppRemote spotifyAppRemote)
                {
                    spotifyConnect = spotifyAppRemote;
                    SpotifyAppRemote.disconnect(spotifyConnect);
                }

                @Override
                public void onFailure(Throwable throwable)
                {
                    Log.e("OnCreateApplication", throwable.getMessage(), throwable);
                }
            });
        }
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
}
