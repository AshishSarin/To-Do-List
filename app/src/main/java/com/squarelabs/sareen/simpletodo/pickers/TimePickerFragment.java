package com.squarelabs.sareen.simpletodo.pickers;

import android.app.Activity;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.text.format.DateFormat;
import android.widget.TimePicker;

import java.util.Calendar;

/**
 * Created by Ashish Sarin on 11-06-2016.
 */
public class TimePickerFragment extends DialogFragment
    implements TimePickerDialog.OnTimeSetListener
{

    // interface for sending time picked to add activity
    public interface onTimeSelectedListener
    {
        public void onTimeSelected(int hourOfDay, int minute);
    }

    private onTimeSelectedListener mCallback;

    @Override
    public void onAttach(Activity activity)
    {
        super.onAttach(activity);

        try
        {
            mCallback = (onTimeSelectedListener)activity;
        }
        catch (ClassCastException e)
        {
            throw new ClassCastException(activity.toString()
                    + " must implement onTimeSelectedListener.");
        }
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState)
    {
        final Calendar c  = Calendar.getInstance();
        int hour = c.get(Calendar.HOUR_OF_DAY);
        int minute = c.get(Calendar.MINUTE);

        return new TimePickerDialog(getActivity(), this, hour, minute,
                DateFormat.is24HourFormat(getActivity()) );
    }

    @Override
    public void onTimeSet(TimePicker view, int hourOfDay, int minute)
    {
        // send time selected to add activity
        mCallback.onTimeSelected(hourOfDay, minute);
    }
}
