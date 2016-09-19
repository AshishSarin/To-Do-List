package com.squarelabs.sareen.simpletodo.data;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.net.Uri;

import com.squarelabs.sareen.simpletodo.data.ToDoContract.TaskEntry;

/**
 * Created by Ashish Sarin on 14-06-2016.
 */
public class ToDoProvider extends ContentProvider
{

    // The URI matcher used by this content provider
    private static final UriMatcher sUriMatcher = buildUriMatcher();

    private ToDoDbHelper mOpenHelper;


    // Codes used for uri matcher
    static final int TASK = 300;
    static final int TASK_WITH_ID = 301;


    @Override
    public boolean onCreate()
    {
        mOpenHelper = new ToDoDbHelper(getContext());
        return true;
    }


    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs,
                        String sortOrder)
    {
        final SQLiteDatabase db = mOpenHelper.getReadableDatabase();
        Cursor retCursor;
        final int match = sUriMatcher.match(uri);
        switch (match)
        {
            case TASK:
                retCursor = db.query(TaskEntry.TABLE_NAME, projection,
                        selection, selectionArgs, null, null, sortOrder);
                break;

            case TASK_WITH_ID:
                long id = TaskEntry.getIdFromUri(uri);
                retCursor = db.query(TaskEntry.TABLE_NAME, projection, sIdSelection,
                        new String[]{Long.toString(id)},
                        null, null, sortOrder);
                break;

            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }

        retCursor.setNotificationUri(getContext().getContentResolver(), uri);
        return retCursor;
    }

    private static final String sIdSelection =
            TaskEntry.TABLE_NAME + "." +
                    TaskEntry._ID + " = ?";

    @Override
    public String getType(Uri uri)
    {
        // use the uri matcher to find out what type of URI this is
        final int match = sUriMatcher.match(uri);
        switch (match)
        {
            case TASK:
                return TaskEntry.CONTENT_TYPE;

            case TASK_WITH_ID:
                return TaskEntry.CONTENT_ITEM_TYPE;

            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
    }

    @Override
    public Uri insert(Uri uri, ContentValues contentValues)
    {
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        final int match = sUriMatcher.match(uri);
        Uri returnUri;

        switch (match)
        {
            case TASK:
            {
                long _id = db.insert(TaskEntry.TABLE_NAME, null, contentValues);
                if (_id > 0)
                {
                    returnUri = TaskEntry.buildTaskWithIdUri(_id);
                }
                else
                {
                    throw new SQLiteException("Failed to insert row into " + uri);
                }
                break;
            }
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }

        getContext().getContentResolver().notifyChange(uri, null);
        return returnUri;
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectioArgs)
    {
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        final int match = sUriMatcher.match(uri);
        int rowsDeleted;

        if(selection == null)
        {
            // This is done to get no of rows deleted
            // when the selection is null i.e. all rows are deleted
            selection = "1";
        }

        switch (match)
        {
            case TASK:
                rowsDeleted = db.delete(TaskEntry.TABLE_NAME, selection, selectioArgs);
                break;

            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);

        }

        if(rowsDeleted != 0)
        {
            getContext().getContentResolver().notifyChange(uri, null);
        }

        return rowsDeleted;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs)
    {
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        final int match = sUriMatcher.match(uri);
        int rowsUpdated;

        switch (match)
        {
            case TASK:
                rowsUpdated = db.update(TaskEntry.TABLE_NAME, values, selection, selectionArgs);
                break;

            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }

        if(rowsUpdated != 0)
        {
            getContext().getContentResolver().notifyChange(uri, null);
        }

        return 0;
    }


    // This method is used to build a uri matcher and return it.
    static UriMatcher buildUriMatcher()
    {
        // The code passed into the constructor represents the code to return for the root
        // In this case we do not want for root to return any data so NO_MATCH is used
        final UriMatcher matcher = new UriMatcher(UriMatcher.NO_MATCH);
        final String authority = ToDoContract.CONTENT_AUTHORITY;
        matcher.addURI(authority, ToDoContract.PATH_TASKS, TASK);
        matcher.addURI(authority, ToDoContract.PATH_TASKS + "/#", TASK_WITH_ID);
        return matcher;
    }
}
