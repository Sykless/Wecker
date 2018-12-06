package com.fpalud.wecker;

import android.app.Application;

import com.spotify.android.appremote.api.SpotifyAppRemote;

public class WeckerParameters  extends Application
{
    String spotifyToken = "";
    SpotifyAppRemote spotifyConnect;

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

    public void setSpotifyToken(String spotifyToken)
    {
        this.spotifyToken = spotifyToken;
    }

    // spotifyConnect.getPlayerApi().play("spotify:track:68osIGtVjM7QWVe6pazLHj");
}
