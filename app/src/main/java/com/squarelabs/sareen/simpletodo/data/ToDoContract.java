package com.squarelabs.sareen.simpletodo.data;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.net.Uri;
import android.provider.BaseColumns;

/**
 * Created by Ashish Sarin on 14-06-2016.
 */

public class ToDoContract
{
    public static final String CONTENT_AUTHORITY = "com.squarelabs.sareen.simpletodo.provider";

    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);

    public static final String PATH_TASKS = "tasks";

    public static final class TaskEntry implements BaseColumns
    {
        /*Declaring and defining constants for table name and columns*/

        // Tasks table name
        public static final String TABLE_NAME = "tasks";

        // Column with title of task
        public static final String COLUMN_TASK_TITLE = "task_title";

        // Column storing whether reminder is on/off for task
        // [stored ON as 1 and OFF as 0]
        public static final String COLUMN_REMINDER_STATE = "reminder_state";

        // Column with reminder date
        // [stored as long - eg. 20161131 for 31/12/2016]
        public static final String COLUMN_REMINDER_DATE = "reminder_date";

        // Column with reminder time
        // stored as long in 24 format - eg. 1636 for 04:36 pm;
        public static final String COLUMN_REMINDER_TIME = "reminder_time";

        //Column storing whether task is ccmpleted
        //[stored as 1 for completed and 0 for not completed
        public static final String COLUMN_COMPLETE_STATE = "complete_state";

        // Column to store the alarm id associated with the task
        // used to cancel or change alarm
        public static final String COLUMN_ALARM_ID = "alarm_id";

        // Column for storing color code of task
        public static final String COLUMN_COLOR_CODE = "color_code";

        /* Declaring and defining Uris for content provider */
        public static final Uri CONTENT_URI =
                BASE_CONTENT_URI.buildUpon().appendPath(PATH_TASKS).build();

        // Defining the type of content returned by uris (single item or multiple items)

        // The type of data returned when querying for multiple item
        //[eg. in MainActivity]
        public static final String CONTENT_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_URI + "/" + PATH_TASKS;

        // The type of data returned when querying for single item
        // [eg. in AddTaskActivity]
        public static final String CONTENT_ITEM_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_URI + "/" + PATH_TASKS;


        public static Uri buildTaskUri()
        {
            return CONTENT_URI;
        }

        public static Uri buildTaskWithIdUri(long id)
        {
            return ContentUris.withAppendedId(CONTENT_URI, id);
        }

        public static long getIdFromUri(Uri uri)
        {
            return ContentUris.parseId(uri);
        }


    }
}
