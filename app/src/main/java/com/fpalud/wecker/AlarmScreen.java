package com.fpalud.wecker;

import android.media.MediaPlayer;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.WindowManager;

import com.deezer.sdk.model.Track;
import com.deezer.sdk.network.connect.DeezerConnect;
import com.deezer.sdk.network.connect.SessionStore;
import com.deezer.sdk.network.request.DeezerRequest;
import com.deezer.sdk.network.request.event.JsonRequestListener;
import com.deezer.sdk.network.request.event.RequestListener;

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
import java.util.Random;

public class AlarmScreen extends BaseActivity
{
    int trackId = 0;
    String selectedPlaylistID = "";

    ArrayList<String> idList = new ArrayList<>();
    ArrayList<Integer> trackNumber = new ArrayList<>();
    Collection<File> musicList;

    WeckerParameters app;
    DeezerConnect deezerConnect;

    int id = 0;
    int offset = 0;

    final String[] SUFFIX = {"mp3","wma","3gp","mp4","m4a","aac","flac","gsm","mkv","wav","ogg","ts"};  // use the suffix to filter

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.wake_up_screen_layout);

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON);

        getTrackLists();

        /*
        MediaPlayer mp = new MediaPlayer();

        try
        {
            mp.setDataSource(file.getAbsolutePath());
            mp.prepare();
            mp.start();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        */

        // WakeLocker.release();
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
}
