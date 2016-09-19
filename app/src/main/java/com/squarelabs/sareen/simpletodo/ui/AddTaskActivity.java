package com.squarelabs.sareen.simpletodo.ui;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.DialogFragment;
import android.app.PendingIntent;
import android.content.ContentValues;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SwitchCompat;
import android.text.Editable;
import android.text.Selection;
import android.text.TextUtils;
import android.text.format.DateFormat;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.inputmethod.InputMethodManager;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.squarelabs.sareen.simpletodo.R;
import com.squarelabs.sareen.simpletodo.alarms.ToDoAlarmReceiver;
import com.squarelabs.sareen.simpletodo.data.ToDoContract;
import com.squarelabs.sareen.simpletodo.data.ToDoContract.TaskEntry;
import com.squarelabs.sareen.simpletodo.pickers.DatePickerFragment;
import com.squarelabs.sareen.simpletodo.pickers.TimePickerFragment;
import com.squarelabs.sareen.simpletodo.utilities.DateTimeFormat;
import com.squarelabs.sareen.simpletodo.utilities.ToDoDate;
import com.squarelabs.sareen.simpletodo.utilities.ToDoTime;
import com.squarelabs.sareen.simpletodo.utilities.Utility;

import java.util.Calendar;

public class AddTaskActivity extends AppCompatActivity
    implements TimePickerFragment.onTimeSelectedListener,
        DatePickerFragment.onDateSelectedListener
{
    private static final int NO_DATE_OR_TIME = -501;
    private String date_text, time_text;                                   // These are to store date and time for reminder_text
    private boolean task_edit_tracker;                                    // This tracks whether the task is being edited or added
    private boolean date_edit_tracker;                                   // This tracks whether the date is being updated or not
    private Uri mSelectedItemUri;
    private EditText title_edit;
    private SwitchCompat reminder_switch;
    private LinearLayout date_time_layout;
    private EditText date_edit;
    private EditText time_edit;
    private TextView reminder_date_time_textView;
    private FloatingActionButton fab_add;

    private String title;
    private boolean reminder_state = false;
    private int colorCode = Utility.color_codes[0];
    private long date = -1;
    private long time = -1;
    private int oldArlamId = -1;                // This is the alarm id of task for its previous alarm (if in edit mode)
    private int newAlarmId = -1;                // This is the new alarm id of task for its new alarm (if in edit mode) or first alarm;



    private String[] projection =
            {
                    TaskEntry._ID,
                    TaskEntry.COLUMN_TASK_TITLE,
                    TaskEntry.COLUMN_REMINDER_STATE,
                    TaskEntry.COLUMN_REMINDER_DATE,
                    TaskEntry.COLUMN_REMINDER_TIME,
                    TaskEntry.COLUMN_COMPLETE_STATE,
                    TaskEntry.COLUMN_ALARM_ID,
                    TaskEntry.COLUMN_COLOR_CODE
            };

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_task);

        CoordinatorLayout coordinatorLayout = (CoordinatorLayout)findViewById(R.id.add_layout);
        coordinatorLayout.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                hideKeyboard(v);
                return true;
            }
        });

        mSelectedItemUri = getIntent().getData();
        if(mSelectedItemUri == null)
        {
            task_edit_tracker = false;
        }
        else
        {
            task_edit_tracker = true;
        }
        initializeViews();
    }

    private void initializeViews()
    {
        title_edit = (EditText)findViewById(R.id.title_add_edit);
        reminder_switch = (SwitchCompat)findViewById(R.id.reminder_add_switch);
        date_edit = (EditText)findViewById(R.id.date_add_edit);
        time_edit = (EditText)findViewById(R.id.time_add_edit);
        reminder_date_time_textView = (TextView)findViewById(R.id.reminder_date_time_add_textview);
        date_time_layout = (LinearLayout)findViewById(R.id.date_time_add_layout);
        fab_add = (FloatingActionButton) findViewById(R.id.add_task_fab);
        setUpViews();
    }

    private void setUpViews()
    {

        reminder_switch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {


                if(isChecked)
                {
                    Animation fadeIn = new AlphaAnimation(0f, 1f);
                    fadeIn.setDuration(500);
                    date_time_layout.startAnimation(fadeIn);
                    reminder_date_time_textView.startAnimation(fadeIn);
                    reminder_date_time_textView.setVisibility(View.VISIBLE);
                    date_time_layout.setVisibility(View.VISIBLE);
                    reminder_date_time_textView.setVisibility(View.VISIBLE);

                    reminder_state = true;


                    // check if date and time are not intiated
                    if(date == -1 || time == -1)
                    {
                        // make date and time equal to today date and next hour time
                        date = Utility.getTodayDate();
                        time = Utility.getNextHourTime();

                        fillUpDateAndTimeViews(date, time);


                    }
                }
                else
                {
                    Animation anim = new AlphaAnimation(1f, 0f);
                    anim.setDuration(500);
                    date_time_layout.startAnimation(anim);
                    reminder_date_time_textView.startAnimation(anim);
                    date_time_layout.setVisibility(View.INVISIBLE);
                    reminder_date_time_textView.setVisibility(View.INVISIBLE);
                    reminder_state = false;
                    date = time = -1;
                }

            }
        });


        // TODO : Convert this code to be used in cursor loader
        if(mSelectedItemUri != null)
        {
            // This is executed if the user click on an item for editing
            Cursor cursor = getContentResolver().query(mSelectedItemUri, projection, null, null, null);
            if(cursor != null & cursor.moveToFirst())
            {
                // getting the color code
                colorCode = cursor.getInt(cursor.getColumnIndex(TaskEntry.COLUMN_COLOR_CODE));

                // setting up the title edit_text
                title = cursor.getString(cursor.getColumnIndex(TaskEntry.COLUMN_TASK_TITLE));
                title_edit.setText(title);
                int l = title_edit.length();
                Editable etext = title_edit.getText();
                Selection.setSelection(etext, l);


                // setting up the reminder switch
                reminder_state = ((cursor.getInt(cursor.getColumnIndex(TaskEntry.COLUMN_REMINDER_STATE))) == 0) ? false : true;

                reminder_switch.setChecked(reminder_state);

                if(reminder_state == true)
                {
                    // set-up the date_edit view
                    date = cursor.getLong(cursor.getColumnIndex(TaskEntry.COLUMN_REMINDER_DATE));

                    // set-up the time edit_view
                    time = cursor.getLong(cursor.getColumnIndex(TaskEntry.COLUMN_REMINDER_TIME));

                    fillUpDateAndTimeViews(date, time);

                }
                else
                {
                    date_time_layout.setVisibility(View.GONE);
                    reminder_date_time_textView.setVisibility(View.GONE);
                }

                oldArlamId = cursor.getInt(cursor.getColumnIndex(TaskEntry.COLUMN_ALARM_ID));
            }

        }

        else
        {
            reminder_state = false;
            reminder_switch.setChecked(false);
            date_time_layout.setVisibility(View.GONE);
            reminder_date_time_textView.setVisibility(View.GONE);
            time = -1;
            date = -1;
        }


        date_edit.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                // open date dialog picker
                DialogFragment datePickerFragment = new DatePickerFragment();
                datePickerFragment.show(getFragmentManager(), "datePicker");
            }
        });

        time_edit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v)
            {
                // open time dialog picker
                DialogFragment timePickerFragment = new TimePickerFragment();
                timePickerFragment.show(getFragmentManager(), "timePicker");
            }
        });

        // TODO make sure it works with loader and keyboard is opened only after loader is finished
        // loading the data

        if(!task_edit_tracker)
        {
            getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
        }

        title_edit.requestFocus();

        fab_add.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onAddTask(v);
            }
        });


    }

    public void hideKeyboard(View view)
    {
        InputMethodManager inputMethodManager
                =(InputMethodManager)getSystemService(Activity.INPUT_METHOD_SERVICE);
        inputMethodManager.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }



    @Override
    public void onTimeSelected(int hourOfDay, int minute)
    {
        long selectedTime = DateTimeFormat.formatTimeForDatabase(hourOfDay, minute);
        int compareToday = Utility.compareWithToday(date);
        if(compareToday == 0)
        {

            // date is today. so check whether the time has passed or not
            long currentTime = Utility.getCurrentTime();
            if(Utility.compareWithCurrentTime(selectedTime) <= 0)
            {
                Toast.makeText(this, "The time has been passed", Toast.LENGTH_SHORT).show();
                return;
            }
        }
        else if(compareToday == -1)
        {
            Toast.makeText(this, "The time has been passed", Toast.LENGTH_SHORT).show();
            return;
        }
        time = selectedTime;
        fillUpDateAndTimeViews(NO_DATE_OR_TIME, time);

    }




    @Override
    public void onDateSelected(int year, int monthOfYear, int dayOfMonth)
    {
        long dateSelected = DateTimeFormat.formatDateForDatabase(year, monthOfYear, dayOfMonth);
        if(Utility.compareWithToday(dateSelected) == -1)
        {
            // Date selected has been passed
            Toast.makeText(this, "The date has been passed", Toast.LENGTH_SHORT).show();
            return;
        }
        date = dateSelected;
        fillUpDateAndTimeViews(date, NO_DATE_OR_TIME);
    }

    private void onAddTask(View view)
    {
        if(Utility.isTimePassed(date, time) && reminder_state )
        {
            Toast.makeText(this, "Time has been passed", Toast.LENGTH_SHORT).show();
            return;
        }
        String t = title_edit.getText().toString();
        if(TextUtils.isEmpty(t))
        {
            Toast.makeText(this, "Title field cannot be empty", Toast.LENGTH_SHORT).show();
        }
        else
        {
            title = t.substring(0,1).toUpperCase() + t.substring(1); // converts first letter into Caps
            reminder_state = reminder_switch.isChecked();
            ContentValues values = new ContentValues();
            values.put(TaskEntry.COLUMN_TASK_TITLE, title);
            values.put(TaskEntry.COLUMN_REMINDER_STATE, reminder_state);
            values.put(TaskEntry.COLUMN_REMINDER_DATE, date);
            values.put(TaskEntry.COLUMN_REMINDER_TIME, time);
            values.put(TaskEntry.COLUMN_COMPLETE_STATE, 0);


            if(date != -1 && time != - 1)
            {
                newAlarmId = getAlarmId();
            }

            if (task_edit_tracker)
            {
                // previous task is updated
                long _id = TaskEntry.getIdFromUri(mSelectedItemUri);
                String selection = TaskEntry._ID + " = ?";
                String[] selectionArgs = {Long.toString(_id)};
                values.put(TaskEntry.COLUMN_COLOR_CODE, colorCode);
                values.put(TaskEntry.COLUMN_ALARM_ID, newAlarmId); // set the updated alarm id
                getContentResolver().update(TaskEntry.CONTENT_URI, values,
                        selection, selectionArgs);
                long taskId = TaskEntry.getIdFromUri(mSelectedItemUri);
                updateAlarm(oldArlamId, taskId);     // update alarm
            }
            else
            {
                // new task is  added
                colorCode = Utility.getColorCode(title.charAt(0));      // TODO: make sure this does not casue exception when first character is empty
                values.put(TaskEntry.COLUMN_COLOR_CODE, colorCode);
                values.put(TaskEntry.COLUMN_ALARM_ID, newAlarmId);
                Uri taskUri = getContentResolver().insert(ToDoContract.TaskEntry.CONTENT_URI, values);
                long taskId = TaskEntry.getIdFromUri(taskUri);
                setAlarm(date, time, taskId);
            }


            // close the activity
            this.finish();
        }
    }

    private int getAlarmId() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        int uniqueId = prefs.getInt(getString(R.string.unique_alarm_id), 0);
        uniqueId++;
        return uniqueId;
    }

    private void updateAlarm(int aID, long taskId)
    {
        cancelAlarm(aID);
        setAlarm(date, time, taskId);
    }

    private void cancelAlarm(int aID)
    {
        if(aID != -1)
        {
            Intent intent = new Intent(getBaseContext(), ToDoAlarmReceiver.class);
            PendingIntent pi = PendingIntent.getBroadcast
                    (getBaseContext(), aID, intent, 0);
            AlarmManager am = (AlarmManager) getSystemService(ALARM_SERVICE);
            am.cancel(pi);
        }
    }

    private void setAlarm(long date, long time, long taskId)
    {
        if(date != -1 && time != -1 && newAlarmId != -1)
        {
            Calendar targetCal = Calendar.getInstance();
            ToDoDate todoDate = DateTimeFormat.formatDateFromDatabase(date);
            ToDoTime todoTime = DateTimeFormat.formatTimeFromDatabase(time);
            targetCal.set(Calendar.YEAR, todoDate.getYear());
            targetCal.set(Calendar.MONTH, todoDate.getMonth());
            targetCal.set(Calendar.DAY_OF_MONTH, todoDate.getDay());
            targetCal.set(Calendar.HOUR_OF_DAY, todoTime.getHour());
            targetCal.set(Calendar.MINUTE, todoTime.getMinute());
            targetCal.set(Calendar.SECOND, 0);
            targetCal.set(Calendar.MILLISECOND, 0);

            Intent intent = new Intent(getBaseContext(), ToDoAlarmReceiver.class);
            intent.putExtra("TASK_ID", taskId);

            PendingIntent pi = PendingIntent.getBroadcast(getBaseContext(), newAlarmId, intent, 0);
            AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT)
            {
                alarmManager.setExact(AlarmManager.RTC_WAKEUP, targetCal.getTimeInMillis(), pi);
            }
            else
            {
                alarmManager.set(AlarmManager.RTC_WAKEUP, targetCal.getTimeInMillis(), pi);
            }
            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
            SharedPreferences.Editor pref_edit = prefs.edit();
            pref_edit.putInt(getString(R.string.unique_alarm_id), newAlarmId);
            pref_edit.commit();

        }
    }

    private  void fillUpDateAndTimeViews(long dt, long tm)
    {
        // TODO: modify this method so that is shows Today in place date for current day
        // filling the date_edit text
        if(dt != NO_DATE_OR_TIME)
        {

            ToDoDate myDate = DateTimeFormat.formatDateFromDatabase(dt);
            Calendar c_date = Calendar.getInstance();
            c_date.set(Calendar.YEAR, myDate.getYear());
            c_date.set(Calendar.MONTH, myDate.getMonth());
            c_date.set(Calendar.DAY_OF_MONTH, myDate.getDay());
            if(Utility.compareWithToday(dt) == 0)
            {
                date_edit.setText("Today");
            }
            else
            {
                date_edit.setText(DateFormat.format("d MMM, yyyy", c_date));
            }
            date_text = (DateFormat.format("d MMM, yyyy", c_date)).toString();
        }

        // filling the time_edit text
        if(tm != NO_DATE_OR_TIME)
        {
            ToDoTime myTime = DateTimeFormat.formatTimeFromDatabase(tm);

            Calendar c_time = Calendar.getInstance();
            c_time.set(Calendar.HOUR_OF_DAY, myTime.getHour());
            c_time.set(Calendar.MINUTE, myTime.getMinute());

            if (DateFormat.is24HourFormat(AddTaskActivity.this)) {
                time_edit.setText(DateFormat.format("kk:mm", c_time));
                time_text = (DateFormat.format("kk:mm", c_time)).toString();

            }
            else
            {
                time_edit.setText(DateFormat.format("h:mm a", c_time));
                time_text = (DateFormat.format("h:mm a", c_time)).toString();
            }


        }

        fillUpReminderText();
    }



    private void fillUpReminderText()
    {
        reminder_date_time_textView.setText("Reminder set for " +  date_text + ", " + time_text);
    }


    public void onClose(View view)
    {
        finish();
    }

    public void onRemoveTask(View view)
    {
        if(task_edit_tracker)
        {
            int pos = getIntent().getIntExtra("ITEM_POSITION", 0);
            Intent resultIntent = new Intent();
            resultIntent.setData(mSelectedItemUri);
            resultIntent.putExtra("ITEM_POSITION", pos);
            setResult(RESULT_OK, resultIntent);
        }
        finish();
    }
}



