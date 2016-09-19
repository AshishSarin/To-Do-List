package com.squarelabs.sareen.simpletodo.ui;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.ContentValues;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.AppCompatSpinner;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.squarelabs.sareen.simpletodo.R;
import com.squarelabs.sareen.simpletodo.alarms.ToDoAlarmReceiver;
import com.squarelabs.sareen.simpletodo.data.ToDoContract;
import com.squarelabs.sareen.simpletodo.utilities.DateTimeFormat;
import com.squarelabs.sareen.simpletodo.utilities.ToDoDate;
import com.squarelabs.sareen.simpletodo.utilities.ToDoTime;

import java.util.Calendar;

public class AlarmActivity extends AppCompatActivity
    implements AppCompatSpinner.OnItemSelectedListener
{

    private int selection = 0;
    private long taskId;
    private String title;
    private long date;
    private long time;
    private int complete_state;
    private int reminder_state;
    private int oldAlarmId;
    private int newAlarmId;

    private String[] projection =
            {
                    ToDoContract.TaskEntry._ID,
                    ToDoContract.TaskEntry.COLUMN_TASK_TITLE,
                    ToDoContract.TaskEntry.COLUMN_REMINDER_STATE,
                    ToDoContract.TaskEntry.COLUMN_REMINDER_DATE,
                    ToDoContract.TaskEntry.COLUMN_REMINDER_TIME,
                    ToDoContract.TaskEntry.COLUMN_COMPLETE_STATE,
                    ToDoContract.TaskEntry.COLUMN_ALARM_ID
            };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_alarm);

        taskId = getIntent().getLongExtra("TASK_ID", -1);

        if(taskId != -1)
        {
            setUpViews();
        }

        AppCompatSpinner snooze_spinner =
                (AppCompatSpinner)findViewById(R.id.snooze_spinner);
        ArrayAdapter<CharSequence> snoozeAdapter =
                ArrayAdapter.createFromResource
                        (this, R.array.snooze_array, android.R.layout.simple_spinner_item);
        snoozeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        snooze_spinner.setAdapter(snoozeAdapter);
        snooze_spinner.setOnItemSelectedListener(this);
        snooze_spinner.setSelection(1);
    }

    private void setUpViews()
    {
        String selection = ToDoContract.TaskEntry._ID + " = ?";
        String[] selectionArgs = {Long.toString(taskId)};
        Cursor c = getContentResolver()
                .query(ToDoContract.TaskEntry.CONTENT_URI,
                        projection, selection, selectionArgs, null);
        if(c.moveToFirst())
        {
            title = c.getString(c.getColumnIndex(ToDoContract.TaskEntry.COLUMN_TASK_TITLE));
            TextView title_alarm = (TextView)findViewById(R.id.task_title_text_alarm);
            title_alarm.setText(title);
            oldAlarmId = c.getInt(c.getColumnIndex(ToDoContract.TaskEntry.COLUMN_ALARM_ID));
            date = c.getLong(c.getColumnIndex(ToDoContract.TaskEntry.COLUMN_REMINDER_DATE));
            time = c.getLong(c.getColumnIndex(ToDoContract.TaskEntry.COLUMN_REMINDER_TIME));
            reminder_state = c.getInt(c.getColumnIndex(ToDoContract.TaskEntry.COLUMN_REMINDER_STATE));
            complete_state = c.getInt(c.getColumnIndex(ToDoContract.TaskEntry.COLUMN_COMPLETE_STATE));
        }
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id)
    {
        selection = position;
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }

    public void onAlarmDone(View view)
    {
        switch (selection)
        {
            case 0:
                // Snooze off is selected
                showToast("Snooze off");
                break;
            case 1:
                // Snooze for 10 minute
                snoozeAlarm(10);
                showToast("Snoozed for 10 minute");
                break;
            case 2:
                // Snooze for 30 minute
                snoozeAlarm(30);
                showToast("Snoozed for 30 minutes");
                break;
            case 3:
                // Snooze for 1 hour
                snoozeAlarm(60);
                showToast("Snoozed for 1 hour");
                break;
        }
        this.finish();
    }

    private void snoozeAlarm(int i)
    {
        Calendar c = Calendar.getInstance();
        c.add(Calendar.MINUTE, i);

        int y = c.get(Calendar.YEAR);
        int m = c.get(Calendar.MONTH);
        int d = c.get(Calendar.DAY_OF_MONTH);
        int h = c.get(Calendar.HOUR_OF_DAY);
        int min = c.get(Calendar.MINUTE);


        date = DateTimeFormat.formatDateForDatabase(y, m, d);
        time = DateTimeFormat.formatTimeForDatabase(h, min);



        cancelAlarm(oldAlarmId);
        newAlarmId = getAlarmId();
        setAlarm(date, time);

        ContentValues cv = new ContentValues();
        cv.put(ToDoContract.TaskEntry.COLUMN_TASK_TITLE, title);
        cv.put(ToDoContract.TaskEntry.COLUMN_REMINDER_STATE, reminder_state);
        cv.put(ToDoContract.TaskEntry.COLUMN_REMINDER_DATE, date);
        cv.put(ToDoContract.TaskEntry.COLUMN_REMINDER_TIME, time);
        cv.put(ToDoContract.TaskEntry.COLUMN_COMPLETE_STATE, 0);
        cv.put(ToDoContract.TaskEntry.COLUMN_ALARM_ID, newAlarmId);
        String selection = ToDoContract.TaskEntry._ID + " = ?";
        String[] selectionArgs = {Long.toString(taskId)};
        getContentResolver().update(ToDoContract.TaskEntry.CONTENT_URI,
                cv, selection, selectionArgs);

    }

    private void showToast(String text)
    {
        Toast.makeText(this, text, Toast.LENGTH_SHORT).show();
    }

    public void onTaskComplete(View view)
    {
        ContentValues cv = new ContentValues();
        cv.put(ToDoContract.TaskEntry.COLUMN_TASK_TITLE, title);
        cv.put(ToDoContract.TaskEntry.COLUMN_REMINDER_STATE, reminder_state);
        cv.put(ToDoContract.TaskEntry.COLUMN_REMINDER_DATE, date);
        cv.put(ToDoContract.TaskEntry.COLUMN_REMINDER_TIME, time);
        cv.put(ToDoContract.TaskEntry.COLUMN_COMPLETE_STATE, 1);
        cv.put(ToDoContract.TaskEntry.COLUMN_ALARM_ID, -1);
        String selection = ToDoContract.TaskEntry._ID + " = ?";
        String[] selectionArgs = {Long.toString(taskId)};
        getContentResolver().update(ToDoContract.TaskEntry.CONTENT_URI,
                cv, selection, selectionArgs);
        this.finish();
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

    private void setAlarm(long date, long time)
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

    private int getAlarmId()
    {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        int uniqueId = prefs.getInt(getString(R.string.unique_alarm_id), 0);
        uniqueId++;
        return uniqueId;
    }
}
