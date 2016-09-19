package com.squarelabs.sareen.simpletodo.utilities;

/**
 * Created by Ashish Sarin on 14-06-2016.
 */

/* This class is used to format date and time for storing to, and retrieving from the database.*/
public class DateTimeFormat
{
    /* This method converts date into long integer for storing into database*/
    public static long formatDateForDatabase(int year, int month, int day)
    {
        if(year == -1 || month == -1 || day == -1)
        {
            // if remind me is turned off
            return -1L;
        }

        // remind me is turned on
        long date = year * 10000 + month * 100 + day;
        return date;
    }

    /* This method converts time into long integer for storing into database*/
    public static long formatTimeForDatabase(int hour, int minute)
    {
        if(hour == -1 || minute == -1)
        {
            // if remind me is turned off
            return -1L;
        }

        // remind me is turned on
        long time = hour * 100 + minute;
        return time;

    }

    public static ToDoTime formatTimeFromDatabase(long time)
    {
        ToDoTime toDoTime  = new ToDoTime();
        if(time == -1)
        {
            toDoTime.setTime(-1,-1);
        }
        else
        {
            int hr = (int) (time / 100);
            int min = (int) (time % 100);
            toDoTime.setTime(hr, min);
        }

        return toDoTime;
    }


    public static ToDoDate formatDateFromDatabase(long date)
    {
        ToDoDate toDoDate = new ToDoDate();
        if(date == -1)
        {
            toDoDate.setDate(-1, -1, -1);
        }
        else
        {
            int y = (int)(date / 10000);

            int m = (int)(date % 10000);

            int d = (int)(m % 100);

            m = (int)(m / 100);

            toDoDate.setDate(y, m, d);

        }

        return toDoDate;
    }
}
