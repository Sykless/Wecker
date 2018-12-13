package com.fpalud.wecker;

import android.media.MediaPlayer;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

public class AlarmScreen extends BaseActivity
{
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.wake_up_screen_layout);

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
    }
}
