package com.fpalud.wecker;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.Toast;

import com.deezer.sdk.model.Permissions;
import com.deezer.sdk.network.connect.DeezerConnect;
import com.deezer.sdk.network.connect.SessionStore;
import com.deezer.sdk.network.connect.event.DialogListener;
import com.spotify.android.appremote.api.SpotifyAppRemote;

import java.io.File;
import yogesh.firzen.filelister.FileListerDialog;
import yogesh.firzen.filelister.OnFileSelectedListener;

public class MusicOrigin extends BaseActivity
{
    CheckBox deezerBox;
    CheckBox spotifyBox;
    CheckBox folderBox;

    ImageView deezerImage;
    ImageView spotifyImage;
    ImageView folderImage;

    AlphaAnimation buttonClick = new AlphaAnimation(1F, 0.7F); // Fading animation on image when clicked
    AlphaAnimation buttonClickRelease = new AlphaAnimation(0.7F,1F); // Unfading animation on image when clicked

    boolean deezerChecked;
    boolean spotifyChecked;
    boolean folderChecked;

    boolean installingSpotify = false;

    private static final int INIT = -1;
    private static final int DEEZER = 0;
    private static final int SPOTIFY = 1;
    private static final int FOLDER = 2;
    private static final int PARAMS = 0;

