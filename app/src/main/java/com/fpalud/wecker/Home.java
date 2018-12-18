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
import android.widget.Toast;

import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.Arrays;

public class Home extends BaseActivity
{
    ArrayList<AnimationDrawable> checkAnimations = new ArrayList<>();
    ArrayList<AnimationDrawable> uncheckAnimations = new ArrayList<>();

    WeckerParameters app;
    LinearLayout mainLayout;

    ArrayList<Alarm> alarmList = new ArrayList<>();
    String[] daysValue = {"L","M","M","J","V","S","D"};

    private static final int PARAMS = 0;
    private static final int NEWALARM = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.home_layout);

        app = (WeckerParameters) getApplicationContext();
        alarmList = app.getAlarmList();

        mainLayout = findViewById(R.id.mainLayout);

        setupAlarmLayout();
    }

    public void setupAlarmLayout()
    {
        mainLayout.removeAllViews();

        for (int alarmIndex = 0 ; alarmIndex < alarmList.size() ; alarmIndex++)
        {
            Alarm alarm = alarmList.get(alarmIndex);

            LinearLayout parentLayout = new LinearLayout(this);

            LinearLayout.LayoutParams parentsParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            parentsParams.setMargins(0, alarmIndex == 0 ? 0 : 16, 0, 0);
            parentLayout.setLayoutParams(parentsParams);
            parentLayout.setGravity(Gravity.CENTER);
            parentLayout.setOrientation(LinearLayout.HORIZONTAL);

            ImageView trashImage = new ImageView(this);
            trashImage.setId(alarmIndex);
            trashImage.setClickable(true);
            trashImage.setFocusable(true);
            trashImage.setVisibility(View.GONE);
            trashImage.setImageResource(R.drawable.ic_delete_white_48dp);
            trashImage.setOnClickListener(new View.OnClickListener()
            {
                public void onClick(View view)
                {
                    int alarmId = view.getId();
                    app = (WeckerParameters) getApplicationContext();

                    ArrayList<Alarm> alarmList = app.getAlarmList();
                    alarmList.remove(alarmId);
                    app.setAlarmList(alarmList);

                    setupAlarmLayout();
                }
            });

            TypedValue outValue = new TypedValue();
            getApplicationContext().getTheme().resolveAttribute(android.R.attr.selectableItemBackgroundBorderless, outValue, true);
            trashImage.setBackgroundResource(outValue.resourceId);

            RelativeLayout newAlarm = new RelativeLayout(this);
            newAlarm.setId(alarmIndex);

            newAlarm.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
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
            daysLayout.setPadding(100,0,0,0);
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
                    day.setTextColor(getResources().getColor(R.color.grey));
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
                    Alarm alarm = alarmList.get(alarmId);
                    app = (WeckerParameters) getApplicationContext();

                    ImageView alarmImage = (ImageView) v;
                    RelativeLayout mainLayout = (RelativeLayout) alarmImage.getParent();
                    TextView time = (TextView) mainLayout.getChildAt(0);
                    LinearLayout daysLayout = (LinearLayout) mainLayout.getChildAt(1);

                    if (alarm.isActive())
                    {
                        time.setTextColor(getResources().getColor(R.color.grey));

                        for (int i = 0 ; i < 7 ; i++)
                        {
                            TextView day = (TextView) daysLayout.getChildAt(i);
                            day.setTextColor(getResources().getColor(R.color.grey));
                        }

                        alarmImage.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.grey)));
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
                                day.setTextColor(getResources().getColor(R.color.grey));
                            }
                        }

                        alarmImage.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.white)));
                        alarmImage.setBackground(uncheckAnimations.get(alarmId));
                        uncheckAnimations.get(alarmId).start();

                        alarm.setActive(true);
                    }

                    alarmList.set(alarmId, alarm);
                    app.setAlarmList(alarmList);
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
                time.setTextColor(getResources().getColor(R.color.grey));
                alarmImage.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.grey)));
                alarmImage.setBackground(uncheckAnimations.get(alarmIndex));
            }

            LinearLayout.LayoutParams trashParams = new LinearLayout.LayoutParams(80, 80);
            trashParams.setMargins(0,0,8,0);

            RelativeLayout.LayoutParams imageParams = new RelativeLayout.LayoutParams(80, 80);
            imageParams.addRule(RelativeLayout.ALIGN_PARENT_END);
            imageParams.addRule(RelativeLayout.CENTER_VERTICAL);
            imageParams.setMargins(0,8,8,8);

            RelativeLayout.LayoutParams timeParams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
            timeParams.addRule(RelativeLayout.ALIGN_PARENT_START);
            timeParams.addRule(RelativeLayout.CENTER_VERTICAL);
            timeParams.setMargins(16,0,0,0);

            RelativeLayout.LayoutParams daysParams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
            daysParams.addRule(RelativeLayout.CENTER_VERTICAL);
            daysParams.addRule(RelativeLayout.CENTER_HORIZONTAL);

            trashImage.setLayoutParams(trashParams);
            time.setLayoutParams(timeParams);
            alarmImage.setLayoutParams(imageParams);
            daysLayout.setLayoutParams(daysParams);

            newAlarm.addView(time,0);
            newAlarm.addView(daysLayout,1);
            newAlarm.addView(alarmImage,2);
            newAlarm.setOnClickListener(new View.OnClickListener()
            {
                public void onClick(View v)
                {
                    // Change playlist
                }
            });
            newAlarm.setOnLongClickListener(new View.OnLongClickListener()
            {
                public boolean onLongClick(View view)
                {
                    LinearLayout parentLayout = (LinearLayout) view.getParent();
                    ImageView trashImage = (ImageView) parentLayout.getChildAt(0);

                    if (trashImage.getVisibility() == View.GONE)
                    {
                        trashImage.setVisibility(View.VISIBLE);
                    }
                    else
                    {
                        trashImage.setVisibility(View.GONE);
                    }

                    return true;
                }
            });

            parentLayout.addView(trashImage,0);
            parentLayout.addView(newAlarm,1);

            mainLayout.addView(parentLayout);
        }
    }

    public void goToParams(View view)
    {
        Intent intent = new Intent(this, MusicOrigin.class);
        intent.putExtra("origin",PARAMS);
        startActivity(intent);
    }

    public void goToNewAlarm(View view)
    {
        app = (WeckerParameters) getApplicationContext();

        if (!app.isDeezerChecked() && !app.isSpotifyChecked() && !app.isFolderChecked())
        {
            Intent intent = new Intent(this, MusicOrigin.class);
            intent.putExtra("origin",NEWALARM);
            startActivity(intent);
        }
        else
        {
            Intent intent = new Intent(this, SetupPlaylist.class);
            startActivity(intent);
        }
    }

    @Override
    public void onBackPressed()
    {
        moveTaskToBack(true);
    }

    @Override
    protected void onLeaveThisActivity()
    {
        // Don't use an exit animation when leaving the main activity!
    }
}
