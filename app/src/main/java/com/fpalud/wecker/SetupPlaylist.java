package com.fpalud.wecker;

import android.content.Context;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Handler;
import android.support.v4.content.res.ResourcesCompat;
import android.os.Bundle;
import android.support.v4.widget.CompoundButtonCompat;
import android.support.v7.widget.SwitchCompat;
import android.text.TextUtils;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.deezer.sdk.model.Playlist;
import com.deezer.sdk.model.Track;
import com.deezer.sdk.network.connect.DeezerConnect;
import com.deezer.sdk.network.connect.SessionStore;
import com.deezer.sdk.network.request.DeezerRequest;
import com.deezer.sdk.network.request.DeezerRequestFactory;
import com.deezer.sdk.network.request.event.JsonRequestListener;
import com.deezer.sdk.network.request.event.RequestListener;
import com.spotify.android.appremote.api.ConnectionParams;
import com.spotify.android.appremote.api.Connector;
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

import static com.spotify.sdk.android.authentication.AuthenticationResponse.Type.TOKEN;

public class SetupPlaylist extends BaseActivity
{
    LinearLayout playlistLayout;
    SwitchCompat songSwitch;
    SwitchCompat playlistSwitch;
    ProgressBar loadingBar;
    TextView randomSongText;
    TextView randomPlaylistText;
    TextView determinedSongText;
    TextView determinedPlaylistText;
    ArrayList<CheckBox> checkboxList = new ArrayList<>();
    ArrayList<Button> buttonList = new ArrayList<>();

    private static final int INIT = -1;
    private static final int DEEZER = 0;
    private static final int SPOTIFY = 1;
    private static final int FOLDER = 2;

    boolean deezerChecked;
    boolean spotifyChecked;
    boolean folderChecked;
    boolean spotifyDisplayReady = false;
    boolean fromAlarm = false;
    int attemptNumber = 2000;

    int deezerPlaylistnumber = 0;
    int spotifyPlaylistnumber = 0;
    int folderPlaylistnumber = 0;
    int totalPlaylistnumber = 0;

    boolean spotifyConnected = false;
    boolean spotifyConnectionChecked = false;

    private static final int REQUEST_CODE = 1337;
    private static final String REDIRECT_URI = "wecker://callback";

    WeckerParameters app;
    Alarm currentAlarm;
    int alarmId = -1;

