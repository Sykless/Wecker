package com.fpalud.wecker;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.View;
import android.widget.CheckBox;

import com.deezer.sdk.model.Permissions;
import com.deezer.sdk.network.connect.DeezerConnect;
import com.deezer.sdk.network.connect.SessionStore;
import com.deezer.sdk.network.connect.event.DialogListener;
import com.spotify.android.appremote.api.ConnectionParams;
import com.spotify.android.appremote.api.Connector;
import com.spotify.android.appremote.api.SpotifyAppRemote;
import com.spotify.sdk.android.authentication.AuthenticationClient;
import com.spotify.sdk.android.authentication.AuthenticationRequest;
import com.spotify.sdk.android.authentication.AuthenticationResponse;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import static com.spotify.sdk.android.authentication.AuthenticationResponse.Type.TOKEN;

public class MusicOrigin extends BaseActivity
{
    CheckBox deezerBox;
    CheckBox spotifyBox;
    CheckBox folderBox;

    boolean deezerChecked;
    boolean spotifyChecked;
    boolean folderChecked;

    boolean deezerConnectionChecked = false;
    boolean spotifyConnectionChecked = false;
    boolean folderConnectionChecked = false;

    boolean deezerConnected = false;
    boolean spotifyConnected = false;
    boolean spotifyAPKConnected = false;
    boolean folderConnected = false;

    boolean installingSpotify = false;

    private static final int INIT = -1;
    private static final int DEEZER = 0;
    private static final int SPOTIFY = 1;
    private static final int FOLDER = 2;

    private static final int REQUEST_CODE = 1337;
    private static final String REDIRECT_URI = "wecker://callback";

