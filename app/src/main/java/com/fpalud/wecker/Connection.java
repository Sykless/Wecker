package com.fpalud.wecker;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

public class Connection extends AppCompatActivity
{
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.connection_layout);

        System.out.println("Connection");

        Intent newIntent = new Intent(this, LaunchAlarm.class);
        newIntent.putExtra("connection",true);
        startActivity(newIntent);
    }
}
