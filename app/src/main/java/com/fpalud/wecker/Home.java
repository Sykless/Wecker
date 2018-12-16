package com.fpalud.wecker;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.drawable.AnimationDrawable;
import android.media.Image;
import android.os.Bundle;
import android.os.Handler;
import android.support.constraint.ConstraintLayout;
import android.support.constraint.ConstraintSet;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v4.widget.CompoundButtonCompat;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.Arrays;

public class Home extends BaseActivity
{
    ArrayList<AnimationDrawable> checkAnimations = new ArrayList<>();
    ArrayList<AnimationDrawable> uncheckAnimations = new ArrayList<>();

    AnimationDrawable checkAnimation;
    AnimationDrawable uncheckAnimation;
    AlphaAnimation buttonClick = new AlphaAnimation(1F, 0.7F); // Fading animation on text when clicked
    AlphaAnimation buttonClickRelease = new AlphaAnimation(0.7F,1F); // Unfading animation on text when clicked

    WeckerParameters app;
    LinearLayout mainLayout;

    ArrayList<Alarm> alarmList = new ArrayList<>();
    String[] daysValue = {"L","M","M","J","V","S","D"};

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.home_layout);

        app = (WeckerParameters) getApplicationContext();
        alarmList = app.getAlarmList();

        mainLayout = findViewById(R.id.mainLayout);

        buttonClick.setDuration(100);
        buttonClickRelease.setDuration(100);
        buttonClickRelease.setStartOffset(100);
        checkAnimation = (AnimationDrawable) getResources().getDrawable(R.drawable.alarm_check_animation);
        uncheckAnimation = (AnimationDrawable) getResources().getDrawable(R.drawable.alarm_uncheck_animation);

        System.out.println(alarmList.size());
        setupAlarmLayout();
    }

    public void setupAlarmLayout()
    {
        for (int alarmIndex = 0 ; alarmIndex < alarmList.size() ; alarmIndex++)
        {
            Alarm alarm = alarmList.get(alarmIndex);

            RelativeLayout newAlarm = new RelativeLayout(this);
            newAlarm.setId(alarmIndex);

            LinearLayout.LayoutParams playlistParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            playlistParams.setMargins(0, 16, 0, 0);

            newAlarm.setLayoutParams(playlistParams);
            newAlarm.setBackground(getResources().getDrawable(R.drawable.border));
            newAlarm.setClickable(true);
            newAlarm.setFocusable(true);

            checkAnimations.add((AnimationDrawable) getResources().getDrawable(R.drawable.alarm_check_animation));
            uncheckAnimations.add((AnimationDrawable) getResources().getDrawable(R.drawable.alarm_uncheck_animation));

            int hours = alarm.getHours();
            int minutes = alarm.getMinutes();
            String minutesDisplay;

            if (minutes < 10)  minutesDisplay = "0" + String.valueOf(minutes);
            else               minutesDisplay = String.valueOf(minutes);

            TextView time = new TextView(this);
            time.setId(View.generateViewId());
            time.setGravity(Gravity.CENTER);
            time.setTextSize(35);
            time.setText(hours+ ":" + minutesDisplay);
            time.setTypeface(ResourcesCompat.getFont(this,R.font.carter_one));

            LinearLayout daysLayout = new LinearLayout(this);
            daysLayout.setId(View.generateViewId());
            daysLayout.setLayoutParams(new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT));
            daysLayout.setGravity(Gravity.CENTER);
            daysLayout.setOrientation(LinearLayout.HORIZONTAL);

            for (int i = 0 ; i < 7 ; i++)
            {
                TextView day = new TextView(this);
                day.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT));
                day.setGravity(Gravity.CENTER);
                day.setPadding(3,0,3,0);
                day.setTypeface(ResourcesCompat.getFont(this,R.font.carter_one));
                day.setText(daysValue[i]);
                day.setTextSize(15);

                if (alarm.getDays().get(i) && alarm.isActive())
                {
                    day.setTextColor(getResources().getColor(R.color.white));
                }
                else
                {
                    day.setTextColor(getResources().getColor(R.color.darkBlue));
                }

                daysLayout.addView(day,i);
            }

            ImageView alarmImage = new ImageView(this);
            alarmImage.setId(alarmIndex);
            alarmImage.setClickable(true);
            alarmImage.setFocusable(true);
            alarmImage.setOnClickListener(new View.OnClickListener()
            {
                public void onClick(View v)
                {
                    int alarmId = v.getId();
                    System.out.println(alarmId);
                    Alarm alarm = alarmList.get(alarmId);

                    ImageView alarmImage = (ImageView) v;
                    RelativeLayout mainLayout = (RelativeLayout) alarmImage.getParent();
                    TextView time = (TextView) mainLayout.getChildAt(0);
                    LinearLayout daysLayout = (LinearLayout) mainLayout.getChildAt(1);

                    if (alarm.isActive())
                    {
                        time.setTextColor(getResources().getColor(R.color.darkBlue));

                        for (int i = 0 ; i < 7 ; i++)
                        {
                            TextView day = (TextView) daysLayout.getChildAt(i);
                            day.setTextColor(getResources().getColor(R.color.darkBlue));
                        }

                        alarmImage.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.darkBlue)));
                        alarmImage.setBackground(checkAnimations.get(alarmId));
                        checkAnimations.get(alarmId).start();

                        alarm.setActive(false);
                    }
                    else
                    {
                        time.setTextColor(getResources().getColor(R.color.white));

                        for (int i = 0 ; i < 7 ; i++)
                        {
                            TextView day = (TextView) daysLayout.getChildAt(i);

                            if (alarm.getDays().get(i))
                            {
                                day.setTextColor(getResources().getColor(R.color.white));
                            }
                            else
                            {
                                day.setTextColor(getResources().getColor(R.color.darkBlue));
                            }
                        }

                        alarmImage.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.white)));
                        alarmImage.setBackground(uncheckAnimations.get(alarmId));
                        uncheckAnimations.get(alarmId).start();

                        alarm.setActive(true);
                    }
                }
            });

            if (alarm.isActive())
            {
                time.setTextColor(getResources().getColor(R.color.white));
                alarmImage.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.white)));
                alarmImage.setBackground(checkAnimations.get(alarmIndex));
            }
            else
            {
                time.setTextColor(getResources().getColor(R.color.darkBlue));
                alarmImage.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.darkBlue)));
                alarmImage.setBackground(uncheckAnimations.get(alarmIndex));
            }

            RelativeLayout.LayoutParams timeParams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
            timeParams.addRule(RelativeLayout.ALIGN_PARENT_START);
            timeParams.addRule(RelativeLayout.CENTER_VERTICAL);
            timeParams.setMargins(16,0,0,0);

            RelativeLayout.LayoutParams daysParams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
            daysParams.addRule(RelativeLayout.CENTER_VERTICAL);
            daysParams.addRule(RelativeLayout.END_OF,time.getId());
            daysParams.setMargins(24,0,0,0);

            RelativeLayout.LayoutParams imageParams = new RelativeLayout.LayoutParams(80, 80);
            imageParams.addRule(RelativeLayout.ALIGN_PARENT_END);
            imageParams.addRule(RelativeLayout.CENTER_VERTICAL);
            imageParams.setMargins(0,8,8,8);

            time.setLayoutParams(timeParams);
            daysLayout.setLayoutParams(daysParams);
            alarmImage.setLayoutParams(imageParams);

            newAlarm.addView(time,0);
            newAlarm.addView(daysLayout,1);
            newAlarm.addView(alarmImage,2);
            newAlarm.setOnClickListener(new View.OnClickListener()
            {
                public void onClick(View v)
                {
                    v.startAnimation(buttonClick);
                    v.startAnimation(buttonClickRelease);
                }
            });

            mainLayout.addView(newAlarm);
        }
    }

    public void goToCreate(View view)
    {
        Intent intent = new Intent(this, MusicOrigin.class);
        startActivity(intent);
    }

    @Override
    protected void onLeaveThisActivity()
    {
        // Don't use an exit animation when leaving the main activity!
    }
}
