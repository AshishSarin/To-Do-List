package com.squarelabs.sareen.simpletodo.utilities;

/**
 * Created by Ashish Sarin on 14-06-2016.
 */
public class ToDoDate
{
    public int year;
    public int month;
    public int day;

    public void setDate(int y, int m, int d)
    {
        year = y;
        month = m;
        day = d;
    }

    public int getYear()
    {
        return year;
    }

    public int getMonth()
    {
        return month;
    }

    public int getDay()
    {
        return day;
    }
}
