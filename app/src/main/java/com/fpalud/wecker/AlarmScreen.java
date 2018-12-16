package com.fpalud.wecker;

import android.media.MediaPlayer;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.WindowManager;

public class AlarmScreen extends BaseActivity
{
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.wake_up_screen_layout);

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON);

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
}
