package com.fpalud.wecker;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.media.MediaPlayer;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.deezer.sdk.model.Playlist;
import com.deezer.sdk.model.Track;
import com.deezer.sdk.network.connect.DeezerConnect;
import com.deezer.sdk.network.connect.SessionStore;
import com.deezer.sdk.network.request.DeezerRequest;
import com.deezer.sdk.network.request.DeezerRequestFactory;
import com.deezer.sdk.network.request.event.JsonRequestListener;
import com.deezer.sdk.network.request.event.RequestListener;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.DirectoryFileFilter;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.regex.Pattern;

public class SetupAlarm extends AppCompatActivity
{
    WeckerParameters app;
    DeezerConnect deezerConnect;

    ArrayList<String> idList = new ArrayList<>();
    ArrayList<Integer> trackNumber = new ArrayList<>();
    Collection<File> musicList;

    Button buttonLundi;
    Button buttonMardi;
    Button buttonMercredi;
    Button buttonJeudi;
    Button buttonVendredi;
    Button buttonSamedi;
    Button buttonDimanche;
    List<Boolean> buttonClicked = new ArrayList<>(Arrays.asList(false, false, false, false, false, false, false));

    int id = 0;
    int offset = 0;
    int trackId = 0;
    String selectedPlaylistID = "";

    final String[] SUFFIX = {"mp3","wma","3gp","mp4","m4a","aac","flac","gsm","mkv","wav","ogg","ts"};  // use the suffix to filter

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.setup_alarm_layout);

        buttonLundi = findViewById(R.id.lundi);
        buttonMardi = findViewById(R.id.mardi);
        buttonMercredi = findViewById(R.id.mercredi);
        buttonJeudi = findViewById(R.id.jeudi);
        buttonVendredi = findViewById(R.id.vendredi);
        buttonSamedi = findViewById(R.id.samedi);
        buttonDimanche = findViewById(R.id.dimanche);

        buttonLundi.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v)
            {
                if (buttonClicked.get(0))
                {
                    buttonClicked.set(0, false);
                    buttonLundi.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.white)));
                    buttonLundi.setTextColor(getResources().getColor(R.color.blue));
                }
                else
                {
                    buttonClicked.set(0, true);
                    buttonLundi.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.darkBlue)));
                    buttonLundi.setTextColor(getResources().getColor(R.color.white));
                }
            }
        });

        buttonMardi.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v)
            {
                if (buttonClicked.get(1))
                {
                    buttonClicked.set(1, false);
                    buttonMardi.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.white)));
                    buttonMardi.setTextColor(getResources().getColor(R.color.blue));
                }
                else
                {
                    buttonClicked.set(1, true);
                    buttonMardi.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.darkBlue)));
                    buttonMardi.setTextColor(getResources().getColor(R.color.white));
                }
            }
        });

        buttonMercredi.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v)
            {
                if (buttonClicked.get(2))
                {
                    buttonClicked.set(2, false);
                    buttonMercredi.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.white)));
                    buttonMercredi.setTextColor(getResources().getColor(R.color.blue));
                }
                else
                {
                    buttonClicked.set(2, true);
                    buttonMercredi.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.darkBlue)));
                    buttonMercredi.setTextColor(getResources().getColor(R.color.white));
                }
            }
        });

        buttonJeudi.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v)
            {
                if (buttonClicked.get(3))
                {
                    buttonClicked.set(3, false);
                    buttonJeudi.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.white)));
                    buttonJeudi.setTextColor(getResources().getColor(R.color.blue));
                }
                else
                {
                    buttonClicked.set(3, true);
                    buttonJeudi.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.darkBlue)));
                    buttonJeudi.setTextColor(getResources().getColor(R.color.white));
                }
            }
        });

        buttonVendredi.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v)
            {
                if (buttonClicked.get(4))
                {
                    buttonClicked.set(4, false);
                    buttonVendredi.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.white)));
                    buttonVendredi.setTextColor(getResources().getColor(R.color.blue));
                }
                else
                {
                    buttonClicked.set(4, true);
                    buttonVendredi.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.darkBlue)));
                    buttonVendredi.setTextColor(getResources().getColor(R.color.white));
                }
            }
        });

        buttonSamedi.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v)
            {
                if (buttonClicked.get(5))
                {
                    buttonClicked.set(5, false);
                    buttonSamedi.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.white)));
                    buttonSamedi.setTextColor(getResources().getColor(R.color.blue));
                }
                else
                {
                    buttonClicked.set(5, true);
                    buttonSamedi.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.darkBlue)));
                    buttonSamedi.setTextColor(getResources().getColor(R.color.white));
                }
            }
        });

        buttonDimanche.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v)
            {
                if (buttonClicked.get(6))
                {
                    buttonClicked.set(6, false);
                    buttonDimanche.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.white)));
                    buttonDimanche.setTextColor(getResources().getColor(R.color.blue));
                }
                else
                {
                    buttonClicked.set(6, true);
                    buttonDimanche.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.darkBlue)));
                    buttonDimanche.setTextColor(getResources().getColor(R.color.white));
                }
            }
        });

        Intent intent = getIntent();
        idList = intent.getStringArrayListExtra("idList");
        boolean randomSong = intent.getBooleanExtra("randomSong",true);

        System.out.println(randomSong + " " + idList);

        getTrackLists();
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
            if ("spotifyLovedSongs".equals(playlistId))
            {
                new SpotifyCrawler().execute("me/tracks");
            }
            else
            {
                new SpotifyCrawler().execute("playlists/" + playlistId);
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

            if ("folderMusic".equals(selectedPlaylistID))
            {
                app.setSelectedFolderMusic((File) musicList.toArray()[trackId]);
            }
            else if (selectedPlaylistID.length() <= 10)
            {
                RequestListener listener = new JsonRequestListener()
                {
                    public void onResult(Object result, Object requestId)
                    {
                        List<Track> trackList = (List<Track>) result;
                        app.setSelectedDeezerMusic(Long.toString(trackList.get(trackId).getId()));
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
                    JSONArray trackJSONList = (JSONArray) server_response.get("items");

                    if (trackJSONList != null)
                    {
                        JSONObject track = (JSONObject) trackJSONList.optJSONObject(trackId).get("track");
                        app = (WeckerParameters) getApplicationContext();
                        app.setSelectedSpotifyMusic(track.getString("id"));
                    }
                }
                catch (Exception e)
                {
                    System.out.println(e.getMessage());
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
}