    DeezerConnect deezerConnect;
    ArrayList<Playlist> deezerPlaylistList = new ArrayList<>();
    ArrayList<Playlist> deezerCorrectPlaylistList = new ArrayList<>();
    ArrayList<String> spotifyPlaylistList = new ArrayList<>();
    ArrayList<String> spotifyPlaylistNameList = new ArrayList<>();
    ArrayList<String> idList = new ArrayList<>();
    ArrayList<String> totalIdList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.setup_playlist_layout);

        app = (WeckerParameters) getApplicationContext();
        deezerChecked = app.isDeezerChecked();
        spotifyChecked = app.isSpotifyChecked();
        folderChecked = app.isFolderChecked();

        loadingBar = findViewById(R.id.loadingBar);
        songSwitch = findViewById(R.id.songSwitch);
        playlistSwitch = findViewById(R.id.playlistSwitch);
        randomSongText = findViewById(R.id.randomSongText);
        randomPlaylistText = findViewById(R.id.randomPlaylistText);
        determinedSongText = findViewById(R.id.determinedSongText);
        determinedPlaylistText = findViewById(R.id.determinedPlaylistText);

        playlistLayout = findViewById(R.id.playlistLayout);

        songSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener()
        {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked)
            {
                checkSwitch();
            }
        });

        playlistSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener()
        {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked)
            {
                checkSwitch();

                if (isChecked)
                {
                    idList.clear();

                    for (int i = 0 ; i < checkboxList.size() ; i++)
                    {
                        checkboxList.get(i).setChecked(false);
                    }
                }
            }
        });

        fromAlarm = getIntent().getBooleanExtra("fromAlarm",false);

        if (fromAlarm)
        {
            alarmId = getIntent().getIntExtra("alarmId",0);

            currentAlarm = app.getAlarmList().get(alarmId);
            idList = currentAlarm.getIdSongsList();
        }

        connectionSetup(INIT);
    }

    @Override
    protected void onResume()
    {
        super.onResume();
        checkSwitch();
    }

    public void checkSwitch()
    {
        if (songSwitch.isChecked())
        {
            determinedPlaylistText.setAlpha(1f);
            randomPlaylistText.setAlpha(0.5f);
            determinedSongText.setAlpha(1f);
            randomSongText.setAlpha(0.5f);
            playlistSwitch.setChecked(true);
            playlistSwitch.setClickable(false);
            playlistSwitch.setAlpha(0.5f);
        }
        else
        {
            determinedSongText.setAlpha(0.5f);
            randomSongText.setAlpha(1f);
            playlistSwitch.setClickable(true);
            playlistSwitch.setAlpha(1f);
        }

        if (playlistSwitch.isChecked())
        {
            determinedPlaylistText.setAlpha(1f);
            randomPlaylistText.setAlpha(0.5f);
        }
        else
        {
            determinedPlaylistText.setAlpha(0.5f);
            randomPlaylistText.setAlpha(1f);
        }
    }

    public void goToNext(View view)
    {
        for (CheckBox checkbox : checkboxList)
        {
            if (checkbox.isChecked())
            {
                if (fromAlarm)
                {
                    finish();
                }
                else
                {
                    Intent intent = new Intent(this, SetupAlarm.class);
                    intent.putExtra("randomSong", !songSwitch.isChecked());
                    intent.putStringArrayListExtra("idList", idList);
                    startActivity(intent);
                }

                break;
            }
        }
    }

    private boolean isNetworkAvailable()
    {
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();

        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    public void connectionSetup(int origin)
    {
        System.out.println(origin + " - " + deezerChecked + " - " + spotifyChecked + " - " + folderChecked);

        if ((deezerChecked || spotifyChecked) && !isNetworkAvailable())
        {
            Toast.makeText(app, "Pas de connexion Internet.", Toast.LENGTH_SHORT).show();

            if (folderChecked)
            {
                getFolderSongs();
            }
        }
        else
        {
            if (origin == INIT)
            {
                if (deezerChecked)
                {
                    if (spotifyChecked)
                    {
                        new SpotifyCrawler().execute("me");
                    }

                    getDeezerPlaylists();
                }
                else if (spotifyChecked)
                {
                    new SpotifyCrawler().execute("me");
                }
                else
                {
                    getFolderSongs();
                }
            }

            if (origin == DEEZER)
            {
                if (spotifyChecked)
                {
                    new SpotifyCrawler().execute("me");
                }
                else if (folderChecked)
                {
                    getFolderSongs();
                }
            }

            if (origin == SPOTIFY)
            {
                if (folderChecked)
                {
                    getFolderSongs();
                }
            }
        }


    }

    public void getDeezerPlaylists()
    {
        deezerConnect = DeezerConnect.forApp("315304").build();

        if (new SessionStore().restore(deezerConnect, this))
        {
            RequestListener listener = new JsonRequestListener()
            {
                public void onResult(Object result, Object requestId)
                {
                    List<Playlist> playlistListTemp = (List<Playlist>) result;
                    deezerPlaylistList.add(playlistListTemp.remove(getLovedTracksID(playlistListTemp)));
                    deezerPlaylistList.addAll(playlistListTemp);

                    displayDeezerPlaylists(0);
                }

                public void onUnparsedResult(String requestResponse, Object requestId)
                {
                    connectionSetup(DEEZER);
                }

                public void onException(Exception e, Object requestId)
                {
                    System.out.println(e.getMessage());
                    connectionSetup(DEEZER);
                }
            };

            Bundle bundle = new Bundle(1);
            bundle.putString("limit","2000");
            DeezerRequest request = DeezerRequestFactory.requestCurrentUserPlaylists();
            deezerConnect.requestAsync(request, listener);
        }
        else
        {
            connectionSetup(DEEZER);
        }
    }

    public void displayDeezerPlaylists(final int id)
    {
        RequestListener listener = new JsonRequestListener()
        {
            public void onResult(Object result, Object requestId)
            {
                List<Track> trackList = (List<Track>) result;

                if (trackList.size() > 0)
                {
                    deezerCorrectPlaylistList.add(deezerPlaylistList.get(id));
                }

                if (id + 1 == deezerPlaylistList.size())
                {
                    if (spotifyChecked)
                    {
                        final Handler handler = new Handler();
                        handler.postDelayed(new Runnable()
                        {
                            public void run()
                            {
                                if (!spotifyDisplayReady && attemptNumber > 0)
                                {
                                    attemptNumber--;
                                    handler.postDelayed(this, 10);
                                }
                                else
                                {
                                    if (deezerCorrectPlaylistList.size() > 0)
                                    {
                                        loadingBar.setVisibility(View.GONE);
                                        addTitle("Deezer");
                                    }

                                    for (Playlist playlist : deezerCorrectPlaylistList)
                                    {
                                        if (playlist.isLovedTracks())
                                        {
                                            addPlaylist(DEEZER, "Musique préférées");
                                        }
                                        else
                                        {
                                            addPlaylist(DEEZER, playlist.getTitle());
                                        }
                                    }

                                    displaySpotifyPlaylists();
                                }
                            }
                        }, 0);
                    }
                    else
                    {
                        if (deezerCorrectPlaylistList.size() > 0)
                        {
                            loadingBar.setVisibility(View.GONE);
                            addTitle("Deezer");
                        }

                        for (Playlist playlist : deezerCorrectPlaylistList)
                        {
                            if (playlist.isLovedTracks())
                            {
                                addPlaylist(DEEZER, "Musique préférées");
                            }
                            else
                            {
                                addPlaylist(DEEZER, playlist.getTitle());
                            }
                        }

                        if (folderChecked)
                        {
                            getFolderSongs();
                        }
                    }
                }
                else
                {
                    displayDeezerPlaylists(id + 1);
                }
            }

            public void onUnparsedResult(String requestResponse, Object requestId) {}
            public void onException(Exception e, Object requestId) {}
        };

        Bundle bundle = new Bundle(1);
        bundle.putString("limit","1");
        DeezerRequest request = new DeezerRequest("playlist/" + deezerPlaylistList.get(id).getId() + "/tracks", bundle);
        deezerConnect.requestAsync(request, listener);
    }

    public void displaySpotifyPlaylists()
    {
        System.out.println("Top");
        final Handler handler = new Handler();
        handler.postDelayed(new Runnable()
        {
            public void run()
            {
                if (!spotifyDisplayReady && attemptNumber > 0)
                {
                    handler.postDelayed(this, 10);
                    attemptNumber--;
                }
            }
        }, 0);

        if (attemptNumber > 0)
        {
            loadingBar.setVisibility(View.GONE);
            addTitle("Spotify");

            for (String name : spotifyPlaylistNameList)
            {
                addPlaylist(SPOTIFY,name);
            }
        }

        if (folderChecked)
        {
            getFolderSongs();
        }
    }

    public int getLovedTracksID(List<Playlist> list)
    {
        for (int i = 0 ; i < list.size() ; i++)
        {
            if (list.get(i).isLovedTracks())
            {
                return i;
            }
        }

        return 0;
    }

    public void getFolderSongs()
    {
        app = (WeckerParameters) getApplicationContext();
        File rootDir = new File(app.getMusicFolderPath());
        String[] SUFFIX = {"mp3","wma","3gp","mp4","m4a","aac","flac","gsm","mkv","wav","ogg","ts"};
        Collection<File> musicList = FileUtils.listFiles(rootDir, SUFFIX, true);

        loadingBar.setVisibility(View.GONE);

        if (musicList.size() > 0)
        {
            addTitle("Dossier Musique");
            addPlaylist(FOLDER,"Liste des musiques");
        }
    }

    public void addTitle(String title)
    {
        // Creating a new TextView
        TextView teamName = new TextView(this);

        teamName.setText(title);
        teamName.setTextSize(TypedValue.COMPLEX_UNIT_PX, 60);
        teamName.setTypeface(ResourcesCompat.getFont(this,R.font.carter_one));
        teamName.setLines(1);

        if ("Deezer".equals(title))
        {
            AlphaAnimation anim = new AlphaAnimation(0.0f, 1.0f);
            anim.setDuration(750);
            teamName.startAnimation(anim);
            teamName.setTextColor(getResources().getColor(R.color.deezerColor));
        }
        else if ("Spotify".equals(title))
        {
            spotifyPlaylistnumber++;

            AlphaAnimation anim = new AlphaAnimation(0.0f, 1.0f);
            anim.setDuration(750);
            anim.setStartOffset(50*(deezerPlaylistnumber + spotifyPlaylistnumber));
            teamName.startAnimation(anim);
            teamName.setTextColor(getResources().getColor(R.color.spotifyColor));
        }
        else
        {
            AlphaAnimation anim = new AlphaAnimation(0.0f, 1.0f);
            anim.setDuration(750);
            anim.setStartOffset(50*(deezerPlaylistnumber + spotifyPlaylistnumber + 1));
            teamName.startAnimation(anim);
            teamName.setTextColor(getResources().getColor(R.color.folderColor));
        }

        // Add the RelativeLayout to the main LinearLayout
        playlistLayout.addView(teamName);
    }

    public void addPlaylist(final int origin, String playlistName)
    {
        LinearLayout newPlaylist = new LinearLayout(this);
        LinearLayout.LayoutParams playlistParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        playlistParams.setMargins(0, 0, 0, 5);

        newPlaylist.setOrientation(LinearLayout.HORIZONTAL);
        newPlaylist.setLayoutParams(playlistParams);
        newPlaylist.setGravity(Gravity.CENTER);

        // Create a CheckBox
        final CheckBox checkBox = new CheckBox(this);
        checkBox.setScaleX(1.5f);
        checkBox.setScaleY(1.5f);
        checkBox.setOnClickListener(new View.OnClickListener()
        {
            public void onClick(View v)
            {
                if (playlistSwitch.isChecked() && ((CheckBox) v).isChecked())
                {
                    for (int i = 0 ; i < checkboxList.size() ; i++)
                    {
                        if (i != checkboxList.indexOf(((CheckBox) v)))
                        {
                            checkboxList.get(i).setChecked(false);
                        }
                    }

                    idList.clear();
                }

                if (origin == DEEZER)
                {
                    if (((CheckBox) v).isChecked())
                    {
                        idList.add(Long.toString(deezerCorrectPlaylistList.get(v.getId()).getId()));
                    }
                    else
                    {
                        idList.remove(Long.toString(deezerCorrectPlaylistList.get(v.getId()).getId()));
                    }

                }
                else if (origin == SPOTIFY)
                {
                    if (((CheckBox) v).isChecked())
                    {
                        idList.add(spotifyPlaylistList.get(v.getId()));
                    }
                    else
                    {
                        idList.remove(spotifyPlaylistList.get(v.getId()));
                    }
                }
                else if (origin == FOLDER)
                {
                    if (((CheckBox) v).isChecked())
                    {
                        idList.add("folderMusic");
                    }
                    else
                    {
                        idList.remove("folderMusic");
                    }
                }

                System.out.println(idList);
            }
        });

        LinearLayout.LayoutParams boxParameters = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        checkBox.setLayoutParams(boxParameters);
        checkboxList.add(checkBox);

        // Create a LinearLayout for the CheckBox
        LinearLayout checkboxLayout = new LinearLayout(this);
        checkboxLayout.setGravity(Gravity.CENTER);
        checkboxLayout.setPadding(0,0,15,0);
        checkboxLayout.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT));
        checkboxLayout.addView(checkBox);

        // Create a new Button
        Button newButton = new Button(this);
        newButton.setText(playlistName);
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
        newButton.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
        newButton.setOnClickListener(new View.OnClickListener()
        {
            public void onClick(View v)
            {
                int buttonId = buttonList.indexOf((Button) v);
                CheckBox checkbox = checkboxList.get(buttonId);

                if (checkbox.isChecked())
                {
                    checkbox.setChecked(false);
                }
                else
                {
                    checkbox.setChecked(true);
                }

                checkboxList.set(buttonId, checkbox);

                if (playlistSwitch.isChecked() && checkbox.isChecked())
                {
                    for (int i = 0 ; i < checkboxList.size() ; i++)
                    {
                        if (i != buttonId)
                        {
                            checkboxList.get(i).setChecked(false);
                        }
                    }

                    idList.clear();
                }

                if (origin == DEEZER)
                {
                    if (checkbox.isChecked())
                    {
                        idList.add(Long.toString(deezerCorrectPlaylistList.get(checkbox.getId()).getId()));
                    }
                    else
                    {
                        idList.remove(Long.toString(deezerCorrectPlaylistList.get(checkbox.getId()).getId()));
                    }

                }
                else if (origin == SPOTIFY)
                {
                    if (checkbox.isChecked())
                    {
                        idList.add(spotifyPlaylistList.get(checkbox.getId()));
                    }
                    else
                    {
                        idList.remove(spotifyPlaylistList.get(checkbox.getId()));
                    }
                }
                else if (origin == FOLDER)
                {
                    if (checkbox.isChecked())
                    {
                        idList.add("folderMusic");
                    }
                    else
                    {
                        idList.remove("folderMusic");
                    }
                }

                System.out.println(idList);
            }
        });

        if (origin == DEEZER)
        {
            newButton.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.deezerColor)));
            CompoundButtonCompat.setButtonTintList(checkBox, ColorStateList.valueOf(getResources().getColor(R.color.deezerColor)));
            checkBox.setId(deezerPlaylistnumber);

            if (fromAlarm)
            {
                checkBox.setChecked(idList.contains(String.valueOf(deezerCorrectPlaylistList.get(deezerPlaylistnumber).getId())));
            }

            deezerPlaylistnumber++;

            AlphaAnimation anim = new AlphaAnimation(0.0f, 1.0f);
            anim.setDuration(750);
            anim.setStartOffset(50*deezerPlaylistnumber);
            newPlaylist.startAnimation(anim);
        }
        else if (origin == SPOTIFY)
        {
            newButton.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.spotifyColor)));
            CompoundButtonCompat.setButtonTintList(checkBox, ColorStateList.valueOf(getResources().getColor(R.color.spotifyColor)));
            checkBox.setId(spotifyPlaylistnumber - 1);

            if (fromAlarm)
            {
                checkBox.setChecked(idList.contains(spotifyPlaylistList.get(spotifyPlaylistnumber - 1)));
            }

            spotifyPlaylistnumber++;

            AlphaAnimation anim = new AlphaAnimation(0.0f, 1.0f);
            anim.setDuration(750);
            anim.setStartOffset(50*(deezerPlaylistnumber + spotifyPlaylistnumber));
            newPlaylist.startAnimation(anim);
        }
        else if (origin == FOLDER)
        {
            newButton.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.folderColor)));
            CompoundButtonCompat.setButtonTintList(checkBox, ColorStateList.valueOf(getResources().getColor(R.color.folderColor)));
            checkBox.setId(folderPlaylistnumber - 1);
            folderPlaylistnumber++;

            if (fromAlarm)
            {
                checkBox.setChecked(idList.contains("folderMusic"));
            }

            AlphaAnimation anim = new AlphaAnimation(0.0f, 1.0f);
            anim.setDuration(750);
            anim.setStartOffset(50*(deezerPlaylistnumber + spotifyPlaylistnumber + 2));
            newPlaylist.startAnimation(anim);
        }

        buttonList.add(newButton);

        newPlaylist.addView(checkboxLayout);
        newPlaylist.addView(newButton);

        totalPlaylistnumber++;

        // Add the RelativeLayout to the main LinearLayout
        playlistLayout.addView(newPlaylist);
    }

    public class SpotifyCrawler extends AsyncTask<String, Void, String>
    {
        JSONObject server_response = new JSONObject();
        String endpoint = "";

        @Override
        protected String doInBackground(String... strings)
        {
            try
            {
                app = (WeckerParameters) getApplicationContext();
                endpoint = strings[0];

                URL url;

                if ("me/tracks".equals(endpoint))
                {
                    url = new URL("https://api.spotify.com/v1/me/tracks?limit=1");
                }
                else
                {
                    url = new URL("https://api.spotify.com/v1/" + endpoint);
                }

                HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestProperty("Authorization", "Bearer " + app.getSpotifyToken());

                if (urlConnection.getResponseCode() == HttpURLConnection.HTTP_OK)
                {
                    server_response = readStream(urlConnection.getInputStream());
                }
            }
            catch (MalformedURLException e)
            {
                connectionSetup(SPOTIFY);
                System.out.println("MalformedURLException : " + e.getMessage());
            }
            catch (IOException e)
            {
                connectionSetup(SPOTIFY);
                System.out.println("IOException : " + e.getMessage());
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
                    connectionSetup(SPOTIFY);
                }
                else
                {
                    if ("me/playlists".equals(endpoint))
                    {
                        try
                        {
                            JSONArray playlistJSONList = (JSONArray) server_response.get("items");

                            if (playlistJSONList != null)
                            {
                                for (int i = 0 ; i < playlistJSONList.length() ; i++)
                                {
                                    JSONObject tracks = (JSONObject) playlistJSONList.optJSONObject(i).get("tracks");

                                    if (!"0".equals(tracks.getString("total")))
                                    {
                                        spotifyPlaylistList.add(playlistJSONList.optJSONObject(i).getString("id"));
                                        spotifyPlaylistNameList.add(playlistJSONList.optJSONObject(i).getString("name"));

                                        System.out.println("Added");
                                    }
                                }

                                spotifyDisplayReady = true;

                                if (!deezerChecked)
                                {
                                    System.out.println("Deezer not checked");
                                    displaySpotifyPlaylists();
                                }
                            }
                        }
                        catch (Exception e)
                        {
                            connectionSetup(SPOTIFY);
                            System.out.println(e.getMessage());
                        }
                    }
                    else if ("me/tracks".equals(endpoint))
                    {
                        try
                        {
                            if (!"0".equals(server_response.getString("total")))
                            {
                                spotifyPlaylistNameList.add("Musique préférées");
                                spotifyPlaylistList.add("spotifyLovedSongs");

                                System.out.println("Added loved");
                            }
                        }
                        catch (Exception e)
                        {
                            connectionSetup(SPOTIFY);
                            System.out.println(e.getMessage());
                        }
                    }
                }
            }
            else
            {
                if (server_response.length() == 0)
                {
                    spotifyConnect();
                }
                else
                {
                    spotifyConnectionChecked = true;
                    new SpotifyCrawler().execute("me/tracks");
                    new SpotifyCrawler().execute("me/playlists");
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
                connectionSetup(SPOTIFY);
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
                        connectionSetup(SPOTIFY);
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
                connectionSetup(SPOTIFY);
                System.out.println("Could not parse malformed JSON");
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

        if (app.getSpotifyConnect() == null)
        {
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
                            SpotifyAppRemote.disconnect(spotifyAppRemote);

                            if (requestCode == REQUEST_CODE)
                            {
                                AuthenticationResponse response = AuthenticationClient.getResponse(resultCode, intent);

                                if (response.getType() == TOKEN)
                                {
                                    app = (WeckerParameters) getApplicationContext();
                                    app.setSpotifyToken(response.getAccessToken());

                                    spotifyConnectionChecked = true;

                                    new SpotifyCrawler().execute("me/tracks");
                                    new SpotifyCrawler().execute("me/playlists");
                                }
                                else
                                {
                                    System.out.println("Connection failed : " + response.getError());
                                    connectionSetup(SPOTIFY);
                                }
                            }
                        }

                        @Override
                        public void onFailure(Throwable throwable)
                        {
                            Log.e("MusicOrigin", throwable.getMessage(), throwable);
                            connectionSetup(SPOTIFY);
                        }
                    });
        }
        else
        {
            if (requestCode == REQUEST_CODE)
            {
                AuthenticationResponse response = AuthenticationClient.getResponse(resultCode, intent);

                if (response.getType() == TOKEN)
                {
                    app = (WeckerParameters) getApplicationContext();
                    app.setSpotifyToken(response.getAccessToken());

                    spotifyConnectionChecked = true;

                    new SpotifyCrawler().execute("me/tracks");
                    new SpotifyCrawler().execute("me/playlists");
                }
                else
                {
                    System.out.println("Connection failed : " + response.getError());
                    connectionSetup(SPOTIFY);
                }
            }
        }
    }
}
