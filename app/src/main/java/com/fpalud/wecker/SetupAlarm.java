package com.fpalud.wecker;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.support.v7.app.AlertDialog;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Arrays;

public class SetupAlarm extends BaseActivity
{
    WeckerParameters app;

    Alarm alarm;
    int alarmId;

    ArrayList<String> idList = new ArrayList<>();

    AlphaAnimation buttonClick = new AlphaAnimation(1F, 0.7F); // Fading animation on text when clicked
    AlphaAnimation buttonClickRelease = new AlphaAnimation(0.7F,1F); // Unfading animation on text when clicked

    EditTextCustom hours;
    EditTextCustom minutes;
    ImageView hoursUp;
    ImageView hoursDown;
    ImageView minutesUp;
    ImageView minutesDown;

    TextView vibrationText;
    TextView emergencyText;
    CheckBox vibrateBox;
    CheckBox emergencyBox;

    ArrayList<Button> dayButtons = new ArrayList<>();
    ArrayList<Boolean> buttonClicked = new ArrayList<>(Arrays.asList(false, false, false, false, false, false, false));

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        app = (WeckerParameters) getApplicationContext();

        Intent intent = getIntent();

        if (intent.getBooleanExtra("fromHome",false))
        {
            alarmId = intent.getIntExtra("alarmId",0);

            alarm = app.getAlarmList().get(alarmId);
            idList = alarm.getIdSongsList();

            setContentView(R.layout.setup_alarm_params_layout);
        }
        else
        {
            alarm = new Alarm();
            idList = intent.getStringArrayListExtra("idList");

            setContentView(R.layout.setup_alarm_layout);
        }

        hours = findViewById(R.id.hours);
        minutes = findViewById(R.id.minutes);
        hoursUp = findViewById(R.id.hoursUp);
        hoursDown = findViewById(R.id.hoursDown);
        minutesUp = findViewById(R.id.minutesUp);
        minutesDown = findViewById(R.id.minutesDown);

        vibrationText = findViewById(R.id.vibrationText);
        emergencyText = findViewById(R.id.emergencyText);
        vibrateBox = findViewById(R.id.vibrateBox);
        emergencyBox = findViewById(R.id.emergencyBox);

        dayButtons.add((Button) findViewById(R.id.lundi));
        dayButtons.add((Button) findViewById(R.id.mardi));
        dayButtons.add((Button) findViewById(R.id.mercredi));
        dayButtons.add((Button) findViewById(R.id.jeudi));
        dayButtons.add((Button) findViewById(R.id.vendredi));
        dayButtons.add((Button) findViewById(R.id.samedi));
        dayButtons.add((Button) findViewById(R.id.dimanche));

