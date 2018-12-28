package com.fpalud.wecker;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Handler;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.support.constraint.ConstraintLayout;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.WindowManager;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.deezer.sdk.model.Track;
import com.deezer.sdk.network.connect.DeezerConnect;
import com.deezer.sdk.network.connect.SessionStore;
import com.deezer.sdk.network.request.DeezerRequest;
import com.deezer.sdk.network.request.event.JsonRequestListener;
import com.deezer.sdk.network.request.event.RequestListener;
import com.deezer.sdk.player.TrackPlayer;
import com.deezer.sdk.player.event.OnPlayerErrorListener;
import com.deezer.sdk.player.event.OnPlayerStateChangeListener;
import com.deezer.sdk.player.event.PlayerState;
import com.deezer.sdk.player.networkcheck.WifiAndMobileNetworkStateChecker;

import com.spotify.android.appremote.api.ConnectionParams;
import com.spotify.android.appremote.api.Connector;
import com.spotify.android.appremote.api.PlayerApi;
import com.spotify.android.appremote.api.SpotifyAppRemote;
import com.spotify.protocol.client.CallResult;
import com.spotify.protocol.client.ErrorCallback;
import com.spotify.protocol.client.Subscription;
import com.spotify.protocol.types.Empty;
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
import java.util.Calendar;
import java.util.Collection;
import java.util.List;
import java.util.Random;

import static android.view.View.GONE;
import static com.deezer.sdk.player.event.PlayerState.PLAYBACK_COMPLETED;
import static com.spotify.sdk.android.authentication.AuthenticationResponse.Type.TOKEN;

public class AlarmScreen extends BaseActivity
{
    int trackId = 0;
    String selectedPlaylistID = "";
    String selectedTrackID = "";

    ArrayList<Integer> trackNumber = new ArrayList<>();
    ArrayList<String> idList = new ArrayList<>();
    Collection<File> musicList;

    WeckerParameters app;
    DeezerConnect deezerConnect;
    ConstraintLayout activityLayout;

    Alarm alarm;
    Vibrator vibrator;

    TrackPlayer trackPlayer;
    PlayerApi spotifyPlayer;
    MediaPlayer mediaPlayer;

    Handler timeHandler;
    TextView currentTime;

    com.spotify.protocol.types.Track spotifyTrackPlayed;

    int alarmId = 0;
    int id = 0;
    int offset = 0;
    int musicOrigin;
    int nextMusicOrigin;

    boolean atLeastITriedSpotify = false;
    boolean atLeastITriedAPKSpotify = false;
    boolean spotifyConnectionChecked = false;
    boolean changingSpotifySong = false;
    boolean newSpotifyTrack = false;
    boolean newDefaultAlarm = true;
    int pauseNextSong = -5;

    String spotifyEndpoint;
    String spotifyOffset;

    boolean chillMode = false;
    JSONArray trackJSONList;
    JSONObject jsonTrack;
    Track deezerTrack;

    private static final int REQUEST_CODE = 1337;
    private static final String REDIRECT_URI = "wecker://callback";

    private static final int DEEZER = 0;
    private static final int SPOTIFY = 1;
    private static final int FOLDER = 2;

    final String[] SUFFIX = {"mp3","wma","3gp","mp4","m4a","aac","flac","gsm","mkv","wav","ogg","ts"};  // use the suffix to filter

    RelativeLayout validateRelative;
    ImageView validateImage;
    int validateSize;
    Circle whiteCircleValidate;
    Circle darkCircleValidate;
    CircleAngleAnimation animationValidate;

    RelativeLayout musicRelative;
    ImageView musicImage;
    int musicSize;
    Circle whiteCircleMusic;
    Circle darkCircleMusic;
    CircleAngleAnimation animationMusic;

    RelativeLayout snoozeRelative;
    ImageView snoozeImage;
    int snoozeSize;
    Circle whiteCircleSnooze;
    Circle darkCircleSnooze;
    CircleAngleAnimation animationSnooze;

    TextView songName;
    TextView snoozeValue;
    ImageView plusButton;
    ImageView minusButton;

    boolean pressingValidate = false;
    boolean pressingSnooze = false;
    boolean pressingMusic = false;

