package com.squarelabs.sareen.simpletodo.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import com.squarelabs.sareen.simpletodo.data.ToDoContract.TaskEntry;

/**
 * Created by Ashish Sarin on 14-06-2016.
 */
public class ToDoDbHelper extends SQLiteOpenHelper
{
    // Version of database
    // If schema of database is changed,
    // then this needs to be incremented
    public static final int DATABASE_VERSION = 3;

    // Name of file in which database is stored
    static final String DATABASE_NAME = "simpletodo.db";

    public ToDoDbHelper(Context context)
    {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db)
    {
        final String SQL_CREATE_TASK_TABLE = "CREATE TABLE " + TaskEntry.TABLE_NAME + " ("
                + TaskEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + TaskEntry.COLUMN_TASK_TITLE + " TEXT NOT NULL, "
                + TaskEntry.COLUMN_REMINDER_STATE + " INTEGER NOT NULL, "
                + TaskEntry.COLUMN_REMINDER_DATE + " INTEGER NOT NULL, "
                + TaskEntry.COLUMN_REMINDER_TIME + " INTEGER NOT NULL, "
                + TaskEntry.COLUMN_COMPLETE_STATE + " INTEGER NOT NULL, "
                + TaskEntry.COLUMN_ALARM_ID + " INTEGER NOT NULL, "
                + TaskEntry.COLUMN_COLOR_CODE + " INTEGER NOT NULL "
                + ");";

        db.execSQL(SQL_CREATE_TASK_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion)
    {
        // TODO: Modify this method so that user data is not lost
        db.execSQL("DROP TABLE IF EXISTS " + TaskEntry.TABLE_NAME);
        onCreate(db);
    }

}