        View.OnClickListener buttonClickListener =  new View.OnClickListener()
        {
            public void onClick(View v)
            {
                int id = dayButtons.indexOf((Button) v);

                if (buttonClicked.get(id))
                {
                    buttonClicked.set(id, false);
                    v.setBackgroundTintList(ColorStateList.valueOf(0xFFBBBBBB));
                    ((Button) v).setTextColor(0x88FFFFFF);
                }
                else
                {
                    buttonClicked.set(id, true);
                    v.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.white)));
                    ((Button) v).setTextColor(getResources().getColor(R.color.blue));
                }
            }
        };

        for (Button button : dayButtons)
        {
            button.setOnClickListener(buttonClickListener);
        }

        buttonClick.setDuration(100);
        buttonClickRelease.setDuration(100);
        buttonClickRelease.setStartOffset(100);

        hours.setCursorVisible(false);
        minutes.setCursorVisible(false);

        hours.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event)
            {
                if(actionId == EditorInfo.IME_ACTION_DONE)
                {
                    checkHours();
                    hours.setCursorVisible(false);
                }

                return false;
            }
        });

        hours.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean hasFocus) {
                if (hasFocus)
                {
                    hours.setCursorVisible(true);
                }
                else
                {
                    hours.setCursorVisible(false);
                    checkHours();
                }
            }
        });

        hours.setOnClickListener(new View.OnClickListener()
        {
            public void onClick(View v)
            {
                hours.setCursorVisible(true);
            }
        });

        minutes.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event)
            {
                if (actionId == EditorInfo.IME_ACTION_DONE)
                {
                    checkMinutes();
                    minutes.setCursorVisible(false);
                }

                return false;
            }
        });

        minutes.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean hasFocus)
            {
                if (hasFocus)
                {
                    minutes.setCursorVisible(true);
                }
                else
                {
                    minutes.setCursorVisible(false);
                    checkMinutes();
                }
            }
        });

        minutes.setOnClickListener(new View.OnClickListener()
        {
            public void onClick(View v)
            {
                minutes.setCursorVisible(true);
            }
        });

        vibrationText.setOnClickListener(new View.OnClickListener()
        {
            public void onClick(View v)
            {
                v.startAnimation(buttonClick);
                v.startAnimation(buttonClickRelease);

                if (vibrateBox.isChecked())
                {
                    vibrateBox.setChecked(false);
                }
                else
                {
                    vibrateBox.setChecked(true);
                }
            }
        });

        emergencyText.setOnClickListener(new View.OnClickListener()
        {
            public void onClick(View v)
            {
                v.startAnimation(buttonClick);
                v.startAnimation(buttonClickRelease);

                if (emergencyBox.isChecked())
                {
                    emergencyBox.setChecked(false);
                }
                else
                {
                    emergencyBox.setChecked(true);
                }
            }
        });

        if (intent.getBooleanExtra("fromHome",false))
        {
            hours.setText(String.valueOf(alarm.getHours()));
            minutes.setText(String.valueOf(alarm.getMinutes()));
            buttonClicked = alarm.getDays();
            vibrateBox.setChecked(alarm.isVibration());
            emergencyBox.setChecked(alarm.isEmergencyAlarm());

            checkMinutes();

            for (int i = 0 ; i < 7 ; i++)
            {
                if (buttonClicked.get(i))
                {
                    dayButtons.get(i).setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.white)));
                    dayButtons.get(i).setTextColor(getResources().getColor(R.color.blue));
                }
                else
                {
                    dayButtons.get(i).setBackgroundTintList(ColorStateList.valueOf(0xFFBBBBBB));
                    dayButtons.get(i).setTextColor(0x88FFFFFF);
                }
            }
        }

        System.out.println(idList);
    }

    public void goToSetupPlaylist(View view)
    {
        Intent intent = new Intent(this, SetupPlaylist.class);
        intent.putExtra("fromAlarm",true);
        intent.putExtra("alarmId",alarmId);
        startActivity(intent);
    }

    public void goToHome(View view)
    {
        alarm.setHours(Integer.valueOf(hours.getText().toString()));
        alarm.setMinutes(Integer.valueOf(minutes.getText().toString()));
        alarm.setDays(buttonClicked);
        alarm.setIdSongsList(idList);
        alarm.setVibration(vibrateBox.isChecked());
        alarm.setEmergencyAlarm(emergencyBox.isChecked());

        app = (WeckerParameters) getApplicationContext();

        if (getIntent().getBooleanExtra("fromHome",false))
        {
            app.getAlarmList().set(alarmId,alarm);
            finish();
        }
        else
        {
            app.addAlarm(alarm);
            Intent intent = new Intent(this, Home.class);
            startActivity(intent);
        }
    }

    public void increaseHours(View v)
    {
        v.startAnimation(buttonClick);
        v.startAnimation(buttonClickRelease);

        hours.setText(String.valueOf((Integer.valueOf(hours.getText().toString()) + 1) % 24));
    }

    public void increaseMinutes(View v)
    {
        v.startAnimation(buttonClick);
        v.startAnimation(buttonClickRelease);

        minutes.setText(String.valueOf((Integer.valueOf(minutes.getText().toString()) + 5) % 60));
        checkMinutes();
    }

    public void decreaseHours(View v)
    {
        v.startAnimation(buttonClick);
        v.startAnimation(buttonClickRelease);

        if (Integer.valueOf(hours.getText().toString()) - 1 < 0)
        {
            hours.setText(String.valueOf(Integer.valueOf(hours.getText().toString()) + 23));
        }
        else
        {
            hours.setText(String.valueOf(Integer.valueOf(hours.getText().toString()) - 1));
        }
    }

    public void decreaseMinutes(View v)
    {
        v.startAnimation(buttonClick);
        v.startAnimation(buttonClickRelease);

        if (Integer.valueOf(minutes.getText().toString()) - 5 < 0)
        {
            minutes.setText(String.valueOf(Integer.valueOf(minutes.getText().toString()) + 55));
        }
        else
        {
            minutes.setText(String.valueOf(Integer.valueOf(minutes.getText().toString()) - 5));
            checkMinutes();
        }
    }

    public void checkHours()
    {
        if (Integer.valueOf(hours.getText().toString()) > 23 || Integer.valueOf(hours.getText().toString()) < 0)
        {
            hours.setText(String.valueOf("0"));
        }
    }

    public void checkMinutes()
    {
        if (minutes.getText().toString().length() == 1)
        {
            minutes.setText("0" + minutes.getText().toString());
        }
        else if (Integer.valueOf(minutes.getText().toString()) > 59 || Integer.valueOf(minutes.getText().toString()) < 0)
        {
            minutes.setText(String.valueOf("00"));
        }
    }

    public void displayEmergencyInstructions(View view)
    {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        builder.setMessage("Alarme d'urgence\n\n"+
                "Déclenche des vibrations continues 5 minutes après validation de l'alarme normale pour garantir le réveil.")
                .setPositiveButton("OK", new DialogInterface.OnClickListener()
                {
                    public void onClick(DialogInterface dialog, int id) {}
                });

        AlertDialog alert = builder.create();
        alert.show();
    }
}
