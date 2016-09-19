package com.squarelabs.sareen.simpletodo.utilities;

/**
 * Created by Ashish Sarin on 14-06-2016.
 */

/* This class is used for sending and receiving minutes and hour
* variable from DateTimeFormat.formatTimeFromDatabase(long date)*/
public class ToDoTime
{
    private int hour;
    private int minute;

    public void setTime(int hr, int min)
    {
        hour = hr;
        minute = min;
    }

    public int getHour()
    {
        return hour;
    }

    public int getMinute()
    {
        return minute;
    }
}
