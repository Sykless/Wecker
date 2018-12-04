package com.fpalud.wecker;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.deezer.sdk.model.Playlist;
import com.deezer.sdk.model.Track;
import com.deezer.sdk.network.connect.DeezerConnect;
import com.deezer.sdk.network.connect.SessionStore;
import com.deezer.sdk.network.request.DeezerRequest;
import com.deezer.sdk.network.request.DeezerRequestFactory;
import com.deezer.sdk.network.request.event.JsonRequestListener;
import com.deezer.sdk.network.request.event.RequestListener;
import com.deezer.sdk.player.AlbumPlayer;
import com.deezer.sdk.player.TrackPlayer;
import com.deezer.sdk.player.networkcheck.WifiAndMobileNetworkStateChecker;

import java.util.ArrayList;
import java.util.List;

public class Deezer extends AppCompatActivity
{
    DeezerConnect deezerConnect;
    ArrayList<Long> playlistIdList = new ArrayList<>();
    long favoritePlaylistId = 0;

    Track test;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.deezer_layout);

        deezerConnect = new DeezerConnect(this, "315304");

        // restore any saved session
        SessionStore sessionStore = new SessionStore();

        if (!sessionStore.restore(deezerConnect, this))
        {
            System.out.println("ERROR : A traiter");
        }

        RequestListener listener = new JsonRequestListener()
        {
            public void onResult(Object result, Object requestId)
            {
                List<Playlist> playlistList = (List<Playlist>) result;

                for (Playlist playlist : playlistList)
                {
                    if (playlist.isLovedTracks())
                    {
                        RequestListener playlistListener = new JsonRequestListener()
                        {
                            public void onResult(Object result, Object requestId)
                            {
                                List<Track> trackList = (List<Track>) result;

                                for (Track track : trackList)
                                {
                                    playMusic(track.getId());
                                    break;
                                }
                            }

                            public void onUnparsedResult(String requestResponse, Object requestId) {}
                            public void onException(Exception e, Object requestId) {}
                        };

                        DeezerRequest request = DeezerRequestFactory.requestPlaylistTracks(playlist.getId());
                        deezerConnect.requestAsync(request, playlistListener);

                        break;
                    }
                }
            }

            public void onUnparsedResult(String requestResponse, Object requestId) {}
            public void onException(Exception e, Object requestId) {}
        };

        DeezerRequest request = DeezerRequestFactory.requestCurrentUserPlaylists();
        deezerConnect.requestAsync(request, listener);
    }

    void playMusic(long id)
    {
        System.out.println("Launch");

        try
        {
            System.out.println("Try");

            TrackPlayer trackPlayer = new TrackPlayer(getApplication(), deezerConnect, new WifiAndMobileNetworkStateChecker());
            trackPlayer.playTrack(id);

            System.out.println("Stopped");

            // trackPlayer.stop();
            // trackPlayer.release();
        }
        catch (Exception e)
        {
            System.out.println(e.getMessage());
        }
    }
}
