package com.fpalud.wecker;

import android.content.Intent;
import android.media.MediaPlayer;
import android.os.AsyncTask;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.deezer.sdk.model.Track;
import com.deezer.sdk.network.connect.DeezerConnect;
import com.deezer.sdk.network.connect.SessionStore;
import com.deezer.sdk.network.request.DeezerRequest;
import com.deezer.sdk.network.request.event.JsonRequestListener;
import com.deezer.sdk.network.request.event.RequestListener;
import com.deezer.sdk.player.TrackPlayer;
import com.deezer.sdk.player.networkcheck.WifiAndMobileNetworkStateChecker;
import com.spotify.android.appremote.api.ConnectionParams;
import com.spotify.android.appremote.api.Connector;
import com.spotify.android.appremote.api.PlayerApi;
import com.spotify.android.appremote.api.SpotifyAppRemote;
import com.spotify.sdk.android.authentication.AuthenticationClient;
import com.spotify.sdk.android.authentication.AuthenticationRequest;
import com.spotify.sdk.android.authentication.AuthenticationResponse;

import org.apache.commons.io.FileUtils;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Random;

import static com.spotify.sdk.android.authentication.AuthenticationResponse.Type.TOKEN;

public class AlarmScreen extends BaseActivity
{
    int trackId = 0;
    String selectedPlaylistID = "";

    ArrayList<String> idList = new ArrayList<>();
    ArrayList<Integer> trackNumber = new ArrayList<>();
    Collection<File> musicList;

    WeckerParameters app;
    DeezerConnect deezerConnect;

    TrackPlayer trackPlayer;
    PlayerApi spotifyPlayer;
    MediaPlayer mediaPlayer;

    int id = 0;
    int offset = 0;
    int musicOrigin;

    boolean atLeastITriedSpotify = false;
    boolean atLeastITriedAPKSpotify = false;
    boolean spotifyConnectionChecked = false;
    boolean spotifyAPKConnected = false;

    private static final int REQUEST_CODE = 1337;
    private static final String REDIRECT_URI = "wecker://callback";

    private static final int DEEZER = 0;
    private static final int SPOTIFY = 1;
    private static final int FOLDER = 2;

    final String[] SUFFIX = {"mp3","wma","3gp","mp4","m4a","aac","flac","gsm","mkv","wav","ogg","ts"};  // use the suffix to filter

    RelativeLayout relativeLayout;
    ImageView validateImage;
    Circle hideCircle;
    CircleAngleAnimation animation;

    int validateSize;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.wake_up_screen_layout);

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON);

        app = (WeckerParameters) getApplicationContext();
