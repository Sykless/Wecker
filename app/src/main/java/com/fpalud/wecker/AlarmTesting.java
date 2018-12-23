package com.fpalud.wecker;

import android.content.res.Resources;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import static android.view.View.GONE;

public class AlarmTesting extends BaseActivity
{
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

    TextView snoozeValue;
    ImageView plusButton;
    ImageView minusButton;

    AlphaAnimation buttonClick = new AlphaAnimation(1F, 0.7F); // Fading animation on image when clicked
    AlphaAnimation buttonClickRelease = new AlphaAnimation(0.7F,1F); // Unfading animation on image when clicked

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.wake_up_screen_layout);
    }
}
