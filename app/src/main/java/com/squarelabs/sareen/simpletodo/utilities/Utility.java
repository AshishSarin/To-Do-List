package com.squarelabs.sareen.simpletodo.utilities;

import com.squarelabs.sareen.simpletodo.R;

import java.util.Calendar;

/**
 * Created by Ashish Sarin on 07-07-2016.
 */
public class Utility
{
    public static int[] color_codes =
            {
                    10,         // red
                    20,         // blue
                    30,         // deep_purple
                    40,         // green
                    50,         // purple
                    60,         // orange
                    70,         // pink
                    80,         // teal
            };

    public static int getBackgroundId(int code)
    {
        switch (code)
        {
            case 10:
                // red code
                return R.drawable.text_round_red;
            case 20:
                // blue
                return R.drawable.text_round_blue;
            case 30:
                // deep_purple
                return R.drawable.text_round_deep_purple;
            case 40:
                //green
                return R.drawable.text_round_green;
            case 50:
                //purple
                return R.drawable.text_round_purple;
            case 60:
                // orange
                return R.drawable.text_round_orange;
            case 70:
                // pink
                return R.drawable.text_round_pink;
            case 80:
                // teal
                return R.drawable.text_round_teal;
            default:
                return R.drawable.text_round_red;
        }
    }

    public static int getColorCode(char c)
    {
        switch (c)
        {
            case 'A':
            case 'I':
            case 'Q':
                return color_codes[0];
            case 'B':
            case 'J':
            case 'R':
                return color_codes[1];
            case 'C':
            case 'K':
            case 'S':
                return color_codes[2];
            case 'D':
            case 'L':
            case 'T':
                return color_codes[3];
            case 'E':
            case 'M':
            case 'U':
                return color_codes[4];
            case 'F':
            case 'N':
            case 'V':
                return color_codes[5];
            case 'G':
            case 'O':
            case 'W':
                return color_codes[6];
            case 'H':
            case 'P':
            case 'X':
                return color_codes[7];
            default:
                return color_codes[0];
        }
    }

    public static long getTodayDate()
    {
        Calendar c = Calendar.getInstance();
        int y = c.get(Calendar.YEAR);
        int m = c.get(Calendar.MONTH);
        int d = c.get(Calendar.DAY_OF_MONTH);
        return DateTimeFormat.formatDateForDatabase(y, m, d);
    }

    public static int compareWithToday(long selectedDate)
    {
        long todayDate = getTodayDate();

        if(todayDate > selectedDate)
        {
            return -1;
        }
        else if(todayDate < selectedDate)
        {
            return 1;
        }
        return 0;
    }



    public static int compareWithCurrentTime(long selectedTime)
    {
        long currentTime = getCurrentTime();
        if(currentTime > selectedTime)
        {
            return -1;
        }
        else if(currentTime < selectedTime)
        {
            return 1;
        }

        return 0;
    }

    public static long getCurrentTime()
    {
        Calendar c = Calendar.getInstance();
        ToDoTime toDoTime = new ToDoTime();
        int h = c.get(Calendar.HOUR_OF_DAY);
        int m = c.get(Calendar.MINUTE);
        return DateTimeFormat.formatTimeForDatabase(h, m);
    }

    public static long getNextHourTime()
    {
        Calendar c = Calendar.getInstance();
        int h = c.get(Calendar.HOUR_OF_DAY);
        int m = c.get(Calendar.MINUTE);

        if(m!=0)
        {
            h++;
        }
        return DateTimeFormat.formatTimeForDatabase(h, 0);
    }

    public static boolean isTimePassed(long date, long time)
    {
        int compareToday = Utility.compareWithToday(date);
        if(compareToday < 0)
        {
            // due date of task is already passed
            return true;
        }
        else if(compareToday == 0)
        {
            // due date of task is today
            // check if time is passed or not
            int compareTime = Utility.compareWithCurrentTime(time);
            if(compareTime < 1)
            {
                /// time is already passed
                return true;
            }
        }
        return false;
    }
}
