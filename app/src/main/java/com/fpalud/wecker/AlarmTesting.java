package com.fpalud.wecker;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.animation.Animation;
import android.widget.ImageView;
import android.widget.RelativeLayout;

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

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.wake_up_screen_layout);

        validateImage = findViewById(R.id.validateImage);
        musicImage = findViewById(R.id.musicImage);
        snoozeImage = findViewById(R.id.snoozeImage);

        validateRelative = findViewById(R.id.validateRelative);
        musicRelative = findViewById(R.id.musicRelative);
        snoozeRelative = findViewById(R.id.snoozeRelative);

        validateRelative.setOnTouchListener(new View.OnTouchListener()
        {
            @Override
            public boolean onTouch(View v, MotionEvent event)
            {
                if (event.getAction() == MotionEvent.ACTION_DOWN)
                {
                    hideValidateCircle();
                }
                else if (event.getAction() == MotionEvent.ACTION_UP)
                {
                    animationValidate.cancel();
                    darkCircleValidate.setVisibility(GONE);
                }

                return true;
            }
        });

        musicRelative.setOnTouchListener(new View.OnTouchListener()
        {
            @Override
            public boolean onTouch(View v, MotionEvent event)
            {
                if (event.getAction() == MotionEvent.ACTION_DOWN)
                {
                    hideMusicCircle();
                }
                else if (event.getAction() == MotionEvent.ACTION_UP)
                {
                    animationMusic.cancel();
                    darkCircleMusic.setVisibility(GONE);
                }

                return true;
            }
        });

        snoozeRelative.setOnTouchListener(new View.OnTouchListener()
        {
            @Override
            public boolean onTouch(View v, MotionEvent event)
            {
                if (event.getAction() == MotionEvent.ACTION_DOWN)
                {
                    hideSnoozeCircle();
                }
                else if (event.getAction() == MotionEvent.ACTION_UP)
                {
                    animationSnooze.cancel();
                    darkCircleSnooze.setVisibility(GONE);
                }

                return true;
            }
        });

        ViewTreeObserver vtoValidate = validateImage.getViewTreeObserver();
        vtoValidate.addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener()
        {
            public boolean onPreDraw()
            {
                validateImage.getViewTreeObserver().removeOnPreDrawListener(this);
                validateSize = validateImage.getMeasuredWidth();

                createValidateCircle();

                return true;
            }
        });

        ViewTreeObserver vtoMusic = musicImage.getViewTreeObserver();
        vtoMusic.addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener()
        {
            public boolean onPreDraw()
            {
                musicImage.getViewTreeObserver().removeOnPreDrawListener(this);
                musicSize = musicImage.getMeasuredWidth();

                System.out.println(musicSize);

                createMusicCircle();

                return true;
            }
        });

        ViewTreeObserver vtoSnooze = snoozeImage.getViewTreeObserver();
        vtoSnooze.addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener()
        {
            public boolean onPreDraw()
            {
                snoozeImage.getViewTreeObserver().removeOnPreDrawListener(this);
                snoozeSize = snoozeImage.getMeasuredWidth();

                createSnoozeCircle();

                return true;
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
                // whiteCircle.setVisibility(GONE);
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
                // whiteCircle.setVisibility(GONE);
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
                // whiteCircle.setVisibility(GONE);
            }
        });

        darkCircleSnooze.startAnimation(animationSnooze);
    }
}
