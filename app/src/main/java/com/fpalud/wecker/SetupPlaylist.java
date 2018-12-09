package com.fpalud.wecker;

import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.v4.content.res.ResourcesCompat;
import android.os.Bundle;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.animation.AlphaAnimation;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.deezer.sdk.model.Permissions;
import com.deezer.sdk.model.Playlist;
import com.deezer.sdk.model.Track;
import com.deezer.sdk.network.connect.DeezerConnect;
import com.deezer.sdk.network.connect.SessionStore;
import com.deezer.sdk.network.connect.event.DialogListener;
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

import static com.spotify.sdk.android.authentication.AuthenticationResponse.Type.TOKEN;

public class SetupPlaylist extends BaseActivity
{
    LinearLayout playlistLayout;

    private static final int INIT = -1;
    private static final int DEEZER = 0;
    private static final int SPOTIFY = 1;

    private static final int REQUEST_CODE = 1337;
    private static final String REDIRECT_URI = "wecker://callback";

    Boolean deezerChecked;
    Boolean spotifyChecked;
    Boolean folderChecked;

    int deezerPlaylistnumber = 0;
    int spotifyPlaylistnumber = 0;
    int folderPlaylistnumber = 0;

    WeckerParameters app;

    DeezerConnect deezerConnect;
    ArrayList<Playlist> deezerPlaylistList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.setup_playlist_layout);

        Intent intent = getIntent();
        deezerChecked = intent.getBooleanExtra("deezer",false);
        spotifyChecked = intent.getBooleanExtra("spotify",false);
        folderChecked = intent.getBooleanExtra("folder",false);

        playlistLayout = findViewById(R.id.playlistLayout);

        connectionSetup(INIT);
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
                // getFolderSongs();
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
                // folderConnection();
            }
        }

        if (origin == SPOTIFY)
        {
            if (folderChecked)
            {
                // folderConnection();
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
                        // folderConnection();
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
            teamName.setTextColor(getResources().getColor(R.color.folderColor));
        }

        // Add the RelativeLayout to the main LinearLayout
        playlistLayout.addView(teamName);
    }

    public void addPlaylist(int origin, String playlistName)
    {
        // Create a new RelativeLayout
        RelativeLayout newButton = new RelativeLayout(this);

        // Defining the RelativeLayout layout parameters
        LinearLayout.LayoutParams buttonParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);

        buttonParams.setMargins(0, 0, 0, 20);

        // Creating a new TextView
        TextView teamName = new TextView(this);

        teamName.setText(playlistName);
        teamName.setTextSize(TypedValue.COMPLEX_UNIT_PX, 50);
        teamName.setTypeface(ResourcesCompat.getFont(this,R.font.carter_one));
        teamName.setTextColor(getResources().getColor(R.color.ic_launcher_background));
        teamName.setLines(1);

        // Defining the layout parameters of the TextView
        RelativeLayout.LayoutParams textParameters = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
        textParameters.setMarginStart(8);

        // Setting the parameters on the TextView
        teamName.setLayoutParams(textParameters);

        // Adding the TextView to the RelativeLayout as a child and make the layout clickable
        newButton.addView(teamName, 0);
        newButton.setGravity(Gravity.CENTER_VERTICAL);
        newButton.setLayoutParams(buttonParams);

        if (origin == DEEZER)
        {
            newButton.setBackgroundColor(getResources().getColor(R.color.deezerColor));
            deezerPlaylistnumber++;

            AlphaAnimation anim = new AlphaAnimation(0.0f, 1.0f);
            anim.setDuration(250);
            anim.setStartOffset(250*deezerPlaylistnumber);
            newButton.startAnimation(anim);
        }
        else if (origin == SPOTIFY)
        {
            newButton.setBackgroundColor(getResources().getColor(R.color.spotifyColor));
            spotifyPlaylistnumber++;

            AlphaAnimation anim = new AlphaAnimation(0.0f, 1.0f);
            anim.setDuration(250);
            anim.setStartOffset(250*(deezerPlaylistnumber + spotifyPlaylistnumber));
            newButton.startAnimation(anim);
        }
        else
        {
            newButton.setBackgroundColor(getResources().getColor(R.color.folderColor));
            folderPlaylistnumber++;

            AlphaAnimation anim = new AlphaAnimation(0.0f, 1.0f);
            anim.setDuration(250);
            anim.setStartOffset(250*folderPlaylistnumber);
            newButton.startAnimation(anim);
        }

        // Add the RelativeLayout to the main LinearLayout
        playlistLayout.addView(newButton);
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

                            for (int i = 0 ; i < playlistJSONList.length() ; i++)
                            {
                                addPlaylist(SPOTIFY,(String) playlistJSONList.optJSONObject(i).get("name"));
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
