package com.fpalud.wecker;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
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
import android.widget.Toast;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.GregorianCalendar;

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

    Intent intent;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        app = (WeckerParameters) getApplicationContext();

        intent = getIntent();

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

            File selectedSong = null;

            if (intent.getStringExtra("selectedMusicPath") != null)
            {
                selectedSong = new File(intent.getStringExtra("selectedMusicPath"));
            }

            System.out.println(intent.getBooleanExtra("randomSong",false));
            System.out.println(intent.getBooleanExtra("randomSong",true));

            alarm.setSelectedSong(selectedSong);
            alarm.setSelectedSongId(intent.getStringExtra("selectedMusicId"));
            alarm.setRandomSong(intent.getBooleanExtra("randomSong",true));
            alarm.setRandomPlaylist(intent.getBooleanExtra("randomPlaylist",true));
            System.out.println("Random song ? " + alarm.isRandomSong());
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

                Toast.makeText(app, "Pas encore implémentée.", Toast.LENGTH_SHORT).show();

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
        System.out.println(app.getAlarmList().size());
    }

    @Override
    public void onResume()
    {
        super.onResume();

        app = (WeckerParameters) getApplicationContext();

        if (intent.getBooleanExtra("fromHome",false))
        {
            alarmId = intent.getIntExtra("alarmId",0);

            alarm = app.getAlarmList().get(alarmId);
            idList = alarm.getIdSongsList();
        }
        else
        {
            alarm = new Alarm();

            File selectedSong = null;

            if (intent.getStringExtra("selectedMusicPath") != null)
            {
                selectedSong = new File(intent.getStringExtra("selectedMusicPath"));
            }

            System.out.println(intent.getBooleanExtra("randomSong",false));
            System.out.println(intent.getBooleanExtra("randomSong",true));

            alarm.setSelectedSong(selectedSong);
            alarm.setSelectedSongId(intent.getStringExtra("selectedMusicId"));
            alarm.setRandomSong(intent.getBooleanExtra("randomSong",true));
            alarm.setRandomPlaylist(intent.getBooleanExtra("randomPlaylist",true));

            idList = intent.getStringArrayListExtra("idList");
        }
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
        System.out.println("random song 2 : " + alarm.isRandomSong());

        alarm.setHours(Integer.valueOf(hours.getText().toString()));
        alarm.setMinutes(Integer.valueOf(minutes.getText().toString()));
        alarm.setDays(buttonClicked);
        System.out.println(idList);
        alarm.setIdSongsList(idList);
        alarm.setVibration(vibrateBox.isChecked());
        alarm.setEmergencyAlarm(emergencyBox.isChecked());
        alarm.setActive(true);

        System.out.println("random song 3 : " + alarm.isRandomSong());

        setupAlarm(alarm, this);

        app = (WeckerParameters) getApplicationContext();

        if (getIntent().getBooleanExtra("fromHome",false))
        {
            ArrayList<Alarm> alarmList = app.getAlarmList();
            alarmList.set(alarmId, alarm);
            app.setAlarmList(alarmList);

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
        if (hours.getText().toString().length() == 0 || Integer.valueOf(hours.getText().toString()) > 23 || Integer.valueOf(hours.getText().toString()) < 0)
        {
            hours.setText(String.valueOf("0"));
        }
    }

    public void checkMinutes()
    {
        if (minutes.getText().toString().length() == 0)
        {
            minutes.setText(String.valueOf("00"));
        }
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

    public static void setupAlarm(Alarm alarm, Context context)
    {
        System.out.println("Random song : " + alarm.isRandomSong());

        int[] days = {7,6,0,1,2,3,4,5};
        int[] endMonth = {31,28,31,30,31,30,31,31,30,31,30,31};

        Calendar futureDate = Calendar.getInstance();
        int originalHours = futureDate.get(Calendar.HOUR_OF_DAY);
        int originalMinutes = futureDate.get(Calendar.MINUTE);
        int originalDate = futureDate.get(Calendar.DATE);
        int originalDay = days[futureDate.get(Calendar.DAY_OF_WEEK)];
        int originalMonth = futureDate.get(Calendar.MONTH);
        int originalYear = futureDate.get(Calendar.YEAR);

        int day = originalDay;

        ArrayList<Boolean> daysList = alarm.getDays();
        int wantedHour = alarm.getHours();
        int wantedMinutes = alarm.getMinutes();
        int wantedDay;
        int wantedDate;
        int wantedMonth;
        int wantedYear;
        int counter;

        if (daysList.get(day) && (wantedHour < originalHours || wantedHour == originalHours && wantedMinutes <= originalMinutes))
        {
            day = (day + 1) % 7;
        }

        for (counter = 0 ; counter < 7 && !daysList.get(day) ; counter++)
        {
            day = (day + 1) % 7;
        }

        if (counter != 7)
        {
            wantedDay = day;
            int dayDifference = 0;

            if (wantedDay == originalDay)
            {
                if (counter != 0)
                {
                    dayDifference = 7;
                }
            }
            else if (originalDay > wantedDay)
            {
                dayDifference = wantedDay + 7 - originalDay;
            }
            else
            {
                dayDifference = wantedDay - originalDay;
            }

            if (new GregorianCalendar().isLeapYear(originalYear))
            {
                endMonth[1] = 29;
            }

            if (originalDate + dayDifference > endMonth[originalMonth])
            {
                wantedDate = originalDate + dayDifference - endMonth[originalMonth];

                if (originalMonth < 11)
                {
                    wantedMonth = originalMonth + 1;
                    wantedYear = originalYear;
                }
                else
                {
                    wantedMonth = 0;
                    wantedYear = originalYear + 1;
                }
            }
            else
            {
                wantedDate = originalDate + dayDifference;
                wantedMonth = originalMonth;
                wantedYear = originalYear;
            }

            endMonth[1] = 28;

            futureDate.set(Calendar.HOUR_OF_DAY, wantedHour);
            futureDate.set(Calendar.MINUTE, wantedMinutes);
            futureDate.set(Calendar.SECOND, 0);
            futureDate.set(Calendar.DATE, wantedDate);
            futureDate.set(Calendar.MONTH, wantedMonth);
            futureDate.set(Calendar.YEAR, wantedYear);

            AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

            Intent intent = new Intent(context, LaunchAlarm.class);
            intent.putExtra("alarmId",alarm.getId());
            PendingIntent sender = PendingIntent.getBroadcast(context, alarm.getId(), intent, PendingIntent.FLAG_UPDATE_CURRENT);

            alarmManager.setExact(AlarmManager.RTC_WAKEUP, futureDate.getTimeInMillis(), sender);

            long difference = (futureDate.getTimeInMillis() - Calendar.getInstance().getTimeInMillis()) / 1000;
            long totalMinutes = difference / 60;
            long totalHours;
            long hours;
            long minutes;
            long numberOfDays;

            if (totalMinutes == 0)
            {
                String secondsDisplay = difference + " seconde" + (difference > 1 ? "s." : ".");
                Toast.makeText(context, "Ce réveil sonnera dans " + secondsDisplay, Toast.LENGTH_SHORT).show();
            }
            else if (totalMinutes < 60)
            {
                String minutesDisplay = totalMinutes + " minute" + (totalMinutes > 1 ? "s." : ".");
                Toast.makeText(context, "Ce réveil sonnera dans " + minutesDisplay, Toast.LENGTH_SHORT).show();
            }
            else
            {
                minutes = totalMinutes % 60;
                totalHours = totalMinutes / 60;

                if (totalHours < 24)
                {
                    String hoursDisplay = totalHours + " heure" + (totalHours > 1 ? "s" : "");
                    String minutesDisplay = (minutes > 0 ? " et " + minutes + " minute" + (minutes > 1 ? "s." : "") : ".");

                    Toast.makeText(context, "Ce réveil sonnera dans " + hoursDisplay + minutesDisplay, Toast.LENGTH_SHORT).show();
                }
                else
                {
                    hours = totalHours % 24;
                    numberOfDays = totalHours / 24;

                    String daysDisplay = numberOfDays + " jour" + (numberOfDays > 1 ? "s" : "");
                    String hoursDisplay = (hours > 0 ? (minutes > 0 ? ", " : " et ") + hours + " heure"
                            + (hours > 1 ? "s" : "") + (minutes > 0 ? " et " : ".") : (minutes > 0 ? " et " : "."));
                    String minutesDisplay = (minutes > 0 ? + minutes + " minute" + (minutes > 1 ? "s." : "") : "");

                    Toast.makeText(context, "Ce réveil sonnera dans " + daysDisplay + hoursDisplay + minutesDisplay, Toast.LENGTH_SHORT).show();
                }
            }
        }
        else
        {
            System.out.println("No day selected");
        }
    }
}
