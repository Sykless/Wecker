package com.fpalud.wecker;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.deezer.sdk.model.Permissions;
import com.deezer.sdk.network.connect.DeezerConnect;
import com.deezer.sdk.network.connect.SessionStore;
import com.deezer.sdk.network.connect.event.DialogListener;

public class Deezer extends AppCompatActivity
{
    DeezerConnect deezerConnect;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.deezer_layout);

        String applicationID = "315304";
        deezerConnect = new DeezerConnect(this, applicationID);

        // restore any saved session
        SessionStore sessionStore = new SessionStore();

        if (sessionStore.restore(deezerConnect, this))
        {
            System.out.println("Already logged in !");
        }
        else
        {
            String[] permissions = new String[] {
                    Permissions.BASIC_ACCESS,
                    Permissions.MANAGE_LIBRARY,
                    Permissions.LISTENING_HISTORY };

            // The listener for authentication events
            DialogListener listener = new DialogListener() {
                public void onComplete(Bundle values)
                {
                    SessionStore sessionStore = new SessionStore();
                    sessionStore.save(deezerConnect, getApplicationContext());

                    System.out.println("Logged in !");
                }

                public void onCancel() {System.out.println("Canceled !");}
                public void onException(Exception e) {System.out.println("Exception !");}
            };

            // Launches the authentication process
            deezerConnect.authorize(this, permissions, listener);
        }
    }
}
