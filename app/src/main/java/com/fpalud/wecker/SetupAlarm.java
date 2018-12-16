package com.fpalud.wecker;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.media.MediaPlayer;
import android.os.AsyncTask;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
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

public class SetupAlarm extends BaseActivity
{
    WeckerParameters app;
    DeezerConnect deezerConnect;

    ArrayList<String> idList = new ArrayList<>();
    ArrayList<Integer> trackNumber = new ArrayList<>();
    Collection<File> musicList;

    AlphaAnimation buttonClick = new AlphaAnimation(1F, 0.7F); // Fading animation on text when clicked
    AlphaAnimation buttonClickRelease = new AlphaAnimation(0.7F,1F); // Unfading animation on text when clicked

    EditTextCustom hours;
    EditTextCustom minutes;
    ImageView hoursUp;
    ImageView hoursDown;
    ImageView minutesUp;
    ImageView minutesDown;

    TextView vibrationText;
    TextView emergencyText;
    CheckBox vibrateBox;
    CheckBox emergencyBox;

    ArrayList<Button> dayButtons = new ArrayList<>();
    ArrayList<Boolean> buttonClicked = new ArrayList<>(Arrays.asList(false, false, false, false, false, false, false));

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

        hours = findViewById(R.id.hours);
        minutes = findViewById(R.id.minutes);
        hoursUp = findViewById(R.id.hoursUp);
        hoursDown = findViewById(R.id.hoursDown);
        minutesUp = findViewById(R.id.minutesUp);
        minutesDown = findViewById(R.id.minutesDown);

        vibrationText = findViewById(R.id.vibrationText);
        emergencyText = findViewById(R.id.emergencyText);
        vibrateBox = findViewById(R.id.vibrateBox);
        emergencyBox = findViewById(R.id.emergencyBox);

        dayButtons.add((Button) findViewById(R.id.lundi));
        dayButtons.add((Button) findViewById(R.id.mardi));
        dayButtons.add((Button) findViewById(R.id.mercredi));
        dayButtons.add((Button) findViewById(R.id.jeudi));
        dayButtons.add((Button) findViewById(R.id.vendredi));
        dayButtons.add((Button) findViewById(R.id.samedi));
        dayButtons.add((Button) findViewById(R.id.dimanche));

