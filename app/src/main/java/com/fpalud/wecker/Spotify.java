package com.fpalud.wecker;

import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.google.gson.JsonObject;
import com.spotify.android.appremote.api.SpotifyAppRemote;

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

public class Spotify extends BaseActivity
{
    WeckerParameters app;

    private static final int REQUEST_CODE = 1337;
    private static final String REDIRECT_URI = "wecker://callback";

    ArrayList<JSONObject> playlistListSpotify = new ArrayList<>();
    ArrayList<JSONObject> trackListSpotify = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.spotify_layout);

        new SpotifyCrawler().execute("playlists/5fMCrRnSy4TauAmM36zrIP");
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
                urlConnection.setRequestProperty("limit","100");

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

            if (server_response.length() == 0)
            {
                System.out.println("Spotify not connected");
            }
            else
            {
                System.out.println("Spotify connected");
                System.out.println(server_response);

                System.out.println(endpoint);

                if ("me/playlists".equals(endpoint))
                {
                    try
                    {
                        JSONArray playlistJSONList = (JSONArray) server_response.get("items");

                        if (playlistJSONList != null)
                        {
                            for (int i = 0 ; i < playlistJSONList.length() ; i++)
                            {
                                playlistListSpotify.add(playlistJSONList.optJSONObject(i));
                                System.out.println(playlistJSONList.optJSONObject(i));
                            }

                            new SpotifyCrawler().execute("playlists/" + playlistListSpotify.get(0).get("id") + "/tracks");
                        }
                    }
                    catch (Exception e)
                    {
                        System.out.println(e.getMessage());
                    }
                }
                else
                {
                    try
                    {
                        JSONObject tracks = (JSONObject) server_response.get("tracks");

                        if (tracks != null)
                        {
                            System.out.println(tracks.getString("total"));
                            System.out.println(tracks.getString("offset"));
                            System.out.println(tracks.getString("limit"));
                            System.out.println(tracks.getString("next"));
                        }
                    }
                    catch (Exception e)
                    {
                        System.out.println(e.getMessage());
                    }

                    /*try
                    {
                        JSONObject tracks = (JSONObject) server_response.get("tracks");

                        if (tracks != null)
                        {
                            trackNumber.add(Integer.valueOf(tracks.getString("total")));
                            newTrackList(true);

                            System.out.println(tracks.getString("total"));
                            System.out.println(tracks.getString("offset"));
                            System.out.println(tracks.getString("limit"));
                            System.out.println(tracks.getString("next"));
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
                    }*/

                    /*
                    try
                    {
                        JSONArray trackJSONList = (JSONArray) server_response.get("items");

                        if (trackJSONList != null)
                        {
                            System.out.println(trackJSONList.length());

                            for (int i = 0 ; i < trackJSONList.length() ; i++)
                            {
                                trackListSpotify.add(trackJSONList.optJSONObject(i));
                                System.out.println(trackJSONList.optJSONObject(i));
                            }

                            app = (WeckerParameters) getApplicationContext();
                            // app.getSpotifyConnect().getPlayerApi().play("spotify:track:" + new JSONObject(trackListSpotify.get(0).getString("track")).get("id"));
                        }
                    }
                    catch (Exception e)
                    {
                        System.out.println(e.getMessage());
                    }
                    */
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
