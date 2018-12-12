package com.fpalud.wecker;

import android.app.AlarmManager;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.ColorFilter;
import android.os.AsyncTask;
import android.support.v4.content.res.ResourcesCompat;
import android.os.Bundle;
import android.support.v4.view.ViewCompat;
import android.support.v4.widget.CompoundButtonCompat;
import android.support.v7.widget.AppCompatButton;
import android.support.v7.widget.SwitchCompat;
import android.text.TextUtils;
import android.util.TypedValue;
import android.view.ContextThemeWrapper;
import android.view.Gravity;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.deezer.sdk.model.Playlist;
import com.deezer.sdk.network.connect.DeezerConnect;
import com.deezer.sdk.network.connect.SessionStore;
import com.deezer.sdk.network.request.DeezerRequest;
import com.deezer.sdk.network.request.DeezerRequestFactory;
import com.deezer.sdk.network.request.event.JsonRequestListener;
import com.deezer.sdk.network.request.event.RequestListener;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class SetupPlaylist extends BaseActivity
{
    LinearLayout playlistLayout;
    SwitchCompat songSwitch;
    SwitchCompat playlistSwitch;
    TextView randomSongText;
    TextView randomPlaylistText;
    TextView determinedSongText;
    TextView determinedPlaylistText;
    ArrayList<CheckBox> checkboxList = new ArrayList<>();

    private static final int INIT = -1;
    private static final int DEEZER = 0;
    private static final int SPOTIFY = 1;
    private static final int FOLDER = 2;

    Boolean deezerChecked;
    Boolean spotifyChecked;
    Boolean folderChecked;

    int deezerPlaylistnumber = 0;
    int spotifyPlaylistnumber = 0;
    int folderPlaylistnumber = 0;

    WeckerParameters app;

    DeezerConnect deezerConnect;
    ArrayList<Playlist> deezerPlaylistList = new ArrayList<>();
    ArrayList<String> spotifyPlaylistList = new ArrayList<>();
    ArrayList<String> idList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.setup_playlist_layout);

        Intent intent = getIntent();
        deezerChecked = intent.getBooleanExtra("deezer",false);
        spotifyChecked = intent.getBooleanExtra("spotify",false);
        folderChecked = intent.getBooleanExtra("folder",false);

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
        Intent intent = new Intent(this, Home.class);
        intent.putStringArrayListExtra("idList", idList);
        startActivity(intent);
    }

    public void connectionSetup(int origin)
    {
        if (origin == INIT)
        {
            if (deezerChecked)
            {
                getDeezerPlaylists();
            }
            else if (spotifyChecked)
            {
                new SpotifyCrawler().execute("me/playlists");
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
                new SpotifyCrawler().execute("me/playlists");
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

    public void getDeezerPlaylists()
    {
        deezerConnect = new DeezerConnect(this, "315304");

        if (new SessionStore().restore(deezerConnect, this))
        {
            RequestListener listener = new JsonRequestListener()
            {
                public void onResult(Object result, Object requestId)
                {
                    List<Playlist> playlistListTemp = (List<Playlist>) result;
                    deezerPlaylistList.add(playlistListTemp.remove(getLovedTracksID(playlistListTemp)));
                    deezerPlaylistList.addAll(playlistListTemp);

                    addTitle("Deezer");

                    for (Playlist playlist : deezerPlaylistList)
                    {
                        if (playlist.isLovedTracks())
                        {
                            addPlaylist(DEEZER,"Musiques préférées");
                        }
                        else
                        {
                            addPlaylist(DEEZER,playlist.getTitle());
                        }
                    }

                    if (spotifyChecked)
                    {
                        new SpotifyCrawler().execute("me/playlists");
                    }
                    else if (folderChecked)
                    {
                        getFolderSongs();
                    }
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
        addTitle("Dossier Musique");
        addPlaylist(FOLDER,"Liste des musiques");
    }

    public void addTitle(String title)
    {
        // Creating a new TextView
        TextView teamName = new TextView(this);

        teamName.setText(title);
        teamName.setTextSize(TypedValue.COMPLEX_UNIT_PX, 70);
        teamName.setTypeface(ResourcesCompat.getFont(this,R.font.carter_one));
        teamName.setLines(1);

        if ("Deezer".equals(title))
        {
            AlphaAnimation anim = new AlphaAnimation(0.0f, 1.0f);
            anim.setDuration(250);
            teamName.startAnimation(anim);
            teamName.setTextColor(getResources().getColor(R.color.deezerColor));
        }
        else if ("Spotify".equals(title))
        {
            spotifyPlaylistnumber++;

            AlphaAnimation anim = new AlphaAnimation(0.0f, 1.0f);
            anim.setDuration(250);
            anim.setStartOffset(250*(deezerPlaylistnumber + spotifyPlaylistnumber));
            teamName.startAnimation(anim);
            teamName.setTextColor(getResources().getColor(R.color.spotifyColor));
        }
        else
        {
            AlphaAnimation anim = new AlphaAnimation(0.0f, 1.0f);
            anim.setDuration(250);
            anim.setStartOffset(250*(deezerPlaylistnumber + spotifyPlaylistnumber + 1));
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
        playlistParams.setMargins(0, 0, 0, 10);

        newPlaylist.setOrientation(LinearLayout.HORIZONTAL);
        newPlaylist.setLayoutParams(playlistParams);
        newPlaylist.setGravity(Gravity.CENTER);

        // Create a CheckBox
        CheckBox checkBox = new CheckBox(this);
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
                        idList.add(Long.toString(deezerPlaylistList.get(v.getId()).getId()));
                    }
                    else
                    {
                        idList.remove(Long.toString(deezerPlaylistList.get(v.getId()).getId()));
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
        TextView newButton = new TextView(this);
        newButton.setText(playlistName);
        newButton.setGravity(Gravity.CENTER_VERTICAL);
        newButton.setTextSize(TypedValue.COMPLEX_UNIT_PX, 50);
        newButton.setTypeface(ResourcesCompat.getFont(this,R.font.carter_one));
        newButton.setLines(1);
        newButton.setHorizontallyScrolling(true);
        newButton.setMarqueeRepeatLimit(-1);
        newButton.setFocusable(true);
        newButton.setFocusableInTouchMode(true);
        newButton.setEllipsize(TextUtils.TruncateAt.MARQUEE);

        newButton.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));

        if (origin == DEEZER)
        {
            newButton.setTextColor(getResources().getColor(R.color.deezerColor));
            CompoundButtonCompat.setButtonTintList(checkBox, ColorStateList.valueOf(getResources().getColor(R.color.deezerColor)));
            checkBox.setId(deezerPlaylistnumber);
            deezerPlaylistnumber++;

            AlphaAnimation anim = new AlphaAnimation(0.0f, 1.0f);
            anim.setDuration(250);
            anim.setStartOffset(250*deezerPlaylistnumber);
            newPlaylist.startAnimation(anim);
        }
        else if (origin == SPOTIFY)
        {
            newButton.setTextColor(getResources().getColor(R.color.spotifyColor));
            CompoundButtonCompat.setButtonTintList(checkBox, ColorStateList.valueOf(getResources().getColor(R.color.spotifyColor)));
            checkBox.setId(spotifyPlaylistnumber - 1);
            spotifyPlaylistnumber++;

            AlphaAnimation anim = new AlphaAnimation(0.0f, 1.0f);
            anim.setDuration(250);
            anim.setStartOffset(250*(deezerPlaylistnumber + spotifyPlaylistnumber));
            newPlaylist.startAnimation(anim);
        }
        else if (origin == FOLDER)
        {
            newButton.setTextColor(getResources().getColor(R.color.folderColor));
            CompoundButtonCompat.setButtonTintList(checkBox, ColorStateList.valueOf(getResources().getColor(R.color.folderColor)));
            checkBox.setId(folderPlaylistnumber - 1);
            folderPlaylistnumber++;

            AlphaAnimation anim = new AlphaAnimation(0.0f, 1.0f);
            anim.setDuration(250);
            anim.setStartOffset(250*(deezerPlaylistnumber + spotifyPlaylistnumber + 2));
            newPlaylist.startAnimation(anim);
        }

        newPlaylist.addView(checkboxLayout);
        newPlaylist.addView(newButton);

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

                URL url = new URL("https://api.spotify.com/v1/" + endpoint);
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
                            addTitle("Spotify");
                            addPlaylist(SPOTIFY,"Musique préférées");
                            spotifyPlaylistList.add("spotifyLovedSongs");

                            for (int i = 0 ; i < playlistJSONList.length() ; i++)
                            {
                                spotifyPlaylistList.add(playlistJSONList.optJSONObject(i).getString("id"));
                                addPlaylist(SPOTIFY, playlistJSONList.optJSONObject(i).getString("name"));
                            }

                            if (folderChecked)
                            {
                                getFolderSongs();
                            }
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
}