    AlphaAnimation buttonClick = new AlphaAnimation(1F, 0.7F); // Fading animation on image when clicked
    AlphaAnimation buttonClickRelease = new AlphaAnimation(0.7F,1F); // Unfading animation on image when clicked

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.wake_up_screen_layout);

        activityLayout = findViewById(R.id.activityLayout);

        for (int i = 0 ; i < activityLayout.getChildCount() ; i++)
        {
            activityLayout.getChildAt(i).setVisibility(View.INVISIBLE);
        }

        app = (WeckerParameters) getApplicationContext();

        validateImage = findViewById(R.id.validateImage);
        musicImage = findViewById(R.id.musicImage);
        snoozeImage = findViewById(R.id.snoozeImage);

        validateRelative = findViewById(R.id.validateRelative);
        musicRelative = findViewById(R.id.musicRelative);
        snoozeRelative = findViewById(R.id.snoozeRelative);

        songName = findViewById(R.id.songName);
        plusButton = findViewById(R.id.plusButton);
        minusButton = findViewById(R.id.minusButton);
        snoozeValue = findViewById(R.id.snoozeValue);

        currentTime = findViewById(R.id.currentTime);

        buttonClick.setDuration(100);
        buttonClickRelease.setDuration(100);
        buttonClickRelease.setStartOffset(100);

        plusButton.setOnClickListener(new View.OnClickListener()
        {
            public void onClick(View v)
            {
                v.startAnimation(buttonClick);
                v.startAnimation(buttonClickRelease);

                int minutes = Integer.valueOf(snoozeValue.getText().toString().substring(0, snoozeValue.getText().toString().length() - 4));

                if (minutes < 60)
                {
                    snoozeValue.setText(String.valueOf(minutes + 5) + " min");
                }
            }
        });

        minusButton.setOnClickListener(new View.OnClickListener()
        {
            public void onClick(View v)
            {
                v.startAnimation(buttonClick);
                v.startAnimation(buttonClickRelease);

                int minutes = Integer.valueOf(snoozeValue.getText().toString().substring(0, snoozeValue.getText().toString().length() - 4));

                if (minutes > 5)
                {
                    snoozeValue.setText(String.valueOf(minutes - 5) + " min");
                }
            }
        });

        validateRelative.setOnTouchListener(new View.OnTouchListener()
        {
            @Override
            public boolean onTouch(View v, MotionEvent event)
            {
                if (!pressingMusic && !pressingSnooze)
                {
                    if (event.getAction() == MotionEvent.ACTION_DOWN)
                    {
                        pressingValidate = true;
                        hideValidateCircle();
                    }
                    else if (event.getAction() == MotionEvent.ACTION_UP)
                    {
                        pressingValidate = false;
                        animationValidate.cancel();
                        darkCircleValidate.setVisibility(GONE);
                    }
                }

                return true;
            }
        });

        musicRelative.setOnTouchListener(new View.OnTouchListener()
        {
            @Override
            public boolean onTouch(View v, MotionEvent event)
            {
                if (!pressingSnooze && !pressingValidate)
                {
                    if (event.getAction() == MotionEvent.ACTION_DOWN)
                    {
                        pressingMusic = true;
                        hideMusicCircle();
                    }
                    else if (event.getAction() == MotionEvent.ACTION_UP)
                    {
                        pressingMusic = false;
                        animationMusic.cancel();
                        darkCircleMusic.setVisibility(GONE);
                    }
                }

                return true;
            }
        });

        snoozeRelative.setOnTouchListener(new View.OnTouchListener()
        {
            @Override
            public boolean onTouch(View v, MotionEvent event)
            {
                if (!pressingMusic && !pressingValidate)
                {
                    if (event.getAction() == MotionEvent.ACTION_DOWN)
                    {
                        pressingSnooze = true;
                        hideSnoozeCircle();
                    }
                    else if (event.getAction() == MotionEvent.ACTION_UP)
                    {
                        pressingSnooze = false;
                        animationSnooze.cancel();
                        darkCircleSnooze.setVisibility(GONE);
                    }
                }

                return true;
            }
        });

        snoozeImage.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout()
            {
                snoozeImage.getViewTreeObserver().removeOnGlobalLayoutListener(this);

                snoozeSize = snoozeImage.getHeight();
                musicSize = snoozeSize;

                createMusicCircle();
                createSnoozeCircle();
            }
        });

        int screenWidth = Resources.getSystem().getDisplayMetrics().widthPixels;
        validateSize = screenWidth / 2;
        createValidateCircle();

        AudioManager audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, app.getAlarmVolume(), 0);

        timeHandler = new Handler();
        timeHandler.post(new Runnable()
        {
            @Override
            public void run()
            {
                timeHandler.postDelayed(this, 1000);

                Calendar date = Calendar.getInstance();
                if (date.get(Calendar.MINUTE) < 10)
                {
                    currentTime.setText(date.get(Calendar.HOUR_OF_DAY) + ":0" + date.get(Calendar.MINUTE));
                }
                else
                {
                    currentTime.setText(date.get(Calendar.HOUR_OF_DAY) + ":" + date.get(Calendar.MINUTE));
                }

            }
        });

        vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);

        alarmId = getIntent().getIntExtra("alarmId",-1);

        if (alarmId != -1)
        {
            for (Alarm a : app.getAlarmList())
            {
                if (a.getId() == alarmId)
                {
                    alarm = a;
                    idList = a.getIdSongsList();
                    break;
                }
            }

            idList = alarm.getIdSongsList();

            if (idList.size() > 0)
            {
                getTrackLists();
            }
        }
    }

    public void launchAlarm()
    {
        System.out.println("launch alarm");

        app = (WeckerParameters) getApplicationContext();

        if (chillMode)
        {
            if (app.isVibrationChillMode())
            {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
                {
                    vibrator.vibrate(VibrationEffect.createOneShot(2000, VibrationEffect.DEFAULT_AMPLITUDE));
                }
                else
                {
                    vibrator.vibrate(2000);
                }
            }
        }
        else if (alarm.isVibration())
        {
            long[] pattern = {0, 2000, 1000}; // delay - duration - number of times

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
            {
                vibrator.vibrate(VibrationEffect.createWaveform(pattern, VibrationEffect.DEFAULT_AMPLITUDE));
            }
            else
            {
                vibrator.vibrate(pattern, 0);
            }
        }

        System.out.println("Get flags");

        runOnUiThread(new Runnable()
        {
            @Override
            public void run()
            {
                for (int i = 0 ; i < activityLayout.getChildCount() ; i++)
                {
                    activityLayout.getChildAt(i).setVisibility(View.VISIBLE);
                }

                getWindow().addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED);
                getWindow().addFlags(WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD);
                getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
                getWindow().addFlags(WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON);
            }
        });

        System.out.println("Got flags");
    }

    public void launchMusic(int origin)
    {
        if (origin == FOLDER)
        {
            mediaPlayer = new MediaPlayer();

            try
            {
                musicOrigin = FOLDER;

                if (alarm.isRandomSong())
                {
                    System.out.println(((File) musicList.toArray()[trackId]).getAbsolutePath());
                    mediaPlayer.setDataSource(((File) musicList.toArray()[trackId]).getAbsolutePath());
                    songName.setText(((File) musicList.toArray()[trackId]).getName());
                }
                else
                {
                    System.out.println(alarm.getSelectedSong().getAbsolutePath());
                    mediaPlayer.setDataSource(alarm.getSelectedSong().getAbsolutePath());
                    songName.setText(alarm.getSelectedSong().getName());
                }

                mediaPlayer.prepare();

                mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                    public void onCompletion(MediaPlayer mp)
                    {
                        System.out.println("Ouiiii");

                        mediaPlayer.stop();
                        mediaPlayer.release();

                        if (chillMode)
                        {
                            launchMusic(nextMusicOrigin);
                        }
                        else
                        {
                            launchMusic(FOLDER);
                        }
                    }
                });

                launchAlarm();
                mediaPlayer.start();

                if (chillMode)
                {
                    startAgain("Folder");
                }
            }
            catch (Exception e)
            {
                e.printStackTrace();
                defaultAlarm();
            }
        }
        else if (origin == DEEZER)
        {
            System.out.println("ORIGIN DEEZER");

            try
            {
                musicOrigin = DEEZER;

                trackPlayer = new TrackPlayer(getApplication(), deezerConnect, new WifiAndMobileNetworkStateChecker());
                trackPlayer.addOnPlayerStateChangeListener(new OnPlayerStateChangeListener() {
                    @Override
                    public void onPlayerStateChange(PlayerState playerState, long l)
                    {
                        if (playerState == PLAYBACK_COMPLETED)
                        {
                            trackPlayer.stop();
                            trackPlayer.release();

                            if (chillMode)
                            {
                                launchMusic(nextMusicOrigin);
                            }
                            else
                            {
                                launchMusic(DEEZER);
                            }
                        }
                    }
                });

                trackPlayer.addOnPlayerErrorListener(new OnPlayerErrorListener()
                {
                    @Override
                    public void onPlayerError(Exception ex, long t)
                    {
                        defaultAlarm();
                    }
                });

                songName.setText(deezerTrack.getArtist().getName() + " - " + deezerTrack.getTitle());

                System.out.println("Track for Deezer chosen");

                launchAlarm();
                trackPlayer.playTrack(deezerTrack.getId());

                if (chillMode)
                {
                    startAgain("Deezer");
                }
            }
            catch (Exception e)
            {
                System.out.println(e.getMessage());
                defaultAlarm();
            }
        }
        else if (origin == SPOTIFY)
        {
            System.out.println("ORIGIN SPOTIFY");

            try
            {
                musicOrigin = SPOTIFY;

                if (alarm.isRandomSong())
                {
                    jsonTrack = (JSONObject) trackJSONList.optJSONObject(trackId).get("track");
                }

                String artist = (((JSONArray) jsonTrack.get("artists")).optJSONObject(0)).getString("name");
                String title = jsonTrack.getString("name");

                songName.setText(artist + " - " + title);
                changingSpotifySong = false;
                pauseNextSong = -5;

                app = (WeckerParameters) getApplicationContext();
                spotifyPlayer = app.getSpotifyConnect().getPlayerApi();
                spotifyPlayer.setRepeat(2); // Always repeat
                CallResult<Empty> playSong = spotifyPlayer.play("spotify:track:" + jsonTrack.get("id"));

                playSong.setResultCallback(new CallResult.ResultCallback<Empty>() {
                    @Override
                    public void onResult(Empty empty)
                    {
                        launchAlarm();

                        spotifyPlayer.getPlayerState().setResultCallback(new CallResult.ResultCallback<com.spotify.protocol.types.PlayerState>() {
                            @Override
                            public void onResult(final com.spotify.protocol.types.PlayerState playerState)
                            {
                                spotifyTrackPlayed = playerState.track;
                                System.out.println(spotifyTrackPlayed.name);

                                spotifyPlayer.subscribeToPlayerState().setEventCallback(new Subscription.EventCallback<com.spotify.protocol.types.PlayerState>()
                                {
                                    @Override
                                    public void onEvent(com.spotify.protocol.types.PlayerState playerState)
                                    {
                                        System.out.println("PLAYER STATE");
                                        System.out.println(playerState.playbackPosition + " - " + playerState.isPaused + " - " + playerState.track);

                                        if (pauseNextSong >= 0 && playerState.playbackPosition > 0)
                                        {
                                            if (pauseNextSong < 5)
                                            {
                                                pauseNextSong++;
                                                spotifyPlayer.pause();

                                                System.out.println("PAUSEUH - " + pauseNextSong);
                                            }
                                            else if (!playerState.isPaused)
                                            {
                                                pauseNextSong++;
                                                spotifyPlayer.pause();

                                                System.out.println("PAUSEUH (parce que pas en pause) - " + pauseNextSong);
                                            }
                                        }
                                        else if (chillMode)
                                        {
                                            System.out.println("Entered");
                                            System.out.println(newSpotifyTrack + " - " + !changingSpotifySong);

                                            if (newSpotifyTrack) // Getting data for next track
                                            {
                                                System.out.println("Getting new data !");

                                                newSpotifyTrack = false;
                                                System.out.println("######################## " + newSpotifyTrack + " #############################");
                                                spotifyPlayer.setRepeat(0);

                                                try
                                                {
                                                    if (playerState.playbackPosition == 0)
                                                    {
                                                        System.out.println("Song hasn't started, reboot");

                                                        newSpotifyTrack = true;
                                                        spotifyPlayer.setRepeat(1);
                                                    }
                                                    else
                                                    {
                                                        spotifyTrackPlayed = playerState.track;
                                                        spotifyPlayer.queue("spotify:track:" + ((JSONObject) trackJSONList.optJSONObject(trackId).get("track")).get("id"))
                                                                .setErrorCallback(new ErrorCallback()
                                                                {
                                                                    @Override
                                                                    public void onError(Throwable throwable)
                                                                    {
                                                                        System.out.println("Error while queuing");
                                                                    }
                                                                });

                                                        System.out.println("Queue " + ((JSONObject) trackJSONList.optJSONObject(trackId).get("track")).get("name"));
                                                    }
                                                }
                                                catch (Exception e)
                                                {
                                                    System.out.println(e.getMessage());
                                                    defaultAlarm();
                                                }
                                            }

                                            if (!changingSpotifySong)
                                            {
                                                if (playerState.playbackPosition < 500 && musicOrigin == SPOTIFY)
                                                {
                                                    try
                                                    {
                                                        System.out.println("Next music origin " + nextMusicOrigin);

                                                        if (nextMusicOrigin == SPOTIFY)
                                                        {
                                                            System.out.println("New Spotify track");

                                                            changingSpotifySong = true;

                                                            new Handler().postDelayed(new Runnable() {
                                                                @Override
                                                                public void run()
                                                                {
                                                                    changingSpotifySong = false;
                                                                }
                                                            },1000);

                                                            launchAlarm();
                                                            startAgain("Spotify");
                                                        }
                                                        else
                                                        {
                                                            System.out.println("New other track");

                                                            if (playerState.playbackPosition > 0)
                                                            {
                                                                System.out.println("Launch pause in the present");

                                                                pauseNextSong = 0;
                                                                spotifyPlayer.pause();
                                                            }
                                                            else
                                                            {
                                                                System.out.println("Launch pause in the future");

                                                                new Handler().postDelayed(new Runnable() {
                                                                    @Override
                                                                    public void run()
                                                                    {
                                                                        pauseNextSong = 0;
                                                                        spotifyPlayer.pause();

                                                                        System.out.println("Launch pause");
                                                                    }
                                                                },500);
                                                            }

                                                            launchMusic(nextMusicOrigin);
                                                        }
                                                    }
                                                    catch (Exception e)
                                                    {
                                                        System.out.println(e.getMessage());
                                                        defaultAlarm();
                                                    }
                                                }
                                                else
                                                {
                                                    if (musicOrigin != SPOTIFY)
                                                    {
                                                        System.out.println("Music does not come from Spotify " + pauseNextSong);

                                                        if (pauseNextSong < 5 && !playerState.isPaused)
                                                        {
                                                            if (playerState.playbackPosition > 0)
                                                            {
                                                                System.out.println("Launch pause in the present v2");

                                                                pauseNextSong = 0;
                                                                spotifyPlayer.pause();
                                                            }
                                                            else
                                                            {
                                                                System.out.println("Launch pause in the future v2");

                                                                new Handler().postDelayed(new Runnable() {
                                                                    @Override
                                                                    public void run()
                                                                    {
                                                                        pauseNextSong = 0;
                                                                        spotifyPlayer.pause();

                                                                        System.out.println("Launch pause v2");
                                                                    }
                                                                },100);
                                                            }
                                                        }
                                                    }
                                                    else
                                                    {
                                                        System.out.println("Too late in the song");
                                                    }
                                                }
                                            }
                                            else
                                            {
                                                spotifyTrackPlayed = playerState.track;
                                                songName.setText(spotifyTrackPlayed.artist.name + " - " + spotifyTrackPlayed.name);
                                            }
                                        }
                                        else
                                        {
                                            spotifyTrackPlayed = playerState.track;
                                            songName.setText(spotifyTrackPlayed.artist.name + " - " + spotifyTrackPlayed.name);
                                        }
                                    }
                                });
                            }
                        });
                    }
                });

                playSong.setErrorCallback(new ErrorCallback()
                {
                    @Override
                    public void onError(Throwable throwable)
                    {
                        System.out.println("Error while playing");
                    }
                });
            }
            catch (Exception e)
            {
                System.out.println(e.getMessage());
                defaultAlarm();
            }
        }
    }

    public void cancelMusic(boolean snooze)
    {
        System.out.println("cancel music");

        if (snooze)
        {
            AlarmManager alarmManager = (AlarmManager) this.getSystemService(Context.ALARM_SERVICE);

            Intent intent = new Intent(this, LaunchAlarm.class);
            intent.putExtra("alarmId",alarmId);
            PendingIntent sender = PendingIntent.getBroadcast(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);

            int minutes = Integer.valueOf(snoozeValue.getText().toString().substring(0, snoozeValue.getText().toString().length() - 4));

            Calendar futureDate = Calendar.getInstance();
            futureDate.set(Calendar.MINUTE, futureDate.get(Calendar.MINUTE) + minutes);
            futureDate.set(Calendar.SECOND,0);

            alarmManager.setExact(AlarmManager.RTC_WAKEUP, futureDate.getTimeInMillis(), sender);
        }
        else
        {
            SetupAlarm.setupAlarm(alarm, this);
        }

        System.out.println("Setup alarm done");

        if (trackPlayer != null)
        {
            trackPlayer.stop();
            trackPlayer.release();
        }

        if (spotifyPlayer != null)
        {
            spotifyPlayer.pause();
        }

        if (mediaPlayer != null)
        {
            mediaPlayer.stop();
        }

        System.out.println("Players done");

        vibrator.cancel();
        System.out.println("vibrator done");

        app = (WeckerParameters) getApplicationContext();
        if (app.getSpotifyConnect() != null && app.getSpotifyConnect().isConnected())
        {
            SpotifyAppRemote.disconnect(app.getSpotifyConnect());
        }

        System.out.println("Just finish");

        finish();
        moveTaskToBack(true);
    }

    public void startAgain(String origin)
    {
        trackNumber = new ArrayList<>();
        offset = 0;
        id = 0;

        System.out.println("Start again origin : " + origin);

        getTrackLists();
    }

    public void getTrackLists()
    {
        app = (WeckerParameters) getApplicationContext();
        String playlistId = idList.get(id);

        System.out.println("Starting again");

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

                    public void onUnparsedResult(String requestResponse, Object requestId) {defaultAlarm();}
                    public void onException(Exception e, Object requestId) {defaultAlarm();}
                };

                Bundle bundle = new Bundle(1);
                bundle.putString("limit","2000");
                DeezerRequest request = new DeezerRequest("playlist/" + playlistId + "/tracks", bundle);
                deezerConnect.requestAsync(request, listener);
            }
            else
            {
                System.out.println("Deezer not connected");
                defaultAlarm();
            }
        }
        else // Spotify playlist
        {
            System.out.println("Trying Spotify");

            getWindow().addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED);
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD);
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON);

            if (app.getSpotifyConnect() == null || !app.getSpotifyConnect().isConnected())
            {
                System.out.println("Connected null");

                if (atLeastITriedAPKSpotify)
                {
                    System.out.println("AT LEAST I TRIED APK");
                    defaultAlarm();
                }
                else
                {
                    System.out.println("Trying APK connected");
                    spotifyAPKConnect();
                }
            }
            else
            {
                System.out.println("Connected is not null");

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
                        defaultAlarm();
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

            if (alarm.isRandomSong())
            {
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

                System.out.println(selectedPlaylistID);
                System.out.println(trackId);

                if ("folderMusic".equals(selectedPlaylistID))
                {
                    if (!chillMode)
                    {
                        launchMusic(FOLDER);
                    }
                    else
                    {
                        nextMusicOrigin = FOLDER;
                    }
                }
                else if (selectedPlaylistID.length() <= 10)
                {
                    RequestListener listener = new JsonRequestListener()
                    {
                        public void onResult(Object result, Object requestId)
                        {
                            List<Track> trackList = (List<Track>) result;
                            deezerTrack = trackList.get(trackId);

                            if (!chillMode)
                            {
                                launchMusic(DEEZER);
                            }
                            else
                            {
                                nextMusicOrigin = DEEZER;
                            }
                        }

                        public void onUnparsedResult(String requestResponse, Object requestId) {defaultAlarm();}
                        public void onException(Exception e, Object requestId) {defaultAlarm();}
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

                        System.out.println("Offset ? " +  offset);

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
            else
            {
                selectedTrackID = alarm.getSelectedSongId();

                if (selectedTrackID == null)
                {
                    launchMusic(FOLDER);
                }
                else if (selectedTrackID.length() <= 10)
                {
                    RequestListener listener = new JsonRequestListener()
                    {
                        public void onResult(Object result, Object requestId)
                        {
                            deezerTrack = (Track) result;
                            launchMusic(DEEZER);
                        }

                        public void onUnparsedResult(String requestResponse, Object requestId) {defaultAlarm();}
                        public void onException(Exception e, Object requestId) {defaultAlarm();}
                    };

                    Bundle bundle = new Bundle(1);
                    bundle.putString("limit","1");
                    DeezerRequest request = new DeezerRequest("track/" + selectedTrackID, bundle);
                    deezerConnect.requestAsync(request, listener);
                }
                else
                {
                    new SpotifyCrawler().execute("tracks/" + selectedTrackID, "single");
                }
            }

            app = (WeckerParameters) getApplicationContext();
        }
    }

    public class SpotifyCrawler extends AsyncTask<String, Void, String>
    {
        JSONObject server_response = new JSONObject();
        String endpoint = "";
        String offset = "0";
        String limit = "100";
        boolean offsetNull = true;
        boolean singleTrack = false;

        @Override
        protected String doInBackground(String... strings)
        {
            try
            {
                app = (WeckerParameters) getApplicationContext();

                endpoint = strings[0];
                spotifyEndpoint = endpoint;

                if (strings.length > 1)
                {
                    offset = strings[1];

                    if ("single".equals(offset))
                    {
                        singleTrack = true;
                    }

                    spotifyOffset = offset;
                    offsetNull = false;
                }
                else
                {
                    spotifyOffset = null;
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
                defaultAlarm();
            }
            catch (IOException e)
            {
                System.out.println("IOException : " + e.getMessage());
                defaultAlarm();
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

                    if (chillMode)
                    {
                        spotifyConnect();
                    }
                }
                else if (singleTrack)
                {
                    jsonTrack = server_response;
                    launchMusic(SPOTIFY);
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
                                defaultAlarm();
                            }
                        }
                        catch (Exception e)
                        {
                            System.out.println(e.getMessage());
                            defaultAlarm();
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
                                defaultAlarm();
                            }
                        }
                        catch (Exception e)
                        {
                            System.out.println(e.getMessage());
                            defaultAlarm();
                        }
                    }
                }
                else
                {
                    try
                    {
                        System.out.println("Launching music");

                        trackJSONList = (JSONArray) server_response.get("items");

                        if (trackJSONList != null)
                        {
                            if (!chillMode)
                            {
                                launchMusic(SPOTIFY);
                            }
                            else
                            {
                                nextMusicOrigin = SPOTIFY;

                                if (musicOrigin == SPOTIFY)
                                {
                                    spotifyPlayer.setRepeat(1);
                                    newSpotifyTrack = true;
                                }
                            }
                        }
                    }
                    catch (Exception e)
                    {
                        System.out.println(e.getMessage());
                        defaultAlarm();
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
                defaultAlarm();
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
                        defaultAlarm();
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
                defaultAlarm();
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

                if (chillMode)
                {
                    if (spotifyOffset == null)
                    {
                        new SpotifyCrawler().execute(spotifyEndpoint);
                    }
                    else
                    {
                        new SpotifyCrawler().execute(spotifyEndpoint, spotifyOffset);
                    }
                }
                else
                {
                    getTrackLists();
                }
            }
            else
            {
                System.out.println("Connection failed : " + response.getError());
                defaultAlarm();
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
                        getTrackLists();
                    }

                    @Override
                    public void onFailure(Throwable throwable)
                    {
                        Log.e("MusicOrigin", throwable.getMessage(), throwable);
                        defaultAlarm();
                    }
                });
    }

    public void createValidateCircle()
    {
        whiteCircleValidate = new Circle(this, validateSize, false, 360);

        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(validateSize, validateSize);
        params.addRule(RelativeLayout.CENTER_HORIZONTAL);
        whiteCircleValidate.setLayoutParams(params);

        validateRelative.addView(whiteCircleValidate);
    }

    public void createMusicCircle()
    {
        whiteCircleMusic = new Circle(this, musicSize, true, 360);

        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(musicSize, musicSize);
        params.addRule(RelativeLayout.CENTER_HORIZONTAL);
        whiteCircleMusic.setLayoutParams(params);

        musicRelative.addView(whiteCircleMusic);
    }

    public void createSnoozeCircle()
    {
        whiteCircleSnooze = new Circle(this, snoozeSize, true, 360);

        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(snoozeSize, snoozeSize);
        params.addRule(RelativeLayout.CENTER_HORIZONTAL);
        whiteCircleSnooze.setLayoutParams(params);

        snoozeRelative.addView(whiteCircleSnooze);
    }

    public void hideValidateCircle()
    {
        darkCircleValidate = new Circle(this, validateSize, false);

        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(validateSize, validateSize);
        params.addRule(RelativeLayout.CENTER_HORIZONTAL);
        darkCircleValidate.setLayoutParams(params);

        validateRelative.addView(darkCircleValidate);

        animationValidate = new CircleAngleAnimation(darkCircleValidate, 360);
        animationValidate.setDuration(500);
        animationValidate.setAnimationListener(new Animation.AnimationListener(){
            @Override
            public void onAnimationStart(Animation arg0) {
            }
            @Override
            public void onAnimationRepeat(Animation arg0) {
            }
            @Override
            public void onAnimationEnd(Animation arg0)
            {
                if (pressingValidate)
                {
                    whiteCircleValidate.setVisibility(View.INVISIBLE);
                    cancelMusic(false);
                }
            }
        });

        darkCircleValidate.startAnimation(animationValidate);
    }

    public void hideMusicCircle()
    {
        darkCircleMusic = new Circle(this, musicSize,true);

        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(musicSize, musicSize);
        params.addRule(RelativeLayout.CENTER_HORIZONTAL);
        darkCircleMusic.setLayoutParams(params);

        musicRelative.addView(darkCircleMusic);

        animationMusic = new CircleAngleAnimation(darkCircleMusic, 360);
        animationMusic.setDuration(500);
        animationMusic.setAnimationListener(new Animation.AnimationListener(){
            @Override
            public void onAnimationStart(Animation arg0) {
            }
            @Override
            public void onAnimationRepeat(Animation arg0) {
            }
            @Override
            public void onAnimationEnd(Animation arg0)
            {
                if (pressingMusic && !chillMode && alarm.isRandomSong())
                {
                    vibrator.cancel();
                    chillMode = true;
                    whiteCircleMusic.setVisibility(GONE);
                    musicImage.setColorFilter(getColor(R.color.blue));

                    startAgain("Origin");

                }
            }
        });

        darkCircleMusic.startAnimation(animationMusic);
    }

    public void hideSnoozeCircle()
    {
        darkCircleSnooze = new Circle(this, snoozeSize, true);

        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(snoozeSize, snoozeSize);
        params.addRule(RelativeLayout.CENTER_HORIZONTAL);
        darkCircleSnooze.setLayoutParams(params);

        snoozeRelative.addView(darkCircleSnooze);

        animationSnooze = new CircleAngleAnimation(darkCircleSnooze, 360);
        animationSnooze.setDuration(500);
        animationSnooze.setAnimationListener(new Animation.AnimationListener(){
            @Override
            public void onAnimationStart(Animation arg0) {
            }
            @Override
            public void onAnimationRepeat(Animation arg0) {
            }
            @Override
            public void onAnimationEnd(Animation arg0)
            {
                if (pressingSnooze)
                {
                    whiteCircleSnooze.setVisibility(View.INVISIBLE);
                    cancelMusic(true);
                }
            }
        });

        darkCircleSnooze.startAnimation(animationSnooze);
    }

    public void defaultAlarm()
    {
        System.out.println("Lauching default alarm");

        if (newDefaultAlarm)
        {
            try
            {
                newDefaultAlarm = false;

                System.out.println("Enter default alarm");

                if (trackPlayer != null)
                {
                    System.out.println("Deezer");
                    trackPlayer.stop();
                    trackPlayer.release();
                    System.out.println("Deezer ok");
                }

                System.out.println("Checkpoint");

                if (spotifyPlayer != null)
                {
                    System.out.println("Spotify");
                    spotifyPlayer.pause();
                    System.out.println("Spotify ok");
                }

                System.out.println("Checkpoint");

                if (mediaPlayer != null && mediaPlayer.isPlaying())
                {
                    System.out.println("Folder");
                    mediaPlayer.stop();
                    System.out.println("Folder ok");
                }

                System.out.println("Playeds ended");

                app = (WeckerParameters) getApplicationContext();
                if (app.getSpotifyConnect() != null && app.getSpotifyConnect().isConnected())
                {
                    SpotifyAppRemote.disconnect(app.getSpotifyConnect());
                }

                System.out.println(app.getDefaultTrack().getAbsolutePath());

                System.out.println("Disconnect + defaultTrack path");

                try
                {
                    mediaPlayer = new MediaPlayer();
                    mediaPlayer.setDataSource(app.getDefaultTrack().getAbsolutePath());
                    mediaPlayer.prepare();

                    runOnUiThread(new Runnable()
                    {
                        @Override
                        public void run()
                        {
                            songName.setText(app.getDefaultTrack().getName());
                        }
                    });
                }
                catch (Exception e)
                {
                    System.out.println(e.getMessage());
                    mediaPlayer = MediaPlayer.create(app, getResources().getIdentifier("deja_vu", "raw", getPackageName()));

                    runOnUiThread(new Runnable()
                    {
                        @Override
                        public void run()
                        {
                            songName.setText("Déjà vu");
                        }
                    });
                }

                System.out.println("A priori là ça marche");
                System.out.println("Default alarm launched");

                mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                    public void onCompletion(MediaPlayer mp)
                    {
                        mediaPlayer.stop();

                        System.out.println("Reboot ");

                        newDefaultAlarm = true;
                        defaultAlarm();
                    }
                });

                launchAlarm();
                System.out.println("Post launch alarm");
                chillMode = true;
                mediaPlayer.start();

                System.out.println("Default alarm launched");
            }
            catch (Exception e)
            {
                System.out.println(e.getMessage());
            }
        }
    }
}
