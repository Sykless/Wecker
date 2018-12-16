package com.fpalud.wecker;

import android.content.res.ColorStateList;
import android.graphics.drawable.AnimationDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.animation.Animation;
import android.widget.ImageView;
import android.widget.TextView;

import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.Arrays;

public class Home extends BaseActivity
{
    ImageView alarmImage;
    AnimationDrawable checkAnimation;
    AnimationDrawable uncheckAnimation;

    TextView time;
    ArrayList<TextView> dayList = new ArrayList<>();
    ArrayList<Boolean> dayChecked = new ArrayList<>(Arrays.asList(false, false, false, false, false, true, true));

    boolean imageChecked = false;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.home_layout);

        alarmImage = findViewById(R.id.alarmImage);
        time = findViewById(R.id.time);
        dayList.add((TextView) findViewById(R.id.lundi));
        dayList.add((TextView) findViewById(R.id.mardi));
        dayList.add((TextView) findViewById(R.id.mercredi));
        dayList.add((TextView) findViewById(R.id.jeudi));
        dayList.add((TextView) findViewById(R.id.vendredi));
        dayList.add((TextView) findViewById(R.id.samedi));
        dayList.add((TextView) findViewById(R.id.dimanche));

        for (int i = 0 ; i < 7 ; i++)
        {
            if (dayChecked.get(i))
            {
                dayList.get(i).setTextColor(getResources().getColor(R.color.darkBlue));
            }
        }

        checkAnimation = (AnimationDrawable) getResources().getDrawable(R.drawable.alarm_check_animation);
        uncheckAnimation = (AnimationDrawable) getResources().getDrawable(R.drawable.alarm_uncheck_animation);
        alarmImage.setBackground(checkAnimation);
    }

    public void clickImage(View view)
    {
        if (imageChecked)
        {
            imageChecked = false;

            time.setTextColor(getResources().getColor(R.color.white));
            for (int i = 0 ; i < 7 ; i++)
            {
                if (!dayChecked.get(i))
                {
                    dayList.get(i).setTextColor(getResources().getColor(R.color.white));
                }
            }

            alarmImage.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.white)));
            alarmImage.setBackground(uncheckAnimation);
            uncheckAnimation.start();
        }
        else
        {
            imageChecked = true;

            time.setTextColor(getResources().getColor(R.color.darkBlue));
            for (TextView day : dayList)
            {
                day.setTextColor(getResources().getColor(R.color.darkBlue));
            }
            alarmImage.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.darkBlue)));
            alarmImage.setBackground(checkAnimation);
            checkAnimation.start();
        }
    }

    public void goToCreate(View view)
    {
        // ad.start();
    }

    @Override
    protected void onLeaveThisActivity()
    {
        // Don't use an exit animation when leaving the main activity!
    }
}