    WeckerParameters app;
    DeezerConnect deezerConnect;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.music_origin_layout);

        deezerBox = findViewById(R.id.deezerBox);
        spotifyBox = findViewById(R.id.spotifyBox);
        folderBox = findViewById(R.id.folderBox);

        deezerConnect = new DeezerConnect(this, "315304");
        deezerConnected = new SessionStore().restore(deezerConnect, this);

        new SpotifyCrawler().execute("me");

        System.out.println("Deezer Connected : " + deezerConnected);

        // TODO : Make validate button not clickable if nothing is selected
    }

    private boolean isNetworkAvailable()
    {
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();

        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    public void clickValidate(View view)
    {
        deezerChecked = deezerBox.isChecked();
        spotifyChecked = spotifyBox.isChecked();
        folderChecked = folderBox.isChecked();

        if (!isNetworkAvailable() && (deezerChecked || spotifyChecked))
        {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);

            builder.setMessage("Ces fonctionnalités nécéssitent une connexion Internet.\n\nVérifiez votre connexion et rééssayez.")
                    .setPositiveButton("OK", new DialogInterface.OnClickListener()
                    {
                        public void onClick(DialogInterface dialog, int id)
                        {
                            //do things
                        }
                    });

            AlertDialog alert = builder.create();
            alert.show();
        }
        else
        {
            connectionSetup(INIT, true);
        }
    }

    public void connectionSetup(int origin, boolean connection)
    {
        if (origin == INIT)
        {
            if (deezerChecked)
            {
                deezerConnectionCheck();
            }
            else if (spotifyChecked)
            {
                spotifyConnectionCheck();
            }
            else
            {
                // folderConnectionCheck();
            }
        }

        if (origin == DEEZER)
        {
            deezerChecked = connection;

            if (spotifyChecked)
            {
                spotifyConnectionCheck();
            }
            else if (folderChecked)
            {
                // folderConnectionCheck();
            }
            else
            {
                goToSetupPlaylist();
            }
        }

        if (origin == SPOTIFY)
        {
            spotifyChecked = connection;

            if (folderChecked)
            {
                // folderConnectionCheck();
            }
            else
            {
                goToSetupPlaylist();
            }
        }

        if (origin == FOLDER)
        {
            folderChecked = connection;
            goToSetupPlaylist();
        }
    }

    public void deezerConnectionCheck()
    {
        if (!deezerConnected)
        {
            DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which)
                {
                    if (which == DialogInterface.BUTTON_POSITIVE)
                    {
                        deezerConnected = true;
                        deezerConnection();
                    }
                }
            };

            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setMessage("Voulez-vous vous connecter à Deezer ?").setNegativeButton("Non", dialogClickListener)
                    .setPositiveButton("Oui", dialogClickListener).show();

        }
        else
        {
            connectionSetup(DEEZER,true);
        }
    }

    public void deezerConnection()
    {
        deezerConnect = new DeezerConnect(this, "315304");

        String[] permissions = new String[]{
                Permissions.BASIC_ACCESS,
                Permissions.MANAGE_LIBRARY,
                Permissions.LISTENING_HISTORY};

        // The listener for authentication events
        DialogListener listener = new DialogListener() {
            public void onComplete(Bundle values) {
                SessionStore sessionStore = new SessionStore();
                sessionStore.save(deezerConnect, getApplicationContext());

                connectionSetup(DEEZER, true);
            }

            public void onCancel()
            {
                connectionSetup(DEEZER, false);
            }

            public void onException(Exception e)
            {
                Log.e("Deezer connection error",e.getMessage());
                connectionSetup(DEEZER, false);
            }
        };

        // Launches the authentication process
        deezerConnect.authorize(this, permissions, listener);
    }

    public void spotifyConnectionCheck()
    {
        if (!SpotifyAppRemote.isSpotifyInstalled(app))
        {
            DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which)
                {
                    if (which == DialogInterface.BUTTON_POSITIVE)
                    {
                        installingSpotify = true;
                        final String appPackageName = "com.spotify.music";
                        final String referrer = "adjust_campaign=com.fpalud.wecker&adjust_tracker=ndjczk&utm_source=adjust_preinstall";

                        try
                        {
                            Uri uri = Uri.parse("market://details")
                                    .buildUpon()
                                    .appendQueryParameter("id", appPackageName)
                                    .appendQueryParameter("referrer", referrer)
                                    .build();

                            startActivity(new Intent(Intent.ACTION_VIEW, uri));
                        }
                        catch (android.content.ActivityNotFoundException ignored)
                        {
                            Uri uri = Uri.parse("https://play.google.com/store/apps/details")
                                    .buildUpon()
                                    .appendQueryParameter("id", appPackageName)
                                    .appendQueryParameter("referrer", referrer)
                                    .build();

                            startActivity(new Intent(Intent.ACTION_VIEW, uri));
                        }
                    }
                }
            };

            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setMessage("Vous devez installer l'application Spotify pour utiliser cette fonctionnalité.\n\nSe rendre sur le Play Store ?").setNegativeButton("Non", dialogClickListener)
                    .setPositiveButton("Oui", dialogClickListener).show();
        }
        else
        {
            if (!spotifyConnected || !spotifyAPKConnected)
            {
                DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener()
                {
                    @Override
                    public void onClick(DialogInterface dialog, int which)
                    {
                        if (which == DialogInterface.BUTTON_POSITIVE)
                        {
                            spotifyConnected = true;
                            spotifyConnection();
                        }
                    }
                };

                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setMessage("Voulez-vous vous connecter à Spotify ?").setNegativeButton("Non", dialogClickListener)
                        .setPositiveButton("Oui", dialogClickListener).show();
            }
            else
            {
                connectionSetup(SPOTIFY,true);
            }
        }
    }

    public void spotifyConnection()
    {
        AuthenticationRequest.Builder builder =
                new AuthenticationRequest.Builder("46065021347f4ef3bd007487a2497d2f", TOKEN, REDIRECT_URI);

        builder.setScopes(new String[]{"streaming","user-library-read"});
        AuthenticationRequest request = builder.build();

        AuthenticationClient.openLoginActivity(this, REQUEST_CODE, request);
    }


    protected void onActivityResult(int requestCode, int resultCode, Intent intent)
    {
        super.onActivityResult(requestCode, resultCode, intent);

        // Check if result comes from the correct activity
        if (requestCode == REQUEST_CODE)
        {
            AuthenticationResponse response = AuthenticationClient.getResponse(resultCode, intent);

            if (response.getType() == TOKEN)
            {
                app = (WeckerParameters) getApplicationContext();
                app.setSpotifyToken(response.getAccessToken());

                // You only need to connect to SpotifyAppRemote to play music
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
                                connectionSetup(SPOTIFY,true);
                            }

                            @Override
                            public void onFailure(Throwable throwable)
                            {
                                Log.e("MusicOrigin", throwable.getMessage(), throwable);
                                connectionSetup(SPOTIFY,false);
                            }
                        });
            }
            else
            {
                System.out.println("Connection failed : " + response.getError());
                connectionSetup(SPOTIFY,false);
            }
        }
    }

    public void goToSetupPlaylist()
    {
        Intent intent = new Intent(this, SetupPlaylist.class);
        intent.putExtra("deezer",deezerChecked);
        intent.putExtra("spotify",spotifyChecked);
        intent.putExtra("folder",folderChecked);
        startActivity(intent);
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

                URL url = new URL("https://api.spotify.com/v1/" + strings[0]);
                HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestProperty("Authorization", "Bearer " + app.getSpotifyToken());

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

            spotifyConnected = (server_response.length() > 0);
            spotifyAPKConnected = (app.getSpotifyConnect() != null);

            System.out.println("Spotify Connected : " + spotifyConnected);
            System.out.println("Spotify APK Connected : " + spotifyAPKConnected);

            spotifyConnectionChecked = true;
        }

        JSONObject readStream(InputStream in)
        {
            BufferedReader reader = null;
            StringBuilder response = new StringBuilder();

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
