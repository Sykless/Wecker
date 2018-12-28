package com.fpalud.wecker;

import android.content.Context;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.content.res.ResourcesCompat;
import android.text.TextUtils;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.deezer.sdk.model.Track;
import com.deezer.sdk.network.connect.DeezerConnect;
import com.deezer.sdk.network.connect.SessionStore;
import com.deezer.sdk.network.request.DeezerRequest;
import com.deezer.sdk.network.request.event.JsonRequestListener;
import com.deezer.sdk.network.request.event.RequestListener;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Array;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import yogesh.firzen.filelister.FileListerDialog;
import yogesh.firzen.filelister.OnFileSelectedListener;

public class SetupSongs extends BaseActivity
{
    WeckerParameters app;

    LinearLayout songsLayout;
    TextView playlistName;
    ProgressBar loadingBar;
    ArrayList<Button> songList = new ArrayList<>();
    ArrayList<String> songIdList = new ArrayList<>();

    DeezerConnect deezerConnect;
    FileListerDialog fileListerDialog;

    String playlistId;
    String selectedMusicId = null;
    String selectedMusicPath = null;

    int songsNumber = 0;

    private static final int DEEZER = 0;
    private static final int SPOTIFY = 1;
    private static final int FOLDER = 2;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.setup_songs_layout);

        app = (WeckerParameters) getApplicationContext();

        songsLayout = findViewById(R.id.songsLayout);
        playlistName = findViewById(R.id.playlistName);
        loadingBar = findViewById(R.id.loadingBar);

        playlistId = getIntent().getStringExtra("playlistId");
        playlistName.setText(getIntent().getStringExtra("playlistName"));

        if ("folderMusic".equals(playlistId)) // Music folder
        {
            loadingBar.setVisibility(View.GONE);
            playlistName.setTextColor(getResources().getColor(R.color.folderColor));

            fileListerDialog = FileListerDialog.createFileListerDialog(this);
            fileListerDialog.setOnFileSelectedListener(new OnFileSelectedListener()
            {
                @Override
                public void onFileSelected(File file, String path)
                {
                    if (file.isFile())
                    {
                        String[] parts = path.split("/");
                        String[] fileName = parts[parts.length - 1].split("\\.");
                        String extension = fileName[fileName.length - 1];
                        String[] musicExtensions = {"mp3","wma","3gp","mp4","m4a","aac","flac","gsm","mkv","wav","ogg","ts"};

                        if (Arrays.asList(musicExtensions).contains(extension))
                        {
                            selectedMusicPath = file.getAbsolutePath();
                            goToNext();
                        }
                        else
                        {
                            fileListerDialog.show();
                        }
                    }
                    else
                    {
                        fileListerDialog.show();
                    }
                }
            });

            fileListerDialog.setDefaultDir(app.getMusicFolderPath());
            fileListerDialog.setFileFilter(FileListerDialog.FILE_FILTER.ALL_FILES);
            fileListerDialog.show();
        }
        else if (isNetworkAvailable())
        {
            if (playlistId.length() <= 10) // Deezer playlist
            {
                playlistName.setTextColor(getResources().getColor(R.color.deezerColor));

                deezerConnect = DeezerConnect.forApp("315304").build();
                SessionStore sessionStore = new SessionStore();

                if (sessionStore.restore(deezerConnect, this))
                {
                    RequestListener listener = new JsonRequestListener()
                    {
                        public void onResult(Object result, Object requestId)
                        {
                            List<Track> trackList = (List<Track>) result;

                            loadingBar.setVisibility(View.GONE);
                            for (Track track : trackList)
                            {
                                addSong(DEEZER, track.getArtist().getName(),track.getTitle(), String.valueOf(track.getId()));
                            }
                        }

                        public void onUnparsedResult(String requestResponse, Object requestId) {}
                        public void onException(Exception e, Object requestId) {}
                    };

                    Bundle bundle = new Bundle(1);
                    bundle.putString("limit","2000");
                    DeezerRequest request = new DeezerRequest("playlist/" + playlistId + "/tracks", bundle);
                    deezerConnect.requestAsync(request, listener);
                }
            }
            else // Spotify playlist
            {
                playlistName.setTextColor(getResources().getColor(R.color.spotifyColor));

                if ("spotifyLovedSongs".equals(playlistId))
                {
                    new SpotifyCrawler().execute("https://api.spotify.com/v1/me/tracks?limit=50&offset=0");
                }
                else
                {
                    new SpotifyCrawler().execute("https://api.spotify.com/v1/playlists/" + playlistId + "/tracks?limit=100&offset=0");
                }
            }
        }
        else
        {
            Toast.makeText(this, "Pas de connexion Internet.", Toast.LENGTH_SHORT).show();
        }
    }

    public void goToNext()
    {
        int alarmId = getIntent().getIntExtra("alarmId",-1);

        if (alarmId == -1)
        {
            Intent intent = new Intent(this, SetupAlarm.class);
            intent.putExtra("selectedMusicPath", selectedMusicPath);
            intent.putExtra("selectedMusicId", selectedMusicId);
            intent.putExtra("randomSong",false);
            intent.putExtra("randomPlaylist", false);
            intent.putStringArrayListExtra("idList", new ArrayList<String>() {{add(playlistId);}});
            startActivity(intent);
        }
        else
        {
            File selectedSong = null;
            Alarm alarm = app.getAlarmList().get(alarmId);

            if (selectedMusicPath != null)
            {
                selectedSong = new File(selectedMusicPath);
            }

            alarm.setSelectedSong(selectedSong);
            alarm.setSelectedSongId(selectedMusicId);
            alarm.setRandomPlaylist(false);
            alarm.setRandomSong(false);

            finish();
        }
    }

    public void addSong(final int origin, String artist, String title, String id)
    {
        LinearLayout.LayoutParams buttonParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        buttonParams.setMargins(0, 0, 0, 5);

        // Create a new Button
        Button newButton = new Button(this);
        newButton.setText(title + " - " + artist);
        newButton.setGravity(Gravity.CENTER_VERTICAL);
        newButton.setTextSize(TypedValue.COMPLEX_UNIT_PX, 40);
        newButton.setTypeface(ResourcesCompat.getFont(this,R.font.carter_one));
        newButton.setLines(1);
        newButton.setAllCaps(false);
        newButton.setHorizontallyScrolling(true);
        newButton.setMarqueeRepeatLimit(-1);
        newButton.setFocusable(true);
        newButton.setEllipsize(TextUtils.TruncateAt.MARQUEE);
        newButton.setTextColor(getResources().getColor(R.color.white));
        newButton.setLayoutParams(buttonParams);
        newButton.setOnClickListener(new View.OnClickListener()
        {
            public void onClick(View v)
            {
                selectedMusicId = songIdList.get(songList.indexOf((Button) v));
                goToNext();
            }
        });

        songsNumber++;

        if (origin == DEEZER)
        {
            newButton.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.deezerColor)));

            AlphaAnimation anim = new AlphaAnimation(0.0f, 1.0f);
            anim.setDuration(750);
            anim.setStartOffset(30*songsNumber);
            newButton.startAnimation(anim);
        }
        else if (origin == SPOTIFY)
        {
            newButton.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.spotifyColor)));

            AlphaAnimation anim = new AlphaAnimation(0.0f, 1.0f);
            anim.setDuration(750);
            anim.setStartOffset(30*songsNumber);
            newButton.startAnimation(anim);
        }

        songList.add(newButton);
        songIdList.add(id);

        // Add the Button to the main LinearLayout
        songsLayout.addView(newButton);
    }

    private boolean isNetworkAvailable()
    {
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();

        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    public class SpotifyCrawler extends AsyncTask<String, Void, String>
    {
        JSONObject server_response = new JSONObject();

        @Override
        protected String doInBackground(String... strings)
        {
            try
            {
                app = (WeckerParameters) getApplicationContext();

                URL url = new URL(strings[0]);
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
            }
            catch (IOException e)
            {
                System.out.println("IOException : " + e.getMessage());
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
            else
            {
                try
                {
                    System.out.println("Getting tracks");

                    JSONArray trackJSONList = (JSONArray) server_response.get("items");

                    if (trackJSONList != null)
                    {
                        loadingBar.setVisibility(View.GONE);

                        for (int i = 0 ; i < trackJSONList.length() ; i++)
                        {
                            JSONObject jsonTrack = (JSONObject) trackJSONList.optJSONObject(i).get("track");

                            String artist = (((JSONArray) jsonTrack.get("artists")).optJSONObject(0)).getString("name");
                            String title = jsonTrack.getString("name");
                            String id = jsonTrack.getString("id");

                            addSong(SPOTIFY, artist,title, id);
                        }

                        if (server_response.getString("next") != null)
                        {
                            new SpotifyCrawler().execute(server_response.getString("next"));
                        }
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
            }

            return null;
        }
    }
}