    WeckerParameters app;
    DeezerConnect deezerConnect;
    FileListerDialog fileListerDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.music_origin_layout);

        app = (WeckerParameters) getApplicationContext();

        deezerBox = findViewById(R.id.deezerBox);
        spotifyBox = findViewById(R.id.spotifyBox);
        folderBox = findViewById(R.id.folderBox);

        deezerImage = findViewById(R.id.deezerImage);
        spotifyImage = findViewById(R.id.spotifyImage);
        folderImage = findViewById(R.id.folderImage);

        buttonClick.setDuration(100);
        buttonClickRelease.setDuration(100);
        buttonClickRelease.setStartOffset(100);

        deezerImage.setOnClickListener(new View.OnClickListener()
        {
            public void onClick(View v)
            {
                if (deezerBox.isChecked())
                {
                    deezerBox.setChecked(false);
                }
                else
                {
                    deezerBox.setChecked(true);
                }

                v.startAnimation(buttonClick);
                v.startAnimation(buttonClickRelease);
            }
        });

        spotifyImage.setOnClickListener(new View.OnClickListener()
        {
            public void onClick(View v)
            {
                v.startAnimation(buttonClick);
                v.startAnimation(buttonClickRelease);

                if (spotifyBox.isChecked())
                {
                    spotifyBox.setChecked(false);
                }
                else
                {
                    spotifyBox.setChecked(true);
                }
            }
        });

        folderImage.setOnClickListener(new View.OnClickListener()
        {
            public void onClick(View v)
            {
                v.startAnimation(buttonClick);
                v.startAnimation(buttonClickRelease);

                if (folderBox.isChecked())
                {
                    folderBox.setChecked(false);
                }
                else
                {
                    folderBox.setChecked(true);
                }
            }
        });

        fileListerDialog = FileListerDialog.createFileListerDialog(this);
        fileListerDialog.setOnFileSelectedListener(new OnFileSelectedListener() {
            @Override
            public void onFileSelected(File file, String path)
            {
                app.setMusicFolderPath(path);
                connectionSetup(FOLDER, app.getMusicFolderPath().length() > 0);
            }
        });

        fileListerDialog.setDefaultDir(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM).getParentFile().getAbsolutePath());
        fileListerDialog.setFileFilter(FileListerDialog.FILE_FILTER.ALL_FILES);

        deezerBox.setChecked(app.isDeezerChecked());
        spotifyBox.setChecked(app.isSpotifyChecked());
        folderBox.setChecked(app.isFolderChecked());
    }

    @Override
    public void onResume()
    {
        super.onResume();

        if (installingSpotify)
        {
            installingSpotify = false;
            if (SpotifyAppRemote.isSpotifyInstalled(app))
            {
                spotifyConnection();
            }
        }
    }

    private boolean isNetworkAvailable()
    {
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();

        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    private boolean isReadPermissionEnabled()
    {
        String requiredPermission = "android.permission.READ_EXTERNAL_STORAGE";
        return (getApplicationContext().checkCallingOrSelfPermission(requiredPermission) == PackageManager.PERMISSION_GRANTED);
    }

    public void clickValidate(View view)
    {
        deezerChecked = deezerBox.isChecked();
        spotifyChecked = spotifyBox.isChecked();
        folderChecked = folderBox.isChecked();

        if (deezerChecked && !isNetworkAvailable())
        {
            deezerConnect = DeezerConnect.forApp("315304").build();

            if (!new SessionStore().restore(deezerConnect, this))
            {
                AlertDialog.Builder builder = new AlertDialog.Builder(this);

                builder.setMessage("Vous devez être connecté à Internet pour lier l'application à Deezer.")
                        .setPositiveButton("OK", new DialogInterface.OnClickListener()
                        {
                            public void onClick(DialogInterface dialog, int id) {}
                        });

                AlertDialog alert = builder.create();
                alert.show();
            }
        }
        else if (deezerChecked || spotifyChecked || folderChecked)
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
                deezerConnection();
            }
            else if (spotifyChecked)
            {
                spotifyConnection();
            }
            else
            {
                folderConnection();
            }
        }

        if (origin == DEEZER)
        {
            deezerChecked = connection;

            if (spotifyChecked)
            {
                spotifyConnection();
            }
            else if (folderChecked)
            {
                folderConnection();
            }
            else
            {
                if (deezerChecked)
                {
                    goToSetupPlaylist();
                }
            }
        }

        if (origin == SPOTIFY)
        {
            spotifyChecked = connection;

            if (folderChecked)
            {
                folderConnection();
            }
            else
            {
                if (deezerChecked || spotifyChecked)
                {
                    goToSetupPlaylist();
                }
            }
        }

        if (origin == FOLDER)
        {
            folderChecked = connection;

            if (deezerChecked || spotifyChecked || folderChecked)
            {
                goToSetupPlaylist();
            }
        }
    }

    public void deezerConnection()
    {
        deezerConnect = DeezerConnect.forApp("315304").build();

        if (new SessionStore().restore(deezerConnect, this))
        {
            connectionSetup(DEEZER,true);
        }
        else
        {
            String[] permissions = new String[]{
                    Permissions.BASIC_ACCESS,
                    Permissions.MANAGE_LIBRARY,
                    Permissions.LISTENING_HISTORY};

            // The listener for authentication events
            DialogListener listener = new DialogListener() {
                public void onComplete(Bundle values)
                {
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
    }

    public void spotifyConnection()
    {
        if (!SpotifyAppRemote.isSpotifyInstalled(app))
        {
            DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which)
                {
                    if (which == DialogInterface.BUTTON_POSITIVE)
                    {
                        if (isNetworkAvailable())
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
                        else
                        {
                            Toast.makeText(app, "Pas de connexion Internet.", Toast.LENGTH_SHORT).show();
                        }
                    }
                    else
                    {
                        connectionSetup(SPOTIFY, false);
                    }
                }
            };

            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setMessage("Vous devez installer l'application Spotify pour utiliser cette fonctionnalité.\n\nSe rendre sur le Play Store ?").setNegativeButton("Non", dialogClickListener)
                    .setPositiveButton("Oui", dialogClickListener).show();
        }
        else
        {
            connectionSetup(SPOTIFY,true);
        }
    }

    public void folderConnection()
    {
        app = (WeckerParameters) getApplicationContext();

        if (!isReadPermissionEnabled())
        {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 1);
        }
        else if (app.getMusicFolderPath().length() == 0)
        {
            DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which)
                {
                    if (which == DialogInterface.BUTTON_POSITIVE)
                    {
                        fileListerDialog.show();
                    }
                    else
                    {
                        connectionSetup(FOLDER, false);
                    }
                }
            };

            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setMessage("Localiser le dossier Musique ?").setNegativeButton("Non", dialogClickListener)
                    .setPositiveButton("Oui", dialogClickListener).show();
        }
        else
        {
            connectionSetup(FOLDER, true);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults)
    {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (grantResults[0]== PackageManager.PERMISSION_GRANTED)
        {
            folderConnection();
        }
        else
        {
            connectionSetup(FOLDER, false);
        }
    }

    public void goToSetupPlaylist()
    {
        app = (WeckerParameters) getApplicationContext();

        app.setDeezerChecked(deezerChecked);
        app.setSpotifyChecked(spotifyChecked);
        app.setFolderChecked(folderChecked);

        if (getIntent().getIntExtra("origin",PARAMS) == PARAMS)
        {
            finish();
        }
        else
        {
            Intent intent = new Intent(this, SetupPlaylist.class);
            startActivity(intent);
        }
    }
}
