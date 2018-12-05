package com.fpalud.wecker;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

import com.deezer.sdk.model.Permissions;
import com.deezer.sdk.network.connect.DeezerConnect;
import com.deezer.sdk.network.connect.SessionStore;
import com.deezer.sdk.network.connect.event.DialogListener;

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
import java.util.Calendar;

import static com.spotify.sdk.android.authentication.AuthenticationResponse.Type.TOKEN;

public class Home extends AppCompatActivity {
    DeezerConnect deezerConnect;

    private static final int REQUEST_CODE = 1337;
    private static final String REDIRECT_URI = "wecker://callback";

    String spotifyToken;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.home_layout);
    }

    public void goToDeezer() {
        Intent intent = new Intent(this, Deezer.class);
        this.startActivity(intent);
    }

    public void launchSpotify(View view) {
        AuthenticationRequest.Builder builder =
                new AuthenticationRequest.Builder("46065021347f4ef3bd007487a2497d2f", TOKEN, REDIRECT_URI);

        builder.setScopes(new String[]{"streaming"});
        AuthenticationRequest request = builder.build();

        // AuthenticationClient.openLoginInBrowser(this, request);
        AuthenticationClient.openLoginActivity(this, REQUEST_CODE, request);
    }

    public void launchDeezer(View view) {
        deezerConnect = new DeezerConnect(this, "315304");

        // restore any saved session
        SessionStore sessionStore = new SessionStore();

        if (sessionStore.restore(deezerConnect, this)) {
            goToDeezer();
        } else {
            String[] permissions = new String[]{
                    Permissions.BASIC_ACCESS,
                    Permissions.MANAGE_LIBRARY,
                    Permissions.LISTENING_HISTORY};

            // The listener for authentication events
            DialogListener listener = new DialogListener() {
                public void onComplete(Bundle values) {
                    SessionStore sessionStore = new SessionStore();
                    sessionStore.save(deezerConnect, getApplicationContext());

                    goToDeezer();
                }

                public void onCancel() {
                    System.out.println("Canceled !");
                }

                public void onException(Exception e) {
                    System.out.println("Exception !");
                }
            };

            // Launches the authentication process
            deezerConnect.authorize(this, permissions, listener);
        }
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);

        // Check if result comes from the correct activity
        if (requestCode == REQUEST_CODE) {
            AuthenticationResponse response = AuthenticationClient.getResponse(resultCode, intent);

            if (response.getType() == TOKEN) {
                spotifyToken = response.getAccessToken();
                System.out.println(spotifyToken);

                new SpotifyCrawler().execute();
            }
        }
    }

    public class SpotifyCrawler extends AsyncTask<String, Void, String>
    {
        String server_response;

        @Override
        protected String doInBackground(String... strings)
        {
            try
            {
                URL url = new URL("https://api.spotify.com/v1/playlists/37i9dQZF1DX0jgyAiPl8Af/tracks");
                HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestProperty("Authorization", "Bearer " + spotifyToken);

                int responseCode = urlConnection.getResponseCode();

                if (responseCode == HttpURLConnection.HTTP_OK)
                {
                    System.out.println(readStream(urlConnection.getInputStream()));
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
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            // System.out.println(server_response);
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

    public void setAlarm()
    {
        AlarmManager am = (AlarmManager) this.getSystemService(Context.ALARM_SERVICE);

        Calendar futureDate = Calendar.getInstance();
        futureDate.set(Calendar.MINUTE,futureDate.get(Calendar.MINUTE) + 1);

        Intent intent = new Intent(this, LaunchAlarm.class);
        PendingIntent sender = PendingIntent.getBroadcast(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);

        if (android.os.Build.VERSION.SDK_INT >= 19)
        {
            System.out.println("Launch api >= 19");
            am.setExact(AlarmManager.RTC_WAKEUP, futureDate.getTimeInMillis(), sender);
        }
        else {
            System.out.println("Launch api < 19");
            am.set(AlarmManager.RTC_WAKEUP, futureDate.getTimeInMillis(), sender);
        }
    }
}
