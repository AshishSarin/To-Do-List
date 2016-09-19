package com.squarelabs.sareen.simpletodo.ui;

import android.app.AlarmManager;
import android.app.LoaderManager;
import android.app.PendingIntent;
import android.content.ContentValues;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Paint;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.AppCompatCheckBox;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import com.squarelabs.sareen.simpletodo.R;
import com.squarelabs.sareen.simpletodo.adapters.TaskAdapter;
import com.squarelabs.sareen.simpletodo.alarms.ToDoAlarmReceiver;
import com.squarelabs.sareen.simpletodo.data.ToDoContract.TaskEntry;
import com.squarelabs.sareen.simpletodo.utilities.DateTimeFormat;
import com.squarelabs.sareen.simpletodo.utilities.ToDoDate;
import com.squarelabs.sareen.simpletodo.utilities.ToDoTime;
import com.squarelabs.sareen.simpletodo.utilities.Utility;

import java.util.Calendar;

public class MainActivity extends AppCompatActivity
    implements LoaderManager.LoaderCallbacks<Cursor>
{
    private static final int TASK_LOADER = -1001;
    private static final int DELETE_MODE = 201;
    private static final int COMPLETE_MODE = 202;
    private static final int ADD_TASK_REQUEST_CODE = 2404;

    private ListView mTaskListView;
    private TaskAdapter mTaskAdapter;

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


    private int newAlarmId = -1;
    private String title;
    private int remider_state;
    private long date;
    private long time;
    private long completed;
    private int colorCode;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        getSupportActionBar().setElevation(0f);

        FloatingActionButton floatingActionButton =
                (FloatingActionButton)findViewById(R.id.main_fab);
        floatingActionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Todo start add task activity
                startActivity(new Intent(MainActivity.this, AddTaskActivity.class));
            }
        });

        mTaskAdapter = new TaskAdapter(this);

        mTaskListView = (ListView) findViewById(R.id.task_listview);


        // Set up the empty view to show when there is no task
        mTaskListView.setEmptyView(findViewById(android.R.id.empty));




        mTaskListView.setAdapter(mTaskAdapter);
        mTaskListView.setOnItemClickListener(new AdapterView.OnItemClickListener()
        {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id)
            {
                if(view.getId() == R.id.task_list_item_complete)
                {
                    View listItem = (View)view.getParent();
                    final TextView titleView = (TextView)listItem.findViewById(R.id.task_list_item_title);
                    AppCompatCheckBox checkBox = (AppCompatCheckBox)view;
                    if(checkBox.isChecked())
                    {
                        titleView.setPaintFlags(titleView.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
                        updateTask(COMPLETE_MODE, id, position, true);
                        // Update the task as completed in database
                    }
                    else
                    {
                        titleView.setPaintFlags(titleView.getPaintFlags() & (~Paint.STRIKE_THRU_TEXT_FLAG));
                        updateTask(COMPLETE_MODE, id, position, false);
                        // update the task as not completed in database
                    }
                }
                else
                {
                    Cursor c = mTaskAdapter.getCursor();
                    c.moveToPosition(position);
                    long selectedItemId = c.getLong(c.getColumnIndex(TaskEntry._ID));
                    Uri mSelectedItemUri = TaskEntry.buildTaskWithIdUri(selectedItemId);
                    Intent intent = new Intent(MainActivity.this, AddTaskActivity.class);
                    intent.setData(mSelectedItemUri);
                    intent.putExtra("ITEM_POSITION", position);
                    startActivityForResult(intent, ADD_TASK_REQUEST_CODE);
                }
            }
        });

        getLoaderManager().initLoader(TASK_LOADER, null, this);

    }

    private void updateTask(int mode, long id, int position, boolean completed)
    {
        if(mode == COMPLETE_MODE)
        {
            Cursor c = mTaskAdapter.getCursor();
            c.moveToPosition(position);
            ContentValues cv = new ContentValues();
            String title = c.getString(c.getColumnIndex(TaskEntry.COLUMN_TASK_TITLE));
            int reminder_state = c.getInt(c.getColumnIndex(TaskEntry.COLUMN_REMINDER_STATE));
            long date = c.getLong(c.getColumnIndex(TaskEntry.COLUMN_REMINDER_DATE));
            long time = c.getLong(c.getColumnIndex(TaskEntry.COLUMN_REMINDER_TIME));
            int alarmId = c.getInt(c.getColumnIndex(TaskEntry.COLUMN_ALARM_ID));
            int color_code = c.getInt(c.getColumnIndex(TaskEntry.COLUMN_COLOR_CODE));
            cv.put(TaskEntry.COLUMN_TASK_TITLE, title);
            cv.put(TaskEntry.COLUMN_REMINDER_STATE, reminder_state);
            cv.put(TaskEntry.COLUMN_REMINDER_DATE, date);
            cv.put(TaskEntry.COLUMN_REMINDER_TIME, time);
            cv.put(TaskEntry.COLUMN_COLOR_CODE, color_code);
            if (completed) {
                // task is completed
                cv.put(TaskEntry.COLUMN_COMPLETE_STATE, 1);
                cancelAlarm(alarmId);
                cv.put(TaskEntry.COLUMN_ALARM_ID, -1);
            } else {
                // task is not completed
                cv.put(TaskEntry.COLUMN_COMPLETE_STATE, 0);
                // Get unique alarm id for this task
                if(Utility.isTimePassed(date, time))
                {
                    newAlarmId = -1;
                }
                else
                {
                    newAlarmId = getAlarmId();
                    setAlarm(date, time, id);
                }
                cv.put(TaskEntry.COLUMN_ALARM_ID, newAlarmId);

            }
            String selection = TaskEntry._ID + " = ?";
            String[] selectionArgs = {Long.toString(id)};
            getContentResolver()
                    .update(TaskEntry.CONTENT_URI, cv, selection, selectionArgs);
        }
        else if(mode == DELETE_MODE)
        {
            String selection = TaskEntry._ID + " = ?";
            String[] selectionArgs = {Long.toString(id)};
            Cursor c = mTaskAdapter.getCursor();
            copyData(c, position);        // Temporary store date in variables
            c.moveToPosition(position);
            int alarmId = c.getInt(c.getColumnIndex(TaskEntry.COLUMN_ALARM_ID));
            cancelAlarm(alarmId);
            getContentResolver().delete(TaskEntry.CONTENT_URI, selection, selectionArgs);
            Snackbar.make(findViewById(R.id.task_list_layout),
                    "The task has been deleted", Snackbar.LENGTH_LONG)
                    .setAction("UNDO", new View.OnClickListener() {
                        @Override
                        public void onClick(View v)
                        {
                            reInsertData(); // reinsert task into the database
                        }
                    }).show();

        }
    }



    private int getAlarmId() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        int uniqueId = prefs.getInt(getString(R.string.unique_alarm_id), 0);
        uniqueId++;
        return uniqueId;
    }


    private void setAlarm(long date, long time, long taskId)
    {
        if(date != -1 && time != -1)
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

    private void copyData(Cursor c, int position)
    {
        c.moveToPosition(position);
        title = c.getString(c.getColumnIndex(TaskEntry.COLUMN_TASK_TITLE));
        remider_state = c.getInt(c.getColumnIndex(TaskEntry.COLUMN_REMINDER_STATE));
        date = c.getLong(c.getColumnIndex(TaskEntry.COLUMN_REMINDER_DATE));
        time = c.getLong(c.getColumnIndex(TaskEntry.COLUMN_REMINDER_TIME));
        completed = c.getInt(c.getColumnIndex(TaskEntry.COLUMN_COMPLETE_STATE));
        colorCode = c.getInt(c.getColumnIndex(TaskEntry.COLUMN_COLOR_CODE));
    }

    private void reInsertData()
    {
        ContentValues cv = new ContentValues();
        cv.put(TaskEntry.COLUMN_TASK_TITLE, title);
        cv.put(TaskEntry.COLUMN_REMINDER_STATE, remider_state);
        cv.put(TaskEntry.COLUMN_REMINDER_DATE, date);
        cv.put(TaskEntry.COLUMN_REMINDER_TIME, time);
        cv.put(TaskEntry.COLUMN_COMPLETE_STATE, completed);
        cv.put(TaskEntry.COLUMN_COLOR_CODE, colorCode);
        boolean isTimePassed = Utility.isTimePassed(date, time);
        if(isTimePassed)
        {
            newAlarmId = -1;
        }
        else
        {
            newAlarmId = getAlarmId();
        }
        cv.put(TaskEntry.COLUMN_ALARM_ID, newAlarmId);
        Uri insertedTaskUri = getContentResolver().insert(TaskEntry.CONTENT_URI, cv);
        long insertedTaskId = TaskEntry.getIdFromUri(insertedTaskUri);
        if(!isTimePassed)
        {
            setAlarm(date, time, insertedTaskId);
        }

    }

    private void cancelAlarm(int alarmId)
    {
        if(alarmId != -1)
        {
            Intent intent = new Intent(getBaseContext(), ToDoAlarmReceiver.class);
            PendingIntent pi = PendingIntent.getBroadcast(getBaseContext(), alarmId, intent, 0);
            AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
            alarmManager.cancel(pi);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode == RESULT_OK && requestCode == ADD_TASK_REQUEST_CODE)
        {
            if(data != null)
            {
                Uri mItemUri = data.getData();
                if(mItemUri != null && data.hasExtra("ITEM_POSITION"))
                {
                    int pos = data.getIntExtra("ITEM_POSITION", 0);
                    long id = TaskEntry.getIdFromUri(mItemUri);
                    updateTask(DELETE_MODE, id, pos, true);
                }
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        int itemId = item.getItemId();
        switch(itemId)
        {
            case R.id.action_about:
                // Todo Open About Page
                startActivity(new Intent(this, AboutActivity.class));
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    // CursorLoader call back methods

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args)
    {
        String sortOrder = TaskEntry._ID + " ASC";
        Uri mTaskUri = TaskEntry.buildTaskUri();
        return new CursorLoader(this, mTaskUri, projection, null, null, sortOrder);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> cursorLoader, Cursor data)
    {
        mTaskAdapter.swapCursor(data);
    }

    public void onLoaderReset(Loader<Cursor> cursorLoader)
    {
        mTaskAdapter.swapCursor(null);
    }

}