/*
        for (Alarm alarm : app.getAlarmList())
        {
            if (alarm.getId() == getIntent().getIntExtra("alarmId",0))
            {
                idList = alarm.getIdSongsList();
                break;
            }
        }*/

        validateImage = findViewById(R.id.validateImage);
        // relativeLayout = findViewById(R.id.relativeLayout);

        ViewTreeObserver vto = validateImage.getViewTreeObserver();
        vto.addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener()
        {
            public boolean onPreDraw()
            {
                validateImage.getViewTreeObserver().removeOnPreDrawListener(this);
                validateSize = validateImage.getMeasuredHeight();

                createCircle();

                return true;
            }
        });

        if (idList.size() > 0)
        {
            // getTrackLists();
        }
    }

    public void createCircle()
    {
        // Circle circle = new Circle(this, validateSize, , 360);
        // relativeLayout.addView(circle, new RelativeLayout.LayoutParams(validateSize, validateSize));
    }

    public void hideCircle()
    {
        hideCircle = new Circle(this, validateSize, false);
        relativeLayout.addView(hideCircle, new RelativeLayout.LayoutParams(validateSize, validateSize));

        System.out.println(hideCircle.getAngle());

        animation = new CircleAngleAnimation(hideCircle, 360);
        animation.setDuration(1000);
        hideCircle.startAnimation(animation);
    }

    public void clickPause(View view)
    {
        if (musicOrigin == DEEZER)
        {
            trackPlayer.stop();
        }
        else if (musicOrigin == SPOTIFY)
        {
            spotifyPlayer.pause();
            SpotifyAppRemote.disconnect(app.getSpotifyConnect());
        }
        else if (musicOrigin == FOLDER)
        {
            mediaPlayer.stop();
        }
    }

    public void getTrackLists()
    {
        app = (WeckerParameters) getApplicationContext();
        String playlistId = idList.get(id);
        System.out.println(playlistId);

        if ("folderMusic".equals(playlistId)) // Music folder
        {
            File rootDir = new File(app.getMusicFolderPath());
            musicList = FileUtils.listFiles(rootDir, SUFFIX, true);
            trackNumber.add(musicList.size());

            newTrackList(true);
        }
        else if (playlistId.length() <= 10) // Deezer playlist
        {
            deezerConnect = DeezerConnect.forApp("315304").build();
            SessionStore sessionStore = new SessionStore();

            if (sessionStore.restore(deezerConnect, this))
            {
                RequestListener listener = new JsonRequestListener()
                {
                    public void onResult(Object result, Object requestId)
                    {
                        List<Track> trackList = (List<Track>) result;

                        trackNumber.add(trackList.size());
                        newTrackList(true);
                    }

                    public void onUnparsedResult(String requestResponse, Object requestId) {newTrackList(false);}
                    public void onException(Exception e, Object requestId) {newTrackList(false);}
                };

                Bundle bundle = new Bundle(1);
                bundle.putString("limit","2000");
                DeezerRequest request = new DeezerRequest("playlist/" + playlistId + "/tracks", bundle);
                deezerConnect.requestAsync(request, listener);
            }
            else
            {
                System.out.println("Deezer not connected");
                newTrackList(false);
            }
        }
        else // Spotify playlist
        {
            System.out.println("Trying Spotify");

            if (!spotifyAPKConnected)
            {
                System.out.println("Connected null");

                if (atLeastITriedAPKSpotify)
                {
                    System.out.println("AT LEAST I TRIED APK");
                    newTrackList(false);
                }
                else
                {
                    System.out.println("Trying APK connected");
                    spotifyAPKConnect();
                }
            }
            else
            {
                System.out.println("Connected has a value");

                if (spotifyConnectionChecked)
                {
                    System.out.println("Connection checked");

                    if ("spotifyLovedSongs".equals(playlistId))
                    {
                        System.out.println("Executing favorites");
                        new SpotifyCrawler().execute("me/tracks");
                    }
                    else
                    {
                        System.out.println("Executing other playlists");
                        new SpotifyCrawler().execute("playlists/" + playlistId);
                    }
                }
                else
                {
                    System.out.println("Connection not checked");

                    if (atLeastITriedSpotify)
                    {
                        System.out.println("AT LEAST I TRIED");
                        newTrackList(false);
                    }
                    else
                    {
                        System.out.println("Testing connection");
                        new SpotifyCrawler().execute("me");
                    }
                }
            }
        }
    }

    public void newTrackList(boolean connection)
    {
        if (!connection)
        {
            trackNumber.add(0);
        }

        if (id + 1 != idList.size())
        {
            id++;
            getTrackLists();
        }
        else
        {
            System.out.println(trackNumber);
            ArrayList<Integer> trackThreasholds = new ArrayList<>(trackNumber);

            for (int i = trackThreasholds.size() - 1 ; i >= 0 ; i--)
            {
                int sum = 0;

                for (int j = i ; j >= 0 ; j--)
                {
                    sum += trackThreasholds.get(j);
                }

                trackThreasholds.set(i,sum);
            }

            int randomId = new Random().nextInt((trackThreasholds.get(trackThreasholds.size() - 1)));

            System.out.println(trackThreasholds);
            System.out.println("Random id : " + randomId);

            for (int i = 0 ; i < trackThreasholds.size() ; i++)
            {
                if (trackThreasholds.get(i) > randomId)
                {
                    selectedPlaylistID = idList.get(i);

                    if (i == 0)
                    {
                        trackId = randomId;
                    }
                    else
                    {
                        trackId = randomId - trackThreasholds.get(i - 1);
                    }

                    break;
                }
            }

            app = (WeckerParameters) getApplicationContext();
            System.out.println(selectedPlaylistID);
            System.out.println(trackId);

            if ("folderMusic".equals(selectedPlaylistID))
            {
                mediaPlayer = new MediaPlayer();

                try
                {
                    mediaPlayer.setDataSource(((File) musicList.toArray()[trackId]).getAbsolutePath());
                    mediaPlayer.prepare();
                    mediaPlayer.start();

                    musicOrigin = FOLDER;
                }
                catch (Exception e)
                {
                    e.printStackTrace();
                }
            }
            else if (selectedPlaylistID.length() <= 10)
            {
                RequestListener listener = new JsonRequestListener()
                {
                    public void onResult(Object result, Object requestId)
                    {
                        List<Track> trackList = (List<Track>) result;

                        try
                        {
                            trackPlayer = new TrackPlayer(getApplication(), deezerConnect, new WifiAndMobileNetworkStateChecker());
                            trackPlayer.playTrack(trackList.get(trackId).getId());

                            musicOrigin = DEEZER;

                            // trackPlayer.stop();
                            // trackPlayer.release();
                        }
                        catch (Exception e)
                        {
                            System.out.println(e.getMessage());
                        }
                    }

                    public void onUnparsedResult(String requestResponse, Object requestId) {newTrackList(false);}
                    public void onException(Exception e, Object requestId) {newTrackList(false);}
                };

                Bundle bundle = new Bundle(1);
                bundle.putString("limit","2000");
                DeezerRequest request = new DeezerRequest("playlist/" + selectedPlaylistID + "/tracks", bundle);
                deezerConnect.requestAsync(request, listener);
            }
            else
            {
                System.out.println("SYSTEM LAUNCHING");

                if ("spotifyLovedSongs".equals(selectedPlaylistID))
                {
                    while (trackId >= 50)
                    {
                        trackId -= 50;
                        offset += 50;
                    }

                    new SpotifyCrawler().execute("me/tracks",Integer.toString(offset));
                }
                else
                {
                    while (trackId >= 100)
                    {
                        trackId -= 100;
                        offset += 100;
                    }

                    new SpotifyCrawler().execute("playlists/" + selectedPlaylistID + "/tracks",Integer.toString(offset));
                }
            }
        }
    }

    public class SpotifyCrawler extends AsyncTask<String, Void, String>
    {
        JSONObject server_response = new JSONObject();
        String endpoint = "";
        String offset = "0";
        String limit = "100";
        boolean offsetNull = true;

        @Override
        protected String doInBackground(String... strings)
        {
            try
            {
                app = (WeckerParameters) getApplicationContext();

                endpoint = strings[0];
                if (strings.length > 1)
                {
                    offset = strings[1];
                    offsetNull = false;
                }

                if ("me/tracks".equals(endpoint))
                {
                    limit = "50";
                }

                URL url = new URL("https://api.spotify.com/v1/" + endpoint + "?limit=" + limit + "&offset=" + offset);
                HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestProperty("Authorization", "Bearer " + app.getSpotifyToken());
                urlConnection.setRequestMethod("GET");

                if (urlConnection.getResponseCode() == HttpURLConnection.HTTP_OK)
                {
                    server_response = readStream(urlConnection.getInputStream());
                }
            }
            catch (MalformedURLException e)
            {
                System.out.println("MalformedURLException : " + e.getMessage());
                newTrackList(false);
            }
            catch (IOException e)
            {
                System.out.println("IOException : " + e.getMessage());
                newTrackList(false);
            }

            return null;
        }

        @Override
        protected void onPostExecute(String s)
        {
            super.onPostExecute(s);

            System.out.println(server_response);

            if (spotifyConnectionChecked)
            {
                if (server_response.length() == 0)
                {
                    System.out.println("Spotify not connected");
                }
                else if (offsetNull)
                {
                    if ("me/tracks".equals(endpoint))
                    {
                        try
                        {
                            String size = server_response.getString("total");

                            if (size != null)
                            {
                                trackNumber.add(Integer.valueOf(size));
                                newTrackList(true);
                            }
                            else
                            {
                                newTrackList(false);
                            }
                        }
                        catch (Exception e)
                        {
                            System.out.println(e.getMessage());
                            newTrackList(false);
                        }
                    }
                    else
                    {
                        try
                        {
                            JSONObject tracks = (JSONObject) server_response.get("tracks");

                            if (tracks != null)
                            {
                                trackNumber.add(Integer.valueOf(tracks.getString("total")));
                                newTrackList(true);
                            }
                            else
                            {
                                newTrackList(false);
                            }
                        }
                        catch (Exception e)
                        {
                            System.out.println(e.getMessage());
                            newTrackList(false);
                        }
                    }
                }
                else
                {
                    try
                    {
                        System.out.println("Launching music");

                        JSONArray trackJSONList = (JSONArray) server_response.get("items");

                        if (trackJSONList != null)
                        {
                            System.out.println(((JSONObject) trackJSONList.optJSONObject(trackId).get("track")).get("id"));

                            app = (WeckerParameters) getApplicationContext();
                            spotifyPlayer = app.getSpotifyConnect().getPlayerApi();
                            spotifyPlayer.play("spotify:track:" + ((JSONObject) trackJSONList.optJSONObject(trackId).get("track")).get("id"));

                            musicOrigin = SPOTIFY;
                        }
                    }
                    catch (Exception e)
                    {
                        System.out.println(e.getMessage());
                    }
                }
            }
            else
            {
                atLeastITriedSpotify = true;

                if (server_response.length() == 0)
                {
                    spotifyConnect();
                }
                else
                {
                    spotifyConnectionChecked = true;
                    getTrackLists();
                }
            }
        }

        JSONObject readStream(InputStream in)
        {
            BufferedReader reader = null;
            StringBuffer response = new StringBuffer();

            try
            {
                reader = new BufferedReader(new InputStreamReader(in));
                String line = "";

                while ((line = reader.readLine()) != null)
                {
                    response.append(line);
                }
            }
            catch (IOException e)
            {
                e.printStackTrace();
                newTrackList(false);
            }
            finally
            {
                if (reader != null)
                {
                    try
                    {
                        reader.close();
                    }
                    catch (IOException e)
                    {
                        e.printStackTrace();
                        newTrackList(false);
                    }
                }
            }

            try
            {
                return new JSONObject(response.toString());
            }
            catch (Throwable t)
            {
                System.out.println("Could not parse malformed JSON");
                newTrackList(false);
            }

            return null;
        }
    }

    public void spotifyConnect()
    {
        AuthenticationRequest.Builder builder =
                new AuthenticationRequest.Builder("46065021347f4ef3bd007487a2497d2f", TOKEN, REDIRECT_URI);

        builder.setScopes(new String[]{"streaming","user-library-read"});
        AuthenticationRequest request = builder.build();

        AuthenticationClient.openLoginActivity(this, REQUEST_CODE, request);
    }

    protected void onActivityResult(final int requestCode, final int resultCode, final Intent intent)
    {
        super.onActivityResult(requestCode, resultCode, intent);

        if (requestCode == REQUEST_CODE)
        {
            AuthenticationResponse response = AuthenticationClient.getResponse(resultCode, intent);

            if (response.getType() == TOKEN)
            {
                app = (WeckerParameters) getApplicationContext();
                app.setSpotifyToken(response.getAccessToken());

                spotifyConnectionChecked = true;

                getTrackLists();
            }
            else
            {
                System.out.println("Connection failed : " + response.getError());
                newTrackList(false);
            }
        }
    }

    public void spotifyAPKConnect()
    {
        atLeastITriedAPKSpotify = true;

        ConnectionParams connectionParams =
                new ConnectionParams.Builder("46065021347f4ef3bd007487a2497d2f")
                        .setRedirectUri(REDIRECT_URI)
                        .showAuthView(true)
                        .build();

        SpotifyAppRemote.connect(this, connectionParams,
                new Connector.ConnectionListener()
                {
                    @Override
                    public void onConnected(SpotifyAppRemote spotifyAppRemote)
                    {
                        app.setSpotifyConnect(spotifyAppRemote);
                        spotifyAPKConnected = true;
                        getTrackLists();
                    }

                    @Override
                    public void onFailure(Throwable throwable)
                    {
                        Log.e("MusicOrigin", throwable.getMessage(), throwable);
                        newTrackList(false);
                    }
                });
    }
}
