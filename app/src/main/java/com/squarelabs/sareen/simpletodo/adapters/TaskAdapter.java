package com.squarelabs.sareen.simpletodo.adapters;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Paint;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CursorAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.squarelabs.sareen.simpletodo.R;
import com.squarelabs.sareen.simpletodo.data.ToDoContract.TaskEntry;
import com.squarelabs.sareen.simpletodo.utilities.DateTimeFormat;
import com.squarelabs.sareen.simpletodo.utilities.ToDoDate;
import com.squarelabs.sareen.simpletodo.utilities.ToDoTime;
import com.squarelabs.sareen.simpletodo.utilities.Utility;

import java.util.Calendar;

/**
 * Created by Ashish Sarin on 15-06-2016.
 */
public class TaskAdapter extends CursorAdapter
{

    private Context mContext;


    public TaskAdapter(Context context)
    {
        super(context, null, 0);
        mContext = context;
    }

    @Override
    public View getView(int position, View convertView, final ViewGroup parent)
    {
        ViewHolder viewHolder;
        if(convertView == null)
        {
            convertView = LayoutInflater.from(mContext)
                    .inflate(R.layout.task_list_item, parent, false);
            viewHolder = new ViewHolder(convertView);
            convertView.setTag(viewHolder);
        }
        else
        {
            viewHolder = (ViewHolder)convertView.getTag();
        }

        Cursor cursor = getCursor();
        if( cursor.moveToPosition(position))
        {
            String t = (cursor.getString(cursor.getColumnIndex(TaskEntry.COLUMN_TASK_TITLE)));
            viewHolder.titleTextView.setText(t);
            // set up letterTextView
            viewHolder.letterTextView.setText(Character.toString(viewHolder.titleTextView.getText().charAt(0)));
            viewHolder.letterTextView.setBackgroundResource(
                    (Utility.getBackgroundId(cursor.getInt(cursor.getColumnIndex(TaskEntry.COLUMN_COLOR_CODE)))));

            if(cursor.getInt(cursor.getColumnIndex(TaskEntry.COLUMN_REMINDER_STATE)) == 1)
            {

                viewHolder.dateTextView.setVisibility(View.VISIBLE);
                long dt = (cursor.getLong(cursor.getColumnIndex(TaskEntry.COLUMN_REMINDER_DATE)));
                long tm = (cursor).getLong(cursor.getColumnIndex(TaskEntry.COLUMN_REMINDER_TIME));

                ToDoDate toDoDate = DateTimeFormat.formatDateFromDatabase(dt);
                ToDoTime toDoTime = DateTimeFormat.formatTimeFromDatabase(tm);

                Calendar c = Calendar.getInstance();
                c.set(Calendar.YEAR, toDoDate.getYear());
                c.set(Calendar.MONTH, toDoDate.getMonth());
                c.set(Calendar.DAY_OF_MONTH, toDoDate.getDay());
                c.set(Calendar.HOUR_OF_DAY, toDoTime.getHour());
                c.set(Calendar.MINUTE, toDoTime.getMinute());

                if(DateFormat.is24HourFormat(mContext))
                {
                    viewHolder.dateTextView.setText
                            (
                                    DateFormat.format("d MMM, yyyy", c) + "  "
                                            + DateFormat.format("kk:mm", c)
                            );
                }
                else
                {
                    viewHolder.dateTextView.setText
                            (
                                    DateFormat.format("d MMM, yyyy", c) + "  "
                                            + DateFormat.format("h:mm a", c)
                            );
                }
            }
            else
            {
                viewHolder.dateTextView.setVisibility(View.GONE);
            }

            int complete_state = cursor.getInt(cursor.getColumnIndex(TaskEntry.COLUMN_COMPLETE_STATE));
            if(complete_state == 1)
            {
                viewHolder.titleTextView.setPaintFlags
                        (viewHolder.titleTextView.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
                viewHolder.titleTextView.setAlpha(0.5f);
                viewHolder.completeCheckBox.setOnCheckedChangeListener(null);
                viewHolder.completeCheckBox.setChecked(true);
            }
            else
            {
                viewHolder.titleTextView.setPaintFlags
                    (viewHolder.titleTextView.getPaintFlags() & (~Paint.STRIKE_THRU_TEXT_FLAG));
                viewHolder.titleTextView.setAlpha(1.0f);
                viewHolder.completeCheckBox.setOnCheckedChangeListener(null);
                viewHolder.completeCheckBox.setChecked(false);
            }

            viewHolder.completeCheckBox.setTag(cursor.getPosition());
            viewHolder.completeCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener()
            {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked)
                {
                    int pos = (int)buttonView.getTag();
                    Cursor c = getCursor();
                    c.moveToPosition(pos);
                    long id = c.getLong(c.getColumnIndex(TaskEntry._ID));
                    ((ListView)parent).performItemClick(buttonView, pos, id);
                }
            });

        }

        return convertView;
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        return null;
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {

    }

    public static class ViewHolder
    {
        public CheckBox completeCheckBox;
        public TextView titleTextView;
        public TextView dateTextView;
        public TextView letterTextView;

        public ViewHolder(View v)
        {
            letterTextView = (TextView)v.findViewById(R.id.task_list_item_letter);
            completeCheckBox = (CheckBox) v.findViewById(R.id.task_list_item_complete);
            titleTextView = (TextView)v.findViewById(R.id.task_list_item_title);;
            dateTextView = (TextView)v.findViewById(R.id.task_list_item_date);

        }
    }

}
