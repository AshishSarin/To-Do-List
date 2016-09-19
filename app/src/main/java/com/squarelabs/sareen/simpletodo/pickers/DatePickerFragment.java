package com.squarelabs.sareen.simpletodo.pickers;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.os.Bundle;
import android.widget.DatePicker;

import java.util.Calendar;

/**
 * Created by Ashish Sarin on 11-06-2016.
 */
public class DatePickerFragment extends DialogFragment
    implements DatePickerDialog.OnDateSetListener
{

    // interface for sending date picked to add activity
    public interface onDateSelectedListener
    {
        public void onDateSelected(int year, int monthOfYear, int dayOfMonth);
    }

    private onDateSelectedListener mCallback;

    @Override
    public void onAttach(Activity activity)
    {
        super.onAttach(activity);

        try
        {
            mCallback = (onDateSelectedListener) activity;
        }
        catch (ClassCastException e)
        {
            throw new ClassCastException(activity.toString()
                    + " must implement onDateSelectedListener.");
        }
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState)
    {
        final Calendar c = Calendar.getInstance();
        int day = c.get(Calendar.DAY_OF_MONTH);
        int month = c.get(Calendar.MONTH);
        int year = c.get(Calendar.YEAR);

        return new DatePickerDialog(getActivity(), this, year, month, day);

    }

    @Override
    public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth)
    {
        // send date selected to add activity
        mCallback.onDateSelected(year, monthOfYear, dayOfMonth);

    }
}