        View.OnClickListener buttonClickListener =  new View.OnClickListener()
        {
            public void onClick(View v)
            {
                int id = dayButtons.indexOf((Button) v);

                if (buttonClicked.get(id))
                {
                    buttonClicked.set(id, false);
                    v.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.white)));
                    ((Button) v).setTextColor(getResources().getColor(R.color.blue));
                }
                else
                {
                    buttonClicked.set(id, true);
                    v.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.darkBlue)));
                    ((Button) v).setTextColor(getResources().getColor(R.color.white));
                }
            }
        };

        for (Button button : dayButtons)
        {
            button.setOnClickListener(buttonClickListener);
        }

        buttonClick.setDuration(100);
        buttonClickRelease.setDuration(100);
        buttonClickRelease.setStartOffset(100);

        hours.setCursorVisible(false);
        minutes.setCursorVisible(false);

        hours.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event)
            {
                if(actionId == EditorInfo.IME_ACTION_DONE)
                {
                    checkHours();
                    hours.setCursorVisible(false);
                }

                return false;
            }
        });

        hours.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean hasFocus) {
                if (hasFocus)
                {
                    hours.setCursorVisible(true);
                }
                else
                {
                    hours.setCursorVisible(false);
                    checkHours();
                }
            }
        });

        hours.setOnClickListener(new View.OnClickListener()
        {
            public void onClick(View v)
            {
                hours.setCursorVisible(true);
            }
        });

        minutes.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event)
            {
                if (actionId == EditorInfo.IME_ACTION_DONE)
                {
                    checkMinutes();
                    minutes.setCursorVisible(false);
                }

                return false;
            }
        });

        minutes.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean hasFocus)
            {
                if (hasFocus)
                {
                    minutes.setCursorVisible(true);
                }
                else
                {
                    minutes.setCursorVisible(false);
                    checkMinutes();
                }
            }
        });

        minutes.setOnClickListener(new View.OnClickListener()
        {
            public void onClick(View v)
            {
                minutes.setCursorVisible(true);
            }
        });

        vibrationText.setOnClickListener(new View.OnClickListener()
        {
            public void onClick(View v)
            {
                v.startAnimation(buttonClick);
                v.startAnimation(buttonClickRelease);

                if (vibrateBox.isChecked())
                {
                    vibrateBox.setChecked(false);
                }
                else
                {
                    vibrateBox.setChecked(true);
                }
            }
        });

        emergencyText.setOnClickListener(new View.OnClickListener()
        {
            public void onClick(View v)
            {
                v.startAnimation(buttonClick);
                v.startAnimation(buttonClickRelease);

                if (emergencyBox.isChecked())
                {
                    emergencyBox.setChecked(false);
                }
                else
                {
                    emergencyBox.setChecked(true);
                }
            }
        });

        Intent intent = getIntent();
        idList = intent.getStringArrayListExtra("idList");
        boolean randomSong = intent.getBooleanExtra("randomSong",true);

        System.out.println(randomSong + " " + idList);

        getTrackLists();
    }

    public void goToNext(View view)
    {
        Alarm alarm = new Alarm(
                Integer.valueOf(hours.getText().toString()),
                Integer.valueOf(minutes.getText().toString()),
                buttonClicked,
                idList,
                vibrateBox.isChecked(),
                emergencyBox.isChecked()
        );

        app = (WeckerParameters) getApplicationContext();
        app.addAlarm(alarm);

        Intent intent = new Intent(this, Home.class);
        startActivity(intent);
    }

    public void increaseHours(View v)
    {
        v.startAnimation(buttonClick);
        v.startAnimation(buttonClickRelease);

        hours.setText(String.valueOf((Integer.valueOf(hours.getText().toString()) + 1) % 24));
    }

    public void increaseMinutes(View v)
    {
        v.startAnimation(buttonClick);
        v.startAnimation(buttonClickRelease);

        minutes.setText(String.valueOf((Integer.valueOf(minutes.getText().toString()) + 5) % 60));
        checkMinutes();
    }

    public void decreaseHours(View v)
    {
        v.startAnimation(buttonClick);
        v.startAnimation(buttonClickRelease);

        if (Integer.valueOf(hours.getText().toString()) - 1 < 0)
        {
            hours.setText(String.valueOf(Integer.valueOf(hours.getText().toString()) + 23));
        }
        else
        {
            hours.setText(String.valueOf(Integer.valueOf(hours.getText().toString()) - 1));
        }
    }

    public void decreaseMinutes(View v)
    {
        v.startAnimation(buttonClick);
        v.startAnimation(buttonClickRelease);

        if (Integer.valueOf(minutes.getText().toString()) - 5 < 0)
        {
            minutes.setText(String.valueOf(Integer.valueOf(minutes.getText().toString()) + 55));
        }
        else
        {
            minutes.setText(String.valueOf(Integer.valueOf(minutes.getText().toString()) - 5));
            checkMinutes();
        }
    }

    public void checkHours()
    {
        if (Integer.valueOf(hours.getText().toString()) > 23 || Integer.valueOf(hours.getText().toString()) < 0)
        {
            hours.setText(String.valueOf("0"));
        }
    }

    public void checkMinutes()
    {
        if (minutes.getText().toString().length() == 1)
        {
            minutes.setText("0" + minutes.getText().toString());
        }
        else if (Integer.valueOf(minutes.getText().toString()) > 59 || Integer.valueOf(minutes.getText().toString()) < 0)
        {
            minutes.setText(String.valueOf("00"));
        }
    }

    public void displayEmergencyInstructions(View view)
    {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        builder.setMessage("Alarme d'urgence\n\nMême après validation de la sonnerie, déclenche des vibrations continues 5 minutes après l'alarme normale " +
                "pour garantir le réveil.")
                .setPositiveButton("OK", new DialogInterface.OnClickListener()
                {
                    public void onClick(DialogInterface dialog, int id) {}
                });

        AlertDialog alert = builder.create();
        alert.show();
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
