package com.squarelabs.sareen.simpletodo.alarms;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.media.RingtoneManager;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.text.format.DateFormat;

import com.squarelabs.sareen.simpletodo.R;
import com.squarelabs.sareen.simpletodo.data.ToDoContract;
import com.squarelabs.sareen.simpletodo.ui.AlarmActivity;
import com.squarelabs.sareen.simpletodo.utilities.DateTimeFormat;
import com.squarelabs.sareen.simpletodo.utilities.ToDoDate;
import com.squarelabs.sareen.simpletodo.utilities.ToDoTime;

import java.util.Calendar;

/**
 * Created by Ashish Sarin on 23-06-2016.
 */
public class ToDoAlarmReceiver extends BroadcastReceiver {

    private int NOTIF_ID;
    @Override
    public void onReceive(Context context, Intent intent)
    {

        // Retrieve data from intent
        String task_title;
        long date, time;
        long taskId = intent.getLongExtra("TASK_ID", -1);

        if(taskId != -1)
        {
            Uri mTaskUri = ToDoContract.TaskEntry.buildTaskWithIdUri(taskId);
            String[] projection =
                    {
                            ToDoContract.TaskEntry._ID,
                            ToDoContract.TaskEntry.COLUMN_TASK_TITLE,
                            ToDoContract.TaskEntry.COLUMN_REMINDER_STATE,
                            ToDoContract.TaskEntry.COLUMN_REMINDER_DATE,
                            ToDoContract.TaskEntry.COLUMN_REMINDER_TIME,
                            ToDoContract.TaskEntry.COLUMN_COMPLETE_STATE,
                            ToDoContract.TaskEntry.COLUMN_ALARM_ID
                    };

            String selection = ToDoContract.TaskEntry._ID + " = ?";
            String[] selectionArgs =
                    {
                            Long.toString(taskId)
                    };

            Cursor c = context.getContentResolver()
                    .query(mTaskUri, projection, selection, selectionArgs, null);

            if(c.moveToFirst())
            {
                date = c.getLong(c.getColumnIndex(ToDoContract.TaskEntry.COLUMN_REMINDER_DATE));
                time = c.getLong(c.getColumnIndex(ToDoContract.TaskEntry.COLUMN_REMINDER_TIME));
                task_title = c.getString(c.getColumnIndex(ToDoContract.TaskEntry.COLUMN_TASK_TITLE));
                ToDoDate toDoDate = DateTimeFormat.formatDateFromDatabase(date);
                ToDoTime toDoTime = DateTimeFormat.formatTimeFromDatabase(time);

                Calendar cal = Calendar.getInstance();
                cal.set(Calendar.YEAR, toDoDate.getYear());
                cal.set(Calendar.MONTH, toDoDate.getMonth());
                cal.set(Calendar.DAY_OF_MONTH, toDoDate.getDay());
                cal.set(Calendar.HOUR_OF_DAY, toDoTime.getHour());
                cal.set(Calendar.MINUTE, toDoTime.getMinute());
                String formattedDate = DateFormat.format("d MMM, yyyy", cal).toString();
                String formattedTime;
                if(DateFormat.is24HourFormat(context))
                {
                    formattedTime = DateFormat.format("kk:mm", cal).toString();
                }
                else
                {
                    formattedTime = DateFormat.format("h:mm a", cal).toString();
                }


                String due_on = "Due on:\tToday, " + formattedTime;

                NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(context);
                mBuilder.setSmallIcon(R.drawable.ic_stat_name);
                mBuilder.setContentTitle(task_title);
                mBuilder.setContentText(due_on);
                mBuilder.setAutoCancel(true);

                Uri soundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
                mBuilder.setSound(soundUri);
                Intent resultIntent = new Intent(context, AlarmActivity.class);
                resultIntent.putExtra("TASK_ID", taskId);

                TaskStackBuilder stackBuilder = TaskStackBuilder.create(context);
                stackBuilder.addParentStack(AlarmActivity.class);
                stackBuilder.addNextIntent(resultIntent);
                PendingIntent resultPendingIntent =
                        stackBuilder.getPendingIntent(0,
                                PendingIntent.FLAG_UPDATE_CURRENT);
                mBuilder.setContentIntent(resultPendingIntent);

                NotificationManager notificationManager =
                        (NotificationManager)context
                                .getSystemService(Context.NOTIFICATION_SERVICE);
                SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
                NOTIF_ID = prefs.getInt("NOTIF_ID", 0);
                NOTIF_ID++;
                prefs.edit().putInt("NOTIF_ID", NOTIF_ID).commit();
                notificationManager.notify(NOTIF_ID, mBuilder.build());
            }



        }

    }
}
