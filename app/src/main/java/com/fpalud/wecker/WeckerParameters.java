package com.fpalud.wecker;

import android.app.Application;

public class WeckerParameters  extends Application
{
    String spotifyToken = "";

    public String getSpotifyToken()
    {
        return spotifyToken;
    }

    public void setSpotifyToken(String spotifyToken)
    {
        this.spotifyToken = spotifyToken;
    }
}
