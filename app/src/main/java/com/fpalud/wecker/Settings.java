package com.fpalud.wecker;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import java.io.File;

import yogesh.firzen.filelister.FileListerDialog;
import yogesh.firzen.filelister.OnFileSelectedListener;

public class Settings extends BaseActivity
{
    WeckerParameters app;

    TextView folderLocation;
    TextView defaultLocation;

    LinearLayout folderlayout;
    LinearLayout defaultLayout;

    Button folderExplanation;
    Button defaultExplanation;
    Button musicExplanation;

    CheckBox vibrationChillMode;
    TextView vibrationText;

    AlphaAnimation buttonClick = new AlphaAnimation(1F, 0.7F); // Fading animation on text when clicked
    AlphaAnimation buttonClickRelease = new AlphaAnimation(0.7F,1F); // Unfading animation on text when clicked

    FileListerDialog fileListerDialog;
    boolean isFolderLocation;

    SeekBar volumeBar;
    MediaPlayer mediaPlayer;
    AudioManager audioManager;
    int maxVolume;

    private static final int PARAMS = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.settings_layout);

        folderLocation = findViewById(R.id.folderLocationText);
        defaultLocation = findViewById(R.id.defaultLocationText);

        folderlayout = findViewById(R.id.folderLocationLayout);
        defaultLayout = findViewById(R.id.defaultLocationLayout);
        volumeBar = findViewById(R.id.volume);

        vibrationChillMode = findViewById(R.id.vibrateBox);
        vibrationText = findViewById(R.id.vibrationText);

        app = (WeckerParameters) getApplicationContext();

        System.out.println(app.getDefaultTrack().getName());

        try
        {
            mediaPlayer = new MediaPlayer();
            mediaPlayer.setDataSource(app.getDefaultTrack().getAbsolutePath());
            mediaPlayer.prepare();

            defaultLocation.setText(app.getDefaultTrack().getName());
        }
        catch (Exception e)
        {
            // mediaPlayer = MediaPlayer.create(this, getResources().getIdentifier("deja_vu", "raw", getPackageName()));
        }

        vibrationChillMode.setChecked(app.isVibrationChillMode());

        // mediaPlayer.setLooping(true);
        audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        maxVolume = audioManager.getStreamMaxVolume(audioManager.STREAM_MUSIC);

        double percentage = (double) app.getAlarmVolume() / (double) maxVolume;

        volumeBar.setProgress((int) Math.round(10 * percentage));
        volumeBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener()
        {
            @Override
            public void onStopTrackingTouch(SeekBar seekBar)
            {
                //mediaPlayer.pause();
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar)
            {
                //mediaPlayer.start();
            }

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser)
            {
                double percentage = (double) progress / 10;
                int actualVolume = (int) Math.round(maxVolume * percentage);

                audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, actualVolume, 0);
                app.setAlarmVolume(actualVolume);
            }
        });

        buttonClick.setDuration(100);
        buttonClickRelease.setDuration(100);
        buttonClickRelease.setStartOffset(100);

        fileListerDialog = FileListerDialog.createFileListerDialog(this);
        fileListerDialog.setOnFileSelectedListener(new OnFileSelectedListener() {
            @Override
            public void onFileSelected(File file, String path)
            {
                if (isFolderLocation)
                {
                    if (file.isDirectory())
                    {
                        app.setMusicFolderPath(path);
                        folderLocation.setText(path);
                    }
                }
                else
                {
                    if (file.isFile())
                    {
                        app.setDefaultTrack(file);
                        defaultLocation.setText(app.getDefaultTrack().getName());

                        try
                        {
                            mediaPlayer = new MediaPlayer();
                            mediaPlayer.setDataSource(app.getDefaultTrack().getAbsolutePath());
                            mediaPlayer.prepare();
                        }
                        catch (Exception e)
                        {
                            //int resID = getResources().getIdentifier("deja_vu", "raw", getPackageName());
                            //mediaPlayer = MediaPlayer.create(app, resID);
                        }

                        //mediaPlayer.setLooping(true);
                    }
                }
            }
        });

        fileListerDialog.setDefaultDir(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM).getParentFile().getAbsolutePath());
        fileListerDialog.setFileFilter(FileListerDialog.FILE_FILTER.ALL_FILES);

        folderlayout.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                view.startAnimation(buttonClick);
                view.startAnimation(buttonClickRelease);

                if (!isReadPermissionEnabled())
                {
                    requestPermission();
                }
                else
                {
                    isFolderLocation = true;

                    if (app.getMusicFolderPath().length() > 0)
                    {
                        fileListerDialog.setDefaultDir(app.getMusicFolderPath());
                    }
                    else
                    {
                        fileListerDialog.setDefaultDir(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM).getParentFile().getAbsolutePath());
                    }

                    fileListerDialog.show();
                }
            }
        });

        defaultLayout.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                view.startAnimation(buttonClick);
                view.startAnimation(buttonClickRelease);

                if (!isReadPermissionEnabled())
                {
                    requestPermission();
                }
                else
                {
                    isFolderLocation = false;

                    if (app.getDefaultTrack().length() > 0)
                    {
                        fileListerDialog.setDefaultDir(app.getDefaultTrack().getParent());
                    }
                    else if (app.getMusicFolderPath().length() > 0)
                    {
                        fileListerDialog.setDefaultDir(app.getMusicFolderPath());
                    }
                    else
                    {
                        fileListerDialog.setDefaultDir(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM).getParentFile().getAbsolutePath());
                    }

                    fileListerDialog.show();
                }

            }
        });

        vibrationChillMode.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                app.setVibrationChillMode(vibrationChillMode.isChecked());
            }
        });

        vibrationText.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                view.startAnimation(buttonClick);
                view.startAnimation(buttonClickRelease);

                if (vibrationChillMode.isChecked())
                {
                    vibrationChillMode.setChecked(false);
                    app.setVibrationChillMode(false);
                }
                else
                {
                    vibrationChillMode.setChecked(true);
                    app.setVibrationChillMode(true);
                }
            }
        });
    }

    @Override
    public void onResume()
    {
        super.onResume();

        if (app.getMusicFolderPath().length() > 0)
        {
            folderLocation.setText(app.getMusicFolderPath());
        }

        folderLocation.setSelected(true);
    }

    public void goToMusicOrigin(View view)
    {
        Intent intent = new Intent(this, MusicOrigin.class);
        intent.putExtra("origin",PARAMS);
        startActivity(intent);
    }

    public void returnToHome(View view)
    {
        finish();
    }

    public void musicFolderInstructions(View view)
    {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        builder.setMessage("Dossier musique\n\n"+
                "Contient toutes les musiques lues par l'alarme.")
                .setPositiveButton("OK", new DialogInterface.OnClickListener()
                {
                    public void onClick(DialogInterface dialog, int id) {}
                });

        AlertDialog alert = builder.create();
        alert.show();
    }

    public void defaultAlarmInstructions(View view)
    {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        builder.setMessage("Alarme par défaut\n\n"+
                "Se lance à la place de l'alarme classique lorsqu'un problème survient.")
                .setPositiveButton("OK", new DialogInterface.OnClickListener()
                {
                    public void onClick(DialogInterface dialog, int id) {}
                });

        AlertDialog alert = builder.create();
        alert.show();
    }

    public void musicModeExplanations(View view)
    {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        builder.setMessage("Mode Musique\n\n"+
                "Permet de faire défiler des musiques aléatoires en se réveillant.\n\n" +
                "Une vibration peut survenir entre chaque musique pour garantir le réveil.")
                .setPositiveButton("OK", new DialogInterface.OnClickListener()
                {
                    public void onClick(DialogInterface dialog, int id) {}
                });

        AlertDialog alert = builder.create();
        alert.show();
    }

    public boolean isReadPermissionEnabled()
    {
        String requiredPermission = "android.permission.READ_EXTERNAL_STORAGE";
        return (getApplicationContext().checkCallingOrSelfPermission(requiredPermission) == PackageManager.PERMISSION_GRANTED);
    }

    public void requestPermission()
    {
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 1);
    }
}
